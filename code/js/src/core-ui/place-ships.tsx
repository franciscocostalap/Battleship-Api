import * as React from 'react'
import { useNavigate, useParams } from "react-router-dom";
import { emptyBoard, Board } from '../components/entities/board';
import { Orientation } from '../components/entities/orientation';
import { Ship } from '../components/entities/ship';
import { Square } from '../components/entities/square';
import { PlaceShipView } from '../pages/place-ships-view';
import { GameState } from '../components/entities/game-state';
import { defineShipLayout, getGameRules, getGameState } from '../api/api';
import { ShipInfo } from '../components/entities/ship-info';
import { authServices } from '../api/auth';
import AnimatedModal from '../components/modal'
import { BoardControls } from '../components/board/board-view';
import { FleetControls, FleetState } from '../components/fleet/fleet-view';
import { IGameRulesDTO } from '../interfaces/dto/game-rules-dto';
import { GameRules } from '../components/entities/game-rules';
import { INITIAL_MODAL_STATE, ModalMessages, ModalState } from './modal-state-config';
import { AppRoutes } from '../constants/routes';
 
const RIGHT_MOUSE_CLICK_EVENT = 2
const INTERVAL_TIME_MS = 1000

export function PlaceShips(){
    const navigate = useNavigate();
    let { gameID } = useParams()

    const validatedGameID = parseInt(gameID)
    const shootingGamePhaseURL = `/game/${gameID}`

    const boardSnapshot = React.useRef<Board>(null) // Board used to store the valid state of the board at each placement
    const gameRules = React.useRef<GameRules>(null)
    const [visibleBoard, setVisibleBoard] = React.useState(null) // Board that is displayed to the user
    const [shipSelected, setShipSelected] = React.useState(null) // Ship that is currently selected to be placed
    const [availableShips, setAvailableShips] = React.useState([]) // Ships that are available to be placed
    const [placedShips, setPlacedShips] = React.useState([]) // Ships that have been placed
    const [customModalState, setCustomModalState] = React.useState<ModalState>(INITIAL_MODAL_STATE)
    const [readyToPlay, setReadyToPlay] = React.useState(false)

    const loading = gameRules.current === null

    function clearBoards(){
        const newBoard = emptyBoard(gameRules.current.boardSide)
        setVisibleBoard(newBoard)
        boardSnapshot.current = newBoard
        setAvailableShips(gameRules.current.ships)
        setPlacedShips([])
        setShipSelected(null)
    }
    
    React.useEffect(() => {
        if(!authServices.isLoggedIn()){
            navigate(AppRoutes.LOGIN, { replace: true }) 
            return
        }
        
        const getRules = async () => {
            const response = await getGameRules(validatedGameID)
            const gameRulesDTO: IGameRulesDTO = response.properties

            const fleetComposition: Map<string, number> = gameRulesDTO.shipRules.fleetComposition
            const shipSizes = Object
                .entries(fleetComposition)
                .flatMap(([shipSize, shipCount]) => {
                    return Array(shipCount).fill(parseInt(shipSize))
                })
                
            const ships: Ship[] = shipSizes.map((size, index) => {
                return new Ship(index+1, size, Orientation.horizontal);
            })

            gameRules.current = { ships, boardSide: gameRulesDTO.boardSide, layoutDefinitionTimeout: gameRulesDTO.layoutDefinitionTimeout }
            clearBoards()
        }

        const checkGameState = async () => {
            const response = await getGameState(validatedGameID)

            const gameState = GameState[response.properties.state]

            if(gameState === GameState.PLAYING){
                navigate(shootingGamePhaseURL, { replace: true })
                return Promise.reject()
            }else if(gameState !== GameState.PLACING_SHIPS){
                const modalMessage = gameState === GameState.FINISHED ? ModalMessages.Finished : ModalMessages.Cancelled
                setCustomModalState({ message: modalMessage, isOpen: true })
            }

            return Promise.resolve()
        }

        checkGameState()
        .then(() => getRules())
        .catch()

    }, [])

    const onShipClicked = (shipID: number) => {
        const ship = availableShips.find((ship) => ship.id === shipID)
        setShipSelected(ship)
    }
   
    const onSquareClicked = (squareClicked: Square) => {
        if(shipSelected == null) return
        const currentBoard = boardSnapshot.current
        if(!currentBoard.canPlace(shipSelected, squareClicked)) return
        setVisibleBoard(currentBoard.place(shipSelected, squareClicked))
       
        setAvailableShips((previousShips: Ship[]): Ship[] =>
            previousShips.filter((ship) => ship.id !== shipSelected.id)
        )

        setPlacedShips([
            ...placedShips,
            new ShipInfo(squareClicked.toDTO(), shipSelected.size, shipSelected.orientation)
        ])

        setShipSelected(null);
        boardSnapshot.current = visibleBoard
    }

    const onSquareHover = (squareHovered: Square) => {
        if(shipSelected == null) return
        setVisibleBoard(() => {
            const board = boardSnapshot.current
            return board.canPlace(shipSelected, squareHovered) ?
                         board.place(shipSelected, squareHovered) : 
                         board.placeInvalid(shipSelected, squareHovered)
        })
    }

    const onSquareLeave = (squareHovered: Square) => {
        if(shipSelected == null) return
        setVisibleBoard(boardSnapshot.current)
    }

    const onBoardMouseDown = (event: React.MouseEvent, square: Square) => {
        if(shipSelected != null && event.button === RIGHT_MOUSE_CLICK_EVENT){
            const newShip = shipSelected.rotate()
            setShipSelected(newShip)
            setVisibleBoard(() => {
                const board = boardSnapshot.current

                return board.canPlace(newShip, square) ? 
                            board.place(newShip, square) : 
                            board.placeInvalid(newShip, square)
                }
            )
        }
    }

    const onTimeout = () => {
        defineShipLayout(validatedGameID, placedShips)
        .catch((problem) => {
            if(problem.status === 400){
                setCustomModalState({ message: ModalMessages.Cancelled, isOpen: true })
            }
        })
        .finally(() => {
            setCustomModalState({ message: ModalMessages.Cancelled, isOpen: true })
        })
    }

    React.useEffect(() => { // Starts polling as soon as the player is ready

        if(!readyToPlay) return
        console.log("Started polling for game state.")

        const checkGameState = async () => {
            const gameStateSiren = await getGameState(validatedGameID)
            const state = gameStateSiren.properties.state
            const gameState = GameState[state]
            if(gameState === GameState.PLAYING){ 
                navigate(shootingGamePhaseURL)
                clearInterval(intervalID)
                return 
            }
            console.log("Opponent not ready yet.")
        }

        let intervalID: NodeJS.Timeout | null = null

        defineShipLayout(validatedGameID, placedShips)
        .then(() => {
            intervalID = setInterval(() => {
                checkGameState()
                .catch((problem) => {
                    if(problem.status === 401){
                        setCustomModalState({ message: ModalMessages.NotLoggedIn, isOpen: true })
                    }
                    console.log(`Stopped polling for game state.`)
                    intervalID ?? clearInterval(intervalID)
                })
                 
            }, INTERVAL_TIME_MS)
        })

        return () => {
            intervalID ?? clearInterval(intervalID)
        }
    }, [readyToPlay])

    const boardControls: BoardControls = {
        onSquareClick: onSquareClicked,
        onSquareHover: onSquareHover,
        onSquareLeave: onSquareLeave,
        onMouseDown: onBoardMouseDown
    }

    const fleetState: FleetState = {
        availableShips: availableShips,
        shipSelected: shipSelected,
    }

    const fleetControls: FleetControls = {
        onShipClick: onShipClicked,
        onResetRequested: clearBoards,
        onSubmitRequested: () => {setReadyToPlay(true); console.log("Ready to play")}
    }

    const handleModalClose = () => navigate(AppRoutes.HOME, { replace: true })

    return(
        <div>
            <PlaceShipView
                board={visibleBoard}
                boardControls={boardControls}
                layoutDefinitionTimeout={gameRules.current?.layoutDefinitionTimeout}
                fleetState={fleetState}
                fleetControls={fleetControls}
                loading={loading}
                timerResetToggle={null}
                onTimeout={onTimeout}
            />
            <AnimatedModal
                message={customModalState.message}
                show={customModalState.isOpen}
                handleClose={handleModalClose}
            />
        </div>
    )
}

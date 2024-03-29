import * as React from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { Square } from '../components/entities/square'
import { GameTurn } from '../components/entities/turn'
import { GameView } from '../pages/game-view'
import { EmbeddedEntity, SirenEntity } from '../interfaces/hypermedia/siren'
import '../css/board.css'
import * as api from '../api/api'
import { SquareType } from '../components/entities/square-type'
import { IBoardDTO, toBoard } from '../interfaces/dto/board-dto'
import { Board } from '../components/entities/board'
import { IGameStateInfoDTO } from '../interfaces/dto/game-state-dto'
import { authServices, getCookie, UID_COOKIE_NAME } from '../api/auth'
import { IGameRulesDTO } from '../interfaces/dto/game-rules-dto'
import { GameState } from '../components/entities/game-state'
import { ModalState, ModalMessages, INITIAL_MODAL_STATE } from '../core-ui/modal-state-config'
import AnimatedModal from '../components/modal'
import { InfoToast } from './toasts'
import { executeWhileDisabled } from '../utils/buttonWrappers'
import useInterval from '../hooks/use-interval'
import { timerReducer } from '../components/timer/timer-reducer'
import { GameConstants } from '../constants/game'
import '../css/timeout-bar.css'

const INTERVAL_TIME_MS = 1000

interface ShotDefinitionRules{
    shotsDefinitionTimeout: number	
    shotsPerTurn: number
}

export function Game() {
    const navigate = useNavigate()
    let { gameID } = useParams()

    const userID = getCookie(UID_COOKIE_NAME)

    const validatedUserID = parseInt(userID)
    const validatedGameID = parseInt(gameID)

    const [currentPlayerBoard, setPlayerBoard] = React.useState<Board>(null)
    const [currentOpponentBoard, setOpponentBoard] = React.useState<Board>(null)
    const [currentShots, setCurrentShots] = React.useState<Array<Square>>([])
    const shotsDefinitionRules = React.useRef<ShotDefinitionRules>(null)
    const [turn, setTurn] = React.useState<GameTurn>(null)
    const [customModalState, setCustomModalState] = React.useState<ModalState>(INITIAL_MODAL_STATE)
    const MyFleetPoolInterval = React.useRef(null)
    const loading = React.useRef<boolean>(true)
    const [timerState, dispatchTimer]  = React.useReducer(timerReducer, {state: "stopped", currentValue: 0})
    const timerRunning = React.useRef(false)

    const getGameState = async () => {
        const gameStateResponse: SirenEntity<IGameStateInfoDTO> = await api.getGameState(validatedGameID)
        return gameStateResponse.properties  
    }
    useInterval(() => {
        dispatchTimer({type: "update"})
        if(timerState.state === "finished"){
            onTimerTimeout()
            timerRunning.current = false
        }
        
    }, timerRunning.current ? GameConstants.TIMER_PERIOD_MS : null)

    React.useEffect(() => {
        if(!authServices.isLoggedIn()){
            navigate('/login', { replace: true }) 
            return
        }

        const updateGameState = async (turnID: number) => {
            const currentTurn = turnID === validatedUserID ? GameTurn.MY : GameTurn.OPPONENT
            console.log("Turn: ", currentTurn === GameTurn.MY ? "My turn" : "Opponent turn")
            
            const [playerBoardResponse, opponentBoardResponse, gameRulesResponse] = await Promise.all([
                api.getBoard(validatedGameID, GameTurn.MY), 
                api.getBoard(validatedGameID, GameTurn.OPPONENT),
                api.getGameRules(validatedGameID)
            ])

            const playerBoardDTO: IBoardDTO = playerBoardResponse.properties 
            const opponentBoardDTO: IBoardDTO = opponentBoardResponse.properties 
            const gameRulesDTO: IGameRulesDTO = gameRulesResponse.properties
            const newShotDefinitionRules = { 
                shotsDefinitionTimeout: gameRulesDTO.playTimeout, 
                shotsPerTurn: gameRulesDTO.shotsPerTurn
            }
            
            setTurn(currentTurn)
            shotsDefinitionRules.current = newShotDefinitionRules
            setPlayerBoard(toBoard(playerBoardDTO))
            setOpponentBoard(toBoard(opponentBoardDTO))    
            loading.current = false
        }

        getGameState()
        .then((gameStateDTO: IGameStateInfoDTO) => {
            const gameState = GameState[gameStateDTO.state]

            if(gameState === GameState.PLACING_SHIPS){
                navigate(`/game/${validatedGameID}/layout-definition`)
                return
            }else if(gameState === GameState.PLAYING){
                if(gameStateDTO.remainingTime <= 0){ //if the user refreshes the page and the game is already cancelled
                    onTimerTimeout()
                    return
                }
            }
            else if(gameState !== GameState.PLAYING){
                const message = gameState === GameState.FINISHED ? ModalMessages.Finished : ModalMessages.Cancelled
                const newModalState: ModalState = {message, isOpen: true} 
                setCustomModalState(newModalState)
                return
            }

            updateGameState(gameStateDTO.turnID)
            dispatchTimer({type : "start", startValue : gameStateDTO.remainingTime})
            timerRunning.current = true
        })

    }, [])

    React.useEffect(() => { // If is not loading and is not my turn then start polling

        if(loading.current) return

        const checkWinner = (gameStateDTO: IGameStateInfoDTO) => {
            const gameState = GameState[gameStateDTO.state]
            if(gameState === GameState.FINISHED){
                const winner = gameStateDTO.turnID === validatedUserID ? GameTurn.OPPONENT : GameTurn.MY //Last to play
                const modalMessage = winner === GameTurn.MY ? ModalMessages.Won : ModalMessages.Lost
                const newModalState: ModalState = {message: modalMessage, isOpen: true}
                setCustomModalState(newModalState)
                clearInterval(MyFleetPoolInterval.current) //stop pooling for my fleet
                console.log("stopping timer")
                timerRunning.current = false
                dispatchTimer({type : "stop"})
            }else{
                console.log("reseting timer")
                dispatchTimer({type : "reset", startValue : gameStateDTO.remainingTime})
            }
        }

        getGameState()
        .then(gameStateDTO => {
            checkWinner(gameStateDTO)
        })
        
        
        if(loading.current || turn === GameTurn.MY) return

        const getMyBoard = async() => {
            const siren: SirenEntity<IBoardDTO> = await api.getBoard(validatedGameID, GameTurn.MY)
            const boardDTO = siren.properties
            return boardDTO
        }
        
        const tryUpdateMyBoard = (boardDTO: IBoardDTO) =>{
            const boardChanged =  boardDTO.shots.length !== currentPlayerBoard.shots.length ||
                                  boardDTO.hits.length !== currentPlayerBoard.hits.length
            if(boardChanged){
                setPlayerBoard(toBoard(boardDTO))
                console.log("Top changing turn")
                changeTurn()
                clearInterval(MyFleetPoolInterval.current)
            }
        }

        MyFleetPoolInterval.current = setInterval(() => {
            getMyBoard()
            .then((boardDTO) => tryUpdateMyBoard(boardDTO))
            
        }, INTERVAL_TIME_MS)

        return () => {
            clearInterval(MyFleetPoolInterval.current)
        }
    }, [turn])


    const changeTurn = () => {
        setTurn((prevTurn) =>{
            return prevTurn === GameTurn.MY ? GameTurn.OPPONENT : GameTurn.MY
        })
    };

    const onOpponentBoardSquareClicked = (squareClicked: Square) => {

        if(turn !== GameTurn.MY) {
            InfoToast("It's not your turn")
            return
        }

        const boardRepresentation = currentOpponentBoard.asMap()

        const squareID = squareClicked.toID()
        const squareType = boardRepresentation.get(squareID) ?? SquareType.WATER

        if(squareType !== SquareType.WATER) return
        
        setCurrentShots((prev: Array<Square>) => {
            const shots = prev
            const shotsIds = shots.map((shot) => shot.toID())
            let newShots: Square[] = shots;
            if(shotsIds.includes(squareID)){
                newShots = shots.filter((shot) => shot.toID() !== squareID)
            }else if(shots.length < shotsDefinitionRules.current.shotsPerTurn){
                newShots = [...shots, squareClicked]
            }

            return newShots
        })
    }

    const submitShots = (button) => {
        const makeShots = async () => {
            
            const sirenResponse = await api.defineShot(
                validatedGameID, 
                currentShots.map((square) => square.toDTO())
            )

            const embeddedEntity = sirenResponse.entities[0] as EmbeddedEntity<IBoardDTO>
            const embeddedBoardDTO = embeddedEntity.properties

            return toBoard(embeddedBoardDTO)
        }

        if(turn !== GameTurn.MY || currentShots.length !== shotsDefinitionRules?.current?.shotsPerTurn) return
        executeWhileDisabled(button,async () =>  {
            const newBoard = await makeShots()
            setOpponentBoard(newBoard)
            console.log("Bottom changing turn")
            changeTurn()
            setCurrentShots([])
        })
    }

    const onTimerTimeout = async () => {
        setCustomModalState({message: ModalMessages.Cancelled, isOpen: true})
    }
    const timerPercentage = Math.round((timerState.currentValue / shotsDefinitionRules.current?.shotsDefinitionTimeout) * 100)
    return (
        <div>
            <GameView
                loading={loading.current}
                playerBoard={currentPlayerBoard}
                opponentBoard={currentOpponentBoard}
                selectedShots={currentShots}
                shotsRemaining={shotsDefinitionRules?.current?.shotsPerTurn - currentShots.length}
                onOpponentBoardSquareClick={onOpponentBoardSquareClicked}
                turn={turn}
                timerPercentage={timerPercentage}
                onSubmitShotsClick={submitShots}
            />
            <AnimatedModal
                message={customModalState.message}
                show={customModalState.isOpen}
                handleClose={() => navigate('/', {replace: true})}
            />
        </div>
    )
}
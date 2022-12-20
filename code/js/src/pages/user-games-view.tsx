import * as React from "react"
import { useNavigate } from 'react-router-dom'
import { authServices } from '../api/auth'
import { GameState } from "../components/entities/game-state"
import { GameButton } from "../components/game-button"
import { IGameStateInfoDTO } from "../interfaces/dto/game-state-dto"
import { getUserGamesWithEmbeddedState } from "../utils/utils"



//TODO: add a waiting for play depending on turn
export function UserGames(){
    const navigate = useNavigate()

    const [gamesWithState, setGamesWithState] : [{ gameID: number, state: IGameStateInfoDTO}[] | null,any] = React.useState(null)
    const [loading, setLoading] = React.useState(true)

    const onGameClick = (event : any, gameID : number,gameInfo: IGameStateInfoDTO) => {
        event.preventDefault()
        if(gameInfo.state == GameState.PLACING_SHIPS){
            navigate(`/game/${gameID}/layout-definition`)
        }
        else if(gameInfo.state = GameState.PLAYING){
            navigate(`/game/${gameID}`)
        }
    }

    React.useEffect(() => {
        if(!authServices.isLoggedIn()){
            navigate('/login', { replace: true }) 
            return
        }
 
        const fetchUserGamesInfo = async () => {
            const games = await getUserGamesWithEmbeddedState()
            setGamesWithState(games)
            setLoading(false)
        }
        fetchUserGamesInfo()
    }, [])

    if(loading == true){
        return <div>Loading...</div>
    }

    return (
        <div>
            <h1>My Games</h1>
            <div className="GameButtonsContainer">
                {gamesWithState.map((it) => {
                    return <GameButton key={it.gameID} text={it.gameID.toString()} onClick={(e) => onGameClick(e,it.gameID,it.state)}/>
                })}
            </div>
        </div>
    )

}
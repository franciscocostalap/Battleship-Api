import { SirenEntity } from '../interfaces/hypermedia/siren';
import { AuthInformation } from '../interfaces/dto/user';
import { Problem } from '../interfaces/hypermedia/problem';
import { LobbyInformationDTO } from '../interfaces/dto/lobby-info';
import { StatisticsDTO } from '../interfaces/dto/statistics';
import axios from 'axios';
import { ShipInfoDTO } from '../interfaces/dto/ships-info';
import { GameRulesDTO } from '../interfaces/dto/game-rules';
import { GameStateInfoDTO } from '../interfaces/dto/game-state';
import { ShipInfo } from '../components/entities/ship-info';
import { Board } from '../components/entities/board';

const hostname = "localhost"
const port = 8090
const basePath = "/api/"

const baseUrl = `http://${hostname}:${port}${basePath}`
axios.defaults.withCredentials = true

export async function fetchLogin (username: string, password: string) : Promise< SirenEntity<AuthInformation>> {
    const response = await axios({
        url :`${baseUrl}user/login`, 
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(
            {
                "username": username,
                "password": password
            } 
        )
    }).catch((e) => {
        throw e.response.data as Problem
    })
    return response.data    
}

export async function fetchRegister (username: string, password: string) : Promise<SirenEntity<AuthInformation>> {
    const response = await axios({
        url: `${baseUrl}user/`, 
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        data: JSON.stringify(
            {
                "username": username,
                "password": password
            }
        )
        
    }).catch((e) => {
        throw e.response.data as Problem
    })

    return response.data
}

export async function getLobby(id:number) : Promise< SirenEntity<any>> {
    const response = await axios({
        method: 'GET',
        url: `${baseUrl}lobby/${id}`,
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .catch((e) => {
        throw e.response.data as Problem
    })

    return response.data
}
    
export async function joinQueue() : Promise< SirenEntity<LobbyInformationDTO>> {
    const response = await axios({
        url: `${baseUrl}lobby/`,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
          
    })
    .catch((e) => {
        throw e.response.data as Problem
    })

    return response.data
}

export async function leavelobby(lobbyID : number) : Promise< SirenEntity<any>> {
    const response = await axios({
        url: `${baseUrl}lobby/${lobbyID}`,
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json',
        }
    })
    .catch((e) => {
        throw e.response.data as Problem
    })

    return response.data
}

export async function getStatistics(): Promise<SirenEntity<StatisticsDTO>> {
    const response = await axios({
        url: `${baseUrl}statistics/`,
        method: 'GET',
        
    }).catch((e) => {
        throw e.response.data as Problem
    })

    return response.data
}

export async function placeShips(gameID: number, ships: ShipInfoDTO[]): Promise<SirenEntity<undefined>> {
    const response = await axios({
        url: `${baseUrl}game/${gameID}/placeShips`,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: JSON.stringify({
            "shipInfo": [...ships]
        })
    }).catch((e) => {
        throw e.response.data as Problem
    })

    return response.data
}

export async function getGameRules(gameID: number): Promise<SirenEntity<GameRulesDTO>> {
    console.log('here')
    const response = await axios({
        url: `${baseUrl}game/${gameID}/rules`,
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    }).catch((e) => {
        throw e.response.data as Problem
    })
    return response.data
}

export async function getGameState(gameID: number): Promise<SirenEntity<GameStateInfoDTO>> {
    const response = await axios({
        url: `${baseUrl}game/${gameID}/state`,
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    }).catch((e) => {
        throw e.response.data as Problem
    })

    return response.data
}

export async function defineShipLayout(gameID: number, shipInfo: ShipInfo[]): Promise<SirenEntity<undefined>> {
    console.log(shipInfo)
    const response = await axios({
        url: `${baseUrl}game/${gameID}/layout-definition`,
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        data: JSON.stringify({
            "shipsInfo": shipInfo
        })
    }).catch((e) => {
        throw e.response.data as Problem
    })

    return response.data
}

export async function getBoard(gameID: number, whichFleet: string): Promise<SirenEntity<Board>> {
    const response = await axios({
        url: `${baseUrl}game/${gameID}/fleet/${whichFleet}`,
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
        }
    }).catch((e) => {
        throw e.response.data as Problem
    })

    return response.data
}
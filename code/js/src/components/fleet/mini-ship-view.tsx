import * as React from 'react'
import { Styles } from '../../constants/styles'
import '../../css/mini-fleet.css'
import { Ship } from '../entities/ship'

interface Props{
    ship: Ship
    onShipClicked: (shipID: number) => void,
    currentlyPlacing: boolean
}

export function MiniShip(
    {
        ship,
        onShipClicked,
        currentlyPlacing
    }: Props
){
    const shipLength = new Array(ship.size).fill(null)
    const shipSquares = shipLength.map((_, index) => {
        return <div className={Styles.SHIP_SQUARE} key={index}/>
    })
    
    return(
        <div
            className={currentlyPlacing ? 'mini-ship placing': 'mini-ship'}
            id={Styles.MINI_SHIP}
            key={ship.id}
            onClick={() => {onShipClicked(ship.id)}}  
        >
            <div className="mini-ship-squares">{shipSquares}</div>
        </div>
        
    )
}
package pt.isel.daw.battleship.services.entities

import pt.isel.daw.battleship.utils.UserID


data class AuthInformation(
    val uid: UserID,
    val token: String
)
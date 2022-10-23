package pt.isel.daw.battleship.controller.exceptions

import org.springframework.http.HttpStatus
import pt.isel.daw.battleship.services.exception.*

val errorToStatusMap = mapOf(
    UserAlreadyExistsException::class to HttpStatus.CONFLICT,
    InvalidParameterException::class to HttpStatus.BAD_REQUEST,
    MissingParameterException::class to HttpStatus.BAD_REQUEST,
    NotFoundAppException::class to HttpStatus.NOT_FOUND,
    InternalErrorAppException::class to HttpStatus.INTERNAL_SERVER_ERROR,
    ForbiddenAccessAppException::class to HttpStatus.FORBIDDEN,
)
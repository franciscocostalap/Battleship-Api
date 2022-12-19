package pt.isel.daw.battleship.controller.controllers


import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import pt.isel.daw.battleship.controller.Uris
import pt.isel.daw.battleship.controller.dto.input.UserInfoInputModel
import pt.isel.daw.battleship.controller.hypermedia.siren.AppSirenNavigation
import pt.isel.daw.battleship.controller.hypermedia.siren.SirenEntity
import pt.isel.daw.battleship.controller.hypermedia.siren.appToSiren
import pt.isel.daw.battleship.controller.hypermedia.siren.noEntitySiren
import pt.isel.daw.battleship.controller.hypermedia.siren.siren_navigation.builders.NoEntitySiren
import pt.isel.daw.battleship.controller.pipeline.authentication.CookieAuthorizationProcessor.Companion.COOKIE_AUTHORIZATION_NAME
import pt.isel.daw.battleship.controller.pipeline.authentication.CookieAuthorizationProcessor.Companion.COOKIE_USER_ID_NAME
import pt.isel.daw.battleship.services.UserService
import pt.isel.daw.battleship.services.entities.AuthInformation
import pt.isel.daw.battleship.services.entities.User
import pt.isel.daw.battleship.services.validationEntities.UserValidation
import pt.isel.daw.battleship.utils.UserID
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse


@RestController
class UserController(
    private val userService: UserService
) {
    companion object {
        const val COOKIE_LIFETIME = 60 * 60 * 24 * 7
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(Uris.User.REGISTER)
    fun createUser(@RequestBody input: UserInfoInputModel,response: HttpServletResponse): SirenEntity<AuthInformation> {
        val authInfo = userService.createUser(
            UserValidation(input.username, input.password)
        )
        val authCookie = Cookie(COOKIE_AUTHORIZATION_NAME, authInfo.token)
        authCookie.path = "/"
        authCookie.maxAge = 60 * 60 * 24 * 7

        response.addCookie(authCookie)

        return authInfo.appToSiren(AppSirenNavigation.AUTH_INFO_NODE_KEY)
    }


    @ResponseStatus(HttpStatus.OK)
    @GetMapping(Uris.User.GET_USER)
    fun getUser(@PathVariable id: UserID): SirenEntity<User> {
        val user = userService.getUser(id)
        return user.appToSiren(AppSirenNavigation.USER_NODE_KEY)
    }

    @PostMapping(Uris.User.LOGIN)
    @ResponseStatus(HttpStatus.OK)
    fun authenticate(@RequestBody input: UserInfoInputModel,response: HttpServletResponse): SirenEntity<AuthInformation> {
        val authInfo = userService.authenticate(
            UserValidation(input.username, input.password)
        )

        val authCookie = Cookie(COOKIE_AUTHORIZATION_NAME, authInfo.token)
        val userIDCookie = Cookie(COOKIE_USER_ID_NAME, authInfo.uid.toString())

        authCookie.path = "/"
        userIDCookie.path = "/"
        authCookie.maxAge = COOKIE_LIFETIME
        userIDCookie.maxAge = COOKIE_LIFETIME


        response.addCookie(authCookie)
        response.addCookie(userIDCookie)

        return authInfo.appToSiren(AppSirenNavigation.AUTH_INFO_NODE_KEY)
    }

    @GetMapping(Uris.User.HOME)
    fun getUserHome(): SirenEntity<NoEntitySiren>
        = noEntitySiren(AppSirenNavigation.graph, AppSirenNavigation.USER_HOME_NODE_KEY)

}
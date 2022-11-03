package pt.isel.daw.battleship.repository.jdbi

import org.jdbi.v3.core.Handle
import pt.isel.daw.battleship.repository.UserRepository
import pt.isel.daw.battleship.services.entities.AuthInformation
import pt.isel.daw.battleship.utils.ID
import pt.isel.daw.battleship.utils.UserID
import pt.isel.daw.battleship.utils.UserToken


class JdbiUsersRepository(val handle: Handle) : UserRepository {

    /**
     * Creates a new user with the given name and password and token.
     * @param userName the name of the user
     * @param userAuthToken the token of the user
     * @param hashedPassword the hashed password of the user
     * @return the [ID] of the new user or null if the user already exists
     */
    override fun addUser(userName: String, userAuthToken: UserToken, hashedPassword: String, salt: String): UserID? {
        val uid = handle.createUpdate("insert into \"User\" (name, password,salt) values (:userName,:password,:salt) returning id")
            .bind("userName", userName)
            .bind("password", hashedPassword)
            .bind("salt", salt)
            .executeAndReturnGeneratedKeys("id")
            .mapTo(Int::class.java)
            .first()

        uid?.run {
            handle.createUpdate("insert into token (userid, token) values (:id, :token)")
                .bind("id", uid)
                .bind("token", userAuthToken)
                .execute()
            userAuthToken
        }

        return uid
    }

    /**
     * Verifies whether the given name is already in use by another user.
     * @param name the name of the user
     * @return true if the name is already in use, false otherwise
     */
    override fun hasUser(name: String): Boolean {
        return handle.createQuery("select count(*) from \"User\" where name = :name")
            .bind("name", name)
            .mapTo(Int::class.java)
            .first() == 1
    }

    /**
     * Checks if the given username and password are valid.
     * @param userName the name of the user
     * @param hashAndSaltedPassword the hashed password of the user
     * @return the [ID] of the user if the credentials are valid, null otherwise
     */
    override fun verifyUserCredentials(userName: String, hashAndSaltedPassword: String): AuthInformation? {
        return handle.createQuery("""
             select u.id as uid, t.token from "User" u join token t on u.id = t.userid where u.name = :name and u.password = :password
        """).bind("name", userName)
            .bind("password", hashAndSaltedPassword)
            .mapTo(AuthInformation::class.java)
            .firstOrNull()
    }
    /**
     * Gets a [User]'s salt.
     * @param userName the name of the user
     * @return the salt of the user if the user exists, null otherwise
     */
    override fun getUserSalt(userName: String): String {
        return handle.createQuery("select salt from \"User\" where name = :name")
            .bind("name", userName)
            .mapTo(String::class.java)
            .first()
    }

    /**
     * Gets the [ID] of the user with the given token.
     * @param token the token of the user
     * @return the [ID] of the user with the given token or null if the user does not exist
     */
    override fun getUserIDByToken(token: UserToken): ID? {
        return handle.createQuery("""
            select userid from token where token = :token    
        """).bind("token", token)
            .mapTo(Int::class.java)
            .firstOrNull()
    }
}
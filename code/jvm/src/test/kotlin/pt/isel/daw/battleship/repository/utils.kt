package pt.isel.daw.battleship.repository

import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.postgresql.ds.PGSimpleDataSource
import pt.isel.daw.battleship.repository.jdbi.JdbiTransaction
import pt.isel.daw.battleship.repository.jdbi.JdbiTransactionFactory
import pt.isel.daw.battleship.repository.jdbi.configure
import pt.isel.daw.battleship.services.exception.AppException
import pt.isel.daw.battleship.services.exception.InternalErrorAppException
import pt.isel.daw.battleship.services.transactions.Transaction
import pt.isel.daw.battleship.services.transactions.TransactionFactory

private val jdbi = Jdbi.create(
    PGSimpleDataSource().apply {
        val url =System.getenv("JDBC_TEST_DATABASE_URL") ?: throw IllegalStateException("JDBC_TEST_DATABASE_URL not set")
        setURL(url)
    }
).configure()

fun jdbiTransactionFactoryTestDB() =  JdbiTransactionFactory(jdbi)

fun clear(){
    executeWithHandle { handle ->
        handle.execute("""
            delete from waitinglobby cascade;
            delete from board cascade;
            delete from game cascade;
            delete from gamerules cascade;
            delete from shiprules cascade;
            delete from token  cascade;
            delete from "User" cascade;
        """.trimIndent())

    }
}

fun executeWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception> { handle ->
    block(handle)

}

fun testWithTransactionManagerAndRollback(block: TransactionFactory.() -> Unit) = jdbi.useTransaction<Exception>
{ handle ->

    val transaction = JdbiTransaction(handle)

    // a test TransactionManager that never commits
    val transactionManager = object : TransactionFactory {
        override fun <R> execute(block: (Transaction) -> R): R {
            return block(transaction)
        }

    }
    block(transactionManager)

    // finally, we rollback everything
    handle.rollback()
}


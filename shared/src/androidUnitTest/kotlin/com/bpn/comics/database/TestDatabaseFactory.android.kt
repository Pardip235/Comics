package com.bpn.comics.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.bpn.comics.database.ComicsDatabase

/**
 * Android unit test implementation of TestDatabaseFactory.
 * Uses JVM SQLite driver since Android unit tests run on JVM.
 */
actual object TestDatabaseFactory {
    actual fun createTestDatabase(): ComicsDatabase {
        // Use JVM driver for unit tests (they run on JVM, not Android runtime)
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ComicsDatabase.Schema.create(driver)
        return ComicsDatabase(driver = driver)
    }
}

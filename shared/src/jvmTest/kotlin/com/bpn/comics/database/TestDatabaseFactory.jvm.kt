package com.bpn.comics.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

/**
 * JVM implementation of TestDatabaseFactory using in-memory SQLite.
 */
actual object TestDatabaseFactory {
    actual fun createTestDatabase(): ComicsDatabase {
        // Create in-memory SQLite database for testing
        val driver: SqlDriver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        ComicsDatabase.Schema.create(driver)
        return ComicsDatabase(driver = driver)
    }
}


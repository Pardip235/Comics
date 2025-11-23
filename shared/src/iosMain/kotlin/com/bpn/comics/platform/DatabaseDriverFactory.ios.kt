package com.bpn.comics.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.bpn.comics.database.ComicsDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        // Use a new database name to force recreation with updated schema
        // This replaces the old database that doesn't have last_refreshed_at column
        return NativeSqliteDriver(
            schema = ComicsDatabase.Schema,
            name = "comics_v2.db"
        )
    }
}


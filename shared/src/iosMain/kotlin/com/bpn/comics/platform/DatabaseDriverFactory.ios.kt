package com.bpn.comics.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.bpn.comics.database.ComicsDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = ComicsDatabase.Schema,
            name = "comics.db"
        )
    }
}


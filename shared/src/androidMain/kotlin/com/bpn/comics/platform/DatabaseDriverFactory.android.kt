package com.bpn.comics.platform

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.bpn.comics.database.ComicsDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = ComicsDatabase.Schema,
            context = context,
            name = "comics.db"
        )
    }
}


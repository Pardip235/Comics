package com.bpn.comics.platform

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.bpn.comics.database.ComicsDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        // Use a new database name to force recreation with updated schema
        // This replaces the old database that doesn't have last_refreshed_at column
        val databaseName = "comics_v2.db"
        
        // Delete old database if it exists (optional - SQLDelight will create new one anyway)
        val oldDatabase = context.getDatabasePath("comics.db")
        if (oldDatabase.exists()) {
            oldDatabase.delete()
        }
        
        return AndroidSqliteDriver(
            schema = ComicsDatabase.Schema,
            context = context,
            name = databaseName
        )
    }
}


package com.bpn.comics.database

import com.bpn.comics.util.Logger

/**
 * Database migration helper.
 * 
 * Note: We use database versioning (different database names) instead of migrations.
 * When schema changes, we create a new database with a new name, effectively replacing
 * the old one. This is simpler and avoids complex migration logic.
 */
object DatabaseMigration {
    /**
     * Initialize database.
     * No migration needed - new database is created with updated schema.
     */
    fun migrate(database: ComicsDatabase) {
        Logger.d("DatabaseMigration", "Database initialized with latest schema")
    }
}


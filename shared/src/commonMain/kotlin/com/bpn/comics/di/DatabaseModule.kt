package com.bpn.comics.di

import com.bpn.comics.database.ComicsDatabase
import com.bpn.comics.database.DatabaseMigration
import com.bpn.comics.platform.DatabaseDriverFactory
import app.cash.sqldelight.db.SqlDriver
import org.koin.dsl.module

val databaseModule = module {
    // Database Driver Factory (platform-specific)
    // Note: For Android, this will be provided in androidMain
    // For iOS, this will be provided in iosMain
    
    // Database
    single<ComicsDatabase> {
        val driverFactory: DatabaseDriverFactory = get()
        val database = ComicsDatabase(driver = driverFactory.createDriver())
        
        // Run migrations after creating database instance
        DatabaseMigration.migrate(database)
        
        database
    }
}


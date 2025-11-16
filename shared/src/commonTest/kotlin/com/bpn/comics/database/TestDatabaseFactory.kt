package com.bpn.comics.database

import app.cash.sqldelight.db.SqlDriver

/**
 * Factory for creating test databases using in-memory SQLite driver.
 * This allows us to test database operations without mocking complex SQLDelight queries.
 * 
 * For multiplatform tests, we use platform-specific implementations.
 * The actual in-memory driver creation is handled by platform-specific code.
 */
expect object TestDatabaseFactory {
    /**
     * Create an in-memory database for testing.
     * The database is created fresh for each test and automatically cleaned up.
     */
    fun createTestDatabase(): ComicsDatabase
}

package com.hedvig.android.database.test

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.util.findAndInstantiateDatabaseImpl
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.junit.rules.ExternalResource

/**
 * Creates an in-memory [RoomDatabase] instance before the test and closes it after the test.
 * ```
 * @get:Rule
 * val appDatabaseRule = TestAppDatabaseRule(AppDatabase::class.java)
 * val appDatabase: AppDatabase
 *   get() = appDatabaseRule.appDatabase as AppDatabase
 * ```
 */
class TestAppDatabaseRule<T : RoomDatabase>(
  private val appDatabaseClass: Class<T>,
) : ExternalResource() {
  lateinit var appDatabase: RoomDatabase

  override fun before() {
    appDatabase = Room
      .inMemoryDatabaseBuilder { findAndInstantiateDatabaseImpl(appDatabaseClass) }
      .setDriver(BundledSQLiteDriver())
      .build()
  }

  override fun after() {
    appDatabase.close()
  }
}

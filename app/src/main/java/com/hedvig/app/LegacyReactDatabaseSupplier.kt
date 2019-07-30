package com.hedvig.app

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import timber.log.Timber

class LegacyReactDatabaseSupplier private constructor(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private var database: SQLiteDatabase? = null
    private var mMaximumDatabaseSize = 6L * 1024L * 1024L // 6 MB in bytes

    override fun onCreate(db: SQLiteDatabase) {
        Timber.e("This database is deprecated and should not be used. Let's not create a new one!")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Timber.e("This database is deprecated and should not be used. Let's not upgrade is!")
    }

    /* package */ @Synchronized
    internal fun ensureDatabase(): Boolean {
        if (database != null && database!!.isOpen) {
            return true
        }

        // Sometimes retrieving the database fails. We do 2 retries: first without database deletion
        // and then with deletion.
        var lastSQLiteException: SQLiteException? = null
        for (tries in 0..1) {
            try {
                if (tries > 0) {
                    deleteDatabase()
                }
                database = writableDatabase
                break
            } catch (e: SQLiteException) {
                lastSQLiteException = e
            }

            // Wait before retrying.
            try {
                Thread.sleep(SLEEP_TIME_MS.toLong())
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
            }

        }
        if (database == null) {
            lastSQLiteException?.let {
                Timber.e(it, "database could not be created :(")
            } ?: Timber.e("database could not be created and lastSQLiteException :crying_sad_face:")
        }

        database?.maximumSize = mMaximumDatabaseSize
        return true
    }

    @Synchronized
    fun get(): SQLiteDatabase? {
        ensureDatabase()
        return database
    }

    @Synchronized
    fun clearAndCloseDatabase() {
        try {
            clear()
            closeDatabase()
        } catch (e: Exception) {
            // Clearing the database has failed, delete it instead.
            if (deleteDatabase()) {
                return
            }
            // Everything failed, throw
            Timber.e("Clearing and deleting database $DATABASE_NAME failed")
        }

    }

    /* package */ @Synchronized
    internal fun clear() {
        get()?.delete(TABLE_CATALYST, null, null)
    }

    @Synchronized
    private fun deleteDatabase(): Boolean {
        closeDatabase()
        return context.deleteDatabase(DATABASE_NAME)
    }

    @Synchronized
    private fun closeDatabase() {
        if (database?.isOpen == true) {
            database?.close()
            database = null
        }
    }

    fun getTokenIfExists(): String? {
        ensureDatabase()

        val cursor = try {
            database?.rawQuery("SELECT * FROM catalystLocalStorage", null)
                ?: return null
        } catch (e: SQLiteException) {
            return null
        }

        var token: String? = null
        while (cursor.moveToNext()) {
            if (cursor.getString(0) == TOKEN_KEY) {
                token = cursor.getString(1)
            }
        }
        cursor.close()
        return token
    }

    companion object {

        const val DATABASE_NAME = "RKStorage"

        private const val DATABASE_VERSION = 1
        private const val SLEEP_TIME_MS = 30

        internal const val TABLE_CATALYST = "catalystLocalStorage"
        private const val TOKEN_KEY = "@hedvig:token"

        fun getInstance(context: Context): LegacyReactDatabaseSupplier {
            return LegacyReactDatabaseSupplier(context.applicationContext)
        }
    }
}

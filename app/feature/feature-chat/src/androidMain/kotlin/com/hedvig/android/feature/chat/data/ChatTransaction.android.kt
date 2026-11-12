package com.hedvig.android.feature.chat.data

import androidx.room.RoomDatabase
import androidx.room.withTransaction

internal actual suspend fun <R> RoomDatabase.chatTransaction(block: suspend () -> R): R = withTransaction(block)

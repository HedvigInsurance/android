package com.hedvig.android.data.chat.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TierQuoteDao {
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(quotes: List<ChangeTierQuoteEntity>)

  @Query("DELETE FROM change_tier_quotes")
  suspend fun clearAllQuotes()

  @Query("SELECT * FROM change_tier_quotes WHERE id=:id LIMIT 1")
  suspend fun getOneQuoteById(id: String): ChangeTierQuoteEntity

  @Query("SELECT * FROM change_tier_quotes WHERE id IN(:ids)")
  suspend fun getQuotesById(ids: List<String>): List<ChangeTierQuoteEntity>
}

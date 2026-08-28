package com.venom7t.lolguide.data.item.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {

    @Query("SELECT * FROM items")
    fun observeAll(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :itemId LIMIT 1")
    suspend fun getById(itemId: String): ItemEntity?

    @Query("SELECT * FROM items WHERE id IN (:itemIds)")
    suspend fun getByIds(itemIds: List<String>): List<ItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Query("DELETE FROM items")
    suspend fun clear()

    /** Same one-patch-at-a-time rule as champions (AGENTS.md section 1). */
    @Transaction
    suspend fun replaceAll(items: List<ItemEntity>) {
        clear()
        insertAll(items)
    }
}

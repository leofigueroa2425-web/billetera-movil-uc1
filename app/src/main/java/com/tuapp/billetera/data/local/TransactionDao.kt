package com.tuapp.billetera.data.local
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert suspend fun insert(tx: TransactionEntity)
    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY id DESC")
    fun observeAccount(accountId: String): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions ORDER BY id DESC") suspend fun getAllOnce(): List<TransactionEntity>
}

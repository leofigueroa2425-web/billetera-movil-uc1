package com.tuapp.billetera.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Double,
    val balanceAfter: Double,
    val dateIso: String,
    val detail: String = "",
    val service: String = "",
    val accountId: String = ""
)

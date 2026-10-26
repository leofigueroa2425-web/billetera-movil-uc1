package com.tuapp.billetera

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.tuapp.billetera.data.local.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountHistoryTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun accountHistoryNeverIncludesAnotherAccountOrLegacyReceipts() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            fun receipt(owner: String) = TransactionEntity(type="DEPOSITO",amount=25.0,balanceAfter=25.0,dateIso="2026-10-26T12:00:00Z",accountId=owner)
            db.txDao().insert(receipt("A"))
            db.txDao().insert(receipt("B"))
            db.txDao().insert(receipt(""))
            assertEquals(listOf("A"), db.txDao().observeAccount("A").first().map { it.accountId })
            assertEquals(listOf("B"), db.txDao().observeAccount("B").first().map { it.accountId })
            assertTrue(db.txDao().observeAccount("C").first().isEmpty())
        } finally { db.close() }
    }

    @Test fun migrationPreservesLegacyReceiptsWithoutAssigningAnUnknownOwner() = runBlocking {
        val name="migration-test.db"
        context.deleteDatabase(name)
        val path=context.getDatabasePath(name)
        SQLiteDatabase.openOrCreateDatabase(path,null).use { old ->
            old.execSQL("CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, type TEXT NOT NULL, amount REAL NOT NULL, balanceAfter REAL NOT NULL, dateIso TEXT NOT NULL, detail TEXT NOT NULL, service TEXT NOT NULL)")
            old.execSQL("INSERT INTO transactions (type,amount,balanceAfter,dateIso,detail,service) VALUES ('DEPOSITO',75,175,'2026-10-26T12:00:00Z','','')")
            old.version=1
        }
        val db=Room.databaseBuilder(context,AppDatabase::class.java,name).addMigrations(DatabaseProvider.MIGRATION_1_2).build()
        try {
            val receipt=db.txDao().getAllOnce().single()
            assertEquals(75.0,receipt.amount,0.001)
            assertEquals("",receipt.accountId)
            assertTrue(db.txDao().observeAccount("84100001").first().isEmpty())
        } finally { db.close(); context.deleteDatabase(name) }
    }
}

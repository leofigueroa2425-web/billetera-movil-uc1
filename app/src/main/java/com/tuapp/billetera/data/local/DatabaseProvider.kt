package com.tuapp.billetera.data.local
import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseProvider {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Legacy receipts have no reliable owner; preserve them without exposing them to another account.
            db.execSQL("ALTER TABLE transactions ADD COLUMN accountId TEXT NOT NULL DEFAULT ''")
        }
    }
    @Volatile private var INSTANCE: AppDatabase? = null
    fun get(context: Context): AppDatabase =
        INSTANCE ?: synchronized(this) {
            INSTANCE ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "billetera.db")
                .addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
        }
}

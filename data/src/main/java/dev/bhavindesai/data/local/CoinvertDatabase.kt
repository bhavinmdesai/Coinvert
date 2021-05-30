package dev.bhavindesai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.bhavindesai.domain.local.Currency
import dev.bhavindesai.domain.local.Quote

@Database(entities = [Currency::class, Quote::class], version = 1, exportSchema = false)
abstract class CoinvertDatabase : RoomDatabase() {

    abstract fun coinvertDataDao(): CoinvertDataDao
}
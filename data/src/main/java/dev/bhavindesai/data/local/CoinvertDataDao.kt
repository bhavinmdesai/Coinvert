package dev.bhavindesai.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.bhavindesai.domain.local.Currency
import dev.bhavindesai.domain.local.Quote

@Dao
interface CoinvertDataDao {

    @Query("SELECT * FROM Currencies")
    suspend fun getAllCurrencies(): List<Currency>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storeAllCurrencies(currencies: List<Currency>)

    @Query("SELECT * FROM Quotes")
    suspend fun getAllQuotes(): List<Quote>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storeAllQuotes(currencies: List<Quote>)
}
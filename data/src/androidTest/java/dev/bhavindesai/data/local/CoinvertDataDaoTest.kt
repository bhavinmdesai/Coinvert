package dev.bhavindesai.data.local

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import dev.bhavindesai.domain.local.Currency
import dev.bhavindesai.domain.local.Quote
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@SmallTest
@RunWith(AndroidJUnit4::class)
class CoinvertDataDaoTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var db: CoinvertDatabase
    private lateinit var dao: CoinvertDataDao

    @ExperimentalCoroutinesApi
    @Test
    fun verifyCurrencyStoredInDatabase() = runBlockingTest {
        val currencies = listOf(
            Currency("USD", "United State Dollar"),
            Currency("INR", "Indian Rupees"),
            Currency("AFN", "Afghan Afghani"),
        )

        dao.storeAllCurrencies(currencies)

        val storedCurrencies = dao.getAllCurrencies()

        assertEquals(currencies.size, storedCurrencies.size)
        assertTrue(currencies.containsAll(storedCurrencies))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun verifyInsertingSameCurrenciesReplacesPreviousOnes() = runBlockingTest {
        val currencies = listOf(
            Currency("USD", "United State Dollar"),
            Currency("INR", "Indian Rupees"),
            Currency("AFN", "Afghan Afghani"),
        )

        dao.storeAllCurrencies(currencies)
        dao.storeAllCurrencies(currencies)

        val storedCurrencies = dao.getAllCurrencies()

        assertEquals(currencies.size, storedCurrencies.size)
        assertTrue(currencies.containsAll(storedCurrencies))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun verifyLiveQuotesStoredInDatabase() = runBlockingTest {
        val quotes = listOf(
            Quote("AFN", 78.634443),
            Quote("INR", 72.58925),
            Quote("ZWL", 322.000305),
        )

        dao.storeAllQuotes(quotes)

        val storedQuotes = dao.getAllQuotes()

        assertEquals(quotes.size, storedQuotes.size)
        assertTrue(quotes.containsAll(storedQuotes))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun verifyInsertingSameQuotesReplacesPreviousOnes() = runBlockingTest {
        val quotes = listOf(
            Quote("AFN", 78.634443),
            Quote("INR", 72.58925),
            Quote("ZWL", 322.000305),
        )

        dao.storeAllQuotes(quotes)
        dao.storeAllQuotes(quotes)

        val storedQuotes = dao.getAllQuotes()

        assertEquals(quotes.size, storedQuotes.size)
        assertTrue(quotes.containsAll(storedQuotes))
    }

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, CoinvertDatabase::class.java)
            .build()

        dao = db.coinvertDataDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
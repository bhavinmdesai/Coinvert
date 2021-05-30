package dev.bhavindesai.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.test.filters.SmallTest
import dev.bhavindesai.data.local.CoinvertDataDao
import dev.bhavindesai.data.preferences.NetworkLogManager
import dev.bhavindesai.data.remote.services.CurrencyLayerService
import dev.bhavindesai.data.repositories.CoinvertRepository
import dev.bhavindesai.data.repositories.CoinvertRepositoryImpl
import dev.bhavindesai.data.utils.InternetUtil
import dev.bhavindesai.domain.local.CurrencyQuote
import dev.bhavindesai.viewmodel.utils.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.Executors

@SmallTest
class CurrencyConversionViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @ObsoleteCoroutinesApi
    private val mainThreadSurrogate = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    @Test
    @FlowPreview
    fun `verify view model states when data is emitted`() {
        withMocks {
            verify(exactly = 1) {
                observerFilteredQuotes.onChanged(any())
                observerQuotesFetchedAt.onChanged(any())
            }

            viewModel.filteredCurrencyQuotes.observeForever {
                Assert.assertNotNull(it)
                Assert.assertEquals(3, it.size)
            }
        }
    }

    @Test
    @FlowPreview
    fun `verify data is filtered based on search text`() {
        withMocks {
            viewModel.setSearchText("AFN")

            viewModel.filteredCurrencyQuotes.observeForever {
                Assert.assertNotNull(it)
                Assert.assertEquals(1, it.size)
                Assert.assertEquals("AFN", it.first().targetAbbreviation)

                val result = BigDecimal(786.34).setScale(2, RoundingMode.HALF_EVEN)
                Assert.assertEquals(result, it.first().amount)
            }
        }
    }

    @Test
    @FlowPreview
    fun `verify floating point conversion on the amount`() {
        withMocks {
            viewModel.setSearchText("IN")

            viewModel.filteredCurrencyQuotes.observeForever {
                Assert.assertNotNull(it)
                Assert.assertEquals(1, it.size)
                Assert.assertEquals("INR", it.first().targetAbbreviation)

                val result = BigDecimal(725.89).setScale(2, RoundingMode.HALF_EVEN)
                Assert.assertEquals(result, it.first().amount)
            }
        }
    }

    @FlowPreview
    private fun withMocks(fixture: () -> Fixture = { Fixture() }, test: Fixture.() -> Unit) {
        val f = fixture()
        f.apply(test)
    }

    @FlowPreview
    private data class Fixture(
        val currencyLayerService: CurrencyLayerService = mockk<CurrencyLayerService>(relaxed = true).apply {
            withSomeCurrencies()
            withSomeQuotes()
        },
        val internetUtil: InternetUtil = mockk<InternetUtil>(relaxed = true).apply {
            withInternetConnection()
        },
        val coinvertDataDao: CoinvertDataDao = mockk<CoinvertDataDao>(relaxed = true).apply {
            withSomeCurrencies()
            withSomeQuotes()
        },
        val networkLogManager: NetworkLogManager = mockk<NetworkLogManager>(relaxed = true).apply {
            withNoHistory()
        },
        val coinvertRepository: CoinvertRepositoryImpl = CoinvertRepositoryImpl(
            currencyLayerService,
            internetUtil,
            coinvertDataDao,
            networkLogManager
        ),
        val viewModel: CurrencyConversionViewModel = CurrencyConversionViewModel(coinvertRepository),
        val observerFilteredQuotes: Observer<List<CurrencyQuote>> = mockk(),
        val observerQuotesFetchedAt: Observer<String> = mockk()
    ) {
        init {
            viewModel.convertCurrency("USD", 1000)

            coEvery { coinvertDataDao.storeAllCurrencies(any()) } just runs
            coEvery { coinvertDataDao.storeAllQuotes(any()) } just runs
            coEvery { networkLogManager.storeQuotesFetchedTimestamp(any()) } just runs
            every { observerFilteredQuotes.onChanged(any()) } just runs
            every { observerQuotesFetchedAt.onChanged(any()) } just runs

            viewModel.filteredCurrencyQuotes.observeForever(observerFilteredQuotes)
            viewModel.fetchQuotesFetchedAt().observeForever(observerQuotesFetchedAt)
        }
    }

    @ObsoleteCoroutinesApi @ExperimentalCoroutinesApi
    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @ObsoleteCoroutinesApi @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }
}

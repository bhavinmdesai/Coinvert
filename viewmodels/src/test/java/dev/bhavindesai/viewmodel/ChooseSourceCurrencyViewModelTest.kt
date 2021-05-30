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
import dev.bhavindesai.domain.local.Currency
import dev.bhavindesai.viewmodel.utils.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*
import java.util.concurrent.Executors

@SmallTest
class ChooseSourceCurrencyViewModelTest {

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
                observerFilteredCurrencies.onChanged(any())
            }

            viewModel.filteredCurrencies.observeForever {
                Assert.assertNotNull(it)
                Assert.assertEquals(3, it.size)
            }
        }
    }

    @Test
    @FlowPreview
    fun `verify data is filtered based on search text`() {
        withMocks {
            viewModel.setSearchText("afn")

            viewModel.filteredCurrencies.observeForever {
                Assert.assertNotNull(it)
                Assert.assertEquals(1, it.size)
                Assert.assertEquals("AFN", it.first().abbreviation)
                Assert.assertEquals("Afghan Afghani", it.first().name)
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
        val currencyLayerService: CurrencyLayerService = mockk<CurrencyLayerService>(relaxed = true)
            .apply {
                withSomeCurrencies()
            },
        val internetUtil: InternetUtil = mockk<InternetUtil>(relaxed = true).apply {
            withInternetConnection()
        },
        val coinvertDataDao: CoinvertDataDao = mockk<CoinvertDataDao>(relaxed = true).apply {
            withNoCurrencies()
            withNoQuotes()
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
        val viewModel: ChooseSourceCurrencyViewModel = ChooseSourceCurrencyViewModel(coinvertRepository),
        val observerFilteredCurrencies: Observer<List<Currency>> = mockk()
    ) {
        init {
            viewModel.fetchCurrencies()

            coEvery { coinvertDataDao.storeAllCurrencies(any()) } just runs
            coEvery { coinvertDataDao.storeAllQuotes(any()) } just runs

            every { observerFilteredCurrencies.onChanged(any()) } just runs
            viewModel.filteredCurrencies.observeForever(observerFilteredCurrencies)
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

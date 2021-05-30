package dev.bhavindesai.data.repositories

import dev.bhavindesai.data.local.CoinvertDataDao
import dev.bhavindesai.data.preferences.NetworkLogManager
import dev.bhavindesai.data.remote.services.CurrencyLayerService
import dev.bhavindesai.data.utils.*
import dev.bhavindesai.domain.local.Currency
import dev.bhavindesai.domain.local.CurrencyQuote
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert
import org.junit.Ignore
import org.junit.Test
import java.math.BigDecimal

@Ignore("TODO: Need to workout why the test cases are not working")
class CoinvertRepositoryTest {

    @FlowPreview
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    @Test
    fun `test cached get currencies emits data once`() {

        withMocks {
            testDispatcher.runBlockingTest {
                val currencyListFlow = coinvertRepository.getCurrencies(cached = true)
                currencyListFlow.collect(currencyListFlowCollector)

                coVerify(exactly = 1) { currencyListFlow.collect(currencyListFlowCollector) }
            }
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    @Test
    fun `test get currencies emits data once when no internet`() {

        withMocks({
            Fixture(internetUtil = mockk<InternetUtil>().apply {
                withNoInternetConnection()
            })
        }) {
            testDispatcher.runBlockingTest {
                val currencyListFlow = coinvertRepository.getCurrencies()
                currencyListFlow.collect(currencyListFlowCollector)

                currencyListFlow.collect {
                    Assert.assertNotNull(it)
                }

                coVerify(exactly = 1) { currencyListFlow.collect(currencyListFlowCollector) }
            }
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    @Test
    fun `test get currencies emits data twice`() {

        withMocks {
            testDispatcher.runBlockingTest {
                val currencyListFlow = coinvertRepository.getCurrencies()
                currencyListFlow.collect(currencyListFlowCollector)

                coVerify(exactly = 2) { currencyListFlow.collect(currencyListFlowCollector) }
            }
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @InternalCoroutinesApi
    @Test
    fun `test convert currencies emits data twice`() {
        withMocks {
            testDispatcher.runBlockingTest {
                val quotesListFlow = coinvertRepository.convertCurrency("USD", 1000)
                quotesListFlow.collect(quotesListFlowCollector)

                coVerify(exactly = 2) { quotesListFlow.collect(quotesListFlowCollector) }
            }
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun withMocks(fixture: () -> Fixture = { Fixture() }, test: Fixture.() -> Unit) {
        val f = fixture()
        f.apply(test)
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private data class Fixture constructor(
        val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher(),
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
            networkLogManager,
            testDispatcher
        ),
        val currencyListFlowCollector: FlowCollector<List<Currency>?> = mockk(),
        val quotesListFlowCollector: FlowCollector<List<CurrencyQuote>> = mockk()
    ) {
        init {
            coEvery { coinvertDataDao.storeAllCurrencies(any()) } just runs
            coEvery { coinvertDataDao.storeAllQuotes(any()) } just runs
            coEvery { networkLogManager.storeQuotesFetchedTimestamp(any()) } just runs

            coEvery { currencyListFlowCollector.emit(listOf(
                Currency("USD", "United State Dollar"),
                Currency("INR", "Indian Rupees"),
                Currency("AFN", "Afghan Afghani"),
            )) } just runs

            coEvery { quotesListFlowCollector.emit(listOf(
                CurrencyQuote("USD", "AFN", 78.634443, BigDecimal(1000)),
                CurrencyQuote("USD","INR", 72.58925, BigDecimal(1000)),
                CurrencyQuote("USD","USD", 1.0, BigDecimal(1000)),
                CurrencyQuote("USD","ZWL", 322.000305, BigDecimal(1000)),
            )) } just runs
        }
    }
}
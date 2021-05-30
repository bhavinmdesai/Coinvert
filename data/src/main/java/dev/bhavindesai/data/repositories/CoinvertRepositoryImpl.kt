package dev.bhavindesai.data.repositories

import androidx.annotation.VisibleForTesting
import dev.bhavindesai.data.local.CoinvertDataDao
import dev.bhavindesai.data.preferences.NetworkLogManager
import dev.bhavindesai.data.remote.services.CurrencyLayerService
import dev.bhavindesai.data.sources.LocalDataSource
import dev.bhavindesai.data.sources.MultiDataSource
import dev.bhavindesai.data.utils.InternetUtil
import dev.bhavindesai.domain.local.Currency
import dev.bhavindesai.domain.local.CurrencyQuote
import dev.bhavindesai.domain.local.Quote
import dev.bhavindesai.domain.remote.GetAllCurrenciesResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import java.math.BigDecimal
import javax.inject.Inject

class CoinvertRepositoryImpl @Inject constructor(
    private val currencyLayerService: CurrencyLayerService,
    private val internetUtil: InternetUtil,
    private val coinvertDataDao: CoinvertDataDao,
    private val networkLogManager: NetworkLogManager,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
): CoinvertRepository {

    private companion object {
        const val SOURCE_CURRENCY = "USD"
    }

    override val flowQuotesFetchedAt = networkLogManager.flowQuotesFetchedAt

    override fun getCurrencies(cached: Boolean): Flow<List<Currency>?> {
        val flowOfCurrencies = if (cached)
            flow { emit(ldsCurrencies.getLocalData()) }
        else
            mdsCurrencies.fetch(Unit)

        return flowOfCurrencies.flowOn(defaultDispatcher)
    }

    private val ldsCurrencies = object : LocalDataSource<List<Currency>> {
        override suspend fun getLocalData() = coinvertDataDao.getAllCurrencies()

        override suspend fun storeLocalData(data: List<Currency>) {
            coinvertDataDao.storeAllCurrencies(data)
        }
    }

    @FlowPreview
    override fun convertCurrency(selectedCurrencyAbbreviation: String, amount: Long) =
        getCurrencies(cached = true)
        .filterNotNull()
        .map { currencyList -> currencyList.filter { it.abbreviation != SOURCE_CURRENCY } }
        .map { currencies -> currencies.joinToString(separator = ",") { it.abbreviation } }
        .flatMapMerge { targetCurrencies -> mdsConvertCurrency.fetch(targetCurrencies) }
        .map { listOfQuotes ->
            listOfQuotes?.let { quotes ->

                val sourceCurrencyQuote = quotes.find { quote ->
                    quote.targetCurrencyAbbreviation == selectedCurrencyAbbreviation
                }

                sourceCurrencyQuote?.let {
                    quotes
                        .filter { it.targetCurrencyAbbreviation != selectedCurrencyAbbreviation }
                        .map {
                            CurrencyQuote(
                                selectedCurrencyAbbreviation,
                                it.targetCurrencyAbbreviation,
                                it.price.div(sourceCurrencyQuote.price),
                                BigDecimal(amount).divide(BigDecimal(100L))
                            )
                        }
                } ?: emptyList()
            } ?: emptyList()
        }
        .flowOn(defaultDispatcher)

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    private val mdsConvertCurrency =
        object: MultiDataSource<List<Quote>, String, Pair<String, Map<String, Double>>>(internetUtil) {
            override suspend fun mapper(remoteData: Pair<String, Map<String, Double>>): List<Quote> {
                val quotes = remoteData.second.map { entry ->
                    Quote(
                        entry.key.replaceFirst(remoteData.first, ""),
                        entry.value,
                    )
                }

                return mutableListOf<Quote>().also {
                    it.addAll(quotes)
                    it.add(Quote(
                        remoteData.first,
                        1.0,
                    ))
                }
            }


            override suspend fun getLocalData(): List<Quote> = coinvertDataDao.getAllQuotes()

            override suspend fun storeLocalData(data: List<Quote>) {
                coinvertDataDao.storeAllQuotes(data)
            }

            override suspend fun getRemoteData(requestData: String):
                    Pair<String, Map<String, Double>>? {
                val currentTime = System.currentTimeMillis()
                val lastQuotesFetchedAt = flowQuotesFetchedAt.first()

                return if (currentTime > (lastQuotesFetchedAt.after30Minutes())) {
                    try {
                        val response = currencyLayerService.getLiveCurrencyQuotes(
                            SOURCE_CURRENCY,
                            requestData
                        )

                        if (response.success) {
                            networkLogManager.storeQuotesFetchedTimestamp(currentTime)

                            response.source to response.quotes
                        } else
                            null
                    } catch (e: Exception) {
                        null
                    }
                } else
                    null
            }

        }

    private val mdsCurrencies =
        object: MultiDataSource<List<Currency>, Unit, GetAllCurrenciesResponse>(internetUtil) {
            override suspend fun mapper(remoteData: GetAllCurrenciesResponse): List<Currency> {

                return if (remoteData.success) {
                    remoteData.currencies.toList().map {
                        Currency(it.first, it.second)
                    }
                } else
                    emptyList()
            }

            override suspend fun getLocalData(): List<Currency> = coinvertDataDao.getAllCurrencies()

            override suspend fun storeLocalData(data: List<Currency>) {
                coinvertDataDao.storeAllCurrencies(data)
            }

            override suspend fun getRemoteData(requestData: Unit): GetAllCurrenciesResponse? {
                return try {
                    currencyLayerService.getAllCurrencies()
                } catch (e: Exception) {
                    null
                }
            }
        }
}

private fun Long.after30Minutes() = this + (60 * 30 * 1000)

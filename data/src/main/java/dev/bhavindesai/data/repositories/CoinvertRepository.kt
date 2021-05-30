package dev.bhavindesai.data.repositories

import dev.bhavindesai.domain.local.Currency
import dev.bhavindesai.domain.local.CurrencyQuote
import kotlinx.coroutines.flow.Flow

interface CoinvertRepository {

    val flowQuotesFetchedAt: Flow<Long>

    fun getCurrencies(cached: Boolean = false): Flow<List<Currency>?>

    fun convertCurrency(selectedCurrencyAbbreviation: String, amount: Long): Flow<List<CurrencyQuote>>
}
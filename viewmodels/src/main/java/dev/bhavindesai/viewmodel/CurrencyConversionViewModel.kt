package dev.bhavindesai.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bhavindesai.data.repositories.CoinvertRepository
import dev.bhavindesai.domain.local.CurrencyQuote
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CurrencyConversionViewModel @Inject constructor(
    private val coinvertRepository: CoinvertRepository
): ViewModel() {

    private val mldFilteredCurrencyQuotes = MediatorLiveData<List<CurrencyQuote>>()
    val filteredCurrencyQuotes: LiveData<List<CurrencyQuote>> = mldFilteredCurrencyQuotes

    private val mldSearchText = MutableLiveData<String>()
    private var mldCurrencyQuoteList = MutableLiveData<List<CurrencyQuote>?>()
    private val quotesFetchedAtDateFormat = SimpleDateFormat(
        "dd MMMM yyyy, h:mm a",
        Locale.US
    )

    init {
        mldFilteredCurrencyQuotes.addSource(mldCurrencyQuoteList) {
            mldFilteredCurrencyQuotes.value = getFilteredCurrencies(
                it ?: emptyList(),
                mldSearchText.value ?: ""
            )
        }

        mldFilteredCurrencyQuotes.addSource(mldSearchText) { searchText ->
            mldFilteredCurrencyQuotes.value = getFilteredCurrencies(
                mldCurrencyQuoteList.value ?: emptyList(),
                searchText
            )
        }
    }

    private fun getFilteredCurrencies(currencies: List<CurrencyQuote>, searchText: String): List<CurrencyQuote> {
        return currencies.filter {
            it.targetAbbreviation.contains(searchText, ignoreCase = true)
        }
    }

    fun setSearchText(searchText: String) {
        mldSearchText.value = searchText
    }

    fun fetchQuotesFetchedAt() = coinvertRepository
        .flowQuotesFetchedAt
        .map { quotesFetchedAtDateFormat.format(Date(it)) }
        .asLiveData(viewModelScope.coroutineContext)

    @FlowPreview
    fun convertCurrency(currencyAbbreviation: String, amount: Long) {
        viewModelScope.launch {
            coinvertRepository
                .convertCurrency(currencyAbbreviation, amount)
                .collect { mldCurrencyQuoteList.value = it }
        }
    }
}
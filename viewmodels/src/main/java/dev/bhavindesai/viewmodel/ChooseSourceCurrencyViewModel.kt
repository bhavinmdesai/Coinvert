package dev.bhavindesai.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bhavindesai.data.repositories.CoinvertRepository
import dev.bhavindesai.data.repositories.CoinvertRepositoryImpl
import dev.bhavindesai.domain.local.Currency
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChooseSourceCurrencyViewModel @Inject constructor(
    private val dataRepository: CoinvertRepository
) : ViewModel() {

    private val mldFilteredCurrencies = MediatorLiveData<List<Currency>>()
    val filteredCurrencies: LiveData<List<Currency>> = mldFilteredCurrencies

    private val mldSearchText = MutableLiveData<String>()
    private var mldCurrencyList = MutableLiveData<List<Currency>?>()

    init {
        mldFilteredCurrencies.addSource(mldCurrencyList) {
            mldFilteredCurrencies.value = getFilteredCurrencies(
                it ?: emptyList(),
                mldSearchText.value ?: ""
            )
        }

        mldFilteredCurrencies.addSource(mldSearchText) { searchText ->
            mldFilteredCurrencies.value = getFilteredCurrencies(
                mldCurrencyList.value ?: emptyList(),
                searchText
            )
        }
    }

    private fun getFilteredCurrencies(currencies: List<Currency>, searchText: String): List<Currency> {
        return currencies.filter {
            it.name.contains(searchText, ignoreCase = true)
                    || it.abbreviation.contains(searchText, ignoreCase = true)
        }
    }

    fun fetchCurrencies() {
        viewModelScope.launch {
            dataRepository.getCurrencies().collect { mldCurrencyList.value = it }
        }
    }

    fun setSearchText(searchText: String) {
        mldSearchText.value = searchText
    }
}
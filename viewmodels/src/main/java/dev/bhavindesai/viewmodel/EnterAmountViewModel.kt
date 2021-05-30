package dev.bhavindesai.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hadilq.liveevent.LiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.bhavindesai.domain.local.Currency
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class EnterAmountViewModel @Inject constructor(): ViewModel() {

    private val mldAmount = MutableLiveData<String>()
    val amount: LiveData<String> = mldAmount

    private val mldSelectedCurrency = MutableLiveData<Currency>()
    val selectedCurrency: LiveData<Currency> = mldSelectedCurrency

    private val leRedirectToConvert = LiveEvent<Pair<String, Long>>()
    val redirectToConvert: LiveData<Pair<String, Long>> = leRedirectToConvert

    private val mldCents = MutableLiveData<Long>()

    private val mldEnableConvertButton = MediatorLiveData<Boolean>()
    val enableConvertButton: LiveData<Boolean> = mldEnableConvertButton

    init {
        mldCents.value = 0L
        onClearClick()

        mldEnableConvertButton.addSource(mldSelectedCurrency) {
            mldEnableConvertButton.value = it != null && (mldCents.value ?: 0L) > 0L
        }

        mldEnableConvertButton.addSource(mldCents) { cents ->
            mldEnableConvertButton.value = mldSelectedCurrency.value != null && cents > 0L
        }
    }

    fun onCurrencySelection(currency: Currency) {
        mldSelectedCurrency.value = currency
    }

    fun onNumpadClick(text: String) {
        if ((mldCents.value == 0L && text == "0") || mldAmount.value?.length == 12) return

        mldCents.value = (mldCents.value ?: 0L) * 10L
        mldCents.value = (mldCents.value ?: 0L) + text.toLong()

        convertToFormattedCents()
    }

    fun onClearClick() {
        mldCents.value = 0L

        convertToFormattedCents()
    }

    fun onDoubleZeroClick() {
        mldCents.value = (mldCents.value ?: 0L) * 100L

        convertToFormattedCents()
    }

    fun onConvertClick() {
        selectedCurrency.value?.let {
            leRedirectToConvert.value = it.abbreviation to (mldCents.value ?: 0L)
        }
    }

    private fun convertToFormattedCents() {
        mldAmount.value = BigDecimal(mldCents.value ?: 0L)
            .divide(BigDecimal(100L))
            .setScale(2, BigDecimal.ROUND_HALF_EVEN).toString()
    }

}
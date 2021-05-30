package dev.bhavindesai.viewmodel.utils

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.hadilq.liveevent.LiveEvent
import dev.bhavindesai.data.local.CoinvertDataDao
import dev.bhavindesai.data.preferences.NetworkLogManager
import dev.bhavindesai.data.remote.services.CurrencyLayerService
import dev.bhavindesai.data.utils.InternetUtil
import dev.bhavindesai.domain.local.Currency
import dev.bhavindesai.domain.local.Quote
import dev.bhavindesai.domain.remote.GetAllCurrenciesResponse
import dev.bhavindesai.domain.remote.GetLiveCurrencyQuotesResponse
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

fun InternetUtil.withInternetConnection() {
    every { isInternetOn() } returns true
}

fun InternetUtil.withNoInternetConnection() {
    every { isInternetOn() } returns false
}

fun CurrencyLayerService.withSomeCurrencies() {
    coEvery { getAllCurrencies() } returns GetAllCurrenciesResponse(
        true,
        mapOf(
            "USD" to "United State Dollar",
            "INR" to "Indian Rupees",
            "AFN" to "Afghan Afghani",
        )
    )
}

fun CurrencyLayerService.withSomeQuotes() {
    val sourceCurrency = "USD"
    coEvery { getLiveCurrencyQuotes(sourceCurrency, any()) } returns GetLiveCurrencyQuotesResponse(
        true,
        0,
        sourceCurrency,
        mapOf(
            "USDAFN" to 78.634443,
            "USDINR" to 72.58925,
            "USDZWL" to 322.000305,
        )
    )
}

fun CoinvertDataDao.withNoCurrencies() {
    coEvery { getAllCurrencies() } returns emptyList()
}

fun CoinvertDataDao.withNoQuotes() {
    coEvery { getAllQuotes() } returns emptyList()
}

fun CoinvertDataDao.withSomeCurrencies() {
    coEvery { getAllCurrencies() } returns listOf(
        Currency("USD", "United State Dollar"),
        Currency("INR", "Indian Rupees"),
        Currency("AFN", "Afghan Afghani"),
    )
}

fun CoinvertDataDao.withSomeQuotes() {
    coEvery { getAllQuotes() } returns listOf(
        Quote("AFN", 78.634443),
        Quote("INR", 72.58925),
        Quote("USD", 1.0),
        Quote("ZWL", 322.000305),
    )
}

fun NetworkLogManager.withNoHistory() {
    coEvery { flowQuotesFetchedAt } returns flowOf(0L)
}

/* Copyright 2019 Google LLC.
   SPDX-License-Identifier: Apache-2.0 */
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    this.observeForever(observer)

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        throw TimeoutException("LiveData value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}

/* Copyright 2019 Google LLC.
   SPDX-License-Identifier: Apache-2.0 */
@VisibleForTesting(otherwise = VisibleForTesting.NONE)
fun <T> LiveEvent<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }

    this.observeForever(observer)

    // Don't wait indefinitely if the LiveData is not set.
    if (!latch.await(time, timeUnit)) {
        throw TimeoutException("LiveEvent value was never set.")
    }

    @Suppress("UNCHECKED_CAST")
    return data as T
}
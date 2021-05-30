package dev.bhavindesai.data.remote.services

import dev.bhavindesai.domain.remote.GetAllCurrenciesResponse
import dev.bhavindesai.domain.remote.GetLiveCurrencyQuotesResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyLayerService {

    @GET("list")
    suspend fun getAllCurrencies(@Query("access_key") accessKey: String = "b0774b23a3424511eb32ab0834454cde") : GetAllCurrenciesResponse

    @GET("live")
    suspend fun getLiveCurrencyQuotes(
        @Query("source") source: String,
        @Query("currencies") currencies: String,
        @Query("access_key") accessKey: String = "b0774b23a3424511eb32ab0834454cde"
    ): GetLiveCurrencyQuotesResponse
}


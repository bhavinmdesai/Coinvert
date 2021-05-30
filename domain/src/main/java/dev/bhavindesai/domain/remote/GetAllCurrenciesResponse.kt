package dev.bhavindesai.domain.remote

data class GetAllCurrenciesResponse (
    val success: Boolean,
    val currencies: Map<String, String>
    )
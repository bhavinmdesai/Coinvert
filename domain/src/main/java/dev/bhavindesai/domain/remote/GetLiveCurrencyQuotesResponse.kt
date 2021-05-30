package dev.bhavindesai.domain.remote

data class GetLiveCurrencyQuotesResponse (
    val success: Boolean,
    val timestamp: Long,
    val source: String,
    val quotes: Map<String, Double>,
    val error: Error? = null
)

data class Error(
    val code: Int,
    val info: String
)
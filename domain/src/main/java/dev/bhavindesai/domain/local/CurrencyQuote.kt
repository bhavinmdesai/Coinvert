package dev.bhavindesai.domain.local

import java.math.BigDecimal
import java.math.RoundingMode

data class CurrencyQuote(
    val sourceAbbreviation: String,
    val targetAbbreviation: String,
    val multiplier: Double,
    private val _amount: BigDecimal,
) {
    val amount: BigDecimal
        get() = _amount
            .multiply(BigDecimal(multiplier))
            .setScale(2, RoundingMode.HALF_EVEN)
}

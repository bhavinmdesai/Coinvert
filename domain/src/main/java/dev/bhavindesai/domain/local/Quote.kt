package dev.bhavindesai.domain.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Quotes")
data class Quote(
    @PrimaryKey
    val targetCurrencyAbbreviation: String,
    val price: Double
)

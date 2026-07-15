package com.cryptotracker.feature.tracker

import com.cryptotracker.domain.CryptoPrice

data class TrackerUiState(
    val cryptoList: List<CryptoPrice> = emptyList(),
    val fngScore: Int = 50,
    val fngLabel: String = "Neutral",
    val isLoading: Boolean = true, 
    val errorMessage: String? = null
)
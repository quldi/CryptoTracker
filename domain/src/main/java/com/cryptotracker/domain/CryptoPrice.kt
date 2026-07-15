package com.cryptotracker.domain

data class CryptoPrice(
    val symbol: String,
    val price: Double,
    val priceChangePercent: Double,
    val volume: Double, 
    val timestamp: Long,
    val priceHistory: List<Double> = emptyList()
)
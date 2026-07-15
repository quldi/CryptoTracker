package com.cryptotracker.domain

import kotlinx.coroutines.flow.Flow

interface CryptoRepository {
    fun getCryptoDashboardStream(filter: CryptoFilter): Flow<List<CryptoPrice>>
    
    suspend fun getFearAndGreedIndex(): Pair<Int, String>
}
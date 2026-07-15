package com.cryptotracker.data

import com.cryptotracker.core.network.CryptoWebSocketClient
import com.cryptotracker.domain.CryptoFilter
import com.cryptotracker.domain.CryptoPrice
import com.cryptotracker.domain.CryptoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class CryptoRepositoryImpl : CryptoRepository {
    
    private val webSocketClient = CryptoWebSocketClient()
    private val maxHistorySize = 30

    override suspend fun getFearAndGreedIndex(): Pair<Int, String> {
        return withContext(Dispatchers.IO) {
            webSocketClient.fetchFearAndGreedIndex()
        }
    }

    override fun getCryptoDashboardStream(filter: CryptoFilter): Flow<List<CryptoPrice>> = flow {
        val priceCache = ConcurrentHashMap<String, CryptoPrice>()
        
        val dynamicSymbols = withContext(Dispatchers.IO) {
            webSocketClient.fetchTopSymbols(filter.name)
        }

        if (dynamicSymbols.isEmpty()) {
            emit(emptyList())
            return@flow
        }

        withContext(Dispatchers.IO) {
            coroutineScope {
                dynamicSymbols.map { symbol ->
                    async {
                        val historicalData = webSocketClient.fetchHistoricalPrices(symbol)
                        if (historicalData.prices.isNotEmpty()) {
                            priceCache[symbol] = CryptoPrice(
                                symbol = symbol,
                                price = historicalData.prices.last(),
                                priceChangePercent = 0.0,
                                volume = historicalData.latestVolume,
                                timestamp = System.currentTimeMillis(),
                                priceHistory = historicalData.prices
                            )
                        }
                    }
                }.awaitAll()
            }
        }

        if (priceCache.isNotEmpty()) {
            emit(dynamicSymbols.mapNotNull { priceCache[it] })
        }

        webSocketClient.mainPriceStream(dynamicSymbols).collect { networkData ->
            val symbol = networkData.symbol
            val existing = priceCache[symbol]
            
            val updatedHistory = (existing?.priceHistory ?: emptyList()).toMutableList().apply {
                if (isNotEmpty()) {
                    set(lastIndex, networkData.price)
                } else {
                    add(networkData.price)
                }
            }

            priceCache[symbol] = CryptoPrice(
                symbol = symbol,
                price = networkData.price,
                priceChangePercent = if (networkData.priceChangePercent != 0.0) networkData.priceChangePercent else existing?.priceChangePercent ?: 0.0,
                volume = networkData.volume,
                timestamp = networkData.timestamp,
                priceHistory = updatedHistory
            )
            
            val sortedResponse = dynamicSymbols.mapNotNull { priceCache[it] }
            emit(sortedResponse)
        }
    }
}
package com.cryptotracker.core.network

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.net.InetAddress
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

data class NetworkCryptoPrice(
    val symbol: String,
    val price: Double,
    val priceChangePercent: Double,
    val volume: Double,
    val timestamp: Long
)

data class HistoricalData(
    val prices: List<Double>,
    val latestVolume: Double
)

class CryptoWebSocketClient {
    private val client = createAntiCensorshipClient()

    fun fetchTopSymbols(filterType: String): List<String> {
        val rawList = mutableListOf<JSONObject>()
        try {
            val url = "https://api.binance.com/api/v3/ticker/24hr"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)
                        val sym = item.getString("symbol")
                        if (sym.endsWith("USDT") && !sym.contains("UP") && !sym.contains("DOWN")) {
                            rawList.add(item)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            return listOf("BTCUSDT", "ETHUSDT", "BNBUSDT")
        }

        return when (filterType) {
            "STABLE" -> {
                rawList.filter { 
                    val price = it.getString("lastPrice").toDouble()
                    price in 0.95..1.05 && (it.getString("symbol").contains("USD") || it.getString("symbol").contains("DAI"))
                }.sortedByDescending { it.getString("quoteVolume").toDouble() }
                 .take(20).map { it.getString("symbol") }
            }
            "NEW" -> {
                rawList.filter { it.getString("quoteVolume").toDouble() > 10000.0 }
                 .sortedByDescending { kotlin.math.abs(it.getString("priceChangePercent").toDouble()) }
                 .take(20).map { it.getString("symbol") }
            }
            else -> {
                rawList.sortedByDescending { it.getString("quoteVolume").toDouble() }
                 .take(20).map { it.getString("symbol") }
            }
        }
    }

    fun fetchHistoricalPrices(symbol: String): HistoricalData {
        val prices = mutableListOf<Double>()
        var volume = 0.0
        try {
            val url = "https://api.binance.com/api/v3/klines?symbol=${symbol.uppercase()}&interval=1h&limit=24"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val jsonArray = JSONArray(body)
                    for (i in 0 until jsonArray.length()) {
                        val kline = jsonArray.getJSONArray(i)
                        prices.add(kline.getString(4).toDouble())
                        if (i == jsonArray.length() - 1) {
                            volume = kline.getString(7).toDouble()
                        }
                    }
                }
            }
        } catch (e: Exception) {}

        if (prices.isEmpty()) {
            try {
                val url = "https://api.binance.com/api/v3/ticker/price?symbol=${symbol.uppercase()}"
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val json = JSONObject(response.body?.string() ?: "")
                        val currentPrice = json.getString("price").toDouble()
                        repeat(24) { prices.add(currentPrice) } 
                    }
                }
            } catch (e: Exception) {
                repeat(24) { prices.add(1.0) }
            }
        }
        return HistoricalData(prices = prices, latestVolume = volume)
    }

    fun fetchFearAndGreedIndex(): Pair<Int, String> {
        try {
            val url = "https://api.alternative.me/fng/"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    val root = JSONObject(body)
                    val dataArray = root.getJSONArray("data")
                    if (dataArray.length() > 0) {
                        val obj = dataArray.getJSONObject(0)
                        return Pair(obj.getString("value").toInt(), obj.getString("value_classification"))
                    }
                }
            }
        } catch (e: Exception) {}
        return Pair(50, "Neutral")
    }

    fun mainPriceStream(symbols: List<String>): Flow<NetworkCryptoPrice> = callbackFlow {
        if (symbols.isEmpty()) {
            close()
            return@callbackFlow
        }
        val streams = symbols.joinToString("/") { "${it.lowercase()}@ticker" }
        val request = Request.Builder().url("wss://stream.binance.com:9443/stream?streams=$streams").build()

        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val root = JSONObject(text)
                    val data = root.getJSONObject("data")
                    trySend(
                        NetworkCryptoPrice(
                            symbol = data.getString("s"),
                            price = data.getString("c").toDouble(),
                            priceChangePercent = data.getString("P").toDouble(),
                            volume = data.getString("q").toDouble(),
                            timestamp = data.getLong("E")
                        )
                    )
                } catch (e: Exception) {}
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                close(t)
            }
        }

        val webSocket = client.newWebSocket(request, listener)
        awaitClose { webSocket.close(1000, "Disconnected") }
    }

    private fun createAntiCensorshipClient(): OkHttpClient {
        try {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("SSL").apply {
                init(null, trustAllCerts, java.security.SecureRandom())
            }
            val customDohResolver = object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    if (hostname.contains("binance")) {
                        try {
                            val dnsRequest = Request.Builder().url("https://8.8.8.8/resolve?name=$hostname&type=A").build()
                            OkHttpClient.Builder().build().newCall(dnsRequest).execute().use { res ->
                                if (res.isSuccessful) {
                                    val json = JSONObject(res.body?.string() ?: "")
                                    val answer = json.optJSONArray("Answer")
                                    if (answer != null && answer.length() > 0) {
                                        val list = mutableListOf<InetAddress>()
                                        for (i in 0 until answer.length()) {
                                            list.add(InetAddress.getByName(answer.getJSONObject(i).getString("data")))
                                        }
                                        if (list.isNotEmpty()) return list
                                    }
                                }
                            }
                        } catch (e: Exception) {}
                    }
                    return Dns.SYSTEM.lookup(hostname)
                }
            }
            return OkHttpClient.Builder()
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .dns(customDohResolver)
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
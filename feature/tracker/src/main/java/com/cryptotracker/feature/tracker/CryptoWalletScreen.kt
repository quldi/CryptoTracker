package com.cryptotracker.feature.tracker

import android.graphics.BitmapFactory
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptotracker.feature.tracker.components.CryptoIcon
import com.cryptotracker.feature.tracker.components.CryptoTrackerLoadingView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

@Suppress("FunctionName")
@Composable
fun CryptoWalletScreen(
    onNavigateToBuySell: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isWalletLoading by remember { mutableStateOf(true) }
    
    var btcPrice by remember { mutableDoubleStateOf(65378.36) }
    var ethPrice by remember { mutableDoubleStateOf(1930.16) }
    var usdtPrice by remember { mutableDoubleStateOf(1.00) }

    var btcChange by remember { mutableDoubleStateOf(2.31) }
    var ethChange by remember { mutableDoubleStateOf(3.39) }
    var usdtChange by remember { mutableDoubleStateOf(-0.02) }

    val btcHoldings = 14.25048
    val ethHoldings = 85.32140
    val usdtHoldings = 24058.99

    LaunchedEffect(Unit) {
        delay(400)
        isWalletLoading = false

        while (true) {
            delay(1500)
            btcPrice += (Math.random() - 0.5) * 42.0
            ethPrice += (Math.random() - 0.5) * 4.5
            usdtPrice += (Math.random() - 0.5) * 0.0004

            btcChange += (Math.random() - 0.5) * 0.05
            ethChange += (Math.random() - 0.5) * 0.07
            usdtChange += (Math.random() - 0.5) * 0.002
        }
    }

    val btcValueUsd = btcHoldings * btcPrice
    val ethValueUsd = ethHoldings * ethPrice
    val usdtValueUsd = usdtHoldings * usdtPrice
    val totalBalance = btcValueUsd + ethValueUsd + usdtValueUsd

    val btcAlloc = (btcValueUsd / totalBalance) * 100.0
    val ethAlloc = (ethValueUsd / totalBalance) * 100.0
    val usdtAlloc = (usdtValueUsd / totalBalance) * 100.0

    val weightedPnL = ((btcValueUsd * btcChange) + (ethValueUsd * ethChange) + (usdtValueUsd * usdtChange)) / totalBalance

    Crossfade(
        targetState = isWalletLoading,
        animationSpec = tween(durationMillis = 250),
        label = "WalletLoadingTransition"
    ) { loading ->
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CryptoTrackerLoadingView()
            }
        } else {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DeveloperProfileAvatar(modifier = Modifier.size(48.dp))
                    
                    Spacer(modifier = Modifier.width(14.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Ahmad Quldi", 
                                color = MaterialTheme.colorScheme.onSurface, 
                                fontSize = 16.sp, 
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Verified Profile",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tier 3 Institutional Portfolio", 
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), 
                            fontSize = 11.sp, 
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Total Balance Valuations", 
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pnlColor = if (weightedPnL >= 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        
                        Text(
                            text = String.format(Locale.US, "$%,.2f", totalBalance),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .background(pnlColor.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                .border(1.dp, pnlColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%+.2f%%", weightedPnL),
                                color = pnlColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("Deposit", "Withdraw", "Transfer").forEach { action ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (action == "Deposit" || action == "Withdraw") {
                                            onNavigateToBuySell()
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = action, 
                                    color = MaterialTheme.colorScheme.onSurface, 
                                    fontSize = 13.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Asset Distribution",
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    WalletAssetRow(
                        symbol = "BTC",
                        name = "Bitcoin",
                        holdings = "14.25048 BTC",
                        usdValue = btcValueUsd,
                        allocation = btcAlloc,
                        change24h = btcChange
                    )
                    WalletAssetRow(
                        symbol = "ETH",
                        name = "Ethereum",
                        holdings = "85.32140 ETH",
                        usdValue = ethValueUsd,
                        allocation = ethAlloc,
                        change24h = ethChange
                    )
                    WalletAssetRow(
                        symbol = "USDT",
                        name = "Tether",
                        holdings = "24,058.99 USDT",
                        usdValue = usdtValueUsd,
                        allocation = usdtAlloc,
                        change24h = usdtChange
                    )
                }
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun DeveloperProfileAvatar(modifier: Modifier = Modifier) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val imageUrl = "https://github.com/quldi.png"

    LaunchedEffect(imageUrl) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(imageUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                bitmap = BitmapFactory.decodeStream(connection.inputStream)?.asImageBitmap()
            } catch (e: Exception) {
                bitmap = null
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!,
            contentDescription = "Ahmad Quldi Profile Photo",
            modifier = modifier
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
    } else {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background, CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AQ", 
                color = MaterialTheme.colorScheme.onBackground, 
                fontSize = 14.sp, 
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun WalletAssetRow(
    symbol: String,
    name: String,
    holdings: String,
    usdValue: Double,
    allocation: Double,
    change24h: Double
) {
    val changeColor = if (change24h >= 0.0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CryptoIcon(symbol = symbol, modifier = Modifier.size(32.dp))
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = symbol, 
                color = MaterialTheme.colorScheme.onSurface, 
                fontSize = 16.sp, 
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = name, 
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f), 
                fontSize = 11.sp, 
                fontWeight = FontWeight.Medium
            )
        }
        
        Column(modifier = Modifier.weight(1.8f), horizontalAlignment = Alignment.End) {
            Text(
                text = String.format(Locale.US, "$%,.2f", usdValue),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = holdings, 
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f), 
                    fontSize = 11.sp, 
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = String.format(Locale.US, "%.1f%%", allocation),
                    color = changeColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
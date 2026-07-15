package com.cryptotracker.feature.tracker.components

import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptotracker.domain.CryptoFilter
import com.cryptotracker.domain.CryptoPrice
import com.cryptotracker.feature.tracker.TrackerViewModel
import com.cryptotracker.feature.tracker.TrackerUiState
import com.cryptotracker.feature.tracker.graph.SparklineGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

private val ThemeCard = Color(0xFF121722)
private val ColorGreen = Color(0xFF00E676)
private val ColorRed = Color(0xFFFF5252)
private enum class ScreenLayout { LOADING, SUCCESS, ERROR }

@Suppress("FunctionName")
@Composable
fun CryptoIcon(symbol: String, modifier: Modifier = Modifier) {
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val cleanName = symbol.replace("USDT", "").lowercase()
    val iconUrl = "https://assets.coincap.io/assets/icons/$cleanName@2x.png"

    LaunchedEffect(iconUrl) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(iconUrl)
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
        Image(bitmap = bitmap!!, contentDescription = null, modifier = modifier)
    } else {
        Box(modifier = modifier.background(Color(0xFF1E293B), shape = CircleShape), contentAlignment = Alignment.Center) {
            Text(text = symbol.take(1), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun formatVolume(vol: Double): String {
    return when {
        vol >= 1_000_000_000_000.0 -> String.format(Locale.US, "Vol: $%,.2fT", vol / 1_000_000_000_000.0)
        vol >= 1_000_000_000.0 -> String.format(Locale.US, "Vol: $%,.2fB", vol / 1_000_000_000.0)
        vol >= 1_000_000.0 -> String.format(Locale.US, "Vol: $%,.2fM", vol / 1_000_000.0)
        else -> String.format(Locale.US, "Vol: $%,.2f", vol)
    }
}

@Suppress("FunctionName")
@Composable
fun TrackerTopAppBar(viewModel: TrackerViewModel, containerColor: Color) {
    val currentFilter by viewModel.activeFilter.collectAsState()
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .statusBarsPadding()
            .padding(top = 22.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Crypto Tracker", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.25.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = "Real-time spot markets and global volume indexes.", color = Color.White.copy(alpha = 0.45f), fontSize = 12.sp, fontWeight = FontWeight.Normal)
            }
            FearGreedMicroGauge(score = state.fngScore, label = state.fngLabel, isLoading = state.isLoading)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            FilterChipItem(text = "Popular", isActive = currentFilter == CryptoFilter.POPULAR, accentColor = Color(0xFF00E676)) {
                viewModel.changeFilter(CryptoFilter.POPULAR)
            }
            FilterChipItem(text = "Stable Coin", isActive = currentFilter == CryptoFilter.STABLE, accentColor = Color(0xFF00B0FF)) {
                viewModel.changeFilter(CryptoFilter.STABLE)
            }
            FilterChipItem(text = "New Movers", isActive = currentFilter == CryptoFilter.NEW, accentColor = Color(0xFFFF9100)) {
                viewModel.changeFilter(CryptoFilter.NEW)
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun FearGreedMicroGauge(score: Int, label: String, isLoading: Boolean, modifier: Modifier = Modifier) {
    val dynamicColor = when (score) {
        in 0..25 -> Color(0xFFFF5252)
        in 26..45 -> Color(0xFFFF9100)
        in 46..55 -> Color(0xFFFBC02D)
        in 56..75 -> Color(0xFF4CAF50)
        else -> Color(0xFF00E676)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "GaugeRingTransition")
    val rotationPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1100, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "RingSpin"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.width(72.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(38.dp)) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawArc(
                    color = Color(0xFF1E293B), startAngle = 140f, sweepAngle = 260f, useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                val sweepAngleCalculated = (score / 100f) * 260f
                drawArc(
                    color = dynamicColor, startAngle = 140f, sweepAngle = sweepAngleCalculated, useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                if (isLoading) {
                    drawArc(
                        color = dynamicColor.copy(alpha = 0.4f), startAngle = rotationPhase, sweepAngle = 90f, useCenter = false,
                        style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            Text(text = score.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label.uppercase(Locale.US), color = dynamicColor, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp)
    }
}

@Suppress("FunctionName")
@Composable
private fun FilterChipItem(text: String, isActive: Boolean, accentColor: Color, onClick: () -> Unit) {
    val animatedTextColor by animateColorAsState(
        targetValue = if (isActive) Color.White else Color.White.copy(alpha = 0.38f),
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "ChipTextColor"
    )
    val animatedIndicatorColor by animateColorAsState(
        targetValue = if (isActive) accentColor else Color.Transparent,
        animationSpec = tween(durationMillis = 200, easing = LinearEasing),
        label = "IndicatorColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .graphicsLayer()
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null 
            ) { onClick() }
    ) {
        Text(text = text, color = animatedTextColor, fontSize = 15.sp, fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium, letterSpacing = 0.2.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Box(modifier = Modifier.size(24.dp, 3.dp).background(color = animatedIndicatorColor, shape = RoundedCornerShape(1.5.dp)))
    }
}

@Suppress("FunctionName")
@Composable
fun CryptoTrackerLoadingView(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "BarLoadingTransition")
    val animationPhase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(animation = tween(1000, easing = LinearEasing), repeatMode = RepeatMode.Restart),
        label = "BarPhase"
    )
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Row(modifier = Modifier.height(45.dp), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
            for (i in 0 until 6) {
                val rawSin = kotlin.math.sin(animationPhase - (i * 0.5f))
                val positiveSin = if (rawSin < 0f) -rawSin else rawSin
                val heightFactor = 0.25f + (positiveSin * 0.75f)
                val barAlpha = 1f - (i * 0.1f)
                Box(modifier = Modifier.width(6.dp).fillMaxHeight(heightFactor).background(color = Color(0xFF00E676).copy(alpha = barAlpha), shape = RoundedCornerShape(3.dp)))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Crypto Tracker", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
    }
}

@Suppress("FunctionName")
@Composable
fun TrackerBottomNavBar(currentTab: Int, onTabSelected: (Int) -> Unit, containerColor: Color, activeColor: Color) {
    val buySellIcon = remember {
        ImageVector.Builder(name = "BuySell", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .path(fill = SolidColor(Color.White)) {
                moveTo(16f, 17.01f); verticalLineTo(10f); horizontalLineToRelative(-2f); verticalLineToRelative(7.01f); horizontalLineToRelative(-3f); lineTo(15f, 21f); lineToRelative(4f, -3.99f); horizontalLineToRelative(-3f); close()
                moveTo(9f, 3f); lineTo(5f, 6.99f); horizontalLineToRelative(3f); verticalLineTo(14f); horizontalLineToRelative(2f); verticalLineTo(6.99f); horizontalLineToRelative(3f); lineTo(9f, 3f); close()
            }.build()
    }
    val walletIcon = remember {
        ImageVector.Builder(name = "Wallet", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
            .path(fill = SolidColor(Color.White)) {
                moveTo(21f, 18f); verticalLineToRelative(1f); curveToRelative(0f, 1.1f, -0.9f, 2f, -2f, 2f); horizontalLineTo(5f); curveToRelative(-1.11f, 0f, -2f, -0.9f, -2f, -2f); verticalLineTo(6f); curveToRelative(0f, -1.1f, 0.89f, -2f, 2f, -2f); horizontalLineToRelative(14f); curveToRelative(1.1f, 0f, 2f, 0.9f, 2f, 2f); verticalLineToRelative(1f); horizontalLineToRelative(-9f); curveToRelative(-1.11f, 0f, -2f, 0.9f, -2f, 2f); verticalLineToRelative(8f); curveToRelative(0f, 1.1f, 0.89f, 2f, 2f, 2f); horizontalLineToRelative(9f); close()
                moveTo(12f, 9f); verticalLineToRelative(6f); horizontalLineToRelative(8f); verticalLineTo(9f); horizontalLineToRelative(-8f); close()
                moveTo(16f, 13f); curveToRelative(-0.55f, 0f, -1f, -0.45f, -1f, -1f); curveToRelative(0f, -0.55f, 0.45f, -1f, 1f, -1f); curveToRelative(0.55f, 0f, 1f, 0.45f, 1f, 1f); curveToRelative(0f, 0.55f, -0.45f, 1f, -1f, 1f); close()
            }.build()
    }
    NavigationBar(containerColor = containerColor, tonalElevation = 12.dp) {
        NavigationBarItem(selected = currentTab == 0, onClick = { onTabSelected(0) }, icon = { Icon(Icons.Default.Star, contentDescription = null) }, label = { Text("Trending") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = activeColor, selectedTextColor = activeColor, unselectedIconColor = Color.White.copy(alpha = 0.3f), unselectedTextColor = Color.White.copy(alpha = 0.3f), indicatorColor = Color.Transparent))
        NavigationBarItem(selected = currentTab == 1, onClick = { onTabSelected(1) }, icon = { Icon(imageVector = buySellIcon, contentDescription = null) }, label = { Text("Buy/Sell") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = activeColor, selectedTextColor = activeColor, unselectedIconColor = Color.White.copy(alpha = 0.3f), unselectedTextColor = Color.White.copy(alpha = 0.3f), indicatorColor = Color.Transparent))
        NavigationBarItem(selected = currentTab == 2, onClick = { onTabSelected(2) }, icon = { Icon(imageVector = walletIcon, contentDescription = null) }, label = { Text("Wallet") }, colors = NavigationBarItemDefaults.colors(selectedIconColor = activeColor, selectedTextColor = activeColor, unselectedIconColor = Color.White.copy(alpha = 0.3f), unselectedTextColor = Color.White.copy(alpha = 0.3f), indicatorColor = Color.Transparent))
    }
}

@Suppress("FunctionName")
@Composable
fun CryptoRowCard(crypto: CryptoPrice, containerColor: Color, greenColor: Color, redColor: Color, onClick: () -> Unit) {
    val isNegative = crypto.priceChangePercent < 0.0
    val dynamicColor = if (isNegative) redColor else greenColor
    val percentageText = if (crypto.priceChangePercent > 0.0) "+${String.format(Locale.US, "%.2f", crypto.priceChangePercent)}%"
                           else "${String.format(Locale.US, "%.2f", crypto.priceChangePercent)}%"
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = containerColor)) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(modifier = Modifier.weight(1.4f), verticalAlignment = Alignment.CenterVertically) {
                CryptoIcon(symbol = crypto.symbol, modifier = Modifier.size(34.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = crypto.symbol.replace("USDT", ""), color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = formatVolume(crypto.volume), color = Color.White.copy(alpha = 0.35f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
            Box(modifier = Modifier.weight(1f).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
                SparklineGraph(prices = crypto.priceHistory, lineColor = dynamicColor, modifier = Modifier.width(85.dp).height(32.dp))
            }
            Column(modifier = Modifier.weight(1.3f), horizontalAlignment = Alignment.End) {
                Text(text = String.format(Locale.US, "%,.2f", crypto.price), color = dynamicColor, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(3.dp))
                Text(text = percentageText, color = dynamicColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
fun TrendingTabContent(state: TrackerUiState, onCryptoClick: (CryptoPrice) -> Unit) {
    val layoutKey = when {
        state.errorMessage != null -> ScreenLayout.ERROR
        state.isLoading -> ScreenLayout.LOADING
        else -> ScreenLayout.SUCCESS
    }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (layoutKey) {
            ScreenLayout.LOADING -> CryptoTrackerLoadingView()
            ScreenLayout.SUCCESS -> {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(items = state.cryptoList, key = { it.symbol }) { crypto ->
                        CryptoRowCard(crypto = crypto, containerColor = ThemeCard, greenColor = ColorGreen, redColor = ColorRed, onClick = { onCryptoClick(crypto) })
                    }
                }
            }
            ScreenLayout.ERROR -> Text(text = state.errorMessage ?: "Unknown Error", color = ColorRed, fontSize = 15.sp)
        }
    }
}
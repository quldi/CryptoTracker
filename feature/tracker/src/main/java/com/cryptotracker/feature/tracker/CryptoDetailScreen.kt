package com.cryptotracker.feature.tracker

import android.graphics.BitmapFactory
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme 
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb 
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.Color
import com.cryptotracker.domain.CryptoPrice
import com.cryptotracker.feature.tracker.components.CryptoIcon
import com.cryptotracker.feature.tracker.components.CryptoTrackerLoadingView
import kotlinx.coroutines.delay
import java.util.Locale

@Suppress("FunctionName")
@Composable
fun CryptoDetailScreen(
    crypto: CryptoPrice,
    isLoading: Boolean,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isTransitionLoading by remember { mutableStateOf(true) }
    var selectedTimeFilter by remember { mutableStateOf("1h") }
    var dynamicPriceHistory by remember { mutableStateOf(crypto.priceHistory) }
    var activeInfoDialog by remember { mutableStateOf<Pair<String, String>?>(null) }

    LaunchedEffect(crypto.symbol, selectedTimeFilter) {
        isTransitionLoading = true
        dynamicPriceHistory = when (selectedTimeFilter) {
            "1m" -> crypto.priceHistory.map { it * (1.0 + (kotlin.math.sin(it) * 0.003)) }
            "1d" -> crypto.priceHistory.map { it * (1.0 + (kotlin.math.cos(it) * 0.008)) }
            "1M" -> crypto.priceHistory.map { it * (1.0 + (kotlin.math.sin(it) * 0.022)) }
            "1Y" -> crypto.priceHistory.map { it * (1.0 + (kotlin.math.cos(it) * 0.055)) }
            else -> crypto.priceHistory
        }
        delay(400)
        isTransitionLoading = false
    }

    val baseSymbol = crypto.symbol.replace("USDT", "")
    val isNegative = crypto.priceChangePercent < 0.0
    
    val changeColor = if (isNegative) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    
    val cleanPercentageText = if (crypto.priceChangePercent >= 0.0) 
        "+${String.format(Locale.US, "%.2f", crypto.priceChangePercent)}%"
    else 
        "${String.format(Locale.US, "%.2f", crypto.priceChangePercent)}%"
    
    val calculatedMktCap = crypto.volume * 4.2 
    val calculatedFdv = calculatedMktCap * 1.05
    val totalSupply = 20_050_000.0
    val maxSupply = 21_000_000.0

    val absoluteLoadingState = isLoading || isTransitionLoading

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                    modifier = Modifier.size(24.dp).clickable { onBack() }
                )
                Spacer(modifier = Modifier.width(18.dp))
                CryptoIcon(symbol = crypto.symbol, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$baseSymbol Details", 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.ExtraBold, 
                    letterSpacing = 0.25.sp
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Crossfade(
                targetState = absoluteLoadingState,
                animationSpec = tween(durationMillis = 250),
                modifier = Modifier.padding(innerPadding),
                label = "DetailLayoutControl"
            ) { loading ->
                if (loading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CryptoTrackerLoadingView()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = formatDynamicPrice(crypto.price), 
                                            color = MaterialTheme.colorScheme.onSurface, 
                                            fontSize = 24.sp, 
                                            fontWeight = FontWeight.Black, 
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = cleanPercentageText, color = changeColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Live Spot Price", 
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), 
                                        fontSize = 11.sp, 
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    listOf("1m", "1h", "1d", "1M", "1Y").forEach { timeRange ->
                                        TimeFilterTab(text = timeRange, isActive = selectedTimeFilter == timeRange, accentColor = changeColor) {
                                            selectedTimeFilter = timeRange
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            val maxPriceValue = dynamicPriceHistory.maxOrNull() ?: 1.0
                            val minPriceValue = dynamicPriceHistory.minOrNull() ?: 0.0

                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.weight(1f)) {
                                    BigInteractiveChartCanvas(
                                        prices = dynamicPriceHistory,
                                        modifier = Modifier.fillMaxWidth().height(180.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column(
                                    modifier = Modifier.height(180.dp),
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(text = formatAxisPrice(maxPriceValue), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text(text = formatAxisPrice((maxPriceValue + minPriceValue) / 2.0), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    Text(text = formatAxisPrice(minPriceValue), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                }
                            }
                        }

                        MetricCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "Market cap",
                            onInfoClick = {
                                activeInfoDialog = "Market Cap" to "The total market value of this cryptocurrency's circulating supply. Calculated as Spot Price multiplied by Circulating Supply."
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = formatCurrency(calculatedMktCap), 
                                    color = MaterialTheme.colorScheme.onSurface, 
                                    fontSize = 22.sp, 
                                    fontWeight = FontWeight.Black, 
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = cleanPercentageText, color = changeColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Volume (24h)",
                                onInfoClick = {
                                    activeInfoDialog = "Volume (24h)" to "A measure of how much of this cryptocurrency was actively traded across major spot exchanges in the last 24 hours."
                                }
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = formatCurrency(crypto.volume), 
                                        color = MaterialTheme.colorScheme.onSurface, 
                                        fontSize = 16.sp, 
                                        fontWeight = FontWeight.Bold, 
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = cleanPercentageText, color = changeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Vol/Mkt Cap (24h)",
                                onInfoClick = {
                                    activeInfoDialog = "Vol/Mkt Cap (24h)" to "The ratio of 24h trading volume to total market cap. Higher ratios indicate strong relative liquidity and active asset swapping."
                                }
                            ) {
                                Text(
                                    text = "${String.format(Locale.US, "%.2f", (crypto.volume / calculatedMktCap) * 100)}%", 
                                    color = MaterialTheme.colorScheme.onSurface, 
                                    fontSize = 16.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        MetricCard(
                            modifier = Modifier.fillMaxWidth(),
                            title = "FDV",
                            onInfoClick = {
                                activeInfoDialog = "Fully Diluted Valuation (FDV)" to "The theoretical market cap if the maximum supply of coins were already in circulation. Calculated as Max Supply multiplied by Current Spot Price."
                            }
                        ) {
                            Text(
                                text = formatCurrency(calculatedFdv), 
                                color = MaterialTheme.colorScheme.onSurface, 
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold, 
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Total supply",
                                onInfoClick = {
                                    activeInfoDialog = "Total Supply" to "The amount of coins that have already been generated, minus any tokens that have been permanently removed from circulation (burned)."
                                }
                            ) {
                                Text(text = "${formatAmount(totalSupply)} $baseSymbol", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Max. supply",
                                onInfoClick = {
                                    activeInfoDialog = "Max. Supply" to "The maximum absolute limit of coins programmed to ever exist in the lifetime of this digital asset."
                                }
                            ) {
                                Text(text = "${formatAmount(maxSupply)} $baseSymbol", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Circulating supply",
                                onInfoClick = {
                                    activeInfoDialog = "Circulating Supply" to "The quantity of coins currently floating in public markets and actively tradable by market participants."
                                },
                                hasVerifiedBadge = true
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "${formatAmount(totalSupply)} $baseSymbol", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(modifier = Modifier.size(14.dp).border(1.2.dp, Color(0xFF00B0FF), CircleShape))
                                }
                            }
                            MetricCard(
                                modifier = Modifier.weight(1f),
                                title = "Treasury Holdings",
                                onInfoClick = {
                                    activeInfoDialog = "Treasury Holdings" to "The portion of this asset held securely inside the project's development foundation, reserve pool, or smart contract locks."
                                }
                            ) {
                                Text(text = "1.34M $baseSymbol", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            if (activeInfoDialog != null) {
                Dialog(onDismissRequest = { activeInfoDialog = null }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { clip = true }
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF00B0FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = activeInfoDialog!!.first,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = activeInfoDialog!!.second,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 19.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background, RoundedCornerShape(10.dp))
                                    .clickable { activeInfoDialog = null }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Got it",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.2.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Suppress("FunctionName")
@Composable
private fun BigInteractiveChartCanvas(
    prices: List<Double>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "PriceDotPulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 4.dp.value, targetValue = 9.dp.value,
        animationSpec = infiniteRepeatable(animation = tween(1200, easing = LinearEasing), repeatMode = RepeatMode.Reverse),
        label = "PulseRadiusScale"
    )

    val greenColor = MaterialTheme.colorScheme.primary
    val redColor = MaterialTheme.colorScheme.error
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val outlineVariantColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier) {
        if (prices.size < 2) return@Canvas
        
        val width = size.width
        val height = size.height
        
        val minPrice = prices.minOrNull() ?: 0.0
        val maxPrice = prices.maxOrNull() ?: 1.0
        val priceDelta = if (maxPrice == minPrice) 1.0 else (maxPrice - minPrice)
        
        val baselinePrice = prices.first()
        val normalizedYBase = ((baselinePrice - minPrice) / priceDelta).toFloat()
        val yBase = height - (normalizedYBase * (height * 0.80f)) - (height * 0.10f)

        val stepX = width / (prices.size - 1)
        val points = prices.mapIndexed { index, price ->
            val x = index * stepX
            val normalizedY = ((price - minPrice) / priceDelta).toFloat()
            val y = height - (normalizedY * (height * 0.80f)) - (height * 0.10f)
            Offset(x, y)
        }

        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                cubicTo(p0.x + (p1.x - p0.x) / 2f, p0.y, p0.x + (p1.x - p0.x) / 2f, p1.y, p1.x, p1.y)
            }
        }

        val areaPath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 0 until points.size - 1) {
                val p0 = points[i]
                val p1 = points[i + 1]
                cubicTo(p0.x + (p1.x - p0.x) / 2f, p0.y, p0.x + (p1.x - p0.x) / 2f, p1.y, p1.x, p1.y)
            }
            lineTo(width, yBase)
            lineTo(points.first().x, yBase)
            close()
        }

        clipRect(top = 0f, bottom = yBase) {
            drawPath(path = areaPath, brush = Brush.verticalGradient(colors = listOf(greenColor.copy(alpha = 0.22f), Color.Transparent), startY = height * 0.10f, endY = yBase))
            drawPath(path = strokePath, color = greenColor, style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round))
        }

        clipRect(top = yBase, bottom = height) {
            drawPath(path = areaPath, brush = Brush.verticalGradient(colors = listOf(Color.Transparent, redColor.copy(alpha = 0.22f)), startY = yBase, endY = height * 0.90f))
            drawPath(path = strokePath, color = redColor, style = Stroke(width = 2.6.dp.toPx(), cap = StrokeCap.Round))
        }

        drawLine(color = onSurfaceColor.copy(alpha = 0.20f), start = Offset(0f, yBase), end = Offset(width, yBase), strokeWidth = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 12f), 0f))

        val baselineText = formatAxisPrice(baselinePrice)
        
        val argbColor = onSurfaceColor.toArgb()
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.argb(
                (0.85f * 255).toInt(), 
                android.graphics.Color.red(argbColor), 
                android.graphics.Color.green(argbColor), 
                android.graphics.Color.blue(argbColor)
            )
            textSize = 9.dp.toPx()
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.MONOSPACE, android.graphics.Typeface.BOLD)
        }
        
        val textWidth = textPaint.measureText(baselineText)
        val textHeight = textPaint.fontMetrics.descent - textPaint.fontMetrics.ascent

        drawRoundRect(
            color = outlineVariantColor.copy(alpha = 0.9f),
            topLeft = Offset(4.dp.toPx(), yBase - (textHeight / 2) - 2.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(textWidth + 10.dp.toPx(), textHeight + 4.dp.toPx()),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        drawContext.canvas.nativeCanvas.drawText(baselineText, 4.dp.toPx() + 5.dp.toPx(), yBase - textPaint.fontMetrics.ascent - (textHeight / 2), textPaint)

        val lastPoint = points.last()
        val lastPointColor = if (prices.last() >= baselinePrice) greenColor else redColor
        drawCircle(color = lastPointColor.copy(alpha = 0.25f), radius = pulseRadius.dp.toPx(), center = lastPoint)
        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = lastPoint)
        drawCircle(color = lastPointColor, radius = 3.dp.toPx(), center = lastPoint, style = Stroke(width = 1.5.dp.toPx()))
    }
}

@Suppress("FunctionName")
@Composable
private fun TimeFilterTab(text: String, isActive: Boolean, accentColor: Color, onClick: () -> Unit) {
    val animatedTextColor by animateColorAsState(targetValue = if (isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), animationSpec = tween(durationMillis = 150, easing = LinearEasing), label = "TimeTabTextColor")
    val animatedBarColor by animateColorAsState(targetValue = if (isActive) accentColor else Color.Transparent, animationSpec = tween(durationMillis = 150, easing = LinearEasing), label = "TimeTabBarColor")

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.graphicsLayer().clickable(interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }, indication = null) { onClick() }) {
        Text(text = text, color = animatedTextColor, fontSize = 12.sp, fontWeight = if (isActive) FontWeight.Black else FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.size(14.dp, 2.5.dp).background(color = animatedBarColor, shape = RoundedCornerShape(1.dp)))
    }
}

@Suppress("FunctionName")
@Composable
private fun MetricCard(
    title: String,
    modifier: Modifier = Modifier,
    onInfoClick: (() -> Unit)? = null, 
    hasVerifiedBadge: Boolean = false,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = title, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            
            if (onInfoClick != null) {
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) { onInfoClick() }
                        .padding(start = 6.dp, end = 6.dp, top = 2.dp, bottom = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "info-trigger",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
            if (hasVerifiedBadge) {
                Spacer(modifier = Modifier.width(5.dp))
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00B0FF), modifier = Modifier.size(13.dp))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { content() }
    }
}

private fun formatDynamicPrice(value: Double): String {
    return when {
        value <= 0.0 -> "$0.00"
        value < 0.0001 -> String.format(Locale.US, "$%.6f", value)
        value < 1.0 -> String.format(Locale.US, "$%.4f", value)
        else -> String.format(Locale.US, "$%,.2f", value)
    }
}

private fun formatAxisPrice(value: Double): String {
    return when {
        value >= 1000.0 -> String.format(Locale.US, "$%,.1fk", value / 1000.0)
        value < 1.0 -> String.format(Locale.US, "$%.5f", value)
        else -> String.format(Locale.US, "$%,.2f", value)
    }
}

private fun formatCurrency(value: Double): String {
    return when {
        value >= 1_000_000_000_000.0 -> String.format(Locale.US, "$%,.2fT", value / 1_000_000_000_000.0)
        value >= 1_000_000_000.0 -> String.format(Locale.US, "$%,.2fB", value / 1_000_000_000.0)
        else -> String.format(Locale.US, "$%,.2fM", value / 1_000_000.0)
    }
}

private fun formatAmount(value: Double): String {
    return String.format(Locale.US, "%,.2fM", value / 1_000_000.0)
}
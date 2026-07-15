package com.cryptotracker.feature.tracker.graph

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Suppress("FunctionName")
@Composable
fun SparklineGraph(
    prices: List<Double>, 
    lineColor: Color, 
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (prices.size < 2) return@Canvas

        val minPrice = prices.minOrNull() ?: 0.0
        val maxPrice = prices.maxOrNull() ?: 0.0
        val priceRange = maxPrice - minPrice
        val distanceX = size.width / (prices.size - 1)

        val path = Path().apply {
            for (i in prices.indices) {
                val currentPrice = prices[i]
                val ratioY = if (priceRange > 0) (currentPrice - minPrice) / priceRange else 0.5
                val x = i * distanceX
                val y = size.height - (ratioY * size.height).toFloat()

                if (i == 0) moveTo(x, y) else lineTo(x, y)
            }
        }

        val fadeBrush = Brush.horizontalGradient(
            0.0f to Color.Transparent, 
            0.15f to lineColor,        
            0.85f to lineColor,        
            1.0f to Color.Transparent, 
            startX = 0f,
            endX = size.width
        )

        drawPath(
            path = path,
            brush = fadeBrush, 
            style = Stroke(width = 1.75.dp.toPx())
        )
    }
}
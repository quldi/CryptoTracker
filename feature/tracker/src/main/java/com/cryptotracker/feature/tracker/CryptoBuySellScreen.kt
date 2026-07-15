package com.cryptotracker.feature.tracker

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cryptotracker.feature.tracker.components.CryptoTrackerLoadingView
import kotlinx.coroutines.delay
import java.util.Locale

@Suppress("FunctionName")
@Composable
fun CryptoBuySellScreen(modifier: Modifier = Modifier) {
    var isTabLoading by remember { mutableStateOf(true) }
    var isBuySide by remember { mutableStateOf(true) }
    var inputAmount by remember { mutableStateOf("") }
    var selectedAsset by remember { mutableStateOf("BTC") }
    var showExecutionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(400) 
        isTabLoading = false
    }

    val activeColor = if (isBuySide) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val currentPrice = if (selectedAsset == "BTC") 65248.00 else 3450.00
    
    val parsedAmount = inputAmount.toDoubleOrNull() ?: 0.0
    val cryptoReceived = if (isBuySide) parsedAmount / currentPrice else parsedAmount
    val totalCostUsd = if (isBuySide) parsedAmount else parsedAmount * currentPrice

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Execute Trade", 
                color = MaterialTheme.colorScheme.onSurface, 
                fontSize = 22.sp, 
                fontWeight = FontWeight.ExtraBold, 
                letterSpacing = 0.25.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Instantly trade spot assets with institutional routing.", 
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f), 
                fontSize = 12.sp, 
                fontWeight = FontWeight.Normal
            )
        }

        Crossfade(
            targetState = isTabLoading,
            animationSpec = tween(durationMillis = 250),
            modifier = Modifier.weight(1f),
            label = "BuySellLoadingTransition"
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (isBuySide) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent, RoundedCornerShape(9.dp))
                                .border(1.dp, if (isBuySide) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(9.dp))
                                .clickable { isBuySide = true }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "BUY", color = if (isBuySide) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(if (!isBuySide) MaterialTheme.colorScheme.error.copy(alpha = 0.08f) else Color.Transparent, RoundedCornerShape(9.dp))
                                .border(1.dp, if (!isBuySide) MaterialTheme.colorScheme.error else Color.Transparent, RoundedCornerShape(9.dp))
                                .clickable { isBuySide = false }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "SELL", color = if (!isBuySide) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontWeight = FontWeight.Black, fontSize = 14.sp)
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Trading Pair", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "$selectedAsset / USDT", color = MaterialTheme.colorScheme.onSurface, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val borderVariant = MaterialTheme.colorScheme.outlineVariant
                            listOf("BTC", "ETH").forEach { asset ->
                                Box(
                                    modifier = Modifier
                                        .background(if (selectedAsset == asset) MaterialTheme.colorScheme.background else Color.Transparent, RoundedCornerShape(8.dp))
                                        .border(1.dp, if (selectedAsset == asset) activeColor else borderVariant, RoundedCornerShape(8.dp))
                                        .clickable { selectedAsset = asset }
                                        .padding(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Text(text = asset, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                            Text(text = if (isBuySide) "Amount to Spend" else "Amount to Sell", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Price: $${String.format(Locale.US, "%,.2f", currentPrice)}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                        
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = inputAmount,
                            onValueChange = { inputAmount = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp)),
                            textStyle = androidx.compose.ui.text.TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace),
                            placeholder = { Text(text = "0.00", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f)) },
                            trailingIcon = { Text(text = if (isBuySide) "USDT" else selectedAsset, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = activeColor,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf(25, 50, 75, 100).forEach { pct ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(MaterialTheme.colorScheme.background, RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                        .clickable {
                                            val baseVal = if (isBuySide) 5000.0 else 0.5 
                                            inputAmount = String.format(Locale.US, "%.2f", baseVal * (pct / 100.0))
                                        }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "$pct%", color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SummaryRow(label = "Estimated Output", value = if (isBuySide) "${String.format(Locale.US, "%.5f", cryptoReceived)} $selectedAsset" else "$${String.format(Locale.US, "%,.2f", totalCostUsd)}")
                        SummaryRow(label = "Network Exchange Fee (0.1%)", value = if (isBuySide) "$${String.format(Locale.US, "%.2f", totalCostUsd * 0.001)}" else "${String.format(Locale.US, "%.5f", cryptoReceived * 0.001)} $selectedAsset")
                        SummaryRow(label = "Slippage Tolerance", value = "0.5%")
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer { clip = true }
                            .background(activeColor, RoundedCornerShape(12.dp))
                            .clickable(enabled = true, onClick = { showExecutionDialog = true })
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isBuySide) "Place Buy Order" else "Place Sell Order",
                            color = MaterialTheme.colorScheme.background,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        if (showExecutionDialog) {
            Dialog(onDismissRequest = { showExecutionDialog = false }) {
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
                                tint = activeColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Integration Required",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "This is a high-fidelity prototype dashboard for your development portfolio. Production-grade execution of this order requires integration with a live exchange API or Order Matching Engine (e.g., Binance REST API / WebSockets) to route liquidity clearing and process automated wallet database state updates.",
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
                                .clickable { showExecutionDialog = false }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Understood",
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

@Suppress("FunctionName")
@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Text(text = value, color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
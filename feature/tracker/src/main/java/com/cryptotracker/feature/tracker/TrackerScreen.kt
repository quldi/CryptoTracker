package com.cryptotracker.feature.tracker

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme 
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color 
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cryptotracker.domain.CryptoPrice
import com.cryptotracker.feature.tracker.components.NetworkGuardWrapper 
import com.cryptotracker.feature.tracker.components.TrackerBottomNavBar
import com.cryptotracker.feature.tracker.components.TrackerTopAppBar
import com.cryptotracker.feature.tracker.components.TrendingTabContent

@Suppress("FunctionName")
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedCrypto by remember { mutableStateOf<CryptoPrice?>(null) }
    var activeBottomTab by remember { mutableStateOf(0) }

    NetworkGuardWrapper(
        onReconnected = { viewModel.retryConnection() }
    ) {
        if (selectedCrypto != null) {
            val liveCryptoData = state.cryptoList.find { it.symbol == selectedCrypto!!.symbol } ?: selectedCrypto!!
            CryptoDetailScreen(
                crypto = liveCryptoData,
                isLoading = state.isLoading,
                onBack = { selectedCrypto = null }
            )
        } else {
            Scaffold(
                modifier = modifier.fillMaxSize().statusBarsPadding(),
                topBar = {
                    if (activeBottomTab == 0) {
                        TrackerTopAppBar(
                            viewModel = viewModel, 
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    }
                },
                bottomBar = { 
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface) 
                            .navigationBarsPadding() 
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                        )
                        
                        TrackerBottomNavBar(
                            currentTab = activeBottomTab,
                            onTabSelected = { activeBottomTab = it },
                            containerColor = Color.Transparent, 
                            activeColor = MaterialTheme.colorScheme.primary
                        ) 
                    }
                },
                containerColor = MaterialTheme.colorScheme.background
            ) { innerPadding ->
                Crossfade(
                    targetState = activeBottomTab,
                    animationSpec = tween(durationMillis = 250),
                    modifier = Modifier.padding(innerPadding),
                    label = "GlobalNavigationRouting"
                ) { tabIndex ->
                    when (tabIndex) {
                        0 -> TrendingTabContent(state = state, onCryptoClick = { selectedCrypto = it })
                        1 -> CryptoBuySellScreen()
                        2 -> CryptoWalletScreen(
                            onNavigateToBuySell = { activeBottomTab = 1 }
                        )
                    }
                }
            }
        }
    }
}
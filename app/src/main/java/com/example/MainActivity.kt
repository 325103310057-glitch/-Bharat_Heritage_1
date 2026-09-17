package com.example

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.AppScreen
import com.example.ui.HeritageViewModel
import com.example.ui.components.HeritageBottomBar
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HeritageDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginPhoneScreen
import com.example.ui.screens.OtpVerificationScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.BharatHeritageTheme

class MainActivity : ComponentActivity() {

    private val viewModel: HeritageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BharatHeritageTheme {
                BharatHeritageApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun BharatHeritageApp(viewModel: HeritageViewModel) {
    val state by viewModel.uiState.collectAsState()
    val filteredItems = viewModel.getFilteredHeritageItems()
    val favoriteItems = viewModel.getFavoriteHeritageItems()
    val context = LocalContext.current
    val activity = context as? Activity

    // Back handling
    BackHandler(enabled = state.currentScreen != AppScreen.HOME && state.currentScreen != AppScreen.LOGIN_PHONE) {
        when (state.currentScreen) {
            AppScreen.LOGIN_OTP -> viewModel.navigateTo(AppScreen.LOGIN_PHONE)
            AppScreen.DETAIL -> viewModel.navigateTo(state.previousScreen)
            AppScreen.FAVORITES, AppScreen.AI_ASSISTANT, AppScreen.PROFILE -> viewModel.navigateTo(AppScreen.HOME)
            else -> {}
        }
    }

    val showBottomBar = state.currentScreen in listOf(
        AppScreen.HOME,
        AppScreen.FAVORITES,
        AppScreen.AI_ASSISTANT,
        AppScreen.PROFILE
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                HeritageBottomBar(
                    currentScreen = state.currentScreen,
                    favoriteCount = state.favoriteIds.size,
                    onNavigate = { screen -> viewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else 0.dp)
        ) {
            when (state.currentScreen) {
                AppScreen.LOGIN_PHONE -> {
                    LoginPhoneScreen(
                        state = state,
                        onPhoneChanged = { viewModel.onPhoneInputChanged(it) },
                        onCountryCodeChanged = { viewModel.onCountryCodeChanged(it) },
                        onRequestOtp = { activity?.let { viewModel.requestOtp(it) } }
                    )
                }

                AppScreen.LOGIN_OTP -> {
                    OtpVerificationScreen(
                        state = state,
                        onDigitChanged = { index, digit -> viewModel.onOtpDigitChanged(index, digit) },
                        onPasteOtp = { viewModel.pasteFullOtp(it) },
                        onVerifyOtp = { viewModel.verifyOtp() },
                        onResendOtp = { activity?.let { viewModel.resendOtp(it) } },
                        onBack = { viewModel.navigateTo(AppScreen.LOGIN_PHONE) }
                    )
                }

                AppScreen.HOME -> {
                    HomeScreen(
                        state = state,
                        items = filteredItems,
                        onCategorySelected = { viewModel.onCategorySelected(it) },
                        onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onItemClick = { monument -> viewModel.selectHeritageItem(monument) },
                        onToggleFavorite = { id -> viewModel.toggleFavorite(id) },
                        onOpenAiVoice = { viewModel.navigateTo(AppScreen.AI_ASSISTANT) }
                    )
                }

                AppScreen.DETAIL -> {
                    val item = state.selectedItem
                    if (item != null) {
                        val isFav = state.favoriteIds.contains(item.id)
                        HeritageDetailScreen(
                            item = item,
                            isFavorite = isFav,
                            voiceState = state.voiceState,
                            onToggleFavorite = { viewModel.toggleFavorite(item.id) },
                            onBack = { viewModel.navigateTo(state.previousScreen) },
                            onPlayAudioGuide = { text -> viewModel.playAudioGuide(text) },
                            onStopAudioGuide = { viewModel.stopAudioGuide() },
                            onAskAi = { monument -> viewModel.askAiAboutMonument(monument) },
                            onSelectRelated = { related -> viewModel.selectHeritageItem(related) }
                        )
                    } else {
                        viewModel.navigateTo(AppScreen.HOME)
                    }
                }

                AppScreen.FAVORITES -> {
                    FavoritesScreen(
                        favorites = favoriteItems,
                        onItemClick = { monument -> viewModel.selectHeritageItem(monument) },
                        onRemoveFavorite = { id -> viewModel.toggleFavorite(id) },
                        onExploreClick = { viewModel.navigateTo(AppScreen.HOME) }
                    )
                }

                AppScreen.AI_ASSISTANT -> {
                    AiAssistantScreen(
                        state = state,
                        onSendMessage = { text -> viewModel.sendUserChatMessage(text) },
                        onStartVoice = { viewModel.startVoiceInput() },
                        onStopVoice = { viewModel.stopVoiceInput() },
                        onSpeakText = { text -> viewModel.playAudioGuide(text) },
                        onStopSpeech = { viewModel.stopAudioGuide() }
                    )
                }

                AppScreen.PROFILE -> {
                    ProfileScreen(
                        state = state,
                        onUpdateProfile = { name, bio -> viewModel.updateProfile(name, bio) },
                        onCheckHealth = { viewModel.checkBackendHealth() },
                        onLogout = { viewModel.logout() },
                        onLoginClick = { viewModel.navigateTo(AppScreen.LOGIN_PHONE) }
                    )
                }
            }
        }
    }
}

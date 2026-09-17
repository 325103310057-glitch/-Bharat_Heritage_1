package com.example.ui

import android.app.Activity
import android.app.Application
import android.os.CountDownTimer
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.BackendAuthResult
import com.example.ai.HealthStatus
import com.example.ai.HeritageApiClient
import com.example.auth.FirebaseAuthService
import com.example.auth.PhoneAuthSendResult
import com.example.auth.PhoneAuthVerifyResult
import com.example.data.datasource.HeritageCatalog
import com.example.data.local.HeritageDatabase
import com.example.data.local.entity.UserProfileEntity
import com.example.data.model.HeritageCategory
import com.example.data.model.HeritageItem
import com.example.data.repository.HeritageRepository
import com.example.voice.VoiceAssistantManager
import com.example.voice.VoiceState
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    LOGIN_PHONE,
    LOGIN_OTP,
    HOME,
    DETAIL,
    FAVORITES,
    AI_ASSISTANT,
    PROFILE
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class HeritageUiState(
    val currentScreen: AppScreen = AppScreen.LOGIN_PHONE,
    val previousScreen: AppScreen = AppScreen.LOGIN_PHONE,
    // Auth - Real Firebase Phone Auth & Render Backend Verification
    val isLoggedIn: Boolean = false,
    val countryCode: String = "+91",
    val phoneInput: String = "",
    val maskedPhone: String = "",
    val verificationId: String? = null,
    val otpDigits: List<String> = List(6) { "" },
    val isOtpSending: Boolean = false,
    val isVerifyingOtp: Boolean = false,
    val isVerifyingBackend: Boolean = false,
    val otpCooldownSeconds: Int = 0,
    val authError: String? = null,
    // Profile
    val userProfile: UserProfileEntity? = null,
    val currentUserPhone: String? = null,
    val firebaseUid: String? = null,
    // Discovery & Heritage
    val selectedCategory: HeritageCategory = HeritageCategory.ALL,
    val searchQuery: String = "",
    val favoriteIds: Set<String> = emptySet(),
    val selectedItem: HeritageItem? = null,
    // AI Chat & Voice
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            text = "Namaste! I am your Bharat Heritage AI Guide. Ask me anything about India's monuments, ancient temples, sacred traditions, or rich history.",
            isUser = false
        )
    ),
    val isAiThinking: Boolean = false,
    val voiceState: VoiceState = VoiceState.IDLE,
    val audioRms: Float = 0f,
    val voiceError: String? = null,
    val isSpeechAvailable: Boolean = true,
    val isTtsSpeaking: Boolean = false,
    // Backend Health
    val backendHealth: HealthStatus? = null,
    val isCheckingHealth: Boolean = false
)

class HeritageViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HeritageRepository
    private val _uiState = MutableStateFlow(HeritageUiState())
    val uiState: StateFlow<HeritageUiState> = _uiState.asStateFlow()

    private var countdownTimer: CountDownTimer? = null
    var voiceManager: VoiceAssistantManager? = null
        private set

    init {
        val db = HeritageDatabase.getInstance(application)
        repository = HeritageRepository(db.heritageDao())

        // Setup voice assistant
        voiceManager = VoiceAssistantManager(
            context = application.applicationContext,
            scope = viewModelScope,
            onSpeechRecognized = { recognizedText ->
                sendUserChatMessage(recognizedText)
            }
        )

        // Observe favorites
        viewModelScope.launch {
            repository.getFavoriteIds().collectLatest { ids ->
                _uiState.update { it.copy(favoriteIds = ids) }
            }
        }

        // Observe session
        viewModelScope.launch {
            repository.getUserSession().collectLatest { session ->
                if (session != null && session.isLoggedIn) {
                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            currentUserPhone = session.phoneNumber,
                            currentScreen = if (it.currentScreen == AppScreen.LOGIN_PHONE || it.currentScreen == AppScreen.LOGIN_OTP) AppScreen.HOME else it.currentScreen
                        )
                    }
                    // Load profile
                    repository.getUserProfile(session.phoneNumber).collectLatest { profile ->
                        _uiState.update { it.copy(userProfile = profile) }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoggedIn = false,
                            currentUserPhone = null,
                            userProfile = null
                        )
                    }
                }
            }
        }

        // Check backend health on startup
        checkBackendHealth()
    }

    fun navigateTo(screen: AppScreen) {
        voiceManager?.stopSpeaking()
        voiceManager?.stopListening()
        _uiState.update {
            it.copy(
                previousScreen = it.currentScreen,
                currentScreen = screen
            )
        }
    }

    fun selectHeritageItem(item: HeritageItem) {
        _uiState.update {
            it.copy(
                selectedItem = item,
                currentScreen = AppScreen.DETAIL
            )
        }
    }

    fun toggleFavorite(itemId: String) {
        viewModelScope.launch {
            val isFav = _uiState.value.favoriteIds.contains(itemId)
            repository.toggleFavorite(itemId, isFav)
        }
    }

    // --- Search & Filter ---
    fun onCategorySelected(category: HeritageCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun getFilteredHeritageItems(): List<HeritageItem> {
        val state = _uiState.value
        return repository.searchHeritage(state.searchQuery, state.selectedCategory)
    }

    fun getFavoriteHeritageItems(): List<HeritageItem> {
        val favIds = _uiState.value.favoriteIds
        return HeritageCatalog.items.filter { favIds.contains(it.id) }
    }

    // --- Firebase Phone Authentication & Render Backend Verification ---
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    fun onPhoneInputChanged(input: String) {
        val digitsOnly = input.filter { it.isDigit() }.take(15)
        _uiState.update { it.copy(phoneInput = digitsOnly, authError = null) }
    }

    fun onCountryCodeChanged(code: String) {
        val clean = if (code.startsWith("+")) code else "+$code"
        _uiState.update { it.copy(countryCode = clean, authError = null) }
    }

    /**
     * Dispatches a real SMS OTP via Firebase Phone Authentication to user's mobile carrier.
     */
    fun requestOtp(activity: Activity) {
        val state = _uiState.value
        if (!FirebaseAuthService.isValidPhoneNumber(state.countryCode, state.phoneInput)) {
            _uiState.update { it.copy(authError = "Please enter a valid mobile number (10-15 digits).") }
            return
        }

        _uiState.update { it.copy(isOtpSending = true, authError = null) }

        FirebaseAuthService.sendRealOtp(
            activity = activity,
            countryCode = state.countryCode,
            rawNumber = state.phoneInput,
            forceResendingToken = null
        ) { result ->
            when (result) {
                is PhoneAuthSendResult.CodeSent -> {
                    resendToken = result.token
                    _uiState.update {
                        it.copy(
                            isOtpSending = false,
                            verificationId = result.verificationId,
                            maskedPhone = FirebaseAuthService.maskPhoneNumber(state.countryCode, state.phoneInput),
                            otpDigits = List(6) { "" },
                            otpCooldownSeconds = result.cooldownSeconds,
                            currentScreen = AppScreen.LOGIN_OTP,
                            authError = null
                        )
                    }
                    startCooldownTimer(result.cooldownSeconds)
                }
                is PhoneAuthSendResult.AutoVerified -> {
                    // Handled automatically if device resolves SMS via Play Services
                    handleInstantVerification(result.credential)
                }
                is PhoneAuthSendResult.Failed -> {
                    _uiState.update {
                        it.copy(isOtpSending = false, authError = result.message)
                    }
                }
            }
        }
    }

    fun onOtpDigitChanged(index: Int, digit: String) {
        val current = _uiState.value.otpDigits.toMutableList()
        if (index in 0..5) {
            current[index] = digit.filter { it.isDigit() }.take(1)
            _uiState.update { it.copy(otpDigits = current, authError = null) }

            // Auto verify once all 6 digits entered
            if (current.all { it.isNotEmpty() }) {
                verifyOtp()
            }
        }
    }

    fun pasteFullOtp(pastedString: String) {
        val digits = pastedString.filter { it.isDigit() }.take(6)
        if (digits.length == 6) {
            val newList = digits.map { it.toString() }
            _uiState.update { it.copy(otpDigits = newList, authError = null) }
            verifyOtp()
        }
    }

    /**
     * Resends a real SMS OTP via Firebase Phone Authentication with resend token.
     */
    fun resendOtp(activity: Activity) {
        val state = _uiState.value
        if (state.otpCooldownSeconds > 0) return

        _uiState.update { it.copy(isOtpSending = true, authError = null) }

        FirebaseAuthService.sendRealOtp(
            activity = activity,
            countryCode = state.countryCode,
            rawNumber = state.phoneInput,
            forceResendingToken = resendToken
        ) { result ->
            when (result) {
                is PhoneAuthSendResult.CodeSent -> {
                    resendToken = result.token
                    _uiState.update {
                        it.copy(
                            isOtpSending = false,
                            verificationId = result.verificationId,
                            otpDigits = List(6) { "" },
                            otpCooldownSeconds = result.cooldownSeconds,
                            authError = null
                        )
                    }
                    startCooldownTimer(result.cooldownSeconds)
                }
                is PhoneAuthSendResult.AutoVerified -> {
                    handleInstantVerification(result.credential)
                }
                is PhoneAuthSendResult.Failed -> {
                    _uiState.update { it.copy(isOtpSending = false, authError = result.message) }
                }
            }
        }
    }

    private fun startCooldownTimer(seconds: Int) {
        countdownTimer?.cancel()
        countdownTimer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val sec = (millisUntilFinished / 1000).toInt()
                _uiState.update { it.copy(otpCooldownSeconds = sec) }
            }

            override fun onFinish() {
                _uiState.update { it.copy(otpCooldownSeconds = 0) }
            }
        }.start()
    }

    /**
     * Verifies the 6-digit OTP through Firebase Auth, then sends the Firebase ID token
     * to the Render backend (POST /api/auth/verify-token) for server-side verification.
     */
    fun verifyOtp() {
        val state = _uiState.value
        val verificationId = state.verificationId
        if (verificationId.isNullOrEmpty()) {
            _uiState.update { it.copy(authError = "Session expired. Please request a new OTP.") }
            return
        }

        val enteredCode = state.otpDigits.joinToString("")
        if (enteredCode.length < 6) {
            _uiState.update { it.copy(authError = "Please enter all 6 digits of the SMS OTP.") }
            return
        }

        _uiState.update { it.copy(isVerifyingOtp = true, authError = null) }

        FirebaseAuthService.verifyRealOtp(verificationId, enteredCode) { verifyResult ->
            handleVerifyResult(verifyResult)
        }
    }

    private fun handleInstantVerification(credential: PhoneAuthCredential) {
        val auth = FirebaseAuthService.getAuthInstance() ?: return
        _uiState.update { it.copy(isVerifyingOtp = true, authError = null) }

        FirebaseAuthService.signInWithCredential(auth, credential) { verifyResult ->
            handleVerifyResult(verifyResult)
        }
    }

    private fun handleVerifyResult(verifyResult: PhoneAuthVerifyResult) {
        when (verifyResult) {
            is PhoneAuthVerifyResult.Success -> {
                // Cryptographically validated by Firebase. Now verify Firebase ID token with Render backend.
                _uiState.update { it.copy(isVerifyingOtp = false, isVerifyingBackend = true) }

                viewModelScope.launch(Dispatchers.IO) {
                    val backendResult = HeritageApiClient.verifyTokenWithBackend(verifyResult.idToken)
                    when (backendResult) {
                        is BackendAuthResult.Success -> {
                            countdownTimer?.cancel()
                            val verifiedPhone = if (!backendResult.phoneNumber.isNullOrEmpty()) {
                                backendResult.phoneNumber
                            } else {
                                verifyResult.phoneNumber
                            }
                            val sessionToken = backendResult.sessionToken ?: verifyResult.idToken

                            repository.saveSession(verifiedPhone, sessionToken)
                            _uiState.update {
                                it.copy(
                                    isVerifyingBackend = false,
                                    isLoggedIn = true,
                                    currentUserPhone = verifiedPhone,
                                    firebaseUid = verifyResult.uid,
                                    currentScreen = AppScreen.HOME,
                                    otpDigits = List(6) { "" },
                                    verificationId = null,
                                    authError = null
                                )
                            }
                        }
                        is BackendAuthResult.Error -> {
                            // Server-side verification failed on Render backend
                            FirebaseAuthService.signOut()
                            _uiState.update {
                                it.copy(
                                    isVerifyingBackend = false,
                                    authError = "Render Backend Verification Failed: ${backendResult.message}. Access Denied."
                                )
                            }
                        }
                    }
                }
            }
            is PhoneAuthVerifyResult.Failed -> {
                _uiState.update {
                    it.copy(
                        isVerifyingOtp = false,
                        isVerifyingBackend = false,
                        authError = verifyResult.message
                    )
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            FirebaseAuthService.signOut()
            repository.clearSession()
            _uiState.update {
                it.copy(
                    isLoggedIn = false,
                    currentUserPhone = null,
                    userProfile = null,
                    firebaseUid = null,
                    currentScreen = AppScreen.LOGIN_PHONE
                )
            }
        }
    }

    fun updateProfile(displayName: String, bio: String) {
        val phone = _uiState.value.currentUserPhone ?: return
        viewModelScope.launch {
            val updated = UserProfileEntity(
                phoneNumber = phone,
                displayName = displayName.trim().ifEmpty { "Heritage Explorer" },
                bio = bio.trim()
            )
            repository.updateUserProfile(updated)
            _uiState.update { it.copy(userProfile = updated) }
        }
    }

    // --- AI Chat & Voice Assistant ---
    fun sendUserChatMessage(message: String) {
        val cleanMsg = message.trim()
        if (cleanMsg.isEmpty()) return

        val userMessage = ChatMessage(text = cleanMsg, isUser = true)
        _uiState.update {
            it.copy(
                chatMessages = it.chatMessages + userMessage,
                isAiThinking = true
            )
        }

        voiceManager?.setProcessingState()

        viewModelScope.launch(Dispatchers.IO) {
            // First attempt backend /api/ai/chat if available
            val backendReply = HeritageApiClient.sendChatToBackend(cleanMsg)
            val replyText = if (!backendReply.isNullOrBlank()) {
                backendReply
            } else {
                // Fallback to Gemini REST / curated knowledge engine
                HeritageApiClient.sendChatToGeminiDirect(cleanMsg)
            }

            val aiMessage = ChatMessage(text = replyText, isUser = false)
            _uiState.update {
                it.copy(
                    chatMessages = it.chatMessages + aiMessage,
                    isAiThinking = false
                )
            }

            // Speak response via TTS
            voiceManager?.speak(replyText)
        }
    }

    fun startVoiceInput() {
        voiceManager?.startListening()
    }

    fun stopVoiceInput() {
        voiceManager?.stopListening()
    }

    fun playAudioGuide(text: String) {
        voiceManager?.speak(text)
    }

    fun stopAudioGuide() {
        voiceManager?.stopSpeaking()
    }

    fun askAiAboutMonument(monument: HeritageItem) {
        val prompt = "Tell me about ${monument.name} (${monument.hindiName}) in ${monument.state}. What is its architectural and cultural significance?"
        navigateTo(AppScreen.AI_ASSISTANT)
        sendUserChatMessage(prompt)
    }

    fun checkBackendHealth() {
        _uiState.update { it.copy(isCheckingHealth = true) }
        viewModelScope.launch {
            val status = HeritageApiClient.checkHealth()
            _uiState.update {
                it.copy(
                    backendHealth = status,
                    isCheckingHealth = false
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownTimer?.cancel()
        voiceManager?.destroy()
    }
}

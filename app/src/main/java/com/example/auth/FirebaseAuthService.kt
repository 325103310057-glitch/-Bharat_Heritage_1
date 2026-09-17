package com.example.auth

import android.app.Activity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

sealed class PhoneAuthSendResult {
    data class CodeSent(
        val verificationId: String,
        val token: PhoneAuthProvider.ForceResendingToken?,
        val cooldownSeconds: Int = 60
    ) : PhoneAuthSendResult()

    data class AutoVerified(
        val credential: PhoneAuthCredential
    ) : PhoneAuthSendResult()

    data class Failed(
        val message: String,
        val exception: Throwable? = null
    ) : PhoneAuthSendResult()
}

sealed class PhoneAuthVerifyResult {
    data class Success(
        val uid: String,
        val phoneNumber: String,
        val idToken: String
    ) : PhoneAuthVerifyResult()

    data class Failed(
        val message: String,
        val isExpired: Boolean = false,
        val isInvalidCode: Boolean = false
    ) : PhoneAuthVerifyResult()
}

object FirebaseAuthService {

    /**
     * Checks if Firebase is initialized on this device/app instance.
     */
    fun getAuthInstance(): FirebaseAuth? {
        return try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Formats country code and phone number to strict E.164 format (+919876543210)
     */
    fun formatToE164(countryCode: String, rawNumber: String): String {
        val cleanCode = if (countryCode.startsWith("+")) countryCode else "+$countryCode"
        val codeDigits = cleanCode.filter { it.isDigit() }
        val numberDigits = rawNumber.filter { it.isDigit() }
        return "+$codeDigits$numberDigits"
    }

    /**
     * Validates that the phone number contains minimum valid digits
     */
    fun isValidPhoneNumber(countryCode: String, rawNumber: String): Boolean {
        val digits = rawNumber.filter { it.isDigit() }
        return digits.length in 10..15 && countryCode.isNotBlank()
    }

    /**
     * Conceals mobile number for secure UI display (e.g. +91 ••••••3210)
     */
    fun maskPhoneNumber(countryCode: String, rawNumber: String): String {
        val digits = rawNumber.filter { it.isDigit() }
        return if (digits.length >= 4) {
            val last4 = digits.takeLast(4)
            val masked = "•".repeat((digits.length - 4).coerceAtLeast(2))
            "$countryCode $masked$last4"
        } else {
            "$countryCode ••••"
        }
    }

    /**
     * Requests a REAL SMS OTP through Firebase Phone Authentication.
     * Firebase triggers carrier SMS dispatch to user's real physical mobile device.
     */
    fun sendRealOtp(
        activity: Activity,
        countryCode: String,
        rawNumber: String,
        forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null,
        onResult: (PhoneAuthSendResult) -> Unit
    ) {
        val auth = getAuthInstance()
        if (auth == null) {
            onResult(
                PhoneAuthSendResult.Failed(
                    "Firebase Authentication is not configured. Please ensure google-services.json is attached and Phone Provider is enabled in Firebase Console."
                )
            )
            return
        }

        if (!isValidPhoneNumber(countryCode, rawNumber)) {
            onResult(PhoneAuthSendResult.Failed("Please enter a valid 10-digit mobile number."))
            return
        }

        val e164Phone = formatToE164(countryCode, rawNumber)

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Instant device verification via Google Play Services SMS Retriever
                onResult(PhoneAuthSendResult.AutoVerified(credential))
            }

            override fun onVerificationFailed(e: FirebaseException) {
                val userFriendlyMessage = mapFirebaseErrorToMessage(e)
                onResult(PhoneAuthSendResult.Failed(userFriendlyMessage, e))
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // Real SMS has been dispatched by Firebase infrastructure
                onResult(
                    PhoneAuthSendResult.CodeSent(
                        verificationId = verificationId,
                        token = token,
                        cooldownSeconds = 60
                    )
                )
            }
        }

        try {
            val builder = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(e164Phone)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(activity)
                .setCallbacks(callbacks)

            if (forceResendingToken != null) {
                builder.setForceResendingToken(forceResendingToken)
            }

            PhoneAuthProvider.verifyPhoneNumber(builder.build())
        } catch (e: Exception) {
            onResult(PhoneAuthSendResult.Failed("Failed to initialize SMS dispatch: ${e.localizedMessage}", e))
        }
    }

    /**
     * Verifies the 6-digit OTP entered by the user against Firebase Authentication.
     * Upon success, extracts the genuine Firebase ID token for Render backend verification.
     */
    fun verifyRealOtp(
        verificationId: String,
        otpCode: String,
        onResult: (PhoneAuthVerifyResult) -> Unit
    ) {
        val auth = getAuthInstance()
        if (auth == null) {
            onResult(PhoneAuthVerifyResult.Failed("Firebase Authentication is not configured."))
            return
        }

        val cleanOtp = otpCode.filter { it.isDigit() }
        if (cleanOtp.length != 6) {
            onResult(
                PhoneAuthVerifyResult.Failed(
                    message = "Please enter all 6 digits of the SMS OTP.",
                    isInvalidCode = true
                )
            )
            return
        }

        try {
            val credential = PhoneAuthProvider.getCredential(verificationId, cleanOtp)
            signInWithCredential(auth, credential, onResult)
        } catch (e: Exception) {
            val msg = mapFirebaseErrorToMessage(e)
            onResult(PhoneAuthVerifyResult.Failed(msg, isInvalidCode = true))
        }
    }

    /**
     * Signs in with PhoneAuthCredential and fetches fresh Firebase ID Token.
     */
    fun signInWithCredential(
        auth: FirebaseAuth,
        credential: PhoneAuthCredential,
        onResult: (PhoneAuthVerifyResult) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        // Force-refresh ID token to guarantee real, unexpired cryptographically signed token
                        user.getIdToken(true)
                            .addOnCompleteListener { tokenTask ->
                                if (tokenTask.isSuccessful) {
                                    val idToken = tokenTask.result?.token ?: ""
                                    onResult(
                                        PhoneAuthVerifyResult.Success(
                                            uid = user.uid,
                                            phoneNumber = user.phoneNumber ?: "",
                                            idToken = idToken
                                        )
                                    )
                                } else {
                                    onResult(
                                        PhoneAuthVerifyResult.Failed(
                                            "Failed to obtain Firebase ID token: ${tokenTask.exception?.localizedMessage}"
                                        )
                                    )
                                }
                            }
                    } else {
                        onResult(PhoneAuthVerifyResult.Failed("Firebase user is null after verification."))
                    }
                } else {
                    val ex = task.exception
                    val msg = mapFirebaseErrorToMessage(ex)
                    val isInvalid = ex is FirebaseAuthInvalidCredentialsException
                    val isExpired = ex is FirebaseAuthInvalidUserException
                    onResult(
                        PhoneAuthVerifyResult.Failed(
                            message = msg,
                            isInvalidCode = isInvalid,
                            isExpired = isExpired
                        )
                    )
                }
            }
    }

    /**
     * Signs out from Firebase Authentication.
     */
    fun signOut() {
        try {
            getAuthInstance()?.signOut()
        } catch (_: Exception) {}
    }

    /**
     * Maps Firebase error codes to production-grade user friendly messages.
     */
    fun mapFirebaseErrorToMessage(throwable: Throwable?): String {
        if (throwable == null) return "An unexpected authentication error occurred. Please try again."

        val className = throwable::class.java.simpleName
        return when {
            throwable is FirebaseAuthInvalidCredentialsException || className == "FirebaseAuthInvalidCredentialsException" -> {
                "Invalid OTP code or phone number. Please check and try again."
            }
            throwable is FirebaseTooManyRequestsException || className == "FirebaseTooManyRequestsException" -> {
                "Too many attempts from this device or number. Please wait a few minutes before trying again."
            }
            throwable is FirebaseAuthInvalidUserException || className == "FirebaseAuthInvalidUserException" -> {
                "OTP verification session has expired. Please request a new OTP."
            }
            else -> {
                val msg = throwable.localizedMessage ?: ""
                when {
                    msg.contains("quota", ignoreCase = true) ->
                        "SMS quota exceeded. Please check Firebase Console SMS limits or try again later."
                    msg.contains("network", ignoreCase = true) ->
                        "Network error. Please check your internet connection and try again."
                    msg.contains("app not authorized", ignoreCase = true) ->
                        "App verification failed. Ensure SHA-256 fingerprint is added in Firebase Console."
                    msg.contains("reCAPTCHA", ignoreCase = true) ->
                        "reCAPTCHA verification failed. Please try again."
                    msg.contains("invalid phone", ignoreCase = true) ->
                        "Invalid phone number format. Please check the country code and number."
                    else -> "Authentication failed: ${throwable.message?.take(120) ?: "Please try again."}"
                }
            }
        }
    }
}

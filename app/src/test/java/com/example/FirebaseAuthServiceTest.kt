package com.example

import com.example.auth.FirebaseAuthService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FirebaseAuthServiceTest {

    @Test
    fun `formatToE164 formats international phone numbers properly`() {
        val formatted1 = FirebaseAuthService.formatToE164("+91", "9876543210")
        assertEquals("+919876543210", formatted1)

        val formatted2 = FirebaseAuthService.formatToE164("91", "98765 43210")
        assertEquals("+919876543210", formatted2)

        val formattedUS = FirebaseAuthService.formatToE164("+1", "(415) 555-2671")
        assertEquals("+14155552671", formattedUS)
    }

    @Test
    fun `isValidPhoneNumber checks length and country code`() {
        assertFalse(FirebaseAuthService.isValidPhoneNumber("+91", "123"))
        assertFalse(FirebaseAuthService.isValidPhoneNumber("", "9876543210"))
        assertTrue(FirebaseAuthService.isValidPhoneNumber("+91", "9876543210"))
        assertTrue(FirebaseAuthService.isValidPhoneNumber("+1", "4155552671"))
    }

    @Test
    fun `maskPhoneNumber conceals digits for user privacy`() {
        val masked = FirebaseAuthService.maskPhoneNumber("+91", "9876543210")
        assertEquals("+91 ••••••3210", masked)
    }

    @Test
    fun `mapFirebaseErrorToMessage returns user friendly error messages for standard failure modes`() {
        val quotaEx = Exception("SMS quota exceeded for project")
        val quotaMsg = FirebaseAuthService.mapFirebaseErrorToMessage(quotaEx)
        assertTrue(quotaMsg.contains("quota exceeded", ignoreCase = true))

        val networkEx = Exception("Network connection lost during verification")
        val networkMsg = FirebaseAuthService.mapFirebaseErrorToMessage(networkEx)
        assertTrue(networkMsg.contains("Network error", ignoreCase = true))

        val nullMsg = FirebaseAuthService.mapFirebaseErrorToMessage(null)
        assertTrue(nullMsg.contains("unexpected", ignoreCase = true))
    }
}

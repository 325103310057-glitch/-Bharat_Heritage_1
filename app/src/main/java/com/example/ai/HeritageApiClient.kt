package com.example.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class HealthStatus(
    val isReachable: Boolean,
    val statusMessage: String,
    val responseTimeMs: Long,
    val endpointUrl: String
)

sealed class BackendAuthResult {
    data class Success(
        val uid: String,
        val phoneNumber: String?,
        val sessionToken: String?
    ) : BackendAuthResult()

    data class Error(
        val message: String,
        val statusCode: Int = 0
    ) : BackendAuthResult()
}

object HeritageApiClient {

    // Production HTTPS Backend URL default - never localhost in Android
    var backendBaseUrl: String = BuildConfig.BACKEND_API_BASE_URL.ifEmpty {
        "https://bharat-heritage-api.onrender.com"
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Verifies GET /api/health against the deployed server
     */
    suspend fun checkHealth(): HealthStatus = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        val cleanUrl = backendBaseUrl.trimEnd('/')
        val healthUrl = "$cleanUrl/api/health"

        try {
            val request = Request.Builder()
                .url(healthUrl)
                .get()
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val duration = System.currentTimeMillis() - start
                if (response.isSuccessful) {
                    val body = response.body?.string() ?: ""
                    HealthStatus(
                        isReachable = true,
                        statusMessage = "Backend Online (HTTP ${response.code})",
                        responseTimeMs = duration,
                        endpointUrl = healthUrl
                    )
                } else {
                    HealthStatus(
                        isReachable = false,
                        statusMessage = "HTTP ${response.code}: ${response.message}",
                        responseTimeMs = duration,
                        endpointUrl = healthUrl
                    )
                }
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - start
            HealthStatus(
                isReachable = false,
                statusMessage = e.localizedMessage ?: "Server unreachable",
                responseTimeMs = duration,
                endpointUrl = healthUrl
            )
        }
    }

    /**
     * Verifies the Firebase ID token with the Render Backend (POST /api/auth/verify-token).
     * The Render backend securely executes Firebase Admin SDK: admin.auth().verifyIdToken(idToken).
     */
    suspend fun verifyTokenWithBackend(idToken: String): BackendAuthResult = withContext(Dispatchers.IO) {
        val cleanUrl = backendBaseUrl.trimEnd('/')
        val authUrl = "$cleanUrl/api/auth/verify-token"

        try {
            val jsonBody = JSONObject().apply {
                put("idToken", idToken)
            }
            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(authUrl)
                .header("Authorization", "Bearer $idToken")
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val json = if (bodyStr.isNotEmpty()) JSONObject(bodyStr) else JSONObject()
                    val uid = json.optString("uid", "")
                    val phone = json.optString("phoneNumber", json.optString("phone", ""))
                    val sessionToken = json.optString("sessionToken", if (idToken.length > 32) idToken.take(32) else idToken)
                    BackendAuthResult.Success(uid = uid, phoneNumber = phone, sessionToken = sessionToken)
                } else {
                    val errMsg = try {
                        val json = JSONObject(bodyStr)
                        json.optString("error", json.optString("message", "Authentication rejected by Render backend (HTTP ${response.code})"))
                    } catch (_: Exception) {
                        "Authentication rejected by Render backend (HTTP ${response.code})"
                    }
                    BackendAuthResult.Error(message = errMsg, statusCode = response.code)
                }
            }
        } catch (e: Exception) {
            BackendAuthResult.Error(message = e.localizedMessage ?: "Failed to connect to Render backend authentication endpoint.")
        }
    }

    /**
     * Calls POST /api/ai/chat on the deployed backend
     */
    suspend fun sendChatToBackend(message: String): String? = withContext(Dispatchers.IO) {
        val cleanUrl = backendBaseUrl.trimEnd('/')
        val chatUrl = "$cleanUrl/api/ai/chat"

        try {
            val jsonBody = JSONObject().apply {
                put("message", message)
                put("context", "Bharat Heritage Android App")
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(chatUrl)
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val responseStr = response.body?.string() ?: return@withContext null
                    val json = JSONObject(responseStr)
                    // Support standard { reply: "..." } or { text: "..." }
                    return@withContext json.optString("reply", json.optString("response", json.optString("text", "")))
                }
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Direct Gemini API fallback using gemini-3.5-flash
     */
    suspend fun sendChatToGeminiDirect(message: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext generateCuratedHeritageResponse(message)
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
            val systemPrompt = "You are the Bharat Heritage AI Guide, an expert scholar on Indian history, monuments, temples, caves, philosophy, culture, and arts. Answer gracefully with accurate historical facts, cultural nuance, and engaging explanations. Keep answers clear, authentic, and inspiring."

            val json = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", message))
                        })
                    })
                })
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemPrompt))
                    })
                })
            }

            val body = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()

            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val resStr = response.body?.string() ?: ""
                    val resJson = JSONObject(resStr)
                    val candidates = resJson.optJSONArray("candidates")
                    val firstCandidate = candidates?.optJSONObject(0)
                    val content = firstCandidate?.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    val text = parts?.optJSONObject(0)?.optString("text")
                    if (!text.isNullOrEmpty()) {
                        return@withContext text
                    }
                }
            }
            generateCuratedHeritageResponse(message)
        } catch (e: Exception) {
            generateCuratedHeritageResponse(message)
        }
    }

    /**
     * Comprehensive Curated Indian Heritage Knowledge Engine for offline or fallback operation
     */
    fun generateCuratedHeritageResponse(prompt: String): String {
        val p = prompt.lowercase()
        return when {
            p.contains("konark") || p.contains("sun temple") ->
                "The Konark Sun Temple in Odisha was constructed in 1250 CE by King Narasimhadeva I of the Eastern Ganga Dynasty. Designed as a colossal 24-wheeled chariot of Surya pulled by seven horses, its carved stone wheels serve as precision sundials capable of measuring solar time down to minutes. It is a UNESCO World Heritage treasure of Kalinga architecture."

            p.contains("hampi") || p.contains("vijayanagara") ->
                "Hampi was the majestic 14th-century capital of the Vijayanagara Empire along the Tungabhadra River. Celebrated for the iconic Stone Chariot, the musical pillars of Vittala Temple, and the unbroken 700-year living worship at Virupaksha Temple, it was one of the grandest trading capitals in the medieval world."

            p.contains("brihadisvara") || p.contains("thanjavur") || p.contains("chola") ->
                "Brihadisvara Temple (Peruvudaiyar Kovil) was consecrated in 1010 CE by Rajaraja Chola I in Thanjavur, Tamil Nadu. Its 216-foot granite Vimana is crowned by an 80-tonne monolithic stone capstone (Kumbam) assembled without binding mortar, standing as the crowning triumph of Chola Dravidian architecture."

            p.contains("ajanta") || p.contains("ellora") || p.contains("caves") ->
                "The Ajanta and Ellora Caves in Maharashtra are ancient rock-cut marvels. Ajanta preserves exquisite 2,000-year-old Buddhist Jataka mural paintings. Ellora features 34 rock sanctuaries uniting Buddhist, Hindu, and Jain traditions, crowned by the Kailasa Temple (Cave 16)—the world's largest monolithic structure carved top-down from a single cliff."

            p.contains("amber") || p.contains("jaipur") || p.contains("sheesh mahal") ->
                "Amber Fort in Jaipur, Rajasthan, was established in 1592 CE by Raja Man Singh I. Overlooking Maota Lake, it is famous for the Sheesh Mahal (Palace of Mirrors) where a single candle flame illuminates the entire hall through thousands of concave mirrors, reflecting the royal splendor of the Kachwaha Rajputs."

            p.contains("varanasi") || p.contains("kashi") || p.contains("ghat") || p.contains("ganga") ->
                "Varanasi (Kashi) is one of the world's oldest continuously inhabited sacred cities, with over 3,000 years of living spiritual heritage. Its 84 stone riverfront ghats along the crescent curve of Mother Ganga host the evening Maha Aarti at Dashashwamedh Ghat and embody India's sacred philosophy of Moksha, music, and scholarship."

            p.contains("nalanda") || p.contains("university") ->
                "Ancient Nalanda Mahavihara in Bihar was founded in 427 CE under the Gupta Empire. As the world's premier ancient residential university, it accommodated 10,000 students and 2,000 scholars from across Asia, housing the legendary Dharmaganja library with millions of manuscripts."

            p.contains("bharatanatyam") || p.contains("dance") || p.contains("natya") ->
                "Bharatanatyam is India's ancient classical dance from Tamil Nadu, codified in Sage Bharata's 'Natyashastra'. It harmoniously unifies Bhava (expression), Raga (melody), and Tala (rhythm), bringing sacred temple storytelling and sculptural geometry to life."

            p.contains("qutub") || p.contains("iron pillar") ->
                "The Qutub Minar complex in Delhi features the 72.5-meter red sandstone minaret begun in 1199 CE, alongside the remarkable 4th-century Gupta Iron Pillar whose ancient metallurgical composition has prevented rust for over 1,600 years."

            p.contains("architecture") || p.contains("temple style") ->
                "Classical Indian temple architecture evolved into three primary traditions: Nagara in Northern India (curvilinear Shikhara spires), Dravidian in Southern India (pyramidal stepped Vimanas and towering Gopuram gateways), and Vesara in the Deccan (a synthesis seen in Chalukya and Hoysala monuments)."

            else ->
                "Bharat Heritage represents five millennia of unbroken civilization, profound architectural wisdom, sacred philosophy, and artistic traditions. From the precision sundials of Konark and the monolithic wonder of Kailasa Temple to the living cultural traditions of Varanasi and classical arts, India's heritage celebrates the timeless harmony of sacred geometry, devotion, and culture."
        }
    }
}

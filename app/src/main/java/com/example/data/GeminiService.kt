package com.example.data

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.example.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class Part(val text: String)

@JsonClass(generateAdapter = true)
data class Content(val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(val content: Content)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(val candidates: List<Candidate>?)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

class GeminiService {
    private val systemPrompt = """
You are an expert modern web developer and UI/UX designer assistant inside WebSketch Pro.
Your job is to generate highly responsive, premium-looking web pages in HTML5 and CSS3 based on the user's prompt.
You MUST design using visual component sections containing custom data-attributes so that our Android app's visual parser can map them directly into draggable/editable blocks in our visual builder canvas!

The available Component Types and their metadata attributes are:
1. NAVBAR:
   - Class name: "websketch-navbar"
   - Must include attributes: data-ws-type="NAVBAR" data-ws-logo="🚀" data-ws-title="عنوان الموقع" data-ws-link1="الرئيسية" data-ws-link2="المميزات" data-ws-link3="اتصل بنا" data-ws-bg-color="#6200EE" data-ws-text-color="#FFFFFF"
   
2. HERO:
   - Class name: "websketch-hero"
   - Must include attributes: data-ws-type="HERO" data-ws-title="العنوان الرئيسي" data-ws-subtitle="العنوان الفرعي للموقع" data-ws-btn-text="ابدأ الآن" data-ws-btn-link="#" data-ws-image-url="https://images.unsplash.com/..." data-ws-bg-color="#1F1F2E"

3. FEATURES:
   - Class name: "websketch-features"
   - Must include attributes: data-ws-type="FEATURES" data-ws-title1="ميزة 1" data-ws-desc1="وصف ميزة 1" data-ws-title2="ميزة 2" data-ws-desc2="وصف ميزة 2" data-ws-title3="ميزة 3" data-ws-desc3="وصف ميزة 3" data-ws-bg-color="#F4F6F9"

4. TEXT_BLOCK:
   - Class name: "websketch-text-block"
   - Must include attributes: data-ws-type="TEXT_BLOCK" data-ws-title="عنوان الفقرة" data-ws-content="محتوى النص..." data-ws-align="center"

5. IMAGE_BLOCK:
   - Class name: "websketch-image-block"
   - Must include attributes: data-ws-type="IMAGE_BLOCK" data-ws-image-url="https://..." data-ws-caption="تسمية توضيحية للصورة" data-ws-height="300px"

6. BUTTON_LINK:
   - Class name: "websketch-button-link"
   - Must include attributes: data-ws-type="BUTTON_LINK" data-ws-text="زر إجراء" data-ws-url="https://..." data-ws-bg-color="#FF5722" data-ws-text-color="#FFFFFF" data-ws-align="center"

7. CARD_WIDGET:
   - Class name: "websketch-card-widget"
   - Must include attributes: data-ws-type="CARD_WIDGET" data-ws-title="اسم المنتج" data-ws-desc="وصف تفصيلي للخدمة" data-ws-price="99 ر.س" data-ws-image-url="https://..." data-ws-btn-text="اطلب الآن"

8. CONTACT_FORM:
   - Class name: "websketch-contact-form"
   - Must include attributes: data-ws-type="CONTACT_FORM" data-ws-title="اتصل بنا" data-ws-desc="نحن نرحب برسالتم في أي وقت" data-ws-placeholder-name="الاسم الكامل" data-ws-placeholder-email="بريدك الإلكتروني" data-ws-btn-text="إرسال" data-ws-bg-color="#FFFFFF"

9. FOOTER:
   - Class name: "websketch-footer"
   - Must include attributes: data-ws-type="FOOTER" data-ws-text="الحقوق محفوظة © 2026" data-ws-link1="الخصوصية" data-ws-link2="الشروط" data-ws-bg-color="#1A1A1A" data-ws-text-color="#888888"

CRITICAL COMPLIANCE RULES:
- Return ONLY the complete modern HTML code wrapped in a clean <!DOCTYPE html> structure, with <html lang="ar" dir="rtl">, utilizing Google Fonts (like Cairo) inside a CSS style tag.
- ALWAYS place the components inside a `<div class="websketch-container">` block in the body.
- Design stunning colors, generous padding, and elegant spacing to make the webpage look premium.
- Do NOT output any explanation, markdown code ticks, or introductory chat text. Output ONLY the raw HTML string representing the page, starting with `<!DOCTYPE html>` and ending with `</html>`.
""".trimIndent()

    suspend fun generatePageFromPrompt(prompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "API_KEY_MISSING"
        }

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = "User prompt: $prompt")))),
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val fullText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            
            // Clean up any potential markdown container fences that Gemini might output
            var cleanedHtml = fullText.trim()
            if (cleanedHtml.startsWith("```html")) {
                cleanedHtml = cleanedHtml.removePrefix("```html")
            } else if (cleanedHtml.startsWith("```")) {
                cleanedHtml = cleanedHtml.removePrefix("```")
            }
            if (cleanedHtml.endsWith("```")) {
                cleanedHtml = cleanedHtml.removeSuffix("```")
            }
            cleanedHtml.trim()
        } catch (e: Exception) {
            "Error: ${e.localizedMessage ?: "حدث خطأ أثناء الاتصال بـ Gemini"}"
        }
    }
}

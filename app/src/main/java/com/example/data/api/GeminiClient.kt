package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateTravelPlan(
        city: String,
        budget: String,
        peopleCount: String,
        duration: String,
        interests: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not set, generating high-quality offline travel plan.")
            return@withContext getMockPlan(city, budget, peopleCount, duration, interests)
        }

        val prompt = """
            أنت خبير سياحي محلي ذكي جداً لمنصة "تِجربة - Tajribah".
            خطط لرحلة سياحية كاملة ومثالية للمعلومات التالية:
            - المدينة: $city
            - الميزانية المحددة: $budget
            - عدد الأشخاص: $peopleCount
            - المدة: $duration أيام
            - الاهتمامات والأنشطة المفضلة: $interests

            المخرجات المطلوبة باللغة العربية الفصحى وبتنسيق جذاب ومنسق مع فواصل متباعدة:
            1. الفندق المقترح (فئة تتناسب مع الميزانية).
            2. السيارة الموصى بها للتأجير.
            3. التجارب المحلية المقترحة (مثل: يوم مع نحال، صناعة الفخار، صعود الجبل، إلخ).
            4. مطاعم شعبية وحديثة مقترحة.
            5. مرشد محلي مقترح.
            6. خطة يومية مفصلة لكل يوم بالتفصيل والأنشطة والمسارات ومواقع تقريبية على الخرائط.
            7. التكلفة الإجمالية المقدرة بشكل تفصيلي بالريال السعودي.

            اجعل الأسلوب فخماً، ملهماً، واحترافياً جداً، يليق بمنصة سياحية عالمية.
        """.trimIndent()

        // Construct standard Gemini generateContent request JSON
        val requestJson = """
            {
              "contents": [
                {
                  "parts": [
                    {
                      "text": ${escapeJsonString(prompt)}
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val requestBody = requestJson.toRequestBody("application/json; charset=utf-8".toMediaType())
        val url = "$BASE_URL?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Gemini API error response: $errBody")
                    return@withContext getMockPlan(city, budget, peopleCount, duration, interests) + "\n\n(ملاحظة: تم استخدام المخطط المحلي لعدم تمكن الاتصال بالخادم)"
                }

                val responseBodyStr = response.body?.string()
                if (responseBodyStr.isNullOrEmpty()) {
                    return@withContext getMockPlan(city, budget, peopleCount, duration, interests)
                }

                val responseText = parseGeminiResponse(responseBodyStr)
                if (responseText.isNullOrEmpty()) {
                    return@withContext getMockPlan(city, budget, peopleCount, duration, interests)
                }

                responseText
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception calling Gemini API", e)
            getMockPlan(city, budget, peopleCount, duration, interests) + "\n\n(ملاحظة: تم توليد هذه الخطة محلياً نظراً لعدم توفر الاتصال بالإنترنت)"
        }
    }

    private fun escapeJsonString(string: String): String {
        val escaped = string
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }

    private fun parseGeminiResponse(jsonString: String): String? {
        return try {
            val jsonMap = moshi.adapter(Map::class.java).fromJson(jsonString) as? Map<*, *>
            val candidates = jsonMap?.get("candidates") as? List<*>
            val firstCandidate = candidates?.getOrNull(0) as? Map<*, *>
            val content = firstCandidate?.get("content") as? Map<*, *>
            val parts = content?.get("parts") as? List<*>
            val firstPart = parts?.getOrNull(0) as? Map<*, *>
            firstPart?.get("text") as? String
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse Gemini response JSON", e)
            null
        }
    }

    // High quality offline travel plan as robust fallback
    fun getMockPlan(
        city: String,
        budget: String,
        peopleCount: String,
        duration: String,
        interests: String
    ): String {
        return """
            ✨ خطة الرحلة المقترحة لمدينة: $city
            👤 لعدد: $peopleCount أشخاص | ⏳ المدة: $duration أيام | 💰 الميزانية المقدرة: $budget
            🎯 الاهتمامات المحددة: $interests
            -----------------------------------------
            
            🏨 1. السكن والإقامة المقترحة:
            • منتجع السحاب التراثي ($city) - يتميز بإطلالة جبلية ساحرة وجلسات مستوحاة من البيئة المحلية العريقة.
            
            🚗 2. السيارة الموصى بها للتأجير:
            • سيارة عائلية دفع رباعي حديثة لتسهيل استكشاف الطبيعة والمسارات الجبلية والوصول لجميع التجارب بيسر وسهولة.
            
            🏺 3. التجارب المحلية المقترحة (Tajribah):
            • تجربة صناعة الفخار مع الحرفي العم صالح (صناعة يدوية أصيلة).
            • يوم كامل مع النحال المحلي أبو فهد لمعرفة أسرار عسل السدر الطبيعي وتذوقه طازجاً.
            • رحلة صيد بحرية مميزة مع الصياد المحلي الكابتن يوسف.
            
            🍽️ 4. المطاعم المقترحة:
            • مطعم "بيت الكرم الشعبى" لتناول أشهى الوجبات التقليدية المطهوة بأيدٍ محلية.
            • مقهى "شذى البن" للاستمتاع بالقهوة السعودية الأصيلة المستخلصة من مزارع البن المحلية.
            
            🗺️ 5. المرشد المحلي المقترح:
            • المرشد السياحي الأستاذ "أحمد العسيري" - مرخص وموثق ولديه معرفة عميقة بتاريخ وثقافة المنطقة.
            
            🗓️ 6. خطة الرحلة اليومية المفصلة:
            
            📅 اليوم الأول: الاستكشاف والتراث الحرفي
            - 09:00 صباحاً: الاستقبال في المطار واستلام السيارة وبدء التوجه للفندق للاستراحة وتناول القهوة.
            - 11:30 صباحاً: زيارة سوق البلد التاريخي والتعرف على الهندسة المعمارية المحلية.
            - 01:30 مساءً: تناول الغداء في مطعم "بيت الكرم".
            - 04:00 مساءً: بدء [تجربة صناعة الفخار] مع العم صالح، عيش لحظة التشكيل باليد واستلام هدية تذكارية.
            - 08:00 مساءً: جلسة مسائية في مقهى "شذى البن".
            
            📅 اليوم الثاني: عسل السدر والطبيعة البكر
            - 08:30 صباحاً: تناول الإفطار الريفي في الفندق.
            - 10:00 صباحاً: الانطلاق إلى مزرعة النحل للعم أبو فهد، ارتداء لباس النحال الواقي والمشاركة في استخراج العسل.
            - 02:00 مساءً: تناول وجبة غداء برية مشوية في المزرعة.
            - 04:30 مساءً: جولة حرة للمشي في المسارات الجبلية الخضراء والتقاط صور بانورامية ساحرة.
            - 07:30 مساءً: العودة للمنتجع للاسترخاء والتمتع بالهدوء والنجوم.
            
            📅 اليوم الثالث: رحلة البحر والأماكن الأكثر زيارة
            - 09:00 صباحاً: ركوب الخيل على الشاطئ أو بجانب الوديان الخضراء.
            - 11:00 صباحاً: زيارة متحف المدينة التراثي برفقة المرشد "أحمد العسيري".
            - 01:30 مساءً: التوجه للمرسى والبدء في تجربة الصيد مع الكابتن يوسف وتناول صيد اليوم طازجاً.
            - 06:00 مساءً: جولة لشراء هدايا تذكارية محلية من صنع الحرفيين.
            - 08:30 مساءً: العشاء وتوديع المعالم الجميلة والاستعداد للمغادرة.

            💸 7. التكلفة الإجمالية المقدرة:
            • الإقامة بالفندق (3 أيام): 1200 ريال سعودي.
            • تأجير السيارة مع الوقود: 450 ريال سعودي.
            • تذاكر التجارب المحلية والمرشد: 800 ريال سعودي.
            • الوجبات والهدايا التذكارية: 550 ريال سعودي.
            • التكلفة الإجمالية التقريبية: 3000 ريال سعودي فقط.
            
            ✨ نتمنى لكم تجربة سفر ذكية وممتعة لا تُنسى مع تِجربة – Tajribah!
        """.trimIndent()
    }
}

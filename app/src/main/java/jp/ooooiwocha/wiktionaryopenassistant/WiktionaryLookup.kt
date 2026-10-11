package jp.ooooiwocha.wiktionaryopenassistant

import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object WiktionaryLookup {
    private val client = OkHttpClient.Builder()
        .connectTimeout(3, TimeUnit.SECONDS)
        .callTimeout(5, TimeUnit.SECONDS)
        .build()

    fun articleUrl(code: String, term: String): Uri = Uri.Builder()
        .scheme("https").authority("$code.wiktionary.org")
        .appendPath("wiki").appendPath(term.trim().replace(' ', '_')).build()

    fun searchUrl(term: String): Uri = Uri.Builder()
        .scheme("https").authority("en.wiktionary.org")
        .appendPath("w").appendPath("index.php")
        .appendQueryParameter("search", term.trim()).build()

    /** Search small batches in parallel, but preserve priority across the whole ordered list. */
    suspend fun firstExisting(
        term: String,
        codes: List<String>,
        onProgress: (completed: Int, total: Int) -> Unit
    ): String? {
        var completed = 0
        for (batch in codes.chunked(3)) {
            val found = coroutineScope {
                batch.map { code -> async(Dispatchers.IO) { exists(code, term) } }.awaitAll()
            }
            completed += batch.size
            onProgress(completed, codes.size)
            val index = found.indexOfFirst { it }
            if (index >= 0) return batch[index]
        }
        return null
    }

    private fun exists(code: String, term: String): Boolean {
        return try {
            val url = HttpUrl.Builder()
                .scheme("https")
                .host("$code.wiktionary.org")
                .addPathSegments("w/api.php")
                .addQueryParameter("action", "query")
                .addQueryParameter("titles", term)
                .addQueryParameter("format", "json")
                .addQueryParameter("formatversion", "2")
                .build()
            client.newCall(Request.Builder().url(url).header("User-Agent", "WiktionaryOpenAssistant/2.0 (Android dictionary lookup)").build()).execute().use { response ->
                if (!response.isSuccessful) return false
                val body = response.body?.string() ?: return false
                val page = JSONObject(body).optJSONObject("query")
                    ?.optJSONArray("pages")?.optJSONObject(0)
                page != null && !page.has("missing") && page.optLong("pageid", -1) > 0
            }
        } catch (_: Exception) {
            false
        }
    }
}

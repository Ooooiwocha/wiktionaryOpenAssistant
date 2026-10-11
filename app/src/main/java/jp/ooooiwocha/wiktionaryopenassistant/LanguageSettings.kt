package jp.ooooiwocha.wiktionaryopenassistant

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

data class LanguageSetting(val code: String, val enabled: Boolean = true)

/**
 * Single source of truth for the user-defined lookup order.
 * Migrates the original app's numeric SharedPreferences keys exactly once.
 */
object LanguageSettings {
    private const val PREFS = "lang_pref"
    private const val LANGUAGES_KEY = "languages_v2"
    private const val ASK_KEY = "ask_before_opening"
    val defaults = listOf("en", "fr", "de", "es", "ru", "ar", "ja", "zh")
        .map { LanguageSetting(it) }

    private val knownNames = mapOf(
        "en" to "英語", "fr" to "フランス語", "de" to "ドイツ語", "es" to "スペイン語",
        "ru" to "ロシア語", "ar" to "アラビア語", "ja" to "日本語", "zh" to "中国語",
        "it" to "イタリア語", "pt" to "ポルトガル語", "ko" to "韓国語", "nl" to "オランダ語",
        "pl" to "ポーランド語", "la" to "ラテン語", "el" to "ギリシャ語",
        "sv" to "スウェーデン語", "hi" to "ヒンディー語", "tr" to "トルコ語",
        "uk" to "ウクライナ語", "vi" to "ベトナム語", "id" to "インドネシア語",
        "th" to "タイ語", "fi" to "フィンランド語", "he" to "ヘブライ語"
    )

    /** Reject unexpected hostnames before constructing xx.wiktionary.org URLs. */
    fun validCode(code: String): Boolean =
        code.length in 2..32 && Regex("^[a-z]{2,12}(-[a-z0-9]{2,12})*$").matches(code)

    fun name(code: String): String = knownNames[code]
        ?: Locale(code).getDisplayLanguage(Locale.JAPANESE).takeIf { it.isNotBlank() && it != code }
        ?: code

    val suggestions: List<String> = listOf(
        "it", "pt", "ko", "nl", "pl", "la", "el", "sv", "hi", "tr", "uk", "vi", "id", "th", "fi", "he"
    )

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun load(context: Context): MutableList<LanguageSetting> {
        val preferences = prefs(context)
        val stored = preferences.getString(LANGUAGES_KEY, null)
        if (stored != null) {
            try {
                val json = JSONArray(stored)
                val result = mutableListOf<LanguageSetting>()
                val used = mutableSetOf<String>()
                for (i in 0 until json.length()) {
                    val item = json.optJSONObject(i) ?: continue
                    val code = item.optString("code").lowercase(Locale.ROOT)
                    if (validCode(code) && used.add(code)) {
                        result.add(LanguageSetting(code, item.optBoolean("enabled", true)))
                    }
                }
                return result
            } catch (_: Exception) {
                // If settings were corrupted, recover the previous numeric configuration below.
            }
        }

        val old = preferences.all.entries.mapNotNull { (key, value) ->
            val number = key.toIntOrNull() ?: return@mapNotNull null
            val code = (value as? String)?.trim()?.lowercase(Locale.ROOT) ?: return@mapNotNull null
            if (validCode(code)) number to code else null
        }.sortedBy { it.first }
        val migrated = old.map { it.second }.distinct().map { LanguageSetting(it) }
            .ifEmpty { defaults }
            .toMutableList()
        save(context, migrated)
        return migrated
    }

    fun save(context: Context, settings: List<LanguageSetting>) {
        val arr = JSONArray()
        settings.forEach { item ->
            arr.put(JSONObject().put("code", item.code).put("enabled", item.enabled))
        }
        prefs(context).edit().putString(LANGUAGES_KEY, arr.toString()).apply()
    }

    fun askBeforeOpening(context: Context): Boolean = prefs(context).getBoolean(ASK_KEY, false)

    fun setAskBeforeOpening(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(ASK_KEY, enabled).apply()
}

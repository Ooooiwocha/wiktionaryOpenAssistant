package jp.ooooiwocha.wiktionaryopenassistant
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import okhttp3.OkHttpClient
import okhttp3.Request
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class ProcessTextActivity : android.app.Activity() {
    fun setInitialValues() {
        val sharedPreferences = getSharedPreferences("lang_pref", Context.MODE_PRIVATE)
        // SharedPreferencesの現在の値をチェック
        if (!sharedPreferences.contains("key")) {
            // Editorを取得して値を設定
            val editor = sharedPreferences.edit()
            resources.openRawResource(R.raw.lang_pref).bufferedReader().forEachLine{
                editor.putString(sharedPreferences.getAll().size.toString(), it)
                editor.apply()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setInitialValues()
        val sharedPreferences = getSharedPreferences("lang_pref", Context.MODE_PRIVATE)
        val myMap: Map<String, *> = sharedPreferences.getAll()
        val size = myMap.size
        for(i in 0 until size){
            langCodeList.add(myMap.get(i.toString()).toString())
        }

        if (Intent.ACTION_PROCESS_TEXT == intent.action && intent.type == "text/plain") {
            // コルーチンを開始します
            GlobalScope.launch(Dispatchers.Main) {
                val selectedText = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT) ?: ""
                val lang: String? = findLanguage(selectedText)

                if (lang != null) {
                    // Wiktionaryで検索するURLを作成します
                    val url = "https://$lang.wiktionary.org/wiki/${Uri.encode(selectedText)}"
                    // 既定のブラウザでURLを開きます
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                }else{
                    val url = "https://en.m.wiktionary.org/w/index.php?search=${Uri.encode(selectedText)}"
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                    Toast.makeText(applicationContext, R.string.CANNOT_FIND_WORD, Toast.LENGTH_SHORT).show()
                }
                // アクティビティを終了します
                finish()
            }
        } else {
            // アクティビティを終了します
            finish()
        }
    }

    private suspend fun findLanguage(selectedText: String): String? {
        val client = OkHttpClient()
        langCodeList.forEach { lang ->
            val request = Request.Builder()
                .url("https://$lang.wiktionary.org/w/api.php?action=query&titles=$selectedText&format=json")
                .build()
            val response = withContext(Dispatchers.IO) { client.newCall(request).execute() }
            val jsonData = response.body?.string()
            jsonData?.let {
                val jsonElement = Json.parseToJsonElement(it)
                val test = jsonElement.jsonObject["query"]?.jsonObject?.get("pages")?.jsonObject
                if (test?.containsKey("-1") == false) {
                    return lang
                }
            }
        }
        return null
    }

    companion object {
        var langCodeList: ArrayList<String> = ArrayList<String>(0)
    }
}

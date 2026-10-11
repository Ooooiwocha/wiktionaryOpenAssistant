package jp.ooooiwocha.wiktionaryopenassistant

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Android text-selection action; also accepts plain text shared from other apps. */
class ProcessTextActivity : AppCompatActivity() {
    private var lookupJob: Job? = null
    private lateinit var selectedText: String
    private lateinit var languages: List<String>
    private lateinit var status: TextView
    private lateinit var spinner: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedText = when (intent.action) {
            Intent.ACTION_PROCESS_TEXT -> intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
            Intent.ACTION_SEND -> intent.getStringExtra(Intent.EXTRA_TEXT)
            else -> null
        }?.trim().orEmpty()

        if (selectedText.isEmpty()) {
            finish()
            return
        }
        languages = LanguageSettings.load(this).filter { it.enabled }.map { it.code }
        setContentView(R.layout.activity_lookup)
        status = findViewById(R.id.lookupStatus)
        spinner = findViewById(R.id.lookupProgress)
        findViewById<TextView>(R.id.lookupTerm).text = selectedText.take(140)
        findViewById<Button>(R.id.chooseLanguageButton).setOnClickListener {
            lookupJob?.cancel()
            spinner.visibility = View.GONE
            chooseLanguage()
        }
        findViewById<Button>(R.id.cancelButton).setOnClickListener { finish() }

        if (LanguageSettings.askBeforeOpening(this)) {
            spinner.visibility = View.GONE
            chooseLanguage()
        } else if (languages.isEmpty()) {
            spinner.visibility = View.GONE
            status.text = "有効な言語がありません"
            AlertDialog.Builder(this)
                .setTitle("言語を選択してください")
                .setMessage("アプリの設定から検索する言語を有効にしてください。")
                .setPositiveButton("設定を開く") { _, _ ->
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .setNegativeButton("閉じる") { _, _ -> finish() }
                .setOnCancelListener { finish() }
                .show()
        } else {
            findFirstLanguage()
        }
    }

    private fun findFirstLanguage() {
        status.text = "辞書に項目があるか確認しています…"
        spinner.visibility = View.VISIBLE
        lookupJob = lifecycleScope.launch {
            val code = WiktionaryLookup.firstExisting(selectedText, languages) { done, total ->
                status.text = "検索中  $done / $total 言語を確認"
            }
            spinner.visibility = View.GONE
            if (code != null) {
                openUrl(WiktionaryLookup.articleUrl(code, selectedText))
            } else {
                Toast.makeText(this@ProcessTextActivity, "見つからなかったため検索結果を開きます", Toast.LENGTH_LONG).show()
                openUrl(WiktionaryLookup.searchUrl(selectedText))
            }
        }
    }

    private fun chooseLanguage() {
        if (languages.isEmpty()) {
            status.text = "設定で言語を有効にしてください"
            return
        }
        status.text = "検索する言語を選択してください"
        val entries = languages.map { "${LanguageSettings.name(it)}  ($it)" }
        AlertDialog.Builder(this)
            .setTitle("どの言語版で調べますか？")
            .setItems(entries.toTypedArray()) { _, index ->
                openUrl(WiktionaryLookup.articleUrl(languages[index], selectedText))
            }
            .setNeutralButton("英語版で検索") { _, _ ->
                openUrl(WiktionaryLookup.searchUrl(selectedText))
            }
            .setNegativeButton("キャンセル") { _, _ -> finish() }
            .setOnCancelListener { finish() }
            .show()
    }

    private fun openUrl(uri: android.net.Uri) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "ブラウザーが見つかりません", Toast.LENGTH_LONG).show()
        } finally {
            finish()
        }
    }

    override fun onDestroy() {
        lookupJob?.cancel()
        super.onDestroy()
    }
}

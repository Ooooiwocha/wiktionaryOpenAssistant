package jp.ooooiwocha.wiktionaryopenassistant

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var settings: MutableList<LanguageSetting>
    private lateinit var languageContainer: LinearLayout
    private lateinit var exampleTerm: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settings = LanguageSettings.load(this)
        languageContainer = findViewById(R.id.languageContainer)
        exampleTerm = findViewById(R.id.exampleTerm)

        findViewById<RadioButton>(R.id.modeAutomatic).isChecked = !LanguageSettings.askBeforeOpening(this)
        findViewById<RadioButton>(R.id.modeChoose).isChecked = LanguageSettings.askBeforeOpening(this)
        findViewById<RadioButton>(R.id.modeAutomatic).setOnClickListener {
            LanguageSettings.setAskBeforeOpening(this, false)
        }
        findViewById<RadioButton>(R.id.modeChoose).setOnClickListener {
            LanguageSettings.setAskBeforeOpening(this, true)
        }

        findViewById<Button>(R.id.tryButton).setOnClickListener { trySelectedTerm() }
        exampleTerm.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                trySelectedTerm()
                true
            } else false
        }
        findViewById<Button>(R.id.addLanguageButton).setOnClickListener { showAddLanguages() }
        findViewById<Button>(R.id.resetButton).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("設定を初期化")
                .setMessage("言語の順番と有効・無効を初期状態に戻しますか？")
                .setNegativeButton("キャンセル", null)
                .setPositiveButton("元に戻す") { _, _ ->
                    settings = LanguageSettings.defaults.toMutableList()
                    saveAndRender()
                }.show()
        }
        renderLanguages()
    }

    private fun trySelectedTerm() {
        val term = exampleTerm.text.toString().trim()
        if (term.isEmpty()) {
            exampleTerm.error = "検索する単語を入力してください"
            return
        }
        startActivity(Intent(this, ProcessTextActivity::class.java).apply {
            action = Intent.ACTION_PROCESS_TEXT
            type = "text/plain"
            putExtra(Intent.EXTRA_PROCESS_TEXT, term)
        })
    }

    private fun saveAndRender() {
        LanguageSettings.save(this, settings)
        renderLanguages()
    }

    private fun renderLanguages() {
        languageContainer.removeAllViews()
        settings.forEachIndexed { index, item ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_language, languageContainer, false)
            val enabled = row.findViewById<CheckBox>(R.id.languageEnabled)
            val up = row.findViewById<ImageButton>(R.id.moveUp)
            val down = row.findViewById<ImageButton>(R.id.moveDown)
            val remove = row.findViewById<ImageButton>(R.id.removeLanguage)
            enabled.text = "${LanguageSettings.name(item.code)}  (${item.code})"
            enabled.isChecked = item.enabled
            enabled.setOnCheckedChangeListener { _, checked ->
                settings[index] = item.copy(enabled = checked)
                LanguageSettings.save(this, settings)
                if (settings.none { it.enabled }) {
                    Toast.makeText(this, "検索する言語がすべて無効です", Toast.LENGTH_SHORT).show()
                }
            }
            up.isEnabled = index > 0
            down.isEnabled = index < settings.lastIndex
            up.contentDescription = "${LanguageSettings.name(item.code)}を上へ移動"
            down.contentDescription = "${LanguageSettings.name(item.code)}を下へ移動"
            remove.contentDescription = "${LanguageSettings.name(item.code)}を削除"
            up.setOnClickListener {
                if (index > 0) {
                    settings.add(index - 1, settings.removeAt(index))
                    saveAndRender()
                }
            }
            down.setOnClickListener {
                if (index < settings.lastIndex) {
                    settings.add(index + 1, settings.removeAt(index))
                    saveAndRender()
                }
            }
            remove.setOnClickListener {
                settings.removeAt(index)
                saveAndRender()
            }
            languageContainer.addView(row)
        }
    }

    private fun showAddLanguages() {
        val choices = LanguageSettings.suggestions.filter { suggestion ->
            settings.none { it.code == suggestion }
        }
        val entries = choices.map { "${LanguageSettings.name(it)}  ($it)" } + "その他の言語コードを入力…"
        AlertDialog.Builder(this)
            .setTitle("言語を追加")
            .setItems(entries.toTypedArray()) { _, selected ->
                if (selected == choices.size) showCustomLanguageDialog()
                else addLanguage(choices[selected])
            }
            .setNegativeButton("キャンセル", null)
            .show()
    }

    private fun showCustomLanguageDialog() {
        val field = EditText(this).apply {
            hint = "例: la, pt, zh-min-nan"
            isSingleLine = true
            setPadding(40, 20, 40, 20)
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle("Wiktionaryの言語コード")
            .setMessage("言語コードを半角英小文字で入力してください。")
            .setView(field)
            .setNegativeButton("キャンセル", null)
            .setPositiveButton("追加", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val code = field.text.toString().trim().lowercase(Locale.ROOT)
                when {
                    !LanguageSettings.validCode(code) -> field.error = "言語コードの形式が正しくありません"
                    settings.any { it.code == code } -> field.error = "その言語は登録済みです"
                    else -> {
                        addLanguage(code)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun addLanguage(code: String) {
        if (settings.any { it.code == code }) {
            Toast.makeText(this, "既に登録されています", Toast.LENGTH_SHORT).show()
            return
        }
        settings.add(LanguageSetting(code))
        saveAndRender()
    }
}

package jp.ooooiwocha.wiktionaryopenassistant

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class DetailsActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyDetailsFragment())
                .commit()
        }

    }

}

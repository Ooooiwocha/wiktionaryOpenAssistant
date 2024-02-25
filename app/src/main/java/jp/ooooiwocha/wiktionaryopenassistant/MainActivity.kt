package jp.ooooiwocha.wiktionaryopenassistant

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class MainActivity : AppCompatActivity() {
    private lateinit var mInterstitialAd: InterstitialAd
    private var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setInitialValues()

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MyFragment())
                .commit()
        }

        MobileAds.initialize(this) {}
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(this,"ca-app-pub-5393754785673452/8891665074", adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                // 広告がロードされた時の処理
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
                showInterstitial()
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // 広告のロードに失敗した時の処理
                Log.d(TAG, loadAdError.message)

                //mInterstitialAd = null
            }
        })
    }
    private fun showInterstitial() {
        // インタースティシャル広告がロードされ、初期化されていることを確認
        if (::mInterstitialAd.isInitialized) {
            mInterstitialAd.show(this)
        } else {
            Log.d(TAG, "The interstitial ad wasn't ready yet.")
        }
    }
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


}


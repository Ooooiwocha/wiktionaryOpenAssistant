package jp.ooooiwocha.wiktionaryopenassistant

import org.junit.Assert.*
import org.junit.Test

class LanguageSettingsTest {
    @Test fun validLanguageCodes() {
        assertTrue(LanguageSettings.validCode("en"))
        assertTrue(LanguageSettings.validCode("zh-min-nan"))
        assertTrue(LanguageSettings.validCode("simple"))
    }

    @Test fun rejectsUnsafeHostnames() {
        assertFalse(LanguageSettings.validCode("https://evil.example"))
        assertFalse(LanguageSettings.validCode("en.example"))
        assertFalse(LanguageSettings.validCode("en@evil"))
        assertFalse(LanguageSettings.validCode(""))
    }
}

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.FirebaseApp

class Prefs(context: Context) {
    companion object {
        private const val PREF_NAME = "mPref"
        private const val TOKEN_KEY = "TOKEN"
        private const val USER_ID_KEY = "USER_ID"
        private const val PROFILE_IMAGE_URL_KEY = "PROFILE_IMAGE_URL"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString(TOKEN_KEY, null)   // null 권장
        set(value) { prefs.edit().putString(TOKEN_KEY, value).apply() }

    var userId: Long
        get() = prefs.getLong(USER_ID_KEY, -1L)
        set(value) { prefs.edit().putLong(USER_ID_KEY, value).apply() }

    // 추가: 프로필 이미지 URL
    var profileImageUrl: String?
        get() = prefs.getString(PROFILE_IMAGE_URL_KEY, null)
        set(value) { prefs.edit().putString(PROFILE_IMAGE_URL_KEY, value).apply() }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

class App : Application() {
    companion object {
        lateinit var context: Context
        lateinit var prefs: Prefs
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        prefs = Prefs(applicationContext)
        FirebaseApp.initializeApp(this)
    }
}

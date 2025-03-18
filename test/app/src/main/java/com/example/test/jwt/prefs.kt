import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class Prefs(context: Context) {
    companion object {
        private const val PREF_NAME = "mPref"
        private const val TOKEN_KEY = "TOKEN"
        private const val USER_ID_KEY = "USER_ID"
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    var token: String?
        get() = prefs.getString(TOKEN_KEY, "") // 기본값을 빈 문자열로 설정
        set(value) {
            prefs.edit().putString(TOKEN_KEY, value).apply()
        }

    var userId: Long
        get() = prefs.getLong(USER_ID_KEY, -1L)  // 기본값 -1L (유효하지 않은 ID)
        set(value) {
            prefs.edit().putLong(USER_ID_KEY, value).apply()
        }

    fun clear() {
        prefs.edit().clear().apply()
    }
}

class App : Application() {
    companion object {
        lateinit var prefs: Prefs
    }

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(applicationContext)
    }
}

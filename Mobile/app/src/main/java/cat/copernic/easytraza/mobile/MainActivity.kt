package cat.copernic.easytraza.mobile

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cat.copernic.easytraza.mobile.core.config.LanguagePreferences
import cat.copernic.easytraza.mobile.navigation.AppNavigation
import cat.copernic.easytraza.mobile.ui.theme.MobileTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguagePreferences.applyLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileTheme {
                AppNavigation()
            }
        }
    }
}

package cat.copernic.easytraza.mobile

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import cat.copernic.easytraza.mobile.core.config.LanguagePreferences
import cat.copernic.easytraza.mobile.navigation.AppNavigation
import cat.copernic.easytraza.mobile.ui.theme.MobileTheme

/**
 * ACTIVITAT PRINCIPAL DE L'APLICACIÓ.
 *
 * Inicialitzada l'aplicació mòbil, aplicada la configuració d'idioma
 * escollida i carregat el contingut Compose amb la navegació principal.
 *
 * @author Ángel Jurado Herruz
 */
class MainActivity : ComponentActivity() {


    /**
     * APLICACIÓ DEL CONTEXT LOCALITZAT.
     *
     * Aplicat al context base l'idioma desat per mostrar
     * la interfície de l'aplicació en la llengua seleccionada.
     *
     * @param newBase context base rebut per l'activitat
     */
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguagePreferences.applyLocale(newBase))
    }


    /**
     * CREACIÓ DE L'ACTIVITAT.
     *
     * Configurada la visualització de l'aplicació i carregats
     * el tema i la navegació principal quan s'inicia l'activitat.
     *
     * @param savedInstanceState estat anterior de l'activitat, si existeix
     */
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

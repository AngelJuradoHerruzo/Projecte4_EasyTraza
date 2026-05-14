package cat.copernic.easytraza.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import cat.copernic.easytraza.mobile.core.session.UsuariSessionManager
import cat.copernic.easytraza.mobile.features.auth.presentation.screens.identificar.IdentificarScreen
import cat.copernic.easytraza.mobile.features.settings.IpConfigScreen
import cat.copernic.easytraza.mobile.main.screens.MainScreen

/**
 * Navegació principal de l'app.
 */
@Composable
fun AppNavigation() {

    val context = LocalContext.current

    var screen by remember {
        mutableStateOf(
            if (UsuariSessionManager.hiHaUsuariIdentificat(context)) {
                "main"
            }
            else {
                "identificar"
            }
        )
    }

    when (screen) {

        "identificar" -> IdentificarScreen(
            onIdentificat = {
                screen = "main"
            },
            onConfigurarIpClick = {
                screen = "config"
            }
        )

        "config" -> IpConfigScreen(
            onTornarClick = {
                screen = if (UsuariSessionManager.hiHaUsuariIdentificat(context)) {
                    "main"
                }
                else {
                    "identificar"
                }
            }
        )

        else -> MainScreen(
            onConfigurarIpClick = {
                screen = "config"
            },
            onSortirClick = {
                UsuariSessionManager.tancarSessio(context)
                screen = "identificar"
            }
        )
    }
}
package cat.copernic.easytraza.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import cat.copernic.easytraza.mobile.core.session.UsuariSessionManager
import cat.copernic.easytraza.mobile.features.auth.presentation.screens.identificar.IdentificarScreen
import cat.copernic.easytraza.mobile.features.lots.presentation.screens.LotDetailScreen
import cat.copernic.easytraza.mobile.features.lots.presentation.screens.LotListScreen
import cat.copernic.easytraza.mobile.features.lots.presentation.viewmodels.LotViewModel
import cat.copernic.easytraza.mobile.features.settings.ConfiguracioMenuScreen
import cat.copernic.easytraza.mobile.features.settings.IdiomaConfigScreen
import cat.copernic.easytraza.mobile.features.settings.IpConfigScreen
import cat.copernic.easytraza.mobile.features.splash.SplashScreen
import cat.copernic.easytraza.mobile.main.screens.MainScreen

/**
 * NAVEGACIÓ PRINCIPAL.
 *
 * Gestionada la pantalla visible de l'aplicació mòbil, la sessió
 * de l'usuari i la navegació entre identificació, lots i configuració.
 */
@Composable
fun AppNavigation() {

    val context = LocalContext.current
    val lotViewModel = remember { LotViewModel(context) }

    var lotIdSeleccionat by remember { mutableLongStateOf(0L) }
    var pantallaAnteriorConfiguracio by remember { mutableStateOf("main") }

    val pantallaInicial = remember {
        if (UsuariSessionManager.hiHaUsuariIdentificat(context)) {
            "main"
        } else {
            "identificar"
        }
    }

    var screen by remember { mutableStateOf("splash") }

    when (screen) {

        "splash" -> SplashScreen(
            onSplashFinished = {
                screen = pantallaInicial
            }
        )

        "identificar" -> IdentificarScreen(
            onIdentificat = {
                screen = "main"
            },
            onConfiguracioClick = {
                pantallaAnteriorConfiguracio = "identificar"
                screen = "configuracio"
            }
        )

        "main" -> MainScreen(
            usuari = UsuariSessionManager.obtenirUsuari(context),
            onLotsClick = {
                lotViewModel.carregarLots()
                screen = "lots"
            },
            onProcessarAlbaraClick = {
                // OCR pendent
            },
            onConfiguracioClick = {
                pantallaAnteriorConfiguracio = "main"
                screen = "configuracio"
            },
            onSortirClick = {
                UsuariSessionManager.tancarSessio(context)
                screen = "identificar"
            }
        )

        "lots" -> LotListScreen(
            viewModel = lotViewModel,
            onLotClick = { lotId ->
                lotIdSeleccionat = lotId
                screen = "detallLot"
            },
            onConfiguracioClick = {
                pantallaAnteriorConfiguracio = "lots"
                screen = "configuracio"
            },
            onSortirClick = {
                UsuariSessionManager.tancarSessio(context)
                screen = "identificar"
            }
        )

        "detallLot" -> LotDetailScreen(
            lotId = lotIdSeleccionat,
            viewModel = lotViewModel,
            onTornarClick = {
                screen = "lots"
            },
            onConfiguracioClick = {
                pantallaAnteriorConfiguracio = "detallLot"
                screen = "configuracio"
            }
        )

        "configuracio" -> ConfiguracioMenuScreen(
            onIpClick = {
                screen = "configIp"
            },
            onIdiomaClick = {
                screen = "configIdioma"
            },
            onTornarClick = {
                screen = pantallaAnteriorConfiguracio
            }
        )

        "configIp" -> IpConfigScreen(
            onTornarClick = {
                screen = "configuracio"
            }
        )

        "configIdioma" -> IdiomaConfigScreen(
            onTornarClick = {
                screen = "configuracio"
            }
        )
    }
}

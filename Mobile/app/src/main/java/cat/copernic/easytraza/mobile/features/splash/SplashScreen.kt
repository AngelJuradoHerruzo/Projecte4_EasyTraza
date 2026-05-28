package cat.copernic.easytraza.mobile.features.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.R
import kotlinx.coroutines.delay

/**
 * PANTALLA D'INICI.
 *
 * Mostrat el logotip d'EasyTraza durant l'arrencada
 * abans de continuar cap al flux principal de l'aplicació.
 *
 * @param onSplashFinished acció executada en finalitzar la pantalla inicial
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(2000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFCF9EF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.easytraza_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(260.dp),
                contentScale = ContentScale.Fit
            )
        }
    }
}
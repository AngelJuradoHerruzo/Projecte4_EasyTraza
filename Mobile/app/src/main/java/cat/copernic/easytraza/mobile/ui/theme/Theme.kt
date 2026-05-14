package cat.copernic.easytraza.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EasyTrazaColorScheme = lightColorScheme(
    primary = EasyBrown,
    onPrimary = EasyWhite,
    secondary = EasyBrownSoft,
    onSecondary = EasyWhite,
    background = EasyBeigeLight,
    onBackground = EasyText,
    surface = EasyWhite,
    onSurface = EasyText,
    error = EasyError
)

@Composable
fun MobileTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EasyTrazaColorScheme,
        typography = Typography,
        content = content
    )
}
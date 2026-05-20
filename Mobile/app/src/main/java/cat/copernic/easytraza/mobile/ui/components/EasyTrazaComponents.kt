package cat.copernic.easytraza.mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBeigeLight
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyCardBorder
import cat.copernic.easytraza.mobile.ui.theme.EasyError
import cat.copernic.easytraza.mobile.ui.theme.EasyText
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite
import cat.copernic.easytraza.mobile.ui.theme.StatusAcabatBackground
import cat.copernic.easytraza.mobile.ui.theme.StatusAcabatBorder
import cat.copernic.easytraza.mobile.ui.theme.StatusAcabatText
import cat.copernic.easytraza.mobile.ui.theme.StatusEstocBackground
import cat.copernic.easytraza.mobile.ui.theme.StatusEstocBorder
import cat.copernic.easytraza.mobile.ui.theme.StatusEstocText
import cat.copernic.easytraza.mobile.ui.theme.StatusObertBackground
import cat.copernic.easytraza.mobile.ui.theme.StatusObertBorder
import cat.copernic.easytraza.mobile.ui.theme.StatusObertText

val EasyButtonShape = RoundedCornerShape(12.dp)
val EasyCardShape = RoundedCornerShape(18.dp)
val EasySmallShape = RoundedCornerShape(12.dp)

/**
 * Contenidor base perquè totes les pantalles mantinguin el mateix marge, fons i separació superior.
 */
@Composable
fun EasyScreen(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EasyBeigeLight)
            .statusBarsPadding()
            .padding(horizontal = 22.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = content
    )
}

/**
 * Capçalera comuna de les pantalles de l'app.
 */
@Composable
fun EasyHeader(
    title: String,
    subtitle: String,
    showConfig: Boolean = true,
    showBack: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    onConfiguracioClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = EasyWhite,
        shape = EasyCardShape,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, EasyCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showBack && onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Tornar",
                        tint = EasyBrownDark
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = if (showBack) 2.dp else 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = EasyBrownDark,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EasyTextSoft,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )

            if (showConfig && onConfiguracioClick != null) {
                TextButton(
                    onClick = onConfiguracioClick,
                    shape = EasyButtonShape
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Configuració",
                        tint = EasyBrown,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
        }
    }
}

/**
 * Targeta base comuna.
 */
@Composable
fun EasyCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = EasyCardShape,
        colors = CardDefaults.cardColors(containerColor = EasyWhite),
        border = BorderStroke(1.dp, EasyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            content = content
        )
    }
}

/**
 * Botó principal comú.
 */
@Composable
fun Modifier.EasyPrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = fillMaxWidth(),
        shape = EasyButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = EasyBrown,
            contentColor = EasyWhite,
            disabledContainerColor = EasyBeige,
            disabledContentColor = EasyTextSoft
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 5.dp)
        )
    }
}

/**
 * Botó secundari comú.
 */
@Composable
fun Modifier.EasySecondaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = fillMaxWidth(),
        shape = EasyButtonShape,
        border = BorderStroke(1.dp, if (enabled) EasyBrown else EasyCardBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = EasyBrown,
            disabledContentColor = EasyTextSoft
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 5.dp)
        )
    }
}

/**
 * Botó d'acció destructiva.
 */
@Composable
fun Modifier.EasyDangerButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = fillMaxWidth(),
        shape = EasyButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = EasyError,
            contentColor = EasyWhite,
            disabledContainerColor = EasyBeige,
            disabledContentColor = EasyTextSoft
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 5.dp)
        )
    }
}

/**
 * Missatge informatiu comú per errors i avisos.
 */
@Composable
fun EasyMessageCard(
    text: String,
    isError: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isError) Color(0xFFFFF5F5) else EasyBeige,
                shape = EasySmallShape
            )
            .border(
                width = 1.dp,
                color = if (isError) Color(0xFFEED4D4) else EasyCardBorder,
                shape = EasySmallShape
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (isError) EasyError else EasyBrown,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Diàleg de confirmació amb estil comú.
 */
@Composable
fun EasyConfirmDialog(
    title: String,
    text: String,
    confirmText: String,
    dismissText: String = "Cancel·lar",
    isDanger: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = EasyCardShape,
        title = {
            Text(
                text = title,
                color = EasyBrownDark,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = text,
                color = EasyText,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = EasyButtonShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDanger) EasyError else EasyBrown,
                    contentColor = EasyWhite
                )
            ) {
                Text(confirmText, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = EasyButtonShape,
                border = BorderStroke(1.dp, EasyBrown)
            ) {
                Text(dismissText, color = EasyBrown, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = EasyWhite
    )
}

/**
 * Etiqueta d'estat dels lots.
 */
@Composable
fun EasyStatusBadge(
    estat: String
) {
    val textColor: Color
    val backgroundColor: Color
    val borderColor: Color
    val text: String

    when (estat) {
        "EN_ESTOC" -> {
            textColor = StatusEstocText
            backgroundColor = StatusEstocBackground
            borderColor = StatusEstocBorder
            text = "EN ESTOC"
        }

        "OBERT" -> {
            textColor = StatusObertText
            backgroundColor = StatusObertBackground
            borderColor = StatusObertBorder
            text = "OBERT"
        }

        "ACABAT" -> {
            textColor = StatusAcabatText
            backgroundColor = StatusAcabatBackground
            borderColor = StatusAcabatBorder
            text = "ACABAT"
        }

        else -> {
            textColor = EasyText
            backgroundColor = EasyBeige
            borderColor = EasyCardBorder
            text = estat.ifBlank { "-" }
        }
    }

    Text(
        text = text,
        color = textColor,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(backgroundColor, EasySmallShape)
            .border(1.dp, borderColor, EasySmallShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

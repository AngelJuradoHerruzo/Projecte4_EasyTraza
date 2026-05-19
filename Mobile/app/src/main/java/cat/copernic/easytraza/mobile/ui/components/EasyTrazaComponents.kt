package cat.copernic.easytraza.mobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
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

/**
 * Capçalera comuna de les pantalles de l'app.
 */
@Composable
fun EasyHeader(
    title: String,
    subtitle: String,
    showConfig: Boolean = true,
    onConfiguracioClick: (() -> Unit)? = null
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = EasyBrownDark,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = EasyTextSoft,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (showConfig && onConfiguracioClick != null) {
            TextButton(
                onClick = onConfiguracioClick,
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Configuració",
                    color = EasyBrown,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuració",
                    tint = EasyBrown,
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .size(21.dp)
                )
            }
        }
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
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = EasyBrown,
            contentColor = EasyWhite
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
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
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, EasyBrown),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = EasyBrown
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    }
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
            borderColor = EasyBeige
            text = estat.ifBlank { "-" }
        }
    }

    Text(
        text = text,
        color = textColor,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}
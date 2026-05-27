package cat.copernic.easytraza.mobile.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import cat.copernic.easytraza.mobile.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.features.auth.domain.models.UsuariIdentificat
import cat.copernic.easytraza.mobile.ui.components.EasyCard
import cat.copernic.easytraza.mobile.ui.components.EasyCardShape
import cat.copernic.easytraza.mobile.ui.components.EasyHeader
import cat.copernic.easytraza.mobile.ui.components.EasyScreen
import cat.copernic.easytraza.mobile.ui.components.EasySecondaryButton
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyCardBorder
import cat.copernic.easytraza.mobile.ui.theme.EasyDisabled
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

/**
 * PANTALLA PRINCIPAL.
 *
 * Mostrat el menú principal de l'aplicació mòbil amb les dades
 * de l'usuari identificat i els accessos a les funcionalitats disponibles.
 *
 * @param usuari usuari identificat que es mostra a la pantalla
 * @param onLotsClick acció executada en accedir a la gestió de lots
 * @param onProcessarAlbaraClick acció executada en accedir al processament d'albarans
 * @param onConfiguracioClick acció executada en accedir a la configuració
 * @param onSortirClick acció executada en canviar d'usuari
 */
@Composable
fun MainScreen(
    usuari: UsuariIdentificat?,
    onLotsClick: () -> Unit,
    onProcessarAlbaraClick: () -> Unit,
    onConfiguracioClick: () -> Unit,
    onSortirClick: () -> Unit
) {
    EasyScreen {
        EasyHeader(
            title = "EasyTraza",
            subtitle = stringResource(R.string.main_subtitle),
            showConfig = true,
            onConfiguracioClick = onConfiguracioClick
        )

        EasyCard {
            Text(
                text = stringResource(R.string.main_hello, usuari?.nomComplet ?: stringResource(R.string.common_user)),
                color = EasyBrownDark,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.main_select_section),
                color = EasyTextSoft,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        MenuOptionCard(
            title = stringResource(R.string.main_manage_lots),
            subtitle = stringResource(R.string.main_manage_lots_description),
            icon = Icons.Default.Inventory,
            enabled = true,
            onClick = onLotsClick
        )

        MenuOptionCard(
            title = stringResource(R.string.main_process_delivery_note),
            subtitle = stringResource(R.string.main_process_delivery_note_description),
            icon = Icons.Default.Description,
            enabled = false,
            onClick = onProcessarAlbaraClick
        )

        Spacer(modifier = Modifier.weight(1f))

        Modifier.EasySecondaryButton(
            text = stringResource(R.string.main_change_user),
            onClick = onSortirClick
        )
    }
}


/**
 * OPCIÓ DEL MENÚ PRINCIPAL.
 *
 * Mostrada una targeta d'accés a una funcionalitat del menú principal,
 * indicant visualment si aquesta opció està disponible per a l'usuari.
 *
 * @param title títol visible de l'opció
 * @param subtitle descripció visible de l'opció
 * @param icon icona representativa de l'opció
 * @param enabled indicador que informa si l'opció està habilitada
 * @param onClick acció executada en seleccionar l'opció habilitada
 */
@Composable
fun MenuOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() },
        shape = EasyCardShape,
        colors = CardDefaults.cardColors(containerColor = EasyWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, EasyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(
                        color = if (enabled) EasyBeige else EasyDisabled,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = if (enabled) EasyBrown else EasyTextSoft,
                    modifier = Modifier.size(30.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (enabled) EasyBrownDark else EasyTextSoft,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = EasyTextSoft
                )

                if (!enabled) {
                    Text(
                        text = stringResource(R.string.main_coming_soon),
                        style = MaterialTheme.typography.labelMedium,
                        color = EasyBrown,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

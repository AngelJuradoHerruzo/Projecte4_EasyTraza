package cat.copernic.easytraza.mobile.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.features.auth.domain.models.UsuariIdentificat
import cat.copernic.easytraza.mobile.ui.components.EasyHeader
import cat.copernic.easytraza.mobile.ui.components.EasySecondaryButton
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBeigeLight
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyDisabled
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

/**
 * Pantalla principal de l'app mòbil.
 */
@Composable
fun MainScreen(
    usuari: UsuariIdentificat?,
    onLotsClick: () -> Unit,
    onProcessarAlbaraClick: () -> Unit,
    onConfiguracioClick: () -> Unit,
    onSortirClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EasyBeigeLight)
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        EasyHeader(
            title = "EasyTraza",
            subtitle = "Menú principal",
            showConfig = true,
            onConfiguracioClick = onConfiguracioClick
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = EasyWhite
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 3.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Hola, ${usuari?.nomComplet ?: "usuari"}",
                    color = EasyBrownDark,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Selecciona l'apartat que vols utilitzar.",
                    color = EasyTextSoft,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        MenuOptionCard(
            title = "Gestionar lots",
            subtitle = "Gestiona, consulta, inicia i finalitza els lots de proveïdor.",
            icon = Icons.Default.Inventory,
            enabled = true,
            onClick = onLotsClick
        )

        MenuOptionCard(
            title = "Processar albarà",
            subtitle = "Processa i llegeix albarans de proveïdor amb OCR.",
            icon = Icons.Default.Description,
            enabled = false,
            onClick = onProcessarAlbaraClick
        )

        Spacer(modifier = Modifier.weight(1f))

        Modifier.EasySecondaryButton(
            text = "Canviar d'usuari",
            onClick = onSortirClick
        )
    }
}


/**
 * Targeta d'opció del menú principal.
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
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = EasyWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
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
                        shape = RoundedCornerShape(18.dp)
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
                        text = "Properament",
                        style = MaterialTheme.typography.labelMedium,
                        color = EasyBrown,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
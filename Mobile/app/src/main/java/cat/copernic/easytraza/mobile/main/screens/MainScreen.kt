package cat.copernic.easytraza.mobile.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.features.auth.domain.models.UsuariIdentificat
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBeigeLight
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyCream
import cat.copernic.easytraza.mobile.ui.theme.EasyText
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

/**
 * Pantalla principal de l'app mòbil.
 */
@Composable
fun MainScreen(
    usuari: UsuariIdentificat?,
    onConfigurarIpClick: () -> Unit,
    onSortirClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EasyBeigeLight)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        Text(
            text = "EasyTraza",
            style = MaterialTheme.typography.headlineMedium,
            color = EasyBrownDark,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Usuari identificat",
            style = MaterialTheme.typography.headlineSmall,
            color = EasyText,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Informació de l'usuari seleccionat a l'app mòbil.",
            style = MaterialTheme.typography.bodyMedium,
            color = EasyTextSoft
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = EasyWhite
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(104.dp)
                        .background(EasyBrown, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Usuari",
                        tint = EasyCream,
                        modifier = Modifier.size(68.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = usuari?.nomComplet ?: "Usuari",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EasyBrownDark,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = usuari?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EasyTextSoft
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = usuari?.rolUsuari ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    color = EasyBrown,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(EasyBeige, RoundedCornerShape(40.dp))
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                )
            }
        }

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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                ProfileInfoRow(
                    icon = Icons.Default.Badge,
                    label = "DNI",
                    value = usuari?.dni ?: "-"
                )

                ProfileInfoRow(
                    icon = Icons.Default.PersonPin,
                    label = "Nom complet",
                    value = usuari?.nomComplet ?: "-"
                )

                ProfileInfoRow(
                    icon = Icons.Default.Email,
                    label = "Correu electrònic",
                    value = usuari?.email ?: "-"
                )

                ProfileInfoRow(
                    icon = Icons.Default.Security,
                    label = "Rol",
                    value = usuari?.rolUsuari ?: "-"
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onConfigurarIpClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Configurar servidor")
        }

        OutlinedButton(
            onClick = onSortirClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Canviar d'usuari")
        }
    }
}


/**
 * Fila d'informació del perfil.
 */
@Composable
fun ProfileInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(42.dp)
                .background(EasyBeige, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = EasyBrown,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 14.dp)
                .weight(1f)
        ) {

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = EasyTextSoft,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = EasyText,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
package cat.copernic.easytraza.mobile.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBeigeLight
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

/**
 * Menú de configuració de l'app.
 */
@Composable
fun ConfiguracioMenuScreen(
    onIpClick: () -> Unit,
    onTornarClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EasyBeigeLight)
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(onClick = onTornarClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Tornar",
                    tint = EasyBrownDark
                )
            }

            Column(
                modifier = Modifier.padding(start = 6.dp)
            ) {
                Text(
                    text = "Configuració",
                    style = MaterialTheme.typography.headlineMedium,
                    color = EasyBrownDark,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Opcions de l'aplicació",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EasyTextSoft,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onIpClick() },
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
                        .background(EasyBeige, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = "Servidor",
                        tint = EasyBrown,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Servidor",
                        style = MaterialTheme.typography.titleMedium,
                        color = EasyBrownDark,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Configurar la IP del backend.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = EasyTextSoft
                    )
                }
            }
        }
    }
}
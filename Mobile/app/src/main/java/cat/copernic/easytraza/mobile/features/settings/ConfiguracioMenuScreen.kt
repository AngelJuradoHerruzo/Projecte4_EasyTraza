package cat.copernic.easytraza.mobile.features.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.R
import cat.copernic.easytraza.mobile.ui.components.EasyCardShape
import cat.copernic.easytraza.mobile.ui.components.EasyHeader
import cat.copernic.easytraza.mobile.ui.components.EasyScreen
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyCardBorder
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

/**
 * Menú de configuració de l'app.
 */
@Composable
fun ConfiguracioMenuScreen(
    onIpClick: () -> Unit,
    onIdiomaClick: () -> Unit,
    onTornarClick: () -> Unit
) {
    EasyScreen {
        EasyHeader(
            title = stringResource(R.string.settings_title),
            subtitle = stringResource(R.string.settings_subtitle),
            showBack = true,
            onBackClick = onTornarClick,
            showConfig = false
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onIpClick() },
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
                        .background(EasyBeige, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Dns,
                        contentDescription = stringResource(R.string.server_title),
                        tint = EasyBrown,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.server_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = EasyBrownDark,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(R.string.server_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = EasyTextSoft
                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onIdiomaClick() },
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
                        .background(EasyBeige, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = stringResource(R.string.language_title),
                        tint = EasyBrown,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Column(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.language_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = EasyBrownDark,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(R.string.language_description),
                        style = MaterialTheme.typography.bodyMedium,
                        color = EasyTextSoft
                    )
                }
            }
        }
    }
}

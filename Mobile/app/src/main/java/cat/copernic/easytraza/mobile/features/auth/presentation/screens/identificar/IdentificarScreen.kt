package cat.copernic.easytraza.mobile.features.auth.presentation.screens.identificar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.features.auth.domain.models.UsuariIdentificat
import cat.copernic.easytraza.mobile.features.auth.presentation.viewmodels.IdentificarViewModel
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBeigeLight
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyCream
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

/**
 * Pantalla d'identificació d'usuari.
 */
@Composable
fun IdentificarScreen(
    onIdentificat: () -> Unit,
    onConfigurarIpClick: () -> Unit
) {

    val context = LocalContext.current
    val viewModel = remember { IdentificarViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EasyBeigeLight)
            .padding(22.dp)
    ) {

        IconButton(
            onClick = onConfigurarIpClick,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Configurar IP",
                tint = EasyBrownDark
            )
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(34.dp))

            Text(
                text = "EasyTraza",
                style = MaterialTheme.typography.headlineMedium,
                color = EasyBrownDark,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Qui ets?",
                style = MaterialTheme.typography.headlineLarge,
                color = EasyBrown,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 18.dp)
            )

            Text(
                text = "Selecciona el teu usuari per continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = EasyTextSoft,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            when {
                uiState.carregant -> {
                    CircularProgressIndicator(
                        color = EasyBrown
                    )
                }

                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Button(
                        onClick = { viewModel.carregarUsuaris() }
                    ) {
                        Text("Reintentar")
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalArrangement = Arrangement.spacedBy(22.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        itemsIndexed(uiState.usuaris) { index, usuari ->
                            UsuariProfileItem(
                                usuari = usuari,
                                index = index,
                                onClick = {
                                    viewModel.identificarUsuari(
                                        usuari = usuari,
                                        onSuccess = onIdentificat
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onConfigurarIpClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Configurar servidor")
            }
        }
    }
}


/**
 * Targeta d'usuari seleccionable.
 */
@Composable
fun UsuariProfileItem(
    usuari: UsuariIdentificat,
    index: Int,
    onClick: () -> Unit
) {

    val colors = listOf(
        EasyBrown,
        EasyBrownSoft,
        Color(0xFF8A5A36),
        Color(0xFF7A4B2E)
    )

    val backgroundColor = colors[index % colors.size]

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = EasyWhite
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 5.dp
            )
        ) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
                    .background(
                        color = backgroundColor,
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = usuari.nomComplet,
                    tint = EasyCream,
                    modifier = Modifier.size(74.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(9.dp))

        Text(
            text = usuari.nomComplet,
            color = EasyBrownDark,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 1
        )

        Text(
            text = usuari.rolUsuari,
            color = EasyTextSoft,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
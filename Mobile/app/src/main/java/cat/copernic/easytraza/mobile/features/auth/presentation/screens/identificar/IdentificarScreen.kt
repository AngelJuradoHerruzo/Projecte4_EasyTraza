package cat.copernic.easytraza.mobile.features.auth.presentation.screens.identificar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import cat.copernic.easytraza.mobile.ui.components.EasyHeader
import cat.copernic.easytraza.mobile.ui.components.EasyPrimaryButton
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBeigeLight
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyCream
import cat.copernic.easytraza.mobile.ui.theme.EasyText
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

/**
 * Pantalla d'identificació d'usuari.
 */
@Composable
fun IdentificarScreen(
    onIdentificat: () -> Unit,
    onConfiguracioClick: () -> Unit
) {

    val context = LocalContext.current
    val viewModel = remember { IdentificarViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EasyBeigeLight)
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        EasyHeader(
            title = "EasyTraza",
            subtitle = "Identificació d'operari",
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
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Qui ets?",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EasyBrownDark,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Selecciona el teu usuari per continuar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EasyTextSoft
                )
            }
        }

        when {
            uiState.carregant -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EasyBrown)
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Modifier.EasyPrimaryButton(
                        text = "Reintentar"
                    ) { viewModel.carregarUsuaris() }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.weight(1f)
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = EasyWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
                    .background(
                        color = EasyBeige,
                        shape = RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Box(
                    modifier = Modifier
                        .size(78.dp)
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
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Text(
                text = usuari.nomComplet,
                color = EasyText,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.padding(top = 10.dp)
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
}
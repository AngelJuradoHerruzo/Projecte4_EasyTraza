package cat.copernic.easytraza.mobile.features.auth.presentation.screens.identificar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.ui.res.stringResource
import cat.copernic.easytraza.mobile.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.features.auth.domain.models.UsuariIdentificat
import cat.copernic.easytraza.mobile.features.auth.presentation.viewmodels.IdentificarViewModel
import cat.copernic.easytraza.mobile.ui.components.EasyCard
import cat.copernic.easytraza.mobile.ui.components.EasyCardShape
import cat.copernic.easytraza.mobile.ui.components.EasyHeader
import cat.copernic.easytraza.mobile.ui.components.EasyMessageCard
import cat.copernic.easytraza.mobile.ui.components.EasyPrimaryButton
import cat.copernic.easytraza.mobile.ui.components.EasyScreen
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyCardBorder
import cat.copernic.easytraza.mobile.ui.theme.EasyCream
import cat.copernic.easytraza.mobile.ui.theme.EasyText
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

/**
 * TARGETA D'USUARI.
 *
 * Mostrada la informació visual d'un usuari disponible i habilitada
 * la seva selecció per iniciar el procés d'identificació.
 *
 * @param usuari usuari representat a la targeta
 * @param index posició utilitzada per definir l'aspecte de la targeta
 * @param onClick acció executada en seleccionar l'usuari
 */
@Composable
fun IdentificarScreen(
    onIdentificat: () -> Unit,
    onConfiguracioClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = remember { IdentificarViewModel(context) }
    val uiState by viewModel.uiState.collectAsState()

    EasyScreen {
        EasyHeader(
            title = "EasyTraza",
            subtitle = stringResource(R.string.auth_subtitle),
            showConfig = true,
            onConfiguracioClick = onConfiguracioClick
        )

        EasyCard {
            Text(
                text = stringResource(R.string.auth_select_user),
                style = MaterialTheme.typography.headlineSmall,
                color = EasyBrownDark,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.auth_choose_profile),
                style = MaterialTheme.typography.bodyMedium,
                color = EasyTextSoft
            )
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
                    EasyMessageCard(text = uiState.error ?: "", isError = true)

                    Modifier.padding(top = 16.dp).EasyPrimaryButton(
                        text = stringResource(R.string.common_retry)
                    ) { viewModel.carregarUsuaris() }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
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
        shape = EasyCardShape,
        colors = CardDefaults.cardColors(containerColor = EasyWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, EasyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
                    .background(
                        color = EasyBeige,
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(78.dp)
                        .background(
                            color = backgroundColor,
                            shape = RoundedCornerShape(16.dp)
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
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

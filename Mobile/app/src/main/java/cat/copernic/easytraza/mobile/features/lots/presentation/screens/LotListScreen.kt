package cat.copernic.easytraza.mobile.features.lots.presentation.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.features.lots.domain.models.Lot
import cat.copernic.easytraza.mobile.features.lots.presentation.viewmodels.LotViewModel
import cat.copernic.easytraza.mobile.ui.components.EasyHeader
import cat.copernic.easytraza.mobile.ui.components.EasySecondaryButton
import cat.copernic.easytraza.mobile.ui.components.EasyStatusBadge
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBeigeLight
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyCardBorder
import cat.copernic.easytraza.mobile.ui.theme.EasyText
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

/**
 * Pantalla de llistat de lots.
 */
@Composable
fun LotListScreen(
    viewModel: LotViewModel,
    onLotClick: (Long) -> Unit,
    onConfiguracioClick: () -> Unit,
    onSortirClick: () -> Unit
) {

    val uiState by viewModel.uiState.collectAsState()
    val grupsPlegats = remember { mutableStateMapOf<String, Boolean>() }

    val lotsAgrupats = uiState.lots
        .groupBy { lot -> lot.materiaPrimeraNom.ifBlank { "Sense matèria primera" } }
        .toSortedMap()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(EasyBeigeLight)
            .padding(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(
                modifier = Modifier.weight(1f)
            ) {
                EasyHeader(
                    title = "EasyTraza",
                    subtitle = "Llistat de Lots",
                    showConfig = true,
                    onConfiguracioClick = onConfiguracioClick
                )
            }

            IconButton(
                onClick = { viewModel.carregarLots() }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Recarregar",
                    tint = EasyBrown
                )
            }
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (uiState.missatge != null) {
            Text(
                text = uiState.missatge ?: "",
                color = EasyBrown,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
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

            uiState.lots.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hi ha lots disponibles.",
                        color = EasyTextSoft,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    lotsAgrupats.forEach { (materiaPrimera, lots) ->
                        item {
                            LotMateriaGroupCard(
                                materiaPrimera = materiaPrimera,
                                lots = lots,
                                plegat = grupsPlegats[materiaPrimera] == true,
                                onHeaderClick = {
                                    grupsPlegats[materiaPrimera] =
                                        !(grupsPlegats[materiaPrimera] ?: false)
                                },
                                onLotClick = onLotClick
                            )
                        }
                    }
                }
            }
        }

        Modifier.EasySecondaryButton(
            text = "Canviar d'usuari",
            onClick = onSortirClick
        )
    }
}


/**
 * Grup visual de lots associats a una matèria primera.
 */
@Composable
fun LotMateriaGroupCard(
    materiaPrimera: String,
    lots: List<Lot>,
    plegat: Boolean,
    onHeaderClick: () -> Unit,
    onLotClick: (Long) -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = EasyWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {

        Column {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EasyBeige)
                    .clickable { onHeaderClick() }
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = materiaPrimera,
                        color = EasyBrownDark,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${lots.size} lots",
                        color = EasyTextSoft,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = if (plegat) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = if (plegat) "Desplegar" else "Plegar",
                    tint = EasyBrown
                )
            }

            if (!plegat) {
                lots.forEachIndexed { index, lot ->

                    LotListItem(
                        lot = lot,
                        onClick = { onLotClick(lot.id) }
                    )

                    if (index < lots.lastIndex) {
                        HorizontalDivider(
                            color = EasyCardBorder,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                    }
                }
            }
        }
    }
}


/**
 * Fila d'un lot dins d'un grup de matèria primera.
 */
@Composable
fun LotListItem(
    lot: Lot,
    onClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .background(EasyBeige, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = "Lot",
                tint = EasyBrown,
                modifier = Modifier.size(25.dp)
            )
        }

        Column(
            modifier = Modifier
                .padding(start = 14.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {

            Text(
                text = lot.identificadorLot,
                color = EasyBrownDark,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${lot.quantitat} ${lot.unitats}",
                color = EasyText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        EasyStatusBadge(estat = lot.estat)
    }
}
package cat.copernic.easytraza.mobile.features.lots.presentation.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.features.lots.domain.models.Lot
import cat.copernic.easytraza.mobile.features.lots.presentation.viewmodels.LotViewModel
import cat.copernic.easytraza.mobile.ui.components.EasyCard
import cat.copernic.easytraza.mobile.ui.components.EasyConfirmDialog
import cat.copernic.easytraza.mobile.ui.components.EasyDangerButton
import cat.copernic.easytraza.mobile.ui.components.EasyHeader
import cat.copernic.easytraza.mobile.ui.components.EasyMessageCard
import cat.copernic.easytraza.mobile.ui.components.EasyPrimaryButton
import cat.copernic.easytraza.mobile.ui.components.EasyScreen
import cat.copernic.easytraza.mobile.ui.components.EasyStatusBadge
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyCardBorder
import cat.copernic.easytraza.mobile.ui.theme.EasyError
import cat.copernic.easytraza.mobile.ui.theme.EasyText
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

/**
 * Pantalla de detall d'un lot.
 */
@Composable
fun LotDetailScreen(
    lotId: Long,
    viewModel: LotViewModel,
    onTornarClick: () -> Unit,
    onConfiguracioClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var mostrarConfirmacioIniciar by remember { mutableStateOf(false) }
    var mostrarConfirmacioFinalitzar by remember { mutableStateOf(false) }

    LaunchedEffect(lotId) {
        viewModel.consultarLot(lotId)
    }

    val lotActual = uiState.lotSeleccionat

    if (mostrarConfirmacioIniciar && lotActual != null) {
        EasyConfirmDialog(
            title = "Confirmar inici de lot",
            text = "Vols iniciar el lot ${lotActual.identificadorLot}? El sistema comprovarà si ja existeix un altre lot obert de la mateixa matèria primera.",
            confirmText = "Iniciar lot",
            onConfirm = {
                mostrarConfirmacioIniciar = false
                viewModel.iniciarLot(lotActual)
            },
            onDismiss = { mostrarConfirmacioIniciar = false }
        )
    }

    if (mostrarConfirmacioFinalitzar && lotActual != null) {
        EasyConfirmDialog(
            title = "Confirmar finalització",
            text = "Vols finalitzar el lot ${lotActual.identificadorLot}? Aquesta acció marcarà el lot com a acabat.",
            confirmText = "Finalitzar lot",
            isDanger = true,
            onConfirm = {
                mostrarConfirmacioFinalitzar = false
                viewModel.finalitzarLot(lotActual)
            },
            onDismiss = { mostrarConfirmacioFinalitzar = false }
        )
    }

    if (uiState.mostrarConfirmacioInici) {
        EasyConfirmDialog(
            title = "Lot obert existent",
            text = uiState.missatge
                ?: "Ja hi ha un lot obert d'aquesta matèria primera. Si continues, es finalitzarà el lot anterior i s'iniciarà aquest.",
            confirmText = "Tancar anterior i iniciar",
            onConfirm = { viewModel.confirmarIniciLot() },
            onDismiss = { viewModel.cancelLarConfirmacioInici() }
        )
    }

    EasyScreen {
        EasyHeader(
            title = "Detall del lot",
            subtitle = "Informació i accions del lot",
            showBack = true,
            onBackClick = onTornarClick,
            showConfig = true,
            onConfiguracioClick = onConfiguracioClick
        )

        uiState.error?.let { EasyMessageCard(text = it, isError = true) }

        if (uiState.missatge != null && !uiState.mostrarConfirmacioInici) {
            EasyMessageCard(text = uiState.missatge ?: "")
        }

        when {
            uiState.carregant && lotActual == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EasyBrown)
                }
            }

            lotActual != null -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LotDetailContent(
                        lot = lotActual,
                        loading = uiState.carregant,
                        onIniciarClick = { mostrarConfirmacioIniciar = true },
                        onFinalitzarClick = { mostrarConfirmacioFinalitzar = true }
                    )
                }
            }
        }
    }
}

/**
 * Contingut del detall d'un lot.
 */
@Composable
fun LotDetailContent(
    lot: Lot,
    loading: Boolean,
    onIniciarClick: () -> Unit,
    onFinalitzarClick: () -> Unit
) {
    EasyCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(EasyBeige, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Lot",
                    tint = EasyBrown,
                    modifier = Modifier.size(34.dp)
                )
            }

            Column(
                modifier = Modifier
                    .padding(start = 14.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = lot.identificadorLot,
                    color = EasyBrownDark,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                EasyStatusBadge(estat = lot.estat)
            }
        }

        DetailRow("Matèria primera", lot.materiaPrimeraNom.ifBlank { "-" })
        DetailRow("Quantitat", "${lot.quantitat} ${lot.unitats}")
        DetailRow("Data caducitat", lot.dataCaducitat.ifBlank { "-" })
        DetailRow("Data obertura", lot.dataObertura.ifBlank { "-" })
        DetailRow("Data acabament", lot.dataAcabament.ifBlank { "-" })
        DetailRow("Albarà proveïdor", lot.albaraProveidorId?.toString() ?: "-")
    }

    if (lot.estat == "EN_ESTOC" || lot.estat == "OBERT") {
        LotActionsCard(
            lot = lot,
            loading = loading,
            onIniciarClick = onIniciarClick,
            onFinalitzarClick = onFinalitzarClick
        )
    }
}

@Composable
private fun LotActionsCard(
    lot: Lot,
    loading: Boolean,
    onIniciarClick: () -> Unit,
    onFinalitzarClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = EasyWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, EasyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Text(
                text = "Gestió de lots",
                style = MaterialTheme.typography.titleLarge,
                color = EasyBrownDark,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Les accions requereixen confirmació abans de canviar l'estat del lot.",
                style = MaterialTheme.typography.bodyMedium,
                color = EasyTextSoft
            )

            LotActionHint(lot.estat)

            Spacer(modifier = Modifier.height(2.dp))

            when (lot.estat) {
                "EN_ESTOC" -> {
                    Modifier.EasyPrimaryButton(
                        text = "Iniciar lot",
                        enabled = !loading,
                        onClick = onIniciarClick
                    )
                }

                "OBERT" -> {
                    Modifier.EasyDangerButton(
                        text = "Finalitzar lot",
                        enabled = !loading,
                        onClick = onFinalitzarClick
                    )
                }
            }

            if (loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = EasyBrown,
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )

                    Text(
                        text = "Processant acció...",
                        color = EasyTextSoft,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun LotActionHint(estat: String) {
    val icon = when (estat) {
        "EN_ESTOC" -> Icons.Default.PlayArrow
        else -> Icons.Default.StopCircle
    }

    val text = when (estat) {
        "EN_ESTOC" -> "Aquest lot està en estoc. L'única acció disponible és iniciar-lo."
        "OBERT" -> "Aquest lot està obert. L'única acció disponible és finalitzar-lo."
        else -> "No hi ha accions disponibles."
    }

    val tint = when (estat) {
        "OBERT" -> EasyError
        else -> EasyBrown
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EasyBeige, RoundedCornerShape(14.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .background(EasyWhite, RoundedCornerShape(10.dp))
                .padding(6.dp)
                .size(20.dp)
        )

        Text(
            text = text,
            color = EasyText,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Fila d'informació del detall.
 */
@Composable
fun DetailRow(
    label: String,
    value: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = label,
            color = EasyTextSoft,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = value,
            color = EasyText,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
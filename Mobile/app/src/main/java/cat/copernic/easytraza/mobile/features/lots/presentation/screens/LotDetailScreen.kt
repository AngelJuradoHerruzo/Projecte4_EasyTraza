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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.Button
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
import cat.copernic.easytraza.mobile.ui.components.EasyHeader
import cat.copernic.easytraza.mobile.ui.components.EasyPrimaryButton
import cat.copernic.easytraza.mobile.ui.components.EasySecondaryButton
import cat.copernic.easytraza.mobile.ui.components.EasyStatusBadge
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBeigeLight
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
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

    if (mostrarConfirmacioIniciar && uiState.lotSeleccionat != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacioIniciar = false },
            title = {
                Text("Confirmar inici")
            },
            text = {
                Text("Vols iniciar aquest lot?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacioIniciar = false
                        viewModel.iniciarLot(uiState.lotSeleccionat!!)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EasyBrown,
                        contentColor = EasyWhite
                    )
                ) {
                    Text("Iniciar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarConfirmacioIniciar = false },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel·lar")
                }
            }
        )
    }

    if (mostrarConfirmacioFinalitzar && uiState.lotSeleccionat != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacioFinalitzar = false },
            title = {
                Text("Confirmar finalització")
            },
            text = {
                Text("Vols finalitzar aquest lot?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        mostrarConfirmacioFinalitzar = false
                        viewModel.finalitzarLot(uiState.lotSeleccionat!!)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EasyError,
                        contentColor = EasyWhite
                    )
                ) {
                    Text("Finalitzar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { mostrarConfirmacioFinalitzar = false },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel·lar")
                }
            }
        )
    }

    if (uiState.mostrarConfirmacioInici) {
        AlertDialog(
            onDismissRequest = { viewModel.cancelLarConfirmacioInici() },
            title = {
                Text("Lot obert existent")
            },
            text = {
                Text(
                    uiState.missatge
                        ?: "Ja hi ha un lot obert d'aquesta matèria primera. Si continues, es finalitzarà el lot anterior i s'iniciarà aquest."
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmarIniciLot() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EasyBrown,
                        contentColor = EasyWhite
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.cancelLarConfirmacioInici() },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel·lar")
                }
            }
        )
    }

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

            IconButton(onClick = onTornarClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Tornar",
                    tint = EasyBrownDark
                )
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                EasyHeader(
                    title = "Detall del lot",
                    subtitle = "Informació i gestió",
                    showConfig = true,
                    onConfiguracioClick = onConfiguracioClick
                )
            }
        }

        if (uiState.error != null) {
            Text(
                text = uiState.error ?: "",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (uiState.missatge != null && !uiState.mostrarConfirmacioInici) {
            Text(
                text = uiState.missatge ?: "",
                color = EasyBrown,
                fontWeight = FontWeight.SemiBold
            )
        }

        when {
            uiState.carregant && uiState.lotSeleccionat == null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EasyBrown)
                }
            }

            uiState.lotSeleccionat != null -> {
                LotDetailContent(
                    lot = uiState.lotSeleccionat!!,
                    onIniciarClick = {
                        mostrarConfirmacioIniciar = true
                    },
                    onFinalitzarClick = {
                        mostrarConfirmacioFinalitzar = true
                    }
                )
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
    onIniciarClick: () -> Unit,
    onFinalitzarClick: () -> Unit
) {

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
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

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .background(EasyBeige, RoundedCornerShape(18.dp)),
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

                DetailRow("Matèria primera", lot.materiaPrimeraNom)
                DetailRow("Quantitat", "${lot.quantitat} ${lot.unitats}")
                DetailRow("Data caducitat", lot.dataCaducitat.ifBlank { "-" })
                DetailRow("Data obertura", lot.dataObertura.ifBlank { "-" })
                DetailRow("Data acabament", lot.dataAcabament.ifBlank { "-" })
                DetailRow("Albarà proveïdor", lot.albaraProveidorId?.toString() ?: "-")
            }
        }

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

            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Text(
                    text = "Gestió de lots",
                    style = MaterialTheme.typography.titleLarge,
                    color = EasyBrownDark,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Gestiona el lot segons l'estat actual.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EasyTextSoft
                )

                Spacer(modifier = Modifier.height(2.dp))

                Modifier.EasyPrimaryButton(
                    text = "Iniciar lot",
                    enabled = lot.estat == "EN_ESTOC",
                    onClick = onIniciarClick
                )

                Modifier.EasySecondaryButton(
                    text = "Finalitzar lot",
                    enabled = lot.estat == "OBERT",
                    onClick = onFinalitzarClick
                )

                if (lot.estat == "ACABAT") {
                    Text(
                        text = "Aquest lot ja està acabat i no admet més accions.",
                        color = EasyTextSoft,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
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

    Column(
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
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
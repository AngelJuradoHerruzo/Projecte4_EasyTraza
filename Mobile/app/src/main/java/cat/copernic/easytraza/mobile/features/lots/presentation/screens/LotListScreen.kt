package cat.copernic.easytraza.mobile.features.lots.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.R
import cat.copernic.easytraza.mobile.features.lots.domain.models.Lot
import cat.copernic.easytraza.mobile.features.lots.presentation.viewmodels.LotViewModel
import cat.copernic.easytraza.mobile.ui.components.EasyButtonShape
import cat.copernic.easytraza.mobile.ui.components.EasyCardShape
import cat.copernic.easytraza.mobile.ui.components.EasyHeader
import cat.copernic.easytraza.mobile.ui.components.EasyMessageCard
import cat.copernic.easytraza.mobile.ui.components.EasyScreen
import cat.copernic.easytraza.mobile.ui.components.EasySecondaryButton
import cat.copernic.easytraza.mobile.ui.components.EasyStatusBadge
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyCardBorder
import cat.copernic.easytraza.mobile.ui.theme.EasyText
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

private const val TOTS = "Tots"

private enum class LotSortOption(@StringRes val labelRes: Int) {
    IDENTIFICADOR_ASC(R.string.lots_sort_identifier_asc),
    IDENTIFICADOR_DESC(R.string.lots_sort_identifier_desc),
    ESTAT(R.string.lots_sort_state),
    MATERIA(R.string.lots_sort_raw_material),
    DATA_CADUCITAT(R.string.lots_sort_expiration)
}


/**
 * ELEMENT DEL LLISTAT DE LOTS.
 *
 * Mostrada la informació principal d'un lot i habilitada
 * la selecció per accedir al seu detall.
 *
 * @param lot lot que s'ha de representar
 * @param onClick acció executada en seleccionar el lot
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

    var filtresPlegats by remember { mutableStateOf(true) }

    var filtreIdentificador by remember { mutableStateOf("") }
    var filtreEstat by remember { mutableStateOf(TOTS) }
    var filtreMateria by remember { mutableStateOf(TOTS) }
    var filtreData by remember { mutableStateOf("") }
    var ordre by remember { mutableStateOf(LotSortOption.MATERIA) }

    val senseMateria = stringResource(R.string.lots_no_raw_material)

    val materies = remember(uiState.lots) {
        listOf(TOTS) + uiState.lots
            .map { it.materiaPrimeraNom.ifBlank { senseMateria } }
            .distinct()
            .sorted()
    }

    val lotsFiltrats = uiState.lots
        .filter { lot ->
            filtreIdentificador.isBlank() ||
                    lot.identificadorLot.contains(filtreIdentificador.trim(), ignoreCase = true)
        }
        .filter { lot -> filtreEstat == TOTS || lot.estat == filtreEstat }
        .filter { lot ->
            filtreMateria == TOTS ||
                    lot.materiaPrimeraNom.ifBlank { senseMateria } == filtreMateria
        }
        .filter { lot ->
            val data = filtreData.trim()
            data.isBlank() ||
                    lot.dataCaducitat.contains(data, ignoreCase = true) ||
                    lot.dataObertura.contains(data, ignoreCase = true) ||
                    lot.dataAcabament.contains(data, ignoreCase = true)
        }
        .let { lots ->
            when (ordre) {
                LotSortOption.IDENTIFICADOR_ASC -> lots.sortedBy {
                    it.identificadorLot.lowercase()
                }

                LotSortOption.IDENTIFICADOR_DESC -> lots.sortedByDescending {
                    it.identificadorLot.lowercase()
                }

                LotSortOption.ESTAT -> lots.sortedWith(
                    compareBy<Lot> { estatOrdre(it.estat) }
                        .thenBy { it.identificadorLot.lowercase() }
                )

                LotSortOption.MATERIA -> lots.sortedWith(
                    compareBy<Lot> { it.materiaPrimeraNom.lowercase() }
                        .thenBy { it.identificadorLot.lowercase() }
                )

                LotSortOption.DATA_CADUCITAT -> lots.sortedBy {
                    it.dataCaducitat.ifBlank { "9999-99-99" }
                }
            }
        }

    val lotsAgrupats = lotsFiltrats
        .groupBy { lot -> lot.materiaPrimeraNom.ifBlank { senseMateria } }
        .toSortedMap()

    EasyScreen {
        EasyHeader(
            title = stringResource(R.string.lots_title),
            subtitle = stringResource(R.string.lots_subtitle),
            showConfig = true,
            onConfiguracioClick = onConfiguracioClick,
            actions = {
                IconButton(onClick = { viewModel.carregarLots() }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.lots_reload),
                        tint = EasyBrown
                    )
                }
            }
        )

        LotFiltersCard(
            filtreIdentificador = filtreIdentificador,
            onFiltreIdentificadorChange = { filtreIdentificador = it },
            filtreEstat = filtreEstat,
            onFiltreEstatChange = { filtreEstat = it },
            filtreMateria = filtreMateria,
            onFiltreMateriaChange = { filtreMateria = it },
            filtreData = filtreData,
            onFiltreDataChange = { filtreData = it },
            materies = materies,
            ordre = ordre,
            onOrdreChange = { ordre = it },
            total = lotsFiltrats.size,
            plegat = filtresPlegats,
            onTogglePlegat = { filtresPlegats = !filtresPlegats },
            onNetejar = {
                filtreIdentificador = ""
                filtreEstat = TOTS
                filtreMateria = TOTS
                filtreData = ""
                ordre = LotSortOption.MATERIA
            }
        )

        uiState.error?.let { EasyMessageCard(text = it, isError = true) }
        uiState.missatge?.let { EasyMessageCard(text = it) }

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
                EmptyLotsMessage(text = stringResource(R.string.lots_none))
            }

            lotsFiltrats.isEmpty() -> {
                EmptyLotsMessage(text = stringResource(R.string.lots_no_match))
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
            text = stringResource(R.string.main_change_user),
            onClick = onSortirClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
/**
 * FILTRES DEL LLISTAT DE LOTS.
 *
 * Presentats els camps de cerca i selecció utilitzats per filtrar
 * i ordenar els lots mostrats a la pantalla.
 *
 * @param searchText text utilitzat per filtrar els lots
 * @param onSearchTextChange acció executada quan canvia el text de cerca
 * @param selectedEstat estat seleccionat per filtrar els lots
 * @param onEstatChange acció executada quan canvia l'estat seleccionat
 * @param selectedMateria matèria primera seleccionada com a filtre
 * @param onMateriaChange acció executada quan canvia la matèria seleccionada
 * @param selectedOrder ordre seleccionat per presentar els lots
 * @param onOrderChange acció executada quan canvia el criteri d'ordre
 * @param materias llista de matèries primeres disponibles
 */
@Composable
private fun LotFiltersCard(
    filtreIdentificador: String,
    onFiltreIdentificadorChange: (String) -> Unit,
    filtreEstat: String,
    onFiltreEstatChange: (String) -> Unit,
    filtreMateria: String,
    onFiltreMateriaChange: (String) -> Unit,
    filtreData: String,
    onFiltreDataChange: (String) -> Unit,
    materies: List<String>,
    ordre: LotSortOption,
    onOrdreChange: (LotSortOption) -> Unit,
    total: Int,
    plegat: Boolean,
    onTogglePlegat: () -> Unit,
    onNetejar: () -> Unit
) {
    var estatExpanded by remember { mutableStateOf(false) }
    var materiaExpanded by remember { mutableStateOf(false) }
    var ordreExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = EasyCardShape,
        colors = CardDefaults.cardColors(containerColor = EasyWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, EasyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTogglePlegat() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = stringResource(R.string.lots_filters),
                    tint = EasyBrown
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.lots_filters_and_sort),
                        color = EasyBrownDark,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = resumFiltres(
                            filtreIdentificador = filtreIdentificador,
                            filtreEstat = filtreEstat,
                            filtreMateria = filtreMateria,
                            filtreData = filtreData,
                            ordre = ordre,
                            total = total
                        ),
                        color = EasyTextSoft,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = if (plegat) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = if (plegat) stringResource(R.string.lots_expand_filters) else stringResource(R.string.lots_collapse_filters),
                    tint = EasyBrown
                )
            }

            if (!plegat) {
                OutlinedTextField(
                    value = filtreIdentificador,
                    onValueChange = onFiltreIdentificadorChange,
                    label = { Text(stringResource(R.string.lots_identifier)) },
                    singleLine = true,
                    shape = EasyButtonShape,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = estatExpanded,
                    onExpandedChange = { estatExpanded = !estatExpanded }
                ) {
                    OutlinedTextField(
                        value = estatLabel(filtreEstat),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.lots_state)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = estatExpanded)
                        },
                        shape = EasyButtonShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                    )

                    ExposedDropdownMenu(
                        expanded = estatExpanded,
                        onDismissRequest = { estatExpanded = false }
                    ) {
                        listOf(TOTS, "EN_ESTOC", "OBERT", "ACABAT").forEach { estat ->
                            DropdownMenuItem(
                                text = { Text(estatLabel(estat)) },
                                onClick = {
                                    onFiltreEstatChange(estat)
                                    estatExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = materiaExpanded,
                    onExpandedChange = { materiaExpanded = !materiaExpanded }
                ) {
                    OutlinedTextField(
                        value = filtreMateria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.lots_raw_material)) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = materiaExpanded)
                        },
                        shape = EasyButtonShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                    )

                    ExposedDropdownMenu(
                        expanded = materiaExpanded,
                        onDismissRequest = { materiaExpanded = false }
                    ) {
                        materies.forEach { materia ->
                            DropdownMenuItem(
                                text = { Text(if (materia == TOTS) stringResource(R.string.lots_all_raw_materials) else materia) },
                                onClick = {
                                    onFiltreMateriaChange(materia)
                                    materiaExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = filtreData,
                    onValueChange = onFiltreDataChange,
                    label = { Text(stringResource(R.string.lots_date)) },
                    placeholder = { Text(stringResource(R.string.lots_date_placeholder)) },
                    singleLine = true,
                    shape = EasyButtonShape,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = ordreExpanded,
                    onExpandedChange = { ordreExpanded = !ordreExpanded }
                ) {
                    OutlinedTextField(
                        value = stringResource(ordre.labelRes),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.lots_sort_by)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Sort,
                                contentDescription = null,
                                tint = EasyBrown
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = ordreExpanded)
                        },
                        shape = EasyButtonShape,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(
                                type = MenuAnchorType.PrimaryNotEditable,
                                enabled = true
                            )
                    )

                    ExposedDropdownMenu(
                        expanded = ordreExpanded,
                        onDismissRequest = { ordreExpanded = false }
                    ) {
                        LotSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(stringResource(option.labelRes)) },
                                onClick = {
                                    onOrdreChange(option)
                                    ordreExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.lots_clear_filters),
                    color = EasyBrown,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNetejar() }
                )
            }
        }
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
        shape = EasyCardShape,
        colors = CardDefaults.cardColors(containerColor = EasyWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, EasyCardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EasyBeige, RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                    .clickable { onHeaderClick() }
                    .padding(horizontal = 18.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = materiaPrimera,
                        color = EasyBrownDark,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = stringResource(R.string.lots_group_count, lots.size),
                        color = EasyTextSoft,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Icon(
                    imageVector = if (plegat) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                    contentDescription = if (plegat) stringResource(R.string.lots_expand) else stringResource(R.string.lots_collapse),
                    tint = EasyBrown
                )
            }

            if (!plegat) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(EasyWhite)
                        .padding(vertical = 4.dp)
                ) {
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
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(EasyBeige, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Inventory,
                contentDescription = stringResource(R.string.common_lot),
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
                text = "${lot.quantitat} ${lot.unitats} · ${stringResource(R.string.lots_expiration_short, lot.dataCaducitat.ifBlank { "-" })}",
                color = EasyText,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.size(8.dp))

        EasyStatusBadge(estat = lot.estat)
    }
}


/**
 * MISSATGE SENSE LOTS.
 *
 * Mostrat un missatge informatiu quan no existeixen lots
 * disponibles per als criteris de consulta aplicats.
 *
 * @param text missatge que s'ha de presentar a l'usuari
 */
@Composable
private fun ColumnScope.EmptyLotsMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = EasyTextSoft,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}


/**
 * RESUM DELS FILTRES.
 *
 * Mostrat el resum dels criteris aplicats sobre el llistat
 * per informar l'usuari de la consulta activa.
 *
 * @param total total de lots obtinguts amb els filtres actius
 * @param estat estat seleccionat com a filtre
 * @param materia matèria primera seleccionada com a filtre
 */
@Composable
private fun resumFiltres(
    filtreIdentificador: String,
    filtreEstat: String,
    filtreMateria: String,
    filtreData: String,
    ordre: LotSortOption,
    total: Int
): String {
    val filtresActius = listOf(
        filtreIdentificador.isNotBlank(),
        filtreEstat != TOTS,
        filtreMateria != TOTS,
        filtreData.isNotBlank()
    ).count { it }

    val textFiltres = if (filtresActius == 0) {
        stringResource(R.string.lots_without_filters)
    } else {
        pluralStringResource(R.plurals.lots_active_filters, filtresActius, filtresActius)
    }

    return stringResource(R.string.lots_summary, total, textFiltres, stringResource(ordre.labelRes))
}


/**
 * ETIQUETA DE L'ESTAT.
 *
 * Obtingut el text traduït que correspon a l'estat intern
 * utilitzat per representar un lot.
 *
 * @param estat estat intern del lot
 * @return text visible associat a l'estat
 */
@Composable
private fun estatLabel(estat: String): String = when (estat) {
    TOTS -> stringResource(R.string.lots_all_states)
    "EN_ESTOC" -> stringResource(R.string.lots_status_in_stock)
    "OBERT" -> stringResource(R.string.lots_status_open)
    "ACABAT" -> stringResource(R.string.lots_status_finished)
    else -> estat
}


/**
 * ORDRE DELS ESTATS.
 *
 * Assignat un valor numèric a cada estat del lot
 * per aplicar l'ordenació definida al llistat.
 *
 * @param estat estat intern del lot
 * @return valor utilitzat per ordenar els estats
 */
private fun estatOrdre(estat: String): Int = when (estat) {
    "OBERT" -> 0
    "EN_ESTOC" -> 1
    "ACABAT" -> 2
    else -> 3
}
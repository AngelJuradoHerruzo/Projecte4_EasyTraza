package cat.copernic.easytraza.mobile.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Pantalla principal de l'app mòbil.
 */
@Composable
fun MainScreen(
    onConfigurarIpClick: () -> Unit,
    onSortirClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "EasyTraza Mobile",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Usuari identificat correctament.",
            style = MaterialTheme.typography.bodyMedium
        )

        Button(
            onClick = onConfigurarIpClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Configurar IP")
        }

        Button(
            onClick = onSortirClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sortir")
        }
    }
}
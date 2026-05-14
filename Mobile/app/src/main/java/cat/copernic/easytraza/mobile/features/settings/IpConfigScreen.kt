package cat.copernic.easytraza.mobile.features.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.core.config.IpPreferences
import cat.copernic.easytraza.mobile.core.network.RetrofitClient

/**
 * Pantalla de configuració de la IP del servidor.
 */
@Composable
fun IpConfigScreen(
    onTornarClick: () -> Unit
) {

    val context = LocalContext.current
    var ip by remember { mutableStateOf(IpPreferences.getIp(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1F1F1F))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        OutlinedButton(
            onClick = onTornarClick
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Tornar"
            )
            Text("Tornar")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Configuració del servidor",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White
        )

        Text(
            text = "Introdueix la IP del servidor backend.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFD6D6D6)
        )

        OutlinedTextField(
            value = ip,
            onValueChange = { ip = it },
            label = { Text("IP del servidor") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                IpPreferences.saveIp(context, ip)
                RetrofitClient.reset()
                ip = IpPreferences.getIp(context)

                Toast.makeText(
                    context,
                    "IP guardada correctament",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}
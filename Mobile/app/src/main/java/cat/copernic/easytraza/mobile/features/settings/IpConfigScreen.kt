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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.core.config.IpPreferences
import cat.copernic.easytraza.mobile.core.network.RetrofitClient
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBeigeLight
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

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
            .background(EasyBeigeLight)
            .padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        OutlinedButton(
            onClick = onTornarClick
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Tornar"
            )
            Text("Tornar")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(
                containerColor = EasyWhite
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {

            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configuració",
                    tint = EasyBrown,
                    modifier = Modifier
                        .background(EasyBeige, RoundedCornerShape(18.dp))
                        .padding(12.dp)
                )

                Text(
                    text = "Configuració del servidor",
                    style = MaterialTheme.typography.headlineSmall,
                    color = EasyBrownDark
                )

                Text(
                    text = "Introdueix la IP del servidor backend per connectar l'app amb EasyTraza.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = EasyTextSoft
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

        Spacer(modifier = Modifier.weight(1f))
    }
}
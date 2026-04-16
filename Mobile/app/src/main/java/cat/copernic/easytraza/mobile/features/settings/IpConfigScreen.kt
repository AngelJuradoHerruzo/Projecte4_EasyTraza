package cat.copernic.easytraza.mobile.features.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.core.config.IpPreferences

@Composable
fun IpConfigScreen() {

    val context = LocalContext.current // Obtenim el context de l'aplicació (necessari per accedir a SharedPreferences)
    var ip by remember { mutableStateOf(IpPreferences.getIp(context)) } // Variable d'estat que conté la IP actual

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Títol de la pantalla
        Text(text = "Configuración de la IP del servidor")

        // Camp de text per introduir la IP
        OutlinedTextField(
            value = ip,                                 // Valor actual
            onValueChange = { ip = it },                // Actualitza la variable quan l'usuari escriu
            label = { Text("IP del servidor") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Botó per guardar la IP
        Button(
            onClick = {
                IpPreferences.saveIp(context, ip) // Desa la IP introduïda a SharedPreferences
                ip = IpPreferences.getIp(context) // Torna a llegir la IP guardada (ja formatejada)

                // Mostra un missatge confirmant que s'ha guardat
                Toast.makeText(
                    context,
                    "IP guardada correctamente",
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar")
        }
    }
}
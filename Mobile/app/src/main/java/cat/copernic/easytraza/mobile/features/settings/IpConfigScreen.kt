package cat.copernic.easytraza.mobile.features.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.R
import cat.copernic.easytraza.mobile.core.config.IpPreferences
import cat.copernic.easytraza.mobile.core.network.RetrofitClient
import cat.copernic.easytraza.mobile.ui.components.EasyButtonShape
import cat.copernic.easytraza.mobile.ui.components.EasyCard
import cat.copernic.easytraza.mobile.ui.components.EasyHeader
import cat.copernic.easytraza.mobile.ui.components.EasyPrimaryButton
import cat.copernic.easytraza.mobile.ui.components.EasyScreen
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft

/**
 * CONFIGURACIÓ DEL SERVIDOR.
 *
 * Mostrat el formulari de configuració de l'adreça IPv4
 * del backend i desats els canvis indicats per l'usuari.
 *
 * @param onTornarClick acció executada en tornar a la pantalla anterior
 */
@Composable
fun IpConfigScreen(
    onTornarClick: () -> Unit
) {
    val context = LocalContext.current
    var ip by remember { mutableStateOf(IpPreferences.getAddress(context)) }

    EasyScreen {
        EasyHeader(
            title = stringResource(R.string.server_title),
            subtitle = stringResource(R.string.ip_subtitle),
            showBack = true,
            onBackClick = onTornarClick,
            showConfig = false
        )

        EasyCard {
            Icon(
                imageVector = Icons.Default.Dns,
                contentDescription = stringResource(R.string.server_title),
                tint = EasyBrown,
                modifier = Modifier
                    .background(EasyBeige, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            )

            Text(
                text = stringResource(R.string.ip_backend),
                style = MaterialTheme.typography.titleLarge,
                color = EasyBrownDark,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = stringResource(R.string.ip_help),
                style = MaterialTheme.typography.bodyMedium,
                color = EasyTextSoft
            )

            OutlinedTextField(
                value = ip,
                onValueChange = { ip = it },
                label = { Text(stringResource(R.string.ip_label)) },
                singleLine = true,
                shape = EasyButtonShape,
                modifier = Modifier.fillMaxWidth()
            )

            Modifier.EasyPrimaryButton(
                text = stringResource(R.string.common_save)
            ) {
                IpPreferences.saveIp(context, ip)
                RetrofitClient.reset()
                ip = IpPreferences.getAddress(context)

                Toast.makeText(
                    context,
                    context.getString(R.string.ip_saved),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

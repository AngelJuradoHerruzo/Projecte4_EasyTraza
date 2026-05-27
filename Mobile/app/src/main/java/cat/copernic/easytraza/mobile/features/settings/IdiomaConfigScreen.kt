package cat.copernic.easytraza.mobile.features.settings

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cat.copernic.easytraza.mobile.R
import cat.copernic.easytraza.mobile.core.config.LanguagePreferences
import cat.copernic.easytraza.mobile.ui.components.EasyCardShape
import cat.copernic.easytraza.mobile.ui.components.EasyHeader
import cat.copernic.easytraza.mobile.ui.components.EasyScreen
import cat.copernic.easytraza.mobile.ui.theme.EasyBeige
import cat.copernic.easytraza.mobile.ui.theme.EasyBrown
import cat.copernic.easytraza.mobile.ui.theme.EasyBrownDark
import cat.copernic.easytraza.mobile.ui.theme.EasyCardBorder
import cat.copernic.easytraza.mobile.ui.theme.EasyTextSoft
import cat.copernic.easytraza.mobile.ui.theme.EasyWhite

/**
 * Pantalla de configuració de l'idioma de l'aplicació.
 */
@Composable
fun IdiomaConfigScreen(
    onTornarClick: () -> Unit
) {
    val context = LocalContext.current
    val currentLanguage = LanguagePreferences.getLanguage(context)

    EasyScreen {
        EasyHeader(
            title = stringResource(R.string.language_title),
            subtitle = stringResource(R.string.language_subtitle),
            showBack = true,
            onBackClick = onTornarClick,
            showConfig = false
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = EasyCardShape,
            colors = CardDefaults.cardColors(containerColor = EasyWhite),
            border = androidx.compose.foundation.BorderStroke(1.dp, EasyCardBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = stringResource(R.string.language_title),
                    tint = EasyBrown,
                    modifier = Modifier
                        .background(EasyBeige, RoundedCornerShape(16.dp))
                        .padding(12.dp)
                )

                Text(
                    text = stringResource(R.string.language_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = EasyBrownDark,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = stringResource(R.string.language_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = EasyTextSoft
                )

                LanguageOption(
                    text = stringResource(R.string.language_catalan),
                    selected = currentLanguage == "ca",
                    onClick = {
                        LanguagePreferences.saveLanguage(context, "ca")
                        Toast.makeText(context, LanguagePreferences.applyLocale(context).getString(R.string.language_saved), Toast.LENGTH_SHORT).show()
                        (context as? Activity)?.recreate()
                    }
                )

                LanguageOption(
                    text = stringResource(R.string.language_spanish),
                    selected = currentLanguage == "es",
                    onClick = {
                        LanguagePreferences.saveLanguage(context, "es")
                        Toast.makeText(context, LanguagePreferences.applyLocale(context).getString(R.string.language_saved), Toast.LENGTH_SHORT).show()
                        (context as? Activity)?.recreate()
                    }
                )
            }
        }
    }
}

@Composable
private fun LanguageOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (selected) EasyBeige else EasyWhite),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) EasyBrown else EasyCardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = EasyBrownDark,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

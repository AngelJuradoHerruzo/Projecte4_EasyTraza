package cat.copernic.easytraza.mobile

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import cat.copernic.easytraza.mobile.features.settings.IpConfigScreen
import cat.copernic.easytraza.mobile.ui.theme.MobileTheme

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MobileTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { IpConfigScreen() }
            }
        }
    }
}
package com.tuapp.billetera
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tuapp.billetera.ui.WalletScreen
import com.tuapp.billetera.ui.WalletViewModel
import com.tuapp.billetera.ui.theme.BilleteraMovilTheme
import android.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val barColor = Color.rgb(244, 246, 250)
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(barColor, barColor),
            navigationBarStyle = SystemBarStyle.light(barColor, barColor))
        setContent {
            val vm: WalletViewModel = viewModel()
            BilleteraMovilTheme { WalletScreen(vm) }
        }
    }
}

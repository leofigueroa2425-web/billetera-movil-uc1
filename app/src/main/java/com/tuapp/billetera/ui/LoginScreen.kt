package com.tuapp.billetera.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.tuapp.billetera.ui.theme.*

@Composable
fun LoginScreen(state: WalletState, onLogin: (String, String) -> Unit) {
    var account by rememberSaveable { mutableStateOf("") }
    // Password is deliberately not saved in the instance-state bundle.
    var password by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }
    val canSubmit = account.isNotBlank() && password.isNotBlank() && !state.busy
    Column(Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Spacer(Modifier.height(12.dp))
        BrandMark()
        Box(Modifier.fillMaxWidth().height(182.dp).clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(BankNavy, Color(0xFF284A75)))) ) {
            Box(Modifier.size(180.dp).offset(x = 210.dp, y = (-60).dp).background(Color.White.copy(alpha=.05f), CircleShape))
            Box(Modifier.size(100.dp).align(Alignment.BottomEnd).offset(x=25.dp,y=35.dp).background(BankYellow, CircleShape))
            Column(Modifier.padding(24.dp).align(Alignment.CenterStart)) {
                Text("Más simple.\nMás cerca de ti.", color=Color.White, style=MaterialTheme.typography.headlineMedium, fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                Text("Tu dinero en un solo lugar.", color=Color.White.copy(alpha=.75f), style=MaterialTheme.typography.bodyMedium)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Bienvenido a tu billetera", style=MaterialTheme.typography.headlineSmall, fontWeight=FontWeight.Bold)
            Text("Ingresa tus datos para empezar.", color=BankMuted)
        }
        Column(verticalArrangement=Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(value=account,onValueChange={ account=it },modifier=Modifier.fillMaxWidth(),
                label={Text("Cuenta")},placeholder={Text("Ingresa tu número de cuenta")},singleLine=true,enabled=!state.busy,
                leadingIcon={BankIcon(BankSymbol.User)},shape=RoundedCornerShape(14.dp),
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number,imeAction=ImeAction.Next))
            OutlinedTextField(value=password,onValueChange={password=it},modifier=Modifier.fillMaxWidth(),
                label={Text("Contraseña")},placeholder={Text("Ingresa tu contraseña")},singleLine=true,enabled=!state.busy,
                leadingIcon={BankIcon(BankSymbol.Lock)},shape=RoundedCornerShape(14.dp),
                trailingIcon={IconButton(onClick={visible=!visible}) {
                    BankIcon(if(visible) BankSymbol.Hidden else BankSymbol.Eye, description=if(visible) "Ocultar contraseña" else "Mostrar contraseña")
                }},visualTransformation=if(visible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password,imeAction=ImeAction.Done),
                keyboardActions=KeyboardActions(onDone={if(canSubmit) onLogin(account,password)}))
            state.loginError?.let { ErrorBanner(it) }
            Button(onClick={onLogin(account,password)},enabled=canSubmit,modifier=Modifier.fillMaxWidth().height(56.dp),
                shape=RoundedCornerShape(14.dp),colors=ButtonDefaults.buttonColors(containerColor=BankYellow,contentColor=BankNavy)) {
                if(state.busy) {
                    CircularProgressIndicator(Modifier.size(20.dp),strokeWidth=2.dp)
                    Spacer(Modifier.width(12.dp)); Text("Conectando…")
                } else {
                    Text("Iniciar sesión",fontWeight=FontWeight.Bold)
                    Spacer(Modifier.width(12.dp)); BankIcon(BankSymbol.Arrow)
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(bottom=24.dp),horizontalArrangement=Arrangement.Center,
            verticalAlignment=Alignment.CenterVertically) {
            BankIcon(BankSymbol.Lock,Modifier.size(16.dp),BankMuted)
            Spacer(Modifier.width(8.dp))
            Text("Conexión cifrada · Acceso personal",style=MaterialTheme.typography.bodySmall,color=BankMuted)
        }
    }
}

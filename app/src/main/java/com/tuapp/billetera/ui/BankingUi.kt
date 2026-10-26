package com.tuapp.billetera.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tuapp.billetera.ui.theme.*
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class BankSymbol { Wallet, Transfer, Deposit, Withdraw, Service, Menu, Home, History, Logout, Eye, Hidden, Lock, User, Arrow, Check, Close }

enum class WalletOperation(val title: String, val subtitle: String, val symbol: BankSymbol, val success: String) {
    Transfer("Transferir", "Envía a otra cuenta", BankSymbol.Transfer, "Transferencia realizada"),
    Deposit("Depositar", "Recarga tu billetera", BankSymbol.Deposit, "Depósito realizado"),
    Withdraw("Retirar", "Dispón de tu saldo", BankSymbol.Withdraw, "Retiro realizado"),
    Service("Pagar servicios", "Luz, agua e internet", BankSymbol.Service, "Pago realizado")
}

fun money(value: Double): String = "$ " + NumberFormat.getNumberInstance(Locale("es", "PE")).apply {
    minimumFractionDigits = 2; maximumFractionDigits = 2
}.format(value)

fun readableDate(value: String): String = runCatching {
    DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", Locale("es", "ES"))
        .withZone(ZoneId.systemDefault()).format(Instant.parse(value))
}.getOrDefault(value)

@Composable
fun BankIcon(symbol: BankSymbol, modifier: Modifier = Modifier.size(24.dp), tint: Color = BankNavy, description: String? = null) {
    Canvas(modifier.semantics { if (description != null) contentDescription = description }) {
        scale(size.width / 24f, size.height / 24f, pivot = Offset.Zero) {
            val stroke = Stroke(1.7f, cap = StrokeCap.Round)
            fun line(x: Float, y: Float, x2: Float, y2: Float) = drawLine(tint, Offset(x, y), Offset(x2, y2), 1.7f, StrokeCap.Round)
            fun box(x: Float, y: Float, w: Float, h: Float) = drawRoundRect(tint, Offset(x,y), Size(w,h), CornerRadius(2f), style = stroke)
            fun arrow(up: Boolean, x: Float = 12f) {
                if (up) { line(x, 17f, x, 6f); line(x,6f,x-4f,10f); line(x,6f,x+4f,10f) }
                else { line(x,6f,x,17f); line(x,17f,x-4f,13f); line(x,17f,x+4f,13f) }
            }
            when (symbol) {
                BankSymbol.Wallet -> { box(3f,5f,18f,15f); box(14f,10f,7f,6f); drawCircle(tint,1f,Offset(17f,13f)) }
                BankSymbol.Menu -> { line(4f,6f,20f,6f); line(4f,12f,20f,12f); line(4f,18f,15f,18f) }
                BankSymbol.Transfer -> { line(3f,7f,20f,7f); line(20f,7f,16f,3f); line(20f,7f,16f,11f); line(21f,17f,4f,17f); line(4f,17f,8f,13f); line(4f,17f,8f,21f) }
                BankSymbol.Deposit -> { arrow(false); line(4f,18f,4f,21f); line(4f,21f,20f,21f); line(20f,21f,20f,18f) }
                BankSymbol.Withdraw -> { arrow(true); line(4f,18f,4f,21f); line(4f,21f,20f,21f); line(20f,21f,20f,18f) }
                BankSymbol.Service -> {
                    val p = Path().apply { moveTo(13f,2f); lineTo(5f,13f); lineTo(11f,13f); lineTo(10f,22f); lineTo(19f,10f); lineTo(13f,10f); close() }
                    drawPath(p,tint,style=stroke)
                }
                BankSymbol.Home -> { line(3f,11f,12f,3f); line(12f,3f,21f,11f); line(5f,10f,5f,21f); line(5f,21f,19f,21f); line(19f,21f,19f,10f); box(10f,14f,4f,7f) }
                BankSymbol.History -> { drawCircle(tint,9f,Offset(12f,12f),style=stroke); line(12f,6f,12f,12f); line(12f,12f,16f,14f) }
                BankSymbol.Logout -> { line(10f,4f,4f,4f); line(4f,4f,4f,20f); line(4f,20f,10f,20f); line(9f,12f,21f,12f); line(21f,12f,17f,8f); line(21f,12f,17f,16f) }
                BankSymbol.Eye, BankSymbol.Hidden -> {
                    val p=Path().apply { moveTo(2f,12f); quadraticTo(12f,-1f,22f,12f); quadraticTo(12f,25f,2f,12f) }
                    drawPath(p,tint,style=stroke); drawCircle(tint,3f,Offset(12f,12f),style=stroke)
                    if(symbol==BankSymbol.Hidden) line(3f,3f,21f,21f)
                }
                BankSymbol.Lock -> { box(5f,10f,14f,11f); drawArc(tint,180f,180f,false,Offset(8f,2f),Size(8f,14f),style=stroke); line(12f,14f,12f,17f) }
                BankSymbol.User -> { drawCircle(tint,4f,Offset(12f,7f),style=stroke); drawArc(tint,180f,180f,false,Offset(4f,13f),Size(16f,14f),style=stroke) }
                BankSymbol.Arrow -> { line(5f,12f,19f,12f); line(19f,12f,14f,7f); line(19f,12f,14f,17f) }
                BankSymbol.Check -> { line(5f,12f,10f,17f); line(10f,17f,20f,6f) }
                BankSymbol.Close -> { line(6f,6f,18f,18f); line(6f,18f,18f,6f) }
            }
        }
    }
}

@Composable
fun BrandMark(compact: Boolean = false) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Box(Modifier.size(if(compact) 38.dp else 48.dp).background(BankYellow, RoundedCornerShape(14.dp)), contentAlignment = androidx.compose.ui.Alignment.Center) {
            BankIcon(BankSymbol.Wallet, Modifier.size(if(compact) 23.dp else 28.dp))
        }
        Column {
            Text("mi billetera", style = if(compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
            Text("TU DINERO, CONTIGO", style = MaterialTheme.typography.labelSmall, color = BankMuted)
        }
    }
}

@Composable
fun ErrorBanner(message: String) {
    Surface(color = BankRed.copy(alpha = .07f), shape = RoundedCornerShape(12.dp)) {
        Text(message, Modifier.padding(14.dp), color = BankRed, style = MaterialTheme.typography.bodyMedium)
    }
}

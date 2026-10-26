package com.tuapp.billetera.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tuapp.billetera.data.local.TransactionEntity
import com.tuapp.billetera.ui.theme.*

private fun TransactionEntity.isIncome() = type == "DEPOSITO" || type == "TRANSFERENCIA_RECIBIDA"

@Composable
fun HistoryScreen(history: List<TransactionEntity>, modifier: Modifier = Modifier) {
    var filter by rememberSaveable { mutableStateOf("Todos") }
    val filtered = history.filter { tx -> when(filter) {
        "Ingresos" -> tx.isIncome()
        "Egresos" -> !tx.isIncome()
        "Servicios" -> tx.type == "SERVICIO"
        else -> true
    } }
    LazyColumn(modifier.fillMaxSize(),contentPadding=PaddingValues(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
        item {
            Text("Tu actividad, al día",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
            Text("Consulta los comprobantes guardados en este dispositivo.",style=MaterialTheme.typography.bodyMedium,color=BankMuted,modifier=Modifier.padding(top=8.dp))
        }
        item {
            Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                listOf("Todos","Ingresos","Egresos","Servicios").forEach { label ->
                    FilterChip(selected=filter==label,onClick={filter=label},label={Text(label)},
                        colors=FilterChipDefaults.filterChipColors(selectedContainerColor=BankYellow,selectedLabelColor=BankNavy))
                }
            }
        }
        if(filtered.isEmpty()) item { EmptyHistory(filter!="Todos") }
        else items(filtered,key={it.id}) { TransactionRow(it) }
    }
}

@Composable
fun TransactionRow(tx: TransactionEntity) {
    val income=tx.isIncome()
    val color=if(income) BankGreen else BankRed
    val title=when(tx.type) {
        "DEPOSITO"->"Depósito"
        "RETIRO"->"Retiro"
        "TRANSFERENCIA_ENVIADA"->"Transferencia enviada"
        "TRANSFERENCIA_RECIBIDA"->"Transferencia recibida"
        "SERVICIO"->"Servicio básico"
        else->tx.type.replace('_',' ').lowercase().replaceFirstChar { it.uppercase() }
    }
    Surface(color=Color.White,shape=RoundedCornerShape(18.dp)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(42.dp).background(color.copy(alpha=.08f),RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center) {
                    BankIcon(if(tx.type=="SERVICIO") BankSymbol.Service else if(income) BankSymbol.Deposit else BankSymbol.Withdraw,tint=color)
                }
                Column(Modifier.weight(1f)) {
                    Text(title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.SemiBold)
                    val detail=listOf(tx.service,tx.detail).filter{it.isNotBlank()}.joinToString(" · ")
                    if(detail.isNotEmpty()) Text(detail,style=MaterialTheme.typography.bodySmall,color=BankMuted)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                Text(readableDate(tx.dateIso),style=MaterialTheme.typography.labelSmall,color=BankMuted,modifier=Modifier.weight(1f))
                Text((if(income) "+ " else "− ")+money(tx.amount),color=color,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptyHistory(filtered: Boolean = false) {
    Surface(color=Color.White,shape=RoundedCornerShape(20.dp),modifier=Modifier.fillMaxWidth()) {
        Column(Modifier.padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(12.dp)) {
            BankIcon(BankSymbol.History,Modifier.size(36.dp),BankMuted)
            Text(if(filtered) "Sin movimientos en esta categoría" else "Aún no tienes movimientos",fontWeight=FontWeight.SemiBold)
            Text("Tus operaciones aparecerán aquí y podrás consultarlas incluso sin conexión.",style=MaterialTheme.typography.bodySmall,color=BankMuted,textAlign=androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

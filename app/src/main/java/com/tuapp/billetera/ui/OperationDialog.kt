package com.tuapp.billetera.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.tuapp.billetera.ui.theme.*
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationDialog(operation: WalletOperation, state: WalletState, onDismiss: () -> Unit, onConfirm: (String, Double) -> Unit) {
    var destination by rememberSaveable(operation) { mutableStateOf("") }
    var amount by rememberSaveable(operation) { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    val contracts = listOf("LUZ-001" to "Luz", "AGUA-02" to "Agua", "NET-10" to "Internet")
    val parsed = amount.replace(',','.').toBigDecimalOrNull()
    val amountError = when {
        parsed == null || parsed <= BigDecimal.ZERO -> "Ingresa un monto mayor que cero."
        parsed > BigDecimal("100000") -> "El monto máximo es 100 000."
        parsed.stripTrailingZeros().scale() > 2 -> "Utiliza como máximo dos decimales."
        else -> null
    }
    val destinationError = when {
        operation==WalletOperation.Transfer && destination.isBlank() -> "Ingresa la cuenta de destino."
        operation==WalletOperation.Transfer && destination.trim()==state.accountId -> "Elige una cuenta diferente a la tuya."
        operation==WalletOperation.Service && destination.isBlank() -> "Selecciona el servicio que deseas pagar."
        else -> null
    }
    AlertDialog(onDismissRequest={if(!state.busy) onDismiss()},properties=DialogProperties(dismissOnBackPress=!state.busy,dismissOnClickOutside=!state.busy),
        shape=RoundedCornerShape(26.dp),icon={BankIcon(operation.symbol,Modifier.size(32.dp))},
        title={Text(when(operation){WalletOperation.Transfer->"Transferir dinero";WalletOperation.Deposit->"Realizar depósito";WalletOperation.Withdraw->"Retirar dinero";WalletOperation.Service->"Pagar un servicio"},fontWeight=FontWeight.Bold)},
        text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(16.dp)) {
            Text(operation.subtitle,color=BankMuted)
            if(operation==WalletOperation.Transfer) OutlinedTextField(value=destination,onValueChange={destination=it},
                label={Text("Cuenta destino")},singleLine=true,enabled=!state.busy,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp),
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),isError=attempted&&destinationError!=null,
                supportingText={if(attempted&&destinationError!=null) Text(destinationError)})
            if(operation==WalletOperation.Service) {
                ExposedDropdownMenuBox(expanded=expanded,onExpandedChange={if(!state.busy) expanded=!expanded}) {
                    OutlinedTextField(value=contracts.firstOrNull{it.first==destination}?.let{"${it.second} · ${it.first}"}.orEmpty(),
                        onValueChange={},readOnly=true,enabled=!state.busy,label={Text("Servicio y contrato")},
                        trailingIcon={ExposedDropdownMenuDefaults.TrailingIcon(expanded)},
                        modifier=Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),shape=RoundedCornerShape(12.dp),
                        isError=attempted&&destinationError!=null,
                        supportingText={if(attempted&&destinationError!=null) Text(destinationError)})
                    ExposedDropdownMenu(expanded=expanded,onDismissRequest={expanded=false}) {
                        contracts.forEach { (code,label) -> DropdownMenuItem(text={Text("$label · $code")},onClick={destination=code;expanded=false}) }
                    }
                }
            }
            OutlinedTextField(value=amount,onValueChange={amount=it},label={Text("Monto ($)")},placeholder={Text("0,00")},
                singleLine=true,enabled=!state.busy,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(12.dp),
                keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Decimal),isError=attempted&&amountError!=null,
                supportingText={if(attempted&&amountError!=null) Text(amountError)})
            state.operationError?.let { ErrorBanner(it) }
            if(state.busy) Text("Procesando tu solicitud…",style=MaterialTheme.typography.bodySmall,color=BankMuted)
        }},
        confirmButton={Button(enabled=!state.busy,onClick={
            attempted=true
            if(amountError==null && destinationError==null) onConfirm(destination,parsed!!.toDouble())
        },shape=RoundedCornerShape(12.dp),colors=ButtonDefaults.buttonColors(containerColor=BankYellow,contentColor=BankNavy)) {
            if(state.busy) {CircularProgressIndicator(Modifier.size(18.dp),strokeWidth=2.dp);Spacer(Modifier.width(8.dp))}
            Text(if(state.busy) "Procesando" else "Confirmar",fontWeight=FontWeight.Bold)
        }},dismissButton={TextButton(enabled=!state.busy,onClick=onDismiss){Text("Cancelar")}})
}

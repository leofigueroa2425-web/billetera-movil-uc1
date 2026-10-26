package com.tuapp.billetera.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tuapp.billetera.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun WalletScreen(vm: WalletViewModel) {
    val state by vm.state.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val connection by vm.connection.collectAsStateWithLifecycle()
    Surface(Modifier.fillMaxSize(), color = BankBackground) {
        Box(Modifier.fillMaxSize().safeDrawingPadding()) {
            if (state.accountId == null) LoginScreen(state, vm::login)
            else key(state.accountId) {
                AuthenticatedWallet(state, history, connection, vm)
            }
        }
    }
}

@Composable
private fun AuthenticatedWallet(
    state: WalletState,
    history: List<com.tuapp.billetera.data.local.TransactionEntity>,
    connection: String,
    vm: WalletViewModel
) {
    val drawer = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    var page by rememberSaveable { mutableStateOf("Inicio") }
    var operation by remember { mutableStateOf<WalletOperation?>(null) }
    var logoutPrompt by remember { mutableStateOf(false) }
    var hideBalance by rememberSaveable { mutableStateOf(false) }
    fun openOperation(value: WalletOperation) { vm.clearOperationError(); operation = value }
    LaunchedEffect(state.notice) {
        state.notice?.let { snackbar.showSnackbar(it); vm.clearNotice() }
    }
    BackHandler(enabled = drawer.isOpen || page != "Inicio") {
        if(drawer.isOpen) scope.launch { drawer.close() } else page = "Inicio"
    }
    ModalNavigationDrawer(drawerState = drawer, drawerContent = {
        ModalDrawerSheet(drawerContainerColor = Color.White, modifier = Modifier.widthIn(max=320.dp)) {
            Column(Modifier.fillMaxHeight().padding(16.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(18.dp)); BrandMark(compact=true)
                Spacer(Modifier.height(24.dp))
                Text("TU CUENTA", style=MaterialTheme.typography.labelSmall, color=BankMuted)
                Text(state.accountId.orEmpty(),style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold,modifier=Modifier.padding(top=6.dp,bottom=20.dp))
                HorizontalDivider(color=BankBorder)
                Spacer(Modifier.height(12.dp))
                listOf("Inicio" to BankSymbol.Home, "Movimientos" to BankSymbol.History).forEach { (label, symbol) ->
                    NavigationDrawerItem(label={Text(label)},selected=page==label,icon={BankIcon(symbol)},onClick={
                        page=label; scope.launch { drawer.close() }
                    },colors=NavigationDrawerItemDefaults.colors(selectedContainerColor=BankYellow.copy(alpha=.35f)))
                }
                Spacer(Modifier.height(12.dp))
                Text("OPERACIONES",style=MaterialTheme.typography.labelSmall,color=BankMuted,modifier=Modifier.padding(16.dp))
                WalletOperation.entries.forEach { action ->
                    NavigationDrawerItem(label={Text(action.title)},selected=false,icon={BankIcon(action.symbol)},onClick={
                        scope.launch { drawer.close() }; openOperation(action)
                    })
                }
                Spacer(Modifier.height(16.dp))
                }
                HorizontalDivider(color=BankBorder)
                NavigationDrawerItem(label={Text("Cerrar sesión",color=BankRed)},selected=false,
                    icon={BankIcon(BankSymbol.Logout,tint=BankRed)},onClick={logoutPrompt=true})
                Text("Mi Billetera · Tu dinero, contigo",style=MaterialTheme.typography.labelSmall,color=BankMuted,modifier=Modifier.padding(16.dp))
            }
        }
    }) {
        Scaffold(containerColor=BankBackground,snackbarHost={SnackbarHost(snackbar)},topBar={
            Row(Modifier.fillMaxWidth().padding(horizontal=12.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick={scope.launch{drawer.open()}}) { BankIcon(BankSymbol.Menu, description="Abrir menú") }
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(if(page=="Inicio") "Hola, bienvenido" else "Tus movimientos",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
                    Text("Cuenta ${state.accountId}",style=MaterialTheme.typography.bodySmall,color=BankMuted)
                }
                IconButton(onClick={logoutPrompt=true}) {BankIcon(BankSymbol.Logout,description="Cerrar sesión")}
            }
        }) { padding ->
            if(page=="Movimientos") {
                HistoryScreen(history,Modifier.padding(padding))
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(start=20.dp,end=20.dp,bottom=28.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                    item { BalanceCard(state.balance,state.accountId.orEmpty(),connection,hideBalance,{hideBalance=!hideBalance}) }
                    item {
                        Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                            Text("¿Qué quieres hacer hoy?",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
                            WalletOperation.entries.chunked(2).forEach { actions ->
                                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                                    actions.forEach { action -> QuickActionCard(action,Modifier.weight(1f)){openOperation(action)} }
                                }
                            }
                        }
                    }
                    item {
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                            Text("Últimos movimientos",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
                            TextButton(onClick={page="Movimientos"}) {Text("Ver todos")}
                        }
                    }
                    if(history.isEmpty()) item { EmptyHistory() }
                    else items(history.take(5),key={it.id}) { TransactionRow(it) }
                    item {
                        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                            BankIcon(BankSymbol.Lock,Modifier.size(14.dp),BankMuted)
                            Spacer(Modifier.width(8.dp)); Text("Tu billetera, siempre contigo",style=MaterialTheme.typography.bodySmall,color=BankMuted)
                        }
                    }
                }
            }
        }
    }
    operation?.let { action ->
        OperationDialog(action,state,onDismiss={if(!state.busy) operation=null},onConfirm={destination,amount ->
            vm.operate(action,destination,amount){operation=null}
        })
    }
    if(logoutPrompt) AlertDialog(onDismissRequest={logoutPrompt=false},icon={BankIcon(BankSymbol.Logout)},
        title={Text("¿Cerrar sesión?")},text={Text(if(state.busy) "Hay una solicitud en curso. Si ya llegó al servidor, podría completarse aunque cierres sesión." else "Podrás ingresar nuevamente con esta u otra cuenta.")},
        confirmButton={TextButton(onClick={logoutPrompt=false;vm.logout()}){Text("Cerrar sesión",color=BankRed)}},
        dismissButton={TextButton(onClick={logoutPrompt=false}){Text("Volver")}})
}

@Composable
private fun BalanceCard(balance: Double, account: String, connection: String, hidden: Boolean, onToggle: () -> Unit) {
    Card(colors=CardDefaults.cardColors(containerColor=BankNavy),shape=RoundedCornerShape(26.dp),modifier=Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp)) {
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Text("MI BILLETERA",color=BankYellow,style=MaterialTheme.typography.labelMedium,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
                BankIcon(BankSymbol.Wallet,tint=BankYellow)
            }
            Spacer(Modifier.height(24.dp))
            Text("Saldo disponible",color=Color.White.copy(alpha=.75f),style=MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment=Alignment.CenterVertically) {
                Text(if(hidden) "$ ••••••" else money(balance),color=Color.White,style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
                IconButton(onClick=onToggle) {BankIcon(if(hidden) BankSymbol.Hidden else BankSymbol.Eye,tint=Color.White,description=if(hidden) "Mostrar saldo" else "Ocultar saldo")}
            }
            Spacer(Modifier.height(20.dp))
            HorizontalDivider(color=Color.White.copy(alpha=.14f))
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
                Text("Cuenta · ${account.takeLast(4)}",color=Color.White.copy(alpha=.8f),style=MaterialTheme.typography.bodySmall)
                Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                    val color = when(connection){"En línea"->Color(0xFF7CE3B3);"Desconectado"->Color(0xFFFF9DA7);else->BankYellow}
                    Box(Modifier.size(7.dp).background(color,CircleShape))
                    Text(connection,color=color,style=MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(action: WalletOperation,modifier: Modifier = Modifier,onClick: () -> Unit) {
    OutlinedCard(onClick=onClick,modifier=modifier,shape=RoundedCornerShape(20.dp),
        colors=CardDefaults.outlinedCardColors(containerColor=Color.White),border=androidx.compose.foundation.BorderStroke(1.dp,BankBorder)) {
        Column(Modifier.fillMaxWidth().padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(44.dp).background(if(action==WalletOperation.Transfer) BankYellow else BankBackground,RoundedCornerShape(14.dp)),contentAlignment=Alignment.Center) {
                BankIcon(action.symbol)
            }
            Text(action.title,style=MaterialTheme.typography.titleSmall,fontWeight=FontWeight.Bold)
            Text(action.subtitle,style=MaterialTheme.typography.bodySmall,color=BankMuted)
        }
    }
}

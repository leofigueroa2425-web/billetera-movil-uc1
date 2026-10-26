package com.tuapp.billetera.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tuapp.billetera.data.local.*
import com.tuapp.billetera.data.remote.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

data class WalletState(
    val accountId: String? = null,
    val balance: Double = 0.0,
    val busy: Boolean = false,
    val loginError: String? = null,
    val operationError: String? = null,
    val notice: String? = null
)

class WalletViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = DatabaseProvider.get(app).txDao()
    private val _state = MutableStateFlow(WalletState())
    val state = _state.asStateFlow()
    private val _history = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val history = _history.asStateFlow()
    private var historyJob: Job? = null
    private var requestJob: Job? = null
    private var session = 0
    private var api: ApiService? = null
    private var socket: WalletSocketManager? = null
    private val _connection = MutableStateFlow("Desconectado")
    val connection = _connection.asStateFlow()
    private var connectionJob: Job? = null

    fun login(account: String, password: String) {
        if (_state.value.busy) return
        if (account.isBlank() || password.isBlank()) {
            _state.update { it.copy(loginError = "Ingresa tu cuenta y contraseña.") }; return
        }
        val id = ++session
        _state.value = WalletState(busy = true)
        requestJob = viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.IO) { ApiClient.api.login(LoginReq(account.trim(), password)) }
                if (id != session) return@launch
                TokenStore.token = result.token
                api = ApiClient.create(result.token)
                _state.value = WalletState(accountId = result.accountId, balance = result.balance)
                historyJob = launch { dao.observeAccount(result.accountId).flowOn(Dispatchers.IO).collect { _history.value = it } }
                val manager = WalletSocketManager(
                    onTransferReceived = { amount, from, balance, date ->
                        viewModelScope.launch {
                            if (id != session) return@launch
                            withContext(Dispatchers.IO) {
                                dao.insert(TransactionEntity(type = "TRANSFERENCIA_RECIBIDA", amount = amount, balanceAfter = balance,
                                    dateIso = date, detail = "De $from", accountId = result.accountId))
                            }
                            if (id == session) _state.update { it.copy(balance = balance, notice = "Recibiste ${money(amount)} de $from") }
                        }
                    },
                    onBalancePush = { balance -> viewModelScope.launch {
                        if (id == session) _state.update { it.copy(balance = balance) }
                    } }
                )
                socket = manager
                connectionJob = launch { manager.status.collect { if (id == session) _connection.value = it } }
                withContext(Dispatchers.IO) { manager.connect(result.token) }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (id == session) _state.value = WalletState(loginError = errorMessage(e, true))
            }
        }
    }

    fun operate(operation: WalletOperation, destination: String, amount: Double, onSuccess: () -> Unit) {
        if (_state.value.busy) return
        val account = _state.value.accountId ?: return
        val client = api ?: return
        val id = session
        _state.update { it.copy(busy = true, operationError = null) }
        requestJob = viewModelScope.launch {
            try {
                val receipt = withContext(Dispatchers.IO) {
                    val r = when (operation) {
                        WalletOperation.Deposit -> client.deposit(AmountReq(amount))
                        WalletOperation.Withdraw -> client.withdraw(AmountReq(amount))
                        WalletOperation.Transfer -> client.transfer(TransferReq(destination.trim(), amount))
                        WalletOperation.Service -> client.payService(PayReq(destination, amount))
                    }
                    dao.insert(TransactionEntity(type = r.type, amount = r.amount, balanceAfter = r.balance, dateIso = r.date,
                        detail = when (operation) {
                            WalletOperation.Transfer -> "A ${destination.trim()}"
                            WalletOperation.Service -> destination
                            else -> ""
                        }, service = r.service.orEmpty(), accountId = account))
                    r
                }
                if (id == session) {
                    _state.update { it.copy(balance = receipt.balance, busy = false, notice = "${operation.success} correctamente") }
                    onSuccess()
                }
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                if (id == session) _state.update { it.copy(busy = false, operationError = errorMessage(e)) }
            }
        }
    }

    fun clearOperationError() { _state.update { it.copy(operationError = null) } }
    fun clearNotice() { _state.update { it.copy(notice = null) } }

    fun logout() {
        session++
        requestJob?.cancel()
        historyJob?.cancel()
        connectionJob?.cancel()
        socket?.release()
        socket = null
        api = null
        TokenStore.token = null
        _history.value = emptyList()
        _connection.value = "Desconectado"
        _state.value = WalletState()
    }

    private fun errorMessage(e: Exception, login: Boolean = false): String = when (e) {
        is HttpException -> {
            val server = runCatching { JSONObject(e.response()?.errorBody()?.string().orEmpty()).optString("error") }.getOrNull()
            when {
                e.code() == 401 -> if (login) "Cuenta o contraseña incorrectas." else "Tu sesión expiró. Cierra sesión e ingresa nuevamente."
                !server.isNullOrBlank() -> server.replaceFirstChar { it.uppercase() }
                else -> "No se pudo completar la solicitud (${e.code()})."
            }
        }
        is IOException -> "No pudimos conectar. Revisa tu conexión e intenta nuevamente."
        else -> "No pudimos completar la operación. Intenta nuevamente."
    }

    override fun onCleared() { logout(); super.onCleared() }
}

package com.tuapp.billetera.data.remote

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WalletSocketManager(
    private val onTransferReceived: (Double, String, Double, String) -> Unit,
    private val onBalancePush: (Double) -> Unit
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val client = OkHttpClient.Builder().pingInterval(25, TimeUnit.SECONDS).build()
    private var socket: WebSocket? = null
    private var retry: Job? = null
    private var generation = 0
    private var attempts = 0
    private var token: String? = null
    private val _status = MutableStateFlow("Desconectado")
    val status: StateFlow<String> = _status

    @Synchronized fun connect(accessToken: String) {
        disconnect()
        token = accessToken
        attempts = 0
        open(generation)
    }

    @Synchronized private fun open(id: Int) {
        val accessToken = token ?: return
        if (id != generation) return
        _status.value = if (attempts == 0) "Conectando" else "Reconectando"
        val url = BASE_URL.replaceFirst("https://", "wss://").replaceFirst("http://", "ws://") + "ws?token=$accessToken"
        socket = client.newWebSocket(Request.Builder().url(url).build(), object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) = synchronized(this@WalletSocketManager) {
                if (id != generation) { webSocket.cancel(); return@synchronized }
                attempts = 0
                _status.value = "En línea"
            }
            override fun onMessage(webSocket: WebSocket, text: String) = synchronized(this@WalletSocketManager) {
                if (id != generation) return@synchronized
                runCatching {
                    val json = JSONObject(text)
                    when (json.optString("event")) {
                        "transfer_received" -> onTransferReceived(json.getDouble("amount"), json.optString("fromAccountId"), json.getDouble("balance"), json.optString("date"))
                        "connected", "deposit", "withdraw", "service_paid" -> onBalancePush(json.getDouble("balance"))
                    }
                }
                Unit
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                reconnect(id, code == 4401 || code == 4403)
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                reconnect(id, response?.code == 401 || response?.code == 403)
            }
        })
    }

    @Synchronized private fun reconnect(id: Int, unauthorized: Boolean) {
        if (id != generation || token == null) return
        _status.value = "Desconectado"
        if (unauthorized || retry?.isActive == true) return
        val delayMs = (1000L shl attempts.coerceAtMost(4)).coerceAtMost(15000)
        attempts++
        retry = scope.launch {
            delay(delayMs)
            synchronized(this@WalletSocketManager) {
                retry = null
                open(id)
            }
        }
    }

    @Synchronized fun disconnect() {
        generation++
        token = null
        retry?.cancel()
        retry = null
        socket?.cancel()
        socket = null
        _status.value = "Desconectado"
    }

    fun release() { disconnect(); scope.cancel(); client.dispatcher.executorService.shutdown() }
}

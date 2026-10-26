package com.tuapp.billetera.data.remote
data class LoginReq(val accountId: String, val password: String)
data class LoginRes(val token: String, val accountId: String, val balance: Double)
data class AmountReq(val amount: Double)
data class TransferReq(val toAccountId: String, val amount: Double)
data class PayReq(val contractCode: String, val amount: Double)
data class Receipt(val type: String, val amount: Double, val balance: Double, val date: String,
                   val toAccountId: String? = null, val contractCode: String? = null, val service: String? = null,
                   val fromAccountId: String? = null)
data class BalanceRes(val accountId: String, val balance: Double)
data class ErrorRes(val error: String)

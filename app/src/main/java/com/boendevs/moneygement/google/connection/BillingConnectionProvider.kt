package com.boendevs.moneygement.google.connection

import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.boendevs.moneygement.google.BillingClientProvider

class BillingConnectionProvider(
    private val billingClientProvider: BillingClientProvider
) {

    private var retryAttempts = MAX_ATTEMPTS
    private var listener: ConnectionListener? = null

    fun setConnectionListener(
        listener: ConnectionListener
    ): BillingConnectionProvider = apply {
        this.listener = listener
    }

    fun connect(): Boolean {
        var isConnected = false
        billingClientProvider.billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                reconnect()
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                isConnected = result.responseCode == BillingClient.BillingResponseCode.OK
                Log.i(TAG, "onBillingSetupFinished code: $isConnected")
                if (isConnected) {
                    isConnected = true
                    listener?.onConnected()
                    return
                }

                reconnect()
            }
        })
        return isConnected
    }

    fun disconnect() {
        billingClientProvider.billingClient.endConnection()
        listener = null
    }

    fun requestReconnect(onConnected: () -> Unit) {
        if (connect()) onConnected()
    }

    private fun reconnect() {
        if (retryAttempts > 0) {
            connect()
            --retryAttempts
            return
        }

        listener?.onErrorConnection()
    }

    interface ConnectionListener {
        fun onConnected()
        fun onErrorConnection()
    }

    companion object {
        private const val MAX_ATTEMPTS = 3
        private val TAG = BillingConnectionProvider::class.java.simpleName
    }
}
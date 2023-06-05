package com.boendevs.moneygement.google

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase

/**
 * This class must be singleton with [applicationContext]
 * */
class BillingClientProvider(context: Context) {

    var billingClient: BillingClient
        private set

    private var onSuccessPayment: ((Purchase) -> Unit)? = null
    private var onCancelledPayment: (() -> Unit)? = null
    private var onErrorPayment: ((errorCode: Int) -> Unit)? = null

    init {
        billingClient = createBillingClientBuilder(context).build()
    }

    fun setSuccessPayment(onSuccess: (Purchase) -> Unit): BillingClientProvider = also {
        it.onSuccessPayment = onSuccess
    }

    fun setCancelledPayment(onCancelled: () -> Unit): BillingClientProvider = also {
        it.onCancelledPayment = onCancelled
    }

    fun setErrorPayment(onError: (Int) -> Unit): BillingClientProvider = also {
        it.onErrorPayment = onError
    }

    private fun createBillingClientBuilder(context: Context) =
        BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                when {
                    billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null -> {
                        for (purchase in purchases) {
                            onSuccessPayment?.invoke(purchase)
                        }
                    }

                    billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED -> {
                        onCancelledPayment?.invoke()
                    }

                    else -> {
                        onErrorPayment?.invoke(billingResult.responseCode)
                    }
                }
            }
            .enablePendingPurchases()
}
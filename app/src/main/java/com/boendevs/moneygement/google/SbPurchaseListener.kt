package com.boendevs.moneygement.google

import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener

class SbPurchaseListener : PurchasesUpdatedListener {
    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        TODO("Not yet implemented")
    }
}

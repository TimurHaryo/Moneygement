package com.boendevs.moneygement.extension

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails

inline fun BillingClient.whenReady(
    crossinline onReady: () -> Unit
) {
    if (isReady) onReady()
}

fun List<ProductDetails.SubscriptionOfferDetails>.firstById(id: String) = run {
    firstOrNull { it.basePlanId.equals(id, true) } ?: firstOrNull()
}

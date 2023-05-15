package com.boendevs.moneygement.util

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.QueryProductDetailsParams

object PaymentUtil {
    val queryProductDetailsParams =
        QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("product_id_example")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build())
            )
            .build()
}
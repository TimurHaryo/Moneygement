package com.boendevs.moneygement.util

import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.QueryProductDetailsParams

object PaymentUtil {
    val sbSubsDayPlanIds = arrayListOf(
        "test-plan-id-day",
        "test-plan-id-auto-3-day",
        "test-plan-id-3-day",
        "test-plan-id-1-week"
    )
    val dummySubsProductIds = arrayListOf("sb_subs_a_day", "sb_subs_paywall")
    val dummyInAppProductIds = arrayListOf("sb_subs_1_week", "sb_subs_1_month")

    fun buildProducts(
        @ProductType productType: String,
        productIds: List<String>
    ): List<QueryProductDetailsParams.Product> {
        val products = arrayListOf<QueryProductDetailsParams.Product>()
        productIds.forEach {
            products.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it)
                    .setProductType(productType)
                    .build()
            )
        }
        return products
    }

    fun buildProductDetailsParams(
        products: List<QueryProductDetailsParams.Product>
    ): QueryProductDetailsParams =
        QueryProductDetailsParams.newBuilder()
            .setProductList(products)
            .build()
}
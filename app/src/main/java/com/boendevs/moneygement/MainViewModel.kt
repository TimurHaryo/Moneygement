package com.boendevs.moneygement

import androidx.lifecycle.ViewModel
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails

class MainViewModel : ViewModel() {

    private val productDetailParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
    private val billingFlowParamsBuilder = BillingFlowParams.newBuilder()

    fun buildBillingParams(productDetails: ProductDetails): BillingFlowParams {
        val productDetailParams = productDetailParamsBuilder
            .setProductDetails(productDetails)
            .setOfferToken(
                productDetails.subscriptionOfferDetails?.getOrNull(0)?.offerToken.orEmpty()
            ) // <-- if any offer available
            .build()

        return billingFlowParamsBuilder
            .setProductDetailsParamsList(listOf(productDetailParams))
            .build()
    }
}
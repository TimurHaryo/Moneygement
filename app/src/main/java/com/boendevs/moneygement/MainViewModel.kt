package com.boendevs.moneygement

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import com.boendevs.moneygement.util.PaymentUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    private val productDetailParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
    private val billingFlowParamsBuilder = BillingFlowParams.newBuilder()

    var productDetails: List<ProductDetails> = emptyList()
        private set

    fun queryProductDetail(billingClient: BillingClient) {
        viewModelScope.launch {
            val params = PaymentUtil.buildProductDetailsParams(
                PaymentUtil.buildProducts(ProductType.SUBS, PaymentUtil.dummySubsProductIds)
            )
            val productDetailsResult = withContext(Dispatchers.IO) {
                billingClient.queryProductDetails(params)
            }
            Log.i(TAG, "queryProductDetail data: ${productDetailsResult.productDetailsList}")
            Log.i(TAG, "queryProductDetail code: ${productDetailsResult.billingResult.responseCode}")
            productDetails = productDetailsResult.productDetailsList.orEmpty()
        }
    }

    fun buildBillingParams(productDetails: ProductDetails?, planId: String): BillingFlowParams? {
        productDetails ?: return null

        val productToken = productDetails
            .subscriptionOfferDetails
            ?.run {
                firstOrNull { it.basePlanId.equals(planId, true) } ?: firstOrNull()
            }
            ?.offerToken
            .orEmpty()
        Log.i(TAG, "buildBillingParams token: ${productDetails.subscriptionOfferDetails?.getOrNull(1)?.offerToken.orEmpty()}")
        Log.i(TAG, "buildBillingParams tokens: $productToken")
        Log.i(TAG, "buildBillingParams ids: ${productDetails.subscriptionOfferDetails?.map { it.basePlanId }}")
        val productDetailParams = productDetailParamsBuilder
            .setProductDetails(productDetails)
            .setOfferToken(productToken)
            .build()

        return billingFlowParamsBuilder
            .setProductDetailsParamsList(listOf(productDetailParams))
            .build()
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }
}
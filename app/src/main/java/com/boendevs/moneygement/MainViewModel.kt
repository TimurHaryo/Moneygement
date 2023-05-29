package com.boendevs.moneygement

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.boendevs.moneygement.util.PaymentUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    private val productDetailParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
    private val billingFlowParamsBuilder = BillingFlowParams.newBuilder()

    private val randomId = listOf("uwu", "qwerty", "sheeshh").random()

    var subscriptionProductDetails: List<ProductDetails> = emptyList()
        private set

    var inAppProductDetails: List<ProductDetails> = emptyList()
        private set

    fun querySubscriptionProductDetail(billingClient: BillingClient) {
        viewModelScope.launch {
            val params = PaymentUtil.buildProductDetailsParams(
                PaymentUtil.buildProducts(ProductType.SUBS, PaymentUtil.dummySubsProductIds)
            )
            val productDetailsResult = withContext(Dispatchers.IO) {
                billingClient.queryProductDetails(params)
            }
            Log.i(TAG, "querySubscriptionProductDetail data: ${productDetailsResult.productDetailsList}")
            Log.i(TAG, "querySubscriptionProductDetail code: ${productDetailsResult.billingResult.responseCode}")
            subscriptionProductDetails = productDetailsResult.productDetailsList.orEmpty()
        }
    }

    fun queryInAppProductDetail(billingClient: BillingClient) {
        viewModelScope.launch {
            val params = PaymentUtil.buildProductDetailsParams(
                PaymentUtil.buildProducts(ProductType.INAPP, PaymentUtil.dummyInAppProductIds)
            )
            val productDetailsResult = withContext(Dispatchers.IO) {
                billingClient.queryProductDetails(params)
            }
            Log.i(TAG, "queryInAppProductDetail data: ${productDetailsResult.productDetailsList}")
            Log.i(TAG, "queryInAppProductDetail code: ${productDetailsResult.billingResult.responseCode}")
            inAppProductDetails = productDetailsResult.productDetailsList.orEmpty()
        }
    }

    fun queryUserInAppPurchases(billingClient: BillingClient) {
        viewModelScope.launch {
            val param = QueryPurchasesParams.newBuilder()
                .setProductType(ProductType.INAPP)
            billingClient.queryPurchasesAsync(param.build()) { result, purchases ->
                Log.i(TAG, "queryUserPurchases code: ${result.responseCode}")
                Log.i(TAG, "queryUserPurchases data: $purchases")
            }
        }
    }

    fun queryUserSubsPurchases(billingClient: BillingClient) {
        viewModelScope.launch {
            val param = QueryPurchasesParams.newBuilder()
                .setProductType(ProductType.SUBS)
            billingClient.queryPurchasesAsync(param.build()) { result, purchases ->
                Log.i(TAG, "queryUserPurchases code: ${result.responseCode}")
                Log.i(TAG, "queryUserPurchases data: $purchases")
            }
        }
    }

    fun acknowledgePurchase(billingClient: BillingClient, token: String) {
        viewModelScope.launch {
            val param = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(token)
            billingClient.acknowledgePurchase(param.build()) { result ->
                Log.i(TAG, "acknowledgePurchase code: ${result.responseCode}")
            }
        }
    }

    fun buildBillingParams(productDetails: ProductDetails?, planId: String): BillingFlowParams? {
        productDetails ?: return null
        Log.i(TAG, "buildBillingParams randomId: $randomId")

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
            .setObfuscatedAccountId(randomId)
            .build()
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }
}
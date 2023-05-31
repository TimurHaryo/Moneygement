package com.boendevs.moneygement.google

import android.util.Log
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.ProductDetailsResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.queryProductDetails
import com.boendevs.moneygement.extension.firstById
import com.boendevs.moneygement.util.PaymentUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

class BillingClientLauncher(
    private val billingClient: BillingClient
) {

    private val productDetailParamsBuilder by lazy {
        BillingFlowParams.ProductDetailsParams.newBuilder()
    }

    private val billingFlowParamsBuilder by lazy {
        BillingFlowParams.newBuilder()
    }

    private val dispatcherIO by lazy { Dispatchers.IO }

    suspend fun queryProductDetails(
        @BillingClient.ProductType productType: String,
        productIds: List<String>
    ): ProductDetailsResult {
        val params = PaymentUtil.buildProductDetailsParams(
            PaymentUtil.buildProducts(productType, productIds)
        )

        return withContext(dispatcherIO) {
            billingClient.queryProductDetails(params)
        }
    }

    fun queryUserPurchases(
        @BillingClient.ProductType productType: String,
        onError: ((Int) -> Unit)? = null,
        onSuccess: (List<Purchase>) -> Unit
    ) {
        val param = QueryPurchasesParams.newBuilder()
            .setProductType(productType)
            .build()
        billingClient.queryPurchasesAsync(param) { result, purchases ->
            if (result.responseCode == BillingResponseCode.OK) {
                onSuccess(purchases)
                return@queryPurchasesAsync
            }

            onError?.invoke(result.responseCode)
        }
    }

    suspend fun billingFlow(
        productDetails: ProductDetails?,
        planId: String,
        accountId: String = ""
    ): Flow<BillingFlowParams> = flow {
        if (productDetails == null) {
            emit(null)
            return@flow
        }

        val productToken = productDetails
            .subscriptionOfferDetails
            ?.firstById(planId)
            ?.offerToken
            .orEmpty()
        Log.i(
            TAG,
            "buildBillingParams token: ${productDetails.subscriptionOfferDetails?.getOrNull(1)?.offerToken.orEmpty()}"
        )
        Log.i(TAG, "buildBillingParams tokens: $productToken")
        Log.i(
            TAG,
            "buildBillingParams ids: ${productDetails.subscriptionOfferDetails?.map { it.basePlanId }}"
        )
        val productDetailParams = productDetailParamsBuilder
            .setProductDetails(productDetails)
            .setOfferToken(productToken)
            .build()

        emit(
            billingFlowParamsBuilder
                .setProductDetailsParamsList(listOf(productDetailParams))
                .setObfuscatedAccountId(accountId)
                .build()
        )
    }.filterNotNull()
        .flowOn(dispatcherIO)

    companion object {
        private val TAG = BillingClientLauncher::class.java.simpleName
    }
}
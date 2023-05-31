package com.boendevs.moneygement

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.ProductDetails
import com.boendevs.moneygement.extension.MutableLiveEvent
import com.boendevs.moneygement.extension.toEvent
import com.boendevs.moneygement.google.BillingClientLauncher
import com.boendevs.moneygement.util.PaymentUtil
import kotlinx.coroutines.launch

class MainViewModel(
    billingClient: BillingClient
) : ViewModel() {

    private val randomId = listOf("uwu", "qwerty", "sheeshh").random()

    var subscriptionProductDetails: List<ProductDetails> = emptyList()
        private set

    var inAppProductDetails: List<ProductDetails> = emptyList()
        private set

    private val billingClientLauncher = BillingClientLauncher(billingClient)

    private val _mainEvent = MutableLiveEvent<MainEvent>()
    val mainEvent get() = _mainEvent

    fun querySubscriptionProductDetail() {
        viewModelScope.launch {
            val productDetailsResult =
                billingClientLauncher.queryProductDetails(ProductType.SUBS, PaymentUtil.dummySubsProductIds)
            Log.i(TAG, "querySubscriptionProductDetail data: ${productDetailsResult.productDetailsList}")
            Log.i(TAG, "querySubscriptionProductDetail code: ${productDetailsResult.billingResult.responseCode}")
            subscriptionProductDetails = productDetailsResult.productDetailsList.orEmpty()
        }
    }

    fun queryInAppProductDetail() {
        viewModelScope.launch {
            val productDetailsResult =
                billingClientLauncher.queryProductDetails(ProductType.INAPP, PaymentUtil.dummyInAppProductIds)
            Log.i(TAG, "queryInAppProductDetail data: ${productDetailsResult.productDetailsList}")
            Log.i(TAG, "queryInAppProductDetail code: ${productDetailsResult.billingResult.responseCode}")
            inAppProductDetails = productDetailsResult.productDetailsList.orEmpty()
        }
    }

    fun queryUserInAppPurchases() {
        viewModelScope.launch {
            billingClientLauncher.queryUserPurchases(
                ProductType.INAPP,
                onError = {
                    Log.i(TAG, "queryUserPurchases code: $it")
                },
                onSuccess = { purchases ->
                    Log.i(TAG, "queryUserPurchases data: $purchases")
                }
            )
        }
    }

    fun queryUserSubsPurchases() {
        viewModelScope.launch {
            billingClientLauncher.queryUserPurchases(
                ProductType.SUBS,
                onError = {
                    Log.i(TAG, "queryUserPurchases code: $it")
                },
                onSuccess = { purchases ->
                    Log.i(TAG, "queryUserPurchases data: $purchases")
                }
            )
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

    fun buildBillingParams(productDetails: ProductDetails?, planId: String) {
        viewModelScope.launch {
            billingClientLauncher.billingFlow(
                productDetails,
                planId,
                randomId
            ).collect { params ->
                _mainEvent.value = MainEvent.SuccessBilling(params).toEvent()
            }
        }
    }

    class Factory(
        private val billingClient: BillingClient
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(billingClient) as T
        }
    }

    companion object {
        private val TAG = MainViewModel::class.java.simpleName
    }
}
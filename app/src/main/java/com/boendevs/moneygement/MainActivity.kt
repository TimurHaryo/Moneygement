package com.boendevs.moneygement

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.boendevs.moneygement.databinding.ActivityMainBinding
import com.boendevs.moneygement.util.PaymentUtil

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private val purchaseUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->
        Log.i(TAG, "billing result: $billingResult | ${billingResult.responseCode}")
        Log.i(TAG, "billing result msg: $purchases")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }

    private val billingClient by lazy {
        BillingClient.newBuilder(this)
            .setListener(purchaseUpdateListener)
            .enablePendingPurchases()
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupBillingConnection()
        setupView()
    }

    override fun onResume() {
        super.onResume()
        viewModel.queryUserInAppPurchases(billingClient)
        viewModel.queryUserSubsPurchases(billingClient)
    }

    private fun setupBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.i(TAG, "onBillingSetupFinished code: ${billingResult.responseCode}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.i(TAG, "onBillingSetupFinished launch")
                    viewModel.querySubscriptionProductDetail(billingClient)
                    viewModel.queryInAppProductDetail(billingClient)
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.i(TAG, "onBillingServiceDisconnected")
                setupBillingConnection() // <--- add some logic. i.e only allowed to retry 3 times
            }
        })
    }

    private fun setupView() {
        binding.btnPayWithGooglePayOneDay.setOnClickListener {
            Log.i(TAG, "setupView btnPayWithGooglePayOneDay productDetail: ${viewModel.subscriptionProductDetails}")
            Log.i(TAG, "setupView btnPayWithGooglePayOneDay productDetail size: ${viewModel.subscriptionProductDetails.size}")
            val billingParams = viewModel.buildBillingParams(
                viewModel.subscriptionProductDetails.getOrNull(0),
                PaymentUtil.sbSubsDayPlanIds[2]
            ) ?: return@setOnClickListener
            val billingResult = billingClient.launchBillingFlow(
                this,
                billingParams
            )

            Log.i(TAG, "setupView btnPayWithGooglePayOneDay code: ${billingResult.responseCode}")
        }

        binding.btnPayWithGooglePayOneWeek.setOnClickListener {
            Log.i(TAG, "setupView btnPayWithGooglePayOneWeek productDetail: ${viewModel.inAppProductDetails}")
            Log.i(TAG, "setupView btnPayWithGooglePayOneWeek productDetail size: ${viewModel.inAppProductDetails.size}")
            val billingParams = viewModel.buildBillingParams(
                viewModel.inAppProductDetails.getOrNull(0),
                PaymentUtil.dummyInAppProductIds[0]
            ) ?: return@setOnClickListener
            val billingResult = billingClient.launchBillingFlow(
                this,
                billingParams
            )

            Log.i(TAG, "setupView btnPayWithGooglePayOneWeek code: ${billingResult.responseCode}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        Toast.makeText(this, "${purchase.orderId} | ${purchase.products}", Toast.LENGTH_SHORT).show()
        Log.i(TAG, "successPurchase orderId: ${purchase.orderId}")
        Log.i(TAG, "successPurchase products: ${purchase.products}")
        Log.i(TAG, "successPurchase purchaseToken: ${purchase.purchaseToken}")
        Log.i(TAG, "successPurchase obfuscateProfileId: ${purchase.accountIdentifiers?.obfuscatedAccountId}")

        /**
         * acknowledging product is actually verify that our server side has recorded the purchase data.
         * So [viewModel.acknowledgePurchase()] must be called AFTER success state from BE
         * */
        viewModel.acknowledgePurchase(billingClient, purchase.purchaseToken)
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}
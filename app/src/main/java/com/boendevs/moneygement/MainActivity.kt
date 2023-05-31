package com.boendevs.moneygement

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.boendevs.moneygement.databinding.ActivityMainBinding
import com.boendevs.moneygement.extension.observeLiveEvent
import com.boendevs.moneygement.extension.whenReady
import com.boendevs.moneygement.google.connection.BillingConnectionProvider
import com.boendevs.moneygement.util.PaymentUtil

class MainActivity : AppCompatActivity() {

    private var _binding: ActivityMainBinding? = null
    private val binding: ActivityMainBinding get() = _binding!!

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory(billingClient) }

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

    private val billingConnectionProvider by lazy {
        BillingConnectionProvider(billingClient)
            .setConnectionListener(object : BillingConnectionProvider.ConnectionListener {
                override fun onConnected() {
                    Log.i(TAG, "onBillingSetupFinished launch")
                    viewModel.querySubscriptionProductDetail()
                    viewModel.queryInAppProductDetail()
                }

                override fun onErrorConnection() {
                    Toast.makeText(
                        this@MainActivity,
                        "Something went wrong during connecting to google service",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupBillingConnection()
        setupView()
        setupObserver()
    }

    override fun onResume() {
        super.onResume()
        billingClient.whenReady {
            viewModel.queryUserInAppPurchases()
            viewModel.queryUserSubsPurchases()
        }
    }

    override fun onDestroy() {
        billingConnectionProvider.disconnect()
        super.onDestroy()
    }

    private fun setupBillingConnection() {
        billingConnectionProvider.connect()
    }

    private fun setupView() {
        binding.btnPayWithGooglePayOneDay.setOnClickListener {
            Log.i(TAG, "setupView btnPayWithGooglePayOneDay productDetail: ${viewModel.subscriptionProductDetails}")
            Log.i(
                TAG,
                "setupView btnPayWithGooglePayOneDay productDetail size: ${viewModel.subscriptionProductDetails.size}"
            )
            viewModel.buildBillingParams(
                viewModel.subscriptionProductDetails.getOrNull(0),
                PaymentUtil.sbSubsDayPlanIds[2]
            )
        }

        binding.btnPayWithGooglePayOneWeek.setOnClickListener {
            Log.i(TAG, "setupView btnPayWithGooglePayOneWeek productDetail: ${viewModel.inAppProductDetails}")
            Log.i(TAG, "setupView btnPayWithGooglePayOneWeek productDetail size: ${viewModel.inAppProductDetails.size}")
            viewModel.buildBillingParams(
                viewModel.inAppProductDetails.getOrNull(0),
                PaymentUtil.dummyInAppProductIds[0]
            )
        }
    }

    private fun setupObserver() {
        observeLiveEvent(viewModel.mainEvent) { event ->
            when(event) {
                is MainEvent.SuccessBilling -> {
                    billingClient.launchBillingFlow(
                        this,
                        event.billingParam
                    )
                }
            }
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
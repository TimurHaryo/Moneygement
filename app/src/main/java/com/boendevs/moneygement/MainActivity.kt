package com.boendevs.moneygement

import android.os.Bundle
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

    private var billingClient = BillingClient.newBuilder(this)
        .setListener(purchaseUpdateListener)
        .enablePendingPurchases()
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setupView()
    }

    private fun setupBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    queryProductDetail()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                setupBillingConnection() // <--- add some logic. i.e only allowed to retry 3 times
            }
        })
    }

    private fun queryProductDetail() {
        billingClient.queryProductDetailsAsync(
            PaymentUtil.queryProductDetailsParams
        ) { _, productDetailsList ->
            productDetailsList.forEach {
                val billingResult = billingClient.launchBillingFlow(this, viewModel.buildBillingParams(it))
                // check billingResult response code
            }
        }
    }

    private fun setupView() {
        binding.btnPayWithGooglePay.setOnClickListener {
            binding.btnPayWithGooglePay.isClickable = false
            setupBillingConnection()
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        Toast.makeText(this, "${purchase.orderId} | ${purchase.products}", Toast.LENGTH_SHORT).show()
    }
}
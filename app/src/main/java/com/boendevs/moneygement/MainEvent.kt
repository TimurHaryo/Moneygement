package com.boendevs.moneygement

import com.android.billingclient.api.BillingFlowParams

sealed class MainEvent {
    data class SuccessBilling(val billingParam: BillingFlowParams) : MainEvent()
}

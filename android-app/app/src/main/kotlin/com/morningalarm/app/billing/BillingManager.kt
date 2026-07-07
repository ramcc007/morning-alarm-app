package com.morningalarm.app.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.android.billingclient.api.acknowledgePurchase
import com.android.billingclient.api.queryProductDetails
import com.android.billingclient.api.queryPurchasesAsync
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Product ID for the single monthly subscription. The 3-day free trial is
 * configured as a base-plan offer on this subscription in Play Console (see
 * android-app/README.md) - it is not something the client sets in code.
 */
const val SUBSCRIPTION_PRODUCT_ID = "morningalarm_plus_monthly"

/** Wraps Google Play Billing Library for the single monthly subscription. */
class BillingManager(private val context: Context) {

    private val scope = CoroutineScope(Dispatchers.Main)

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed

    private val _productDetails = MutableStateFlow<ProductDetails?>(null)
    val productDetails: StateFlow<ProductDetails?> = _productDetails

    private val _lastErrorMessage = MutableStateFlow<String?>(null)
    val lastErrorMessage: StateFlow<String?> = _lastErrorMessage

    private val purchasesUpdatedListener = PurchasesUpdatedListener { result, purchases ->
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch { purchases.forEach { handlePurchase(it) } }
        } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            _lastErrorMessage.value = "Purchase failed: ${result.debugMessage}"
        }
    }

    // Billing Library 7+ requires an explicit pending-purchases declaration or
    // BillingClient.Builder.build() throws IllegalArgumentException, even for a
    // subscription-only app - enableOneTimeProducts() is the required call shape.
    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .build()

    fun start() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        loadProductDetails()
                        refreshEntitlements()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // The next purchase/refresh call will trigger a reconnect via startConnection.
            }
        })
    }

    /** Human readable trial description, e.g. "3-day free trial, then ₹50.00/month". */
    fun trialDescription(): String? {
        val offer = productDetails.value
            ?.subscriptionOfferDetails
            ?.firstOrNull { offer -> offer.pricingPhases.pricingPhaseList.any { it.priceAmountMicros == 0L } }
            ?: return null

        val freePhase = offer.pricingPhases.pricingPhaseList.first { it.priceAmountMicros == 0L }
        val paidPhase = offer.pricingPhases.pricingPhaseList.firstOrNull { it.priceAmountMicros > 0L }
        val trialDays = parseIso8601DurationDays(freePhase.billingPeriod)
        val price = paidPhase?.formattedPrice ?: "₹50.00"
        return "$trialDays-day free trial, then $price/month"
    }

    private suspend fun loadProductDetails() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SUBSCRIPTION_PRODUCT_ID)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _productDetails.value = result.productDetailsList?.firstOrNull()
        } else {
            _lastErrorMessage.value = "Couldn't load subscription info. Check your connection and try again."
        }
    }

    fun purchase(activity: Activity) {
        val product = productDetails.value
        val offerToken = product?.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (product == null || offerToken == null) {
            _lastErrorMessage.value = "Subscription is not available right now."
            return
        }

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(product)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        billingClient.launchBillingFlow(activity, params)
    }

    suspend fun refreshEntitlements() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        val result = billingClient.queryPurchasesAsync(params)
        val subscribed = result.purchasesList.any { purchase ->
            purchase.products.contains(SUBSCRIPTION_PRODUCT_ID) &&
                purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }
        _isSubscribed.value = subscribed
        result.purchasesList.forEach { handlePurchase(it) }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        _isSubscribed.value = true
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params)
        }
    }

    private fun parseIso8601DurationDays(period: String): Int {
        // Handles the simple "P<n>D" case Play uses for short free-trial periods.
        val match = Regex("""P(\d+)D""").find(period)
        return match?.groupValues?.get(1)?.toIntOrNull() ?: 3
    }
}

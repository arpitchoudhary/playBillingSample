package com.arpit.play.billing

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.android.billingclient.api.QueryProductDetailsParams.Product
import com.google.common.collect.ImmutableList

class MainActivity : AppCompatActivity() {
    private var billingClient: BillingClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases()
            .setListener { billingResult: BillingResult, list: List<Purchase?>? ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    Log.d("NOTEPAD", ": onpurchase update")
                    for (purchase in list) {
                        Log.d("NOTEPAD", ": ${purchase.toString()}")
//                        verifySubPurchase(purchase!!)
                    }
                }
            }.build()

        establishConnection()
    }

    fun establishConnection() {
        Log.d("NOTEPAD", ": establishConnection")

        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("NOTEPAD", ": onBillingSetupFinished")
                    // The BillingClient is ready. You can query purchases here.
                    showProducts()
//                    queryPurchase()
//                    queryPurchaseHistory()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                establishConnection()
                Log.d("NOTEPAD", ": onBillingServiceDisconnected")
            }
        })
    }

    fun showProducts() {
        Log.d("NOTEPAD", ": showProducts")

        val productList: ImmutableList<Product> = ImmutableList.of( //Product 1
            Product.newBuilder()
                .setProductId("yearly_plan")
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),  //Product 2
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient!!.queryProductDetailsAsync(
            params) { billingResult: BillingResult?, prodDetailsList: List<ProductDetails> ->
            // Process the result
            val d = prodDetailsList[0]
            Log.d("NOTEPAD", ": ${d}")
            launchPurchaseFlow(d)
        }
    }

    private fun launchPurchaseFlow(productDetails: ProductDetails) {
        assert(productDetails.subscriptionOfferDetails != null)
        val productDetailsParamsList = ImmutableList.of(
            ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails)
                .setOfferToken(productDetails.subscriptionOfferDetails!![0].offerToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        billingClient!!.launchBillingFlow(this, billingFlowParams)
    }

    override fun onResume() {
        super.onResume()
    }

    fun queryPurchase(){
        billingClient!!.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.SUBS).build()
        ) { billingResult: BillingResult, list: List<Purchase> ->
            Log.d("NOTEPAD", ": queryPurchasesAsync")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in list) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                        Log.d("NOTEPAD", ": ${purchase} ")
                    }
                }
            }
        }

    }

    fun verifySubPurchase(purchases: Purchase) {
        Log.d("NOTEPAD", "verifySubPurchase")
        val acknowledgePurchaseParams = AcknowledgePurchaseParams
            .newBuilder()
            .setPurchaseToken(purchases.purchaseToken)
            .build()
        billingClient!!.acknowledgePurchase(
            acknowledgePurchaseParams) { billingResult: BillingResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("NOTEPAD", "verifySubPurchase")
            }else{
                Log.d("NOTEPAD", "false")
            }
        }

    }

    fun restorePurchases() {
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { billingResult: BillingResult?, list: List<Purchase?>? -> }
            .build()
        val finalBillingClient: BillingClient = billingClient!!
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    finalBillingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS).build()
                    ) { billingResult1: BillingResult, list: List<Purchase> ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK) {

                        }
                    }
                }
            }
        })
    }

    fun queryPurchaseHistory(){
        billingClient = BillingClient.newBuilder(this).enablePendingPurchases()
            .setListener { billingResult: BillingResult?, list: List<Purchase?>? -> }
            .build()
        val finalBillingClient: BillingClient = billingClient!!
        billingClient!!.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                   finalBillingClient.queryPurchaseHistoryAsync(
                       QueryPurchaseHistoryParams.newBuilder()
                           .setProductType(BillingClient.ProductType.SUBS).build()
                   ) { billingResult, purchaseHistoryRecordss ->

                       Log.d("NOTEPAD", ": ${purchaseHistoryRecordss} ")
                   }
                }
            }
        })

    }


}
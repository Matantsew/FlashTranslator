package com.example.latranslator.helpers

import android.app.Activity
import android.app.Application
import android.util.Log
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingHelper private constructor(
    application: Application,
    private val defaultScope: CoroutineScope
): PurchasesUpdatedListener, BillingClientStateListener, ProductDetailsResponseListener {

  private val billingClient: BillingClient

  private var onFetchOffersCallback: OnFetchOffersCallback? = null
  private var onUpdatePurchasedOfferItem: OnUpdatePurchasedOfferItem? = null

  init {
    billingClient = BillingClient.newBuilder(application)
      .setListener(this)
      .enablePendingPurchases()
      .build()

    billingClient.startConnection(this)
  }

  suspend fun processPurchases(): ProductDetailsResult {

    val productList = ArrayList<QueryProductDetailsParams.Product>()

    productList.add(
      QueryProductDetailsParams.Product.newBuilder()
        //.setProductId()
        .setProductType(BillingClient.ProductType.INAPP)
        .build()
    )

    val params = QueryProductDetailsParams.newBuilder()
    params.setProductList(productList)

    // leverage queryProductDetails Kotlin extension function
    val productDetailsResult = withContext(Dispatchers.IO) {
      billingClient.queryProductDetails(params.build())
    }

    return productDetailsResult
  }

  fun launchBillingFlow(activity: Activity) {

    defaultScope.launch {

      val productDetailsResult = processPurchases()
      val productDetailsList = productDetailsResult.productDetailsList

      val productDetails = productDetailsList?.run {
        if(this.isNotEmpty())get(0) else null
      }

      productDetails?.let {
        launchBillingFlow(activity)
        setOnUpdatePurchasedOfferItem(object : OnUpdatePurchasedOfferItem {
          override fun onUpdatePurchasedOfferItem() {
            CoroutineScope(Dispatchers.Main).launch pressEnter@ {

            }
          }
        })
      }

      val token = productDetails?.subscriptionOfferDetails?.first()?.offerToken ?: return@launch

      val productDetailsParamsList = listOf(
        BillingFlowParams.ProductDetailsParams.newBuilder()
          // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
          .setProductDetails(productDetails)
          // to get an offer token, call ProductDetails.subscriptionOfferDetails()
          // for a list of offers that are available to the user
          .setOfferToken(token)
          .build()
      )

      val billingFlowParams = BillingFlowParams.newBuilder()
        .setProductDetailsParamsList(productDetailsParamsList)
        .build()

      billingClient.launchBillingFlow(activity, billingFlowParams)
    }
  }

  private suspend fun queryPurchaseHistory() {
    /*val params = QueryPurchaseHistoryParams.newBuilder()
      .setProductType(BillingClient.ProductType.)

    // uses queryPurchaseHistory Kotlin extension function
    val purchaseHistoryResult = billingClient.queryPurchaseHistory(params.build())
    // check purchaseHistoryResult.billingResult
    val purchaseHistory = purchaseHistoryResult.purchaseHistoryRecordList
    val jsonDecoder = Json{ ignoreUnknownKeys = true }
*/
  }

  override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
      for (purchase in purchases) {

      }
    } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
      // Handle an error caused by a user cancelling the purchase flow.
    } else {
      // Handle any other error codes.
    }
  }

  companion object {
    private val TAG = "Purchases:" + BillingHelper::class.java.simpleName

    @Volatile
    private var sInstance: BillingHelper? = null

    // Standard boilerplate double check locking pattern for thread-safe singletons.
    @JvmStatic
    fun getInstance(
        application: Application,
        defaultScope: CoroutineScope
    ) = sInstance ?: synchronized(this) {
      sInstance ?: BillingHelper(
          application,
          defaultScope
      ).also { sInstance = it }
    }

    fun close() {
      sInstance = null
    }
  }


  override fun onBillingServiceDisconnected() {
    defaultScope.launch {
      delay(3000)
      billingClient.startConnection(this@BillingHelper)
    }
  }

    override fun onBillingSetupFinished(result: BillingResult) {

      val responseCode = result.responseCode
      val debugMessage = result.debugMessage

      Log.d(TAG, "onBillingSetupFinished: $responseCode $debugMessage")

      when (responseCode) {
        BillingClient.BillingResponseCode.OK -> {
          defaultScope.launch {
            queryPurchasesAsync()
          }
        }
      }
    }
  // Additional verifying a purchases user made
  suspend fun queryPurchasesAsync() {
    val params = QueryPurchasesParams.newBuilder()
      .setProductType(BillingClient.ProductType.INAPP)

    // uses queryPurchasesAsync Kotlin extension function
    val purchasesResult = billingClient.queryPurchasesAsync(params.build())

    val purchasesIdSet = purchasesResult.purchasesList.map { purchase ->
      purchase.orderId
    }.toSet()

    onFetchOffersCallback?.onFetchOffers(purchasesIdSet)

    // check purchasesResult.billingResult
    // process returned purchasesResult.purchasesList, e.g. display the plans user owns
  }

  override fun onProductDetailsResponse(billingResult: BillingResult, productDetailsList: MutableList<ProductDetails>) {
  }

  fun setOnFetchPurchasesHistoryCallback(callback: OnFetchOffersCallback) {
    onFetchOffersCallback = callback
  }

  fun setOnUpdatePurchasedOfferItem(callback: OnUpdatePurchasedOfferItem) {
    onUpdatePurchasedOfferItem = callback
  }

  interface OnFetchOffersCallback {
    fun onFetchOffers(offersIdSet: Set<String?>)
  }

  interface OnUpdatePurchasedOfferItem {
    fun onUpdatePurchasedOfferItem()
  }
}
package com.mpressavicenna.app.ui.purchase

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.scale
import com.android.billingclient.api.*
import com.mpressavicenna.app.R
import com.mpressavicenna.app.databinding.ActivityPurchaseBinding
import com.mpressavicenna.app.model.User
import com.mpressavicenna.app.ui.HomeActivity
import com.mpressavicenna.app.util.*
import io.paperdb.Paper
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class PurchaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPurchaseBinding
    private lateinit var billingClient: BillingClient
    private val mListSubscriptions = listOf(
        "monthly_sub",
        "yearly_sub",
        "life_time_sub",
    )
    private val mListSubscriptionNames = listOf(
        "monthly",
        "yearly",
        "lifeTime",
    )
    val cardId = "tappze_card"
    var selectedSubscription = ""
    val user = Paper.book().read<User>(Constant.k_activeUser)
    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                purchases.forEach {
                    verifySubPurchase(it)
                }
            } else {
                Log.e("PurchaseActivity", "purchaseUpdateListener: ${billingResult.responseCode}")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPurchaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        selectedSubscription = mListSubscriptions[0]

        Paper.book().read<User>(Constant.k_activeUser)?.name?.let { binding.tvUserName.text = it }
        binding.tvCardPrice.text = "$55"

        if (Paper.book().read<User>(Constant.k_activeUser)?.isCardPurchased == true) {
            binding.btnBuyCard.text = "Purchased"
            binding.btnBuyCard.isEnabled = false
        }

        binding.ivBack.setOnClickListener { onBackPressed() }

        binding.btnSubPlans.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {

                binding.cbPackageOne.isChecked = false
                binding.cbPackageTwo.isChecked = false
                selectedSubscription = ""

                when (checkedId) {
                    R.id.btnMonthly -> {
                        changePackagePrice(0)
                    }
                    R.id.btnYearly -> {
                        changePackagePrice(1)
                    }
                    R.id.btnLifeTime -> {
                        changePackagePrice(2)
                    }
                }
            }
        }
        binding.btnSubPlans.check(R.id.btnMonthly)

        /*binding.cbPackageOne.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (binding.btnBasic.isChecked) {
                    selectedSubscription = mListSubscriptions[1]
                }
                if (binding.btnPremium.isChecked) {
                    selectedSubscription = mListSubscriptions[3]
                }
                binding.cbPackageTwo.isChecked = false
            }
        }*/

        /*binding.cbPackageTwo.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (binding.btnBasic.isChecked) {
                    selectedSubscription = mListSubscriptions[0]
                }
                if (binding.btnPremium.isChecked) {
                    selectedSubscription = mListSubscriptions[2]
                }
                binding.cbPackageOne.isChecked = false
            }
        }*/

        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        binding.btnBuyCard.setOnClickListener { openWebsite() }

        binding.btnBuySub.setOnClickListener {
            if (selectedSubscription.isEmpty()) {
                binding.root.snackBar("Please select package 1 or package 2 to continue")
                return@setOnClickListener
            }
            Log.e("PurchaseActivity", "onCreate: $selectedSubscription")
            establishConnectionForSubscription()
        }

    }

    private fun changePackagePrice(plan: Int) {
        when (plan) {
            0 -> {
                binding.tvSubscriptionName.text = getString(R.string.monthly_subscription)
                binding.tvPackagePrice.text = getString(R.string.monthly_price)
                binding.tvPackageDetails.text = getString(R.string.monthly_detail)
                selectedSubscription = mListSubscriptions[0]
            }
            1 -> {
                binding.tvSubscriptionName.text = getString(R.string.yearly_subscription)
                binding.tvPackagePrice.text = getString(R.string.yearly_price)
                binding.tvPackageDetails.text = getString(R.string.yearly_detail)
                selectedSubscription = mListSubscriptions[1]
            }
            2 -> {
                binding.tvSubscriptionName.text = getString(R.string.life_time_subscription)
                binding.tvPackagePrice.text = getString(R.string.life_time_price)
                binding.tvPackageDetails.text = getString(R.string.life_time_detail)
                selectedSubscription = mListSubscriptions[2]
            }
        }

    }

    private fun establishConnectionForSubscription() {
        Log.e("PurchaseActivity", "establishConnectionForSubscription: $selectedSubscription")
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    billingClient.queryPurchasesAsync(
                        QueryPurchasesParams.newBuilder()
                            .setProductType(
                                if (selectedSubscription == cardId) {
                                    BillingClient.ProductType.INAPP
                                } else {
                                    BillingClient.ProductType.SUBS
                                }
                            )
                            .build()
                    ) { billingResult1: BillingResult, list: List<Purchase?> ->
                        if (billingResult1.responseCode == BillingClient.BillingResponseCode.OK && list.isNotEmpty()) {
                            list[0]?.let {
                                verifySubPurchase(it)
                            }
                        } else if (list.isEmpty()) {
                            if (selectedSubscription == cardId) {
                                val skuList = ArrayList<String>()
                                skuList.add(cardId)
                                val params = SkuDetailsParams.newBuilder()
                                params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                                billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
                                    // Process the result.
                                    if (skuDetailsList?.isNotEmpty() == true) {
                                        val billingFlowParams = BillingFlowParams.newBuilder()
                                            .setSkuDetails(skuDetailsList[0])
                                            .build()
                                        val responseCode = billingClient.launchBillingFlow(
                                            this@PurchaseActivity,
                                            billingFlowParams
                                        ).responseCode
                                    } else {
                                        Log.e("PurchaseActivity", "No SKU Details found")
                                    }
                                }
                            } else {
                                showSubscriptions(selectedSubscription)
                            }

                        }
                    }
                } else {
                    Log.e(
                        "PurchaseActivity",
                        "onBillingSetupFinished: ${billingResult.debugMessage}"
                    )
                }

            }

            override fun onBillingServiceDisconnected() {
                establishConnectionForSubscription()
            }
        })
    }

    private fun showSubscriptions(subId: String) {

        val params = QueryProductDetailsParams.newBuilder()

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(subId)
                .setProductType(
                    if (selectedSubscription == cardId) {
                        BillingClient.ProductType.INAPP
                    } else {
                        BillingClient.ProductType.SUBS
                    }
                )
                .build()
        )

        params.setProductList(productList).let { subDetailsParams ->
            billingClient.queryProductDetailsAsync(subDetailsParams.build()) { billingResult, productDetailsList ->
                Log.e("PurchaseActivity", "showSubscriptions: ${billingResult.responseCode}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    productDetailsList.forEach { launchPurchaseFlow(it) }
                }
            }
        }

    }

    private fun launchPurchaseFlow(product: ProductDetails) {
        val offerToken = product.subscriptionOfferDetails?.get(0)?.offerToken
        val productDetailsParamsList =
            listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(product)
                    .setOfferToken(offerToken!!)
                    .build()
            )
        val billingFlowParams =
            BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()


        val responseCode =
            billingClient.launchBillingFlow(this, billingFlowParams).responseCode

    }

    private fun verifySubPurchase(purchase: Purchase) {
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(
            acknowledgePurchaseParams
        ) {
            if (it.responseCode == BillingClient.BillingResponseCode.OK) {
                toast { "subscribed" }
            }
        }

        //Purchase Complete
        if (purchase.products[0] == cardId) {
            user?.isCardPurchased = true
            Constant.rootRef.child(Constant.k_tableUser)
                .child(user?.id!!)
                .child("isCardPurchased")
                .setValue(true)
                .addOnCompleteListener {
                    binding.btnBuyCard.text = "Purchased"
                    binding.btnBuyCard.isEnabled = false
                    moveToNextActivity(user)
                }
        } else {
            val purchaseDate = Date(purchase.purchaseTime)
            val format = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault(Locale.Category.FORMAT))
            val c = Calendar.getInstance()
            try {
                c.time = purchaseDate
            } catch (e: ParseException) {
                e.printStackTrace()
            }

            if (purchase.products[0] == mListSubscriptions[0]) {
                c.add(Calendar.MONTH, 1)
                user?.subscription = mListSubscriptionNames[0]
            } else if (purchase.products[1] == mListSubscriptions[1]) {
                c.add(Calendar.YEAR, 1)
                user?.subscription = mListSubscriptionNames[1]
            } else if (purchase.products[2] == mListSubscriptions[2]) {
                c.add(Calendar.YEAR, 40)
                user?.subscription = mListSubscriptionNames[2]
            }

            val isProVersion =
                purchase.products[0] == mListSubscriptions[2] || purchase.products[0] == mListSubscriptions[3]

            if (isProVersion) {
                if (user?.subscription == mListSubscriptionNames[1]) {
                    user.subscription = mListSubscriptionNames[3]
                } else {
                    user?.subscription = mListSubscriptionNames[2]
                }
            }

            user?.isSubscribed = true
            user?.isProVersion = isProVersion
            user?.subscriptionPurchaseDate = format.format(purchaseDate)
            user?.subscriptionExpiryDate = format.format(c.time)

            Log.e("PurchaseActivity", "purchaseDate: ${format.format(purchaseDate)}")
            Log.e("PurchaseActivity", "expiryDate: ${format.format(c.time)}")

            Constant.rootRef.child(Constant.k_tableUser)
                .child(Paper.book().read<User>(Constant.k_activeUser)?.id!!)
                .setValue(user).addOnCompleteListener {
                    moveToNextActivity(user)
                }

        }

        Log.e("HomeFragment", "Purchase Token: " + purchase.purchaseToken)
        Log.e("HomeFragment", "Purchase Time: " + purchase.purchaseTime)
        Log.e("HomeFragment", "Purchase OrderID: " + purchase.orderId)

    }

    private fun moveToNextActivity(user: User?) {
        Paper.book().write(Constant.k_activeUser, user!!)
        openActivity(HomeActivity::class.java, true)
    }

    override fun onResume() {
        super.onResume()

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(
                    if (selectedSubscription == cardId) {
                        BillingClient.ProductType.INAPP
                    } else {
                        BillingClient.ProductType.SUBS
                    }
                )
                .build()
        ) { billingResult, purchaseList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                purchaseList.forEach {
                    if (it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged) {
                        verifySubPurchase(it)
                    }
                }
            }
        }

    }

    /*override fun onBackPressed() {
        if (user?.isSubscribed == true){
            super.onBackPressed()
        } else {
            displayPopUpOptions(
                object : GeneralListener{
                    override fun buttonClick(clicked: Boolean) {
                        signOut()
                    }
                },
                "Are you sure to logout?"
            )
        }
    }*/

}
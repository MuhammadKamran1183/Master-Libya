package com.mpressavicenna.app.ui

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.mpressavicenna.app.R
import com.mpressavicenna.app.databinding.ActivityHomeBinding
import com.mpressavicenna.app.model.User
import com.mpressavicenna.app.ui.purchase.PurchaseActivity
import com.mpressavicenna.app.util.Constant
import com.mpressavicenna.app.util.setHomeStatusBarColor
import io.paperdb.Paper

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private var adapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setHomeStatusBarColor(this)

        val navView: BottomNavigationView = binding.navView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_home) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)
        followNavController(navController)
        initNfcAdapter()

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_edit_profile -> {
                    binding.navView.visibility = View.GONE
                }
                R.id.businessNetworkFragment -> {
                    binding.navView.visibility = View.GONE
                }
                else ->{
                    binding.navView.visibility = View.VISIBLE
                }
            }
        }

    }

    override fun onResume() {
        if (checkNFCDeviceSupport()) {
            enableNfcForegroundDispatch()
        }
        super.onResume()
    }

    private fun initNfcAdapter() {
        val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
        adapter = nfcManager.defaultAdapter
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Constant.k_newIntent.value = intent
    }

    private fun enableNfcForegroundDispatch() {
        try {
            val intent =
                Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val nfcPendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    PendingIntent.FLAG_MUTABLE
                } else {
                    0
                }
            )
            adapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        } catch (ex: IllegalStateException) {
            Log.e("HomeActivity", "Error enabling NFC foreground dispatch", ex)
        }
    }

    private fun disableNfcForegroundDispatch() {
        try {
            adapter?.disableForegroundDispatch(this)
        } catch (ex: IllegalStateException) {
            Log.e("HomeActivity", "Error disabling NFC foreground dispatch", ex)
        }
    }

    override fun onPause() {
        if (checkNFCDeviceSupport()) {
            disableNfcForegroundDispatch()
        }
        super.onPause()
    }

    private fun followNavController(navController: NavController) {
        /*navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.nav_analytics -> {
                    if (Paper.book()
                            .read<User>(Constant.k_activeUser)?.subscription!! != "lifeTime"
                    ) {
                        startActivity(Intent(this, PurchaseActivity::class.java))
                        return@addOnDestinationChangedListener
                    }
                }
            }
        }*/
    }

    private fun checkNFCDeviceSupport(): Boolean {
        if (adapter == null) {
//            toast { "${resources.getString(R.string.nfc_not_supported)}!" }
            return false
        } else if (!adapter!!.isEnabled) {
//            toast { "${resources.getString(R.string.nfc_not_enabled)}!" }
            return false
        }
        Constant.k_nfcDevice = true
        return true
    }

}
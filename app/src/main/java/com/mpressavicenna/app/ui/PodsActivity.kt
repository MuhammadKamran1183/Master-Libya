package com.mpressavicenna.app.ui

import android.Manifest
import com.mpressavicenna.app.R
import android.app.Activity
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.mpressavicenna.app.model.Tag
import android.nfc.*
import android.nfc.NdefRecord
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.mpressavicenna.app.databinding.ActivityPodsBinding
import com.mpressavicenna.app.databinding.ItemBottomSheetChooseNfcBinding
import com.mpressavicenna.app.model.User
import com.mpressavicenna.app.util.Constant
import com.mpressavicenna.app.util.Loading
import com.mpressavicenna.app.util.Loading.cancelLoading
import com.mpressavicenna.app.util.Loading.showLoading
import com.mpressavicenna.app.util.displayPopUp
import com.mpressavicenna.app.util.nfc.NfcUtils
import com.mpressavicenna.app.util.nfc.WritableTag
import com.mpressavicenna.app.util.openActivity
import com.mpressavicenna.app.util.toast
import io.paperdb.Paper

class PodsActivity : AppCompatActivity() {

    val TAG = PodsActivity::class.java.simpleName
    private var adapter: NfcAdapter? = null
    var tag: WritableTag? = null
    var tagId: String? = null
    var progressDialog: ProgressDialog? = null
    private lateinit var binding: ActivityPodsBinding
    private lateinit var codeScanner: CodeScanner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPodsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        codeScanner = CodeScanner(this, binding.scannerView)

        binding.btnActivatePod.setOnClickListener {
            openActivity(RegisterActivity::class.java)
        }

        displayMethodBottomSheet()

//        initNfcAdapter()
//        binding.tvUserName.text = "${Paper.book().read<User>(Constant.k_activeUser)?.username}"
//
//        binding.btnActivateClickle.setOnClickListener {
//            if (checkNFCDeviceSupport()) {
//                displayAlertForReader()
//            }
//        }
//
//        binding.tvInfoBottomLink.setOnClickListener {
//            openBrowser(Constant.k_SERVER_IP)
//        }
    }

    fun scanQR() {

        binding.scannerView.visibility=View.VISIBLE

        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        codeScanner.decodeCallback = DecodeCallback {}
        codeScanner.errorCallback = ErrorCallback {}

//        codeScanner.startPreview()

        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }

        binding.scannerView.performClick()
    }

    private fun initNfcAdapter() {
        val nfcManager = getSystemService(Context.NFC_SERVICE) as NfcManager
        adapter = nfcManager.defaultAdapter
    }
    private fun writeNDefMessage() {
        val message = NfcUtils.prepareMessageToWrite(
            "${Constant.k_SERVER_IP}${Paper.book().read<User>(Constant.k_activeUser)?.id}",
            this
        )

        val record: NdefRecord =
            NdefRecord.createUri(
                Uri.parse(
                    "${Constant.k_SERVER_IP}${
                        Paper.book().read<User>(Constant.k_activeUser)?.username
                    }"
                )
            )
        val msg =
            NdefMessage(arrayOf(record/*, NdefRecord.createApplicationRecord(this.packageName)*/))

        val writeResult = tag?.writeData(tagId!!, msg)
        if (writeResult != null) {
            progressDialog?.dismiss()
            toast { "Pod Activated!" }
        } else {
            progressDialog?.dismiss()
            toast { "Pod activation failed. Please try to place pod within 10cm range of nfc reader!" }
        }
    }
    private fun enableNfcForegroundDispatch() {
        try {
            val intent = Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val nfcPendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE)
            adapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
        } catch (ex: IllegalStateException) {
            Log.e(TAG, "Error enabling NFC foreground dispatch", ex)
        }
    }
    private fun disableNfcForegroundDispatch() {
        try {
            adapter?.disableForegroundDispatch(this)
        } catch (ex: IllegalStateException) {
            Log.e(TAG, "Error disabling NFC foreground dispatch", ex)
        }
    }
    override fun onResume() {
        if (checkNFCDeviceSupportAtStart()) {
            enableNfcForegroundDispatch()
        }
        super.onResume()
    }
    override fun onPause() {
        if (checkNFCDeviceSupportAtStart()) {
            disableNfcForegroundDispatch()
        }
        super.onPause()
    }

    private fun checkNFCDeviceSupportAtStart(): Boolean {
        if (adapter == null) {
            return false
        } else if (!adapter!!.isEnabled) {
            return false
        }
        return true
    }

    private fun checkNFCDeviceSupport(): Boolean {
        if (adapter == null) {
            displayConfigureAlert("nfcNotSupported")
            return false
        } else if (!adapter!!.isEnabled) {
            displayConfigureAlert("enableNFC")
            return false
        }
        return true
    }

    private fun displayAlertForReader() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage(" Your Nano Product will be activated…")
        progressDialog!!.show()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.e("PodsActivity", "onNewIntent: ${intent?.dataString}")

        if (progressDialog != null) {
            if (progressDialog!!.isShowing) {
                if (intent?.dataString?.isNotEmpty() == true) {
                    onTagTapped(intent.dataString!!)
                } else {
                    progressDialog?.dismiss()
                    displayConfigureAlert("emptyTag")
                }
            }
        } else {
            displayConfigureAlert("pressBtn")
        }

    }

    private fun onTagTapped(superTagData: String) {
        progressDialog?.dismiss()

        if (superTagData.contains(Constant.k_SERVER_IP)) {
            val tagId = superTagData.replace(Constant.k_SERVER_IP, "")
            if (tagId.isNotEmpty()) {
                validateTagId(tagId)
            } else {
                displayConfigureAlert("contactAdmin")
            }
        } else {
            displayConfigureAlert("invalidUrl")
        }

    }

    private fun validateTagId(id: String) {
        showLoading()
        val isTagIdExist: Query =
            Constant.rootRef.child(Constant.k_tableTag).orderByChild("tagId")
                .equalTo(id)

        isTagIdExist.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        it.getValue(Tag::class.java)?.let { tag ->

                            if (tag.status) {
                                displayConfigureAlert("statusTrue")
                                return@forEach
                            }

                            activateTag(tag)
                        }
                        return@forEach
                    }
                } else {
                    cancelLoading()
                    displayConfigureAlert("missingTagId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                cancelLoading()
                displayPopUp(
                    getString(R.string.error),
                    error.message
                )
            }
        })
    }

    private fun activateTag(tag: Tag) {

        Constant.rootRef.child(Constant.k_tableUser)
            .child(Paper.book().read<User>(Constant.k_activeUser)?.id!!)
            .child("tagUid").setValue(tag.tagId)
            .addOnCompleteListener {
                val user = Paper.book().read<User>(Constant.k_activeUser)!!
                user.tagUid = tag.tagId
                Paper.book().write(Constant.k_activeUser, user)

                tag.id?.let { it1 ->
                    Constant.rootRef.child(Constant.k_tableTag).child(it1)
                        .child("status").setValue(true)
                    Constant.rootRef.child(Constant.k_tableTag).child(it1)
                        .child("username")
                        .setValue(Paper.book().read<User>(Constant.k_activeUser)?.username)
                }
                //displayConfigureAlert("activeTag")
                startActivity(Intent(this,HomeActivity::class.java))
                finish()
//                ApplicationHandler.intentClearTop(HomeActivity::class.java)

            }
    }

    /*private fun checkUserProfile(contactID: String): Boolean {
        if (contactID == SessionManager.getUser().id) {
            ApplicationHandler.displayPopUp(
                this,
                Constants.k_Error,
                "You can't connect your own tag."
            )
            return false
        }
        return true
    }*/

   /* private val launchSomeActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                ApplicationHandler.toast("Contact Added")
            }
            if (result.resultCode == Activity.RESULT_CANCELED) {
                ApplicationHandler.toast("Cancelled Added Contact")
            }
        }*/

    private fun addContact(contact: User) {
        val contactIntent = Intent(ContactsContract.Intents.Insert.ACTION)
        contactIntent.type = ContactsContract.RawContacts.CONTENT_TYPE

        if (contact.name?.isNotEmpty() == true) {
            contactIntent.putExtra(ContactsContract.Intents.Insert.NAME, contact.name)
        }
        if (contact.phone?.isNotEmpty() == true) {
            contactIntent.putExtra(ContactsContract.Intents.Insert.PHONE, contact.phone)
        }
        if (contact.address?.isNotEmpty() == true) {
            contactIntent.putExtra(ContactsContract.Intents.Insert.POSTAL, contact.address)
        }
        if (contact.email?.isNotEmpty() == true) {
            contactIntent.putExtra(ContactsContract.Intents.Insert.EMAIL, contact.email)
        }

//        launchSomeActivity.launch(contactIntent)

    }

    private fun displayConfigureAlert(s: String) {
        progressDialog?.dismiss()
        cancelLoading()

        val bottomSheet = BottomSheetDialog(this, R.style.bottomSheetStyle)
        bottomSheet.setContentView(R.layout.item_tag_verification_bottom_sheet)
        bottomSheet.show()
        val info1 = bottomSheet.findViewById<TextView>(R.id.tvInfo1)
        val info2 = bottomSheet.findViewById<TextView>(R.id.tvInfo2)
        val ivAlert = bottomSheet.findViewById<ImageView>(R.id.ivAlert)

        if (s == "invalidUrl") {
            info1?.text = getString(R.string.this_device_has_not_official_nano_product)
            info2?.text = getString(R.string.plz_visit_our_website)
            ivAlert?.setImageResource(R.drawable.ic_attention)
        } else if (s == "activeTag") {
            info1?.text = getString(R.string.your_account_is_active_now)
            ivAlert?.setImageResource(R.drawable.ic_success)
            //info2?.text = getString(R.string.plz_visit_our_website)
        } else if (s == "contactAdmin") {
            info1?.text = getString(R.string.contact_admin_for_support)
            //info2?.text = getString(R.string.plz_visit_our_website)
            ivAlert?.setImageResource(R.drawable.ic_attention)
        } else if (s == "missingTagId") {
            info1?.text = getString(R.string.this_product_is_not_listed_in_our_stock)
            //info2?.text = getString(R.string.plz_visit_our_website)
            ivAlert?.setImageResource(R.drawable.ic_attention)
        } else if (s == "statusTrue") {
            info1?.text = getString(R.string.this_tag_already_userd_by_someone)
            //info2?.text = getString(R.string.plz_visit_our_website)
            ivAlert?.setImageResource(R.drawable.ic_attention)
        } else if (s == "emptyTag") {
            info1?.text = getString(R.string.empty_tag)
            //info2?.text = getString(R.string.plz_visit_our_website)
            ivAlert?.setImageResource(R.drawable.ic_attention)
        } else if (s == "pressBtn") {
            info1?.text = getString(R.string.press_btn)
            //info2?.text = getString(R.string.plz_visit_our_website)
            ivAlert?.setImageResource(R.drawable.ic_attention)
        } else if (s == "enableNFC") {
            info1?.text = getString(R.string.enable_nfc)
            ivAlert?.setImageResource(R.drawable.ic_attention)
            //info2?.text = getString(R.string.plz_visit_our_website)
        } else if (s == "nfcNotSupported") {
            info1?.text = getString(R.string.nfc_not_sopported)
            ivAlert?.setImageResource(R.drawable.ic_attention)
            //info2?.text = getString(R.string.plz_visit_our_website)
        }

    }

    private fun openBrowser(url: String) {
        if (url.startsWith("https://") || url.startsWith("http://")) {
            val uri: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } else {
            toast{"Invalid Url"}
        }
    }

    private fun displayMethodBottomSheet() {
        val bindingDevice = ItemBottomSheetChooseNfcBinding.inflate(layoutInflater)
        val builder =
            BottomSheetDialog(this, R.style.bottomSheetStyle)
        builder.setContentView(bindingDevice.root)
        builder.setCancelable(false)
        builder.show()
        builder.setOnShowListener {
            val dialog = it as BottomSheetDialog
            val bottomSheet =
                dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
                sheet.parent.parent.requestLayout()
            }
        }
        builder.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        bindingDevice.cvNfc.setOnClickListener {
            builder.dismiss()
        }

        bindingDevice.cvQr.setOnClickListener {
            binding.ivCircles.visibility=View.GONE
            binding.rlBoard.visibility=View.GONE
            scanQR()
            builder.dismiss()
        }

    }

}
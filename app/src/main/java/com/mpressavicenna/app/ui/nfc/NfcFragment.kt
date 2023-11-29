package com.mpressavicenna.app.ui.nfc

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.nfc.*
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.mpressavicenna.app.R
import com.mpressavicenna.app.databinding.FragmentNfcBinding
import com.mpressavicenna.app.model.User
import com.mpressavicenna.app.util.Constant
import com.mpressavicenna.app.util.Loading.cancelLoading
import com.mpressavicenna.app.util.Loading.showLoading
import com.mpressavicenna.app.util.displayPopUp
import com.mpressavicenna.app.util.nfc.NfcUtils
import com.mpressavicenna.app.util.nfc.WritableTag
import com.mpressavicenna.app.util.openWebsite
import com.mpressavicenna.app.util.toast
import io.paperdb.Paper

class NfcFragment : Fragment(R.layout.fragment_nfc) {

    private lateinit var binding: FragmentNfcBinding
    var tag: WritableTag? = null
    var tagId: String? = null
    var user: User? = null
    var progressDialog: ProgressDialog? = null
    var mListUser = mutableListOf<User>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentNfcBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        getUserProfiles()

        binding.btnActivatePod.setOnClickListener {
            Log.e("NfcFragment", "onViewCreated: $user")

            if (user == null) {
                toast { "Please select a profile first..." }
                return@setOnClickListener
            }

            if (Constant.k_nfcDevice) {
                progressDialog = ProgressDialog(requireContext())
                progressDialog?.setMessage("Approach an NFC tag")
                progressDialog!!.show()
            } else {
                toast { "${resources.getString(R.string.nfc_not_supported)}!" }
            }

        }

        Constant.k_newIntent.observe(viewLifecycleOwner) {
            it?.let { it1 -> onNewIntent(it1) }
        }

//        binding.btnBuyCard.setOnClickListener {
//            requireActivity().openWebsite()
//        }

    }

    private fun getUserProfiles() {

        try {
            requireActivity().showLoading()
            val getUserProfiles: Query =
                Constant.rootRef.child(Constant.k_tableUser).orderByChild("parentID")
                    .equalTo(Paper.book().read<User>(Constant.k_activeUser)?.parentId!!)

            getUserProfiles.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    requireActivity().cancelLoading()
                    if (snapshot.exists()) {
                        snapshot.children.forEach { profile ->
                            try {
                                profile.getValue(User::class.java)
                                    ?.let { it1 -> mListUser.add(it1) }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        mListUser.add(Paper.book().read<User>(Constant.k_activeUser)!!)
                    }
//                    displayProfiles()
                }

                override fun onCancelled(error: DatabaseError) {
                    requireActivity().cancelLoading()
                    requireActivity().displayPopUp(
                        getString(R.string.error),
                        error.message
                    )
                }
            })
        }catch (_: NullPointerException){
            requireActivity().cancelLoading()
        }

    }

//    private fun displayProfiles() {
//
//        binding.spinnerUsername.setItems(mListUser.map { user -> user.name })
//        binding.spinnerUsername.lifecycleOwner = viewLifecycleOwner
//        binding.spinnerUsername.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
//            user = mListUser.last { it.name.lowercase() == newItem.lowercase() }
//            binding.tvUserName.text = user?.username
//        }
//
//        try {
//            binding.spinnerUsername.selectItemByIndex(0)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//    }

    private fun onNewIntent(intent: Intent?) {
        Log.e("NfcFragment", "onNewIntent: $intent")
        val tagFromIntent = intent?.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        try {
            tag = WritableTag(tagFromIntent!!)
        } catch (e: FormatException) {
            Log.e("NfcFragment", "Unsupported tag tapped", e)
            return
        }
        tagId = tag!!.tagId

        if (progressDialog != null) {
            if (progressDialog!!.isShowing) {
                writeNDefMessage()
            }
        }

    }

    private fun onTagTapped(superTagData: String) {
        if (superTagData.contains("http")) {
            val contactID = superTagData.replace(Constant.k_SERVER_IP, "")
            if (contactID == user?.id) {
                requireActivity().displayPopUp(
                    resources.getString(R.string.error),
                    resources.getString(R.string.not_connect_your_own_profile),
                    null
                )
                return
            }
        } else {
            requireActivity().displayPopUp(
                resources.getString(R.string.error),
                resources.getString(R.string.tag_not_readable),
                null
            )
        }
    }

    private fun writeNDefMessage() {
        val message = NfcUtils.prepareMessageToWrite(
            "${Constant.k_SERVER_IP}${user?.id}",
            requireContext()
        )

        val record: NdefRecord =
            NdefRecord.createUri(Uri.parse("${Constant.k_SERVER_IP}${user?.id}"))
        val msg =
            NdefMessage(arrayOf(record))

        val writeResult = tag?.writeData(tagId!!, msg)
        if (writeResult != null) {
            progressDialog?.dismiss()
            toast { "${resources.getString(R.string.pod_activated)}!" }
        } else {
            progressDialog?.dismiss()
            toast { "${resources.getString(R.string.pod_activation_failed)}!" }
        }
    }

}
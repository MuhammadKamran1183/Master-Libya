package com.mpressavicenna.app.ui.share

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.nfc.*
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.mpressavicenna.app.R
import com.mpressavicenna.app.databinding.FragmentShareBinding
import com.mpressavicenna.app.model.User
import com.mpressavicenna.app.util.Constant
import com.mpressavicenna.app.util.copyOnHold
import com.mpressavicenna.app.util.loadImage
import com.mpressavicenna.app.util.nfc.NfcUtils
import com.mpressavicenna.app.util.nfc.WritableTag
import com.mpressavicenna.app.util.toast
import io.paperdb.Paper

class ShareFragment : Fragment(R.layout.fragment_share) {

    private lateinit var binding: FragmentShareBinding
    var tag: WritableTag? = null
    var tagId: String? = null
    var user: User? = null
    var progressDialog: ProgressDialog? = null
    var mListUser = mutableListOf<User>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentShareBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        val profileLink =
            "${Constant.k_SERVER_IP}${Paper.book().read<User>(Constant.k_activeUser)?.id}"

        Paper.book().read<User>(Constant.k_activeUser)?.profileUrl?.let {
            loadImage(it, binding.ivLogoRegister)
        }

        binding.tvProfileLink.text = profileLink
        binding.tvProfileLink.copyOnHold(profileLink)

        binding.ivShareProfile.setOnClickListener {
            try {
                val shareMessage = "\nHey, \nYou can find my profile link below: \n\n$profileLink"
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                }
                this.startActivity(Intent.createChooser(shareIntent, "Share To"))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding.btnActivateTag.setOnClickListener {

            if (Constant.k_nfcDevice) {
                progressDialog = ProgressDialog(requireContext())
                progressDialog?.setMessage("Approach an NFC tag")
                progressDialog!!.show()
            } else {
                toast { "${resources.getString(R.string.nfc_not_supported)}!" }
            }
        }

        binding.btnViewMyProfile.setOnClickListener {
            openBrowser(
                "${Constant.k_SERVER_IP}${
                    Paper.book().read<User>(Constant.k_activeUser)!!.id
                }"
            )
        }

        Constant.k_newIntent.observe(viewLifecycleOwner) {
            it?.let { it1 -> onNewIntent(it1) }
        }

        createQR(profileLink)

    }

    private fun createQR(profileLink: String) {
        val writer = QRCodeWriter()
        try {
            val bitMatrix = writer.encode(profileLink, BarcodeFormat.QR_CODE, 250, 250)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            for (x in 0 until width) {
                for (y in 0 until height) {
                    bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
            binding.ivQrImage.setImageBitmap(bmp)
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

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

    fun openBrowser(url: String) {
        if (url.startsWith("https://") || url.startsWith("http://")) {
            val uri: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } else {
            toast { "Invalid Url" }
        }
    }

}
package com.mpressavicenna.app.util

import android.app.Activity
import android.app.DatePickerDialog
import android.content.*
import android.content.ClipboardManager
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.mpressavicenna.app.R
import com.mpressavicenna.app.databinding.ItemBioBsBinding
import com.mpressavicenna.app.databinding.ItemDialogUserOptionBinding
import com.mpressavicenna.app.databinding.ItemForgotPasswordBinding
import com.mpressavicenna.app.databinding.ItemSocialLinkBsBinding
import com.mpressavicenna.app.model.Instruction
import com.mpressavicenna.app.model.SocialLink
import com.mpressavicenna.app.model.User
import com.mpressavicenna.app.ui.IntroActivity
import com.mpressavicenna.app.ui.LoginActivity
import com.mpressavicenna.app.util.Loading.cancelLoading
import com.mpressavicenna.app.util.Loading.showLoading
import io.paperdb.Paper
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

private val TAG = "Utils"

fun <A : Activity> FragmentActivity.openActivity(
    activity: Class<A>,
    newAct: Boolean = true,
    extras: Bundle.() -> Unit = {}
) {
    val intent = Intent(this, activity)
    intent.putExtras(Bundle().apply(extras))
    if (newAct) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }
    startActivity(intent)
}

fun View.snackBar(message: String, action: (() -> Unit)? = null) {
    val snackBar = Snackbar.make(this, message, 5000)
    action?.let {
        snackBar.setAction("Yes") {
            it()
        }
    }
    snackBar.show()
}

inline fun Context.toast(message: () -> String) {
    Toast.makeText(this, message(), Toast.LENGTH_LONG).show()
}

inline fun Fragment.toast(message: () -> String) {
    Toast.makeText(this.context, message(), Toast.LENGTH_LONG).show()
}

fun TextView.makeLinks(vararg links: Pair<String, View.OnClickListener>) {
    val spannableString = SpannableString(this.text)
    var startIndexOfLink = -1
    for (link in links) {
        val clickableSpan = object : ClickableSpan() {
            override fun updateDrawState(textPaint: TextPaint) {
                textPaint.color = ContextCompat.getColor(this@makeLinks.context, R.color.black)
                textPaint.isUnderlineText = true
            }

            override fun onClick(view: View) {
                Selection.setSelection((view as TextView).text as Spannable, 0)
                view.invalidate()
                view.setBackgroundColor(Color.TRANSPARENT)
                view.highlightColor = Color.TRANSPARENT
                link.second.onClick(view)
            }

        }
        startIndexOfLink = this.text.toString().indexOf(link.first, startIndexOfLink + 1)
        spannableString.setSpan(
            clickableSpan, startIndexOfLink, startIndexOfLink + link.first.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    this.movementMethod =
        LinkMovementMethod.getInstance()
    this.setText(spannableString, TextView.BufferType.SPANNABLE)
}

fun TextInputLayout.validate(): Boolean {
    val result = this.editText?.text.toString().trim { it <= ' ' }
    if (result.isEmpty()) {
        this.error = context.resources.getString(R.string.editTextEmptyFieldError)
        this.requestFocus()
        return false
    }
    return true
}

fun TextInputLayout.isEmailValid(): Boolean {
    val email = this.editText?.text.toString().trim { it <= ' ' }.lowercase()

    if (email.isEmpty()) {
        this.error = context.resources.getString(R.string.editTextEmptyFieldError)
        this.requestFocus()
        return false
    }

    if (emailPattern(email)) {
        this.error = context.resources.getString(R.string.emailFormatError)
        this.requestFocus()
        return false
    }
    return true
}

private fun emailPattern(email: String): Boolean {
    var isValid = false
    val expression = ("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
            + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
            + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
            + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
            + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$")
    val inputStr: CharSequence = email
    val pattern = Pattern.compile(
        expression,
        Pattern.CASE_INSENSITIVE
    )
    val matcher = pattern.matcher(inputStr)
    if (!matcher.matches()) {
        isValid = true
    }
    return isValid
}

fun TextInputLayout.isPasswordValid(): Boolean {
    val password = this.editText?.text.toString().trim { it <= ' ' }
    if (password.isEmpty()) {
        this.error = context.resources.getString(R.string.editTextEmptyFieldError)
        this.requestFocus()
        return false
    }
    if (password.length < 6) {
        this.error = context.resources.getString(R.string.passwordShortLength)
        this.requestFocus()
        return false
    }

    return true
}

fun TextInputLayout.value(isLower: Boolean = false): String {
    return if (isLower) {
        editText?.text.toString().trim().lowercase()
    } else {
        editText?.text.toString().trim()
    }
}

fun FragmentActivity.forgotPassword() {
    val forgotBinding = ItemForgotPasswordBinding.inflate(layoutInflater)
    val builder = BottomSheetDialog(this, R.style.bottomSheetStyle)
    builder.setContentView(forgotBinding.root)
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

    forgotBinding.ivBack.setOnClickListener { builder.dismiss() }

    forgotBinding.btnForgot.setOnClickListener {
        forgotBinding.tilEmail.error = null
        if (forgotBinding.tilEmail.isEmailValid()) {
            Constant.mAuth.sendPasswordResetEmail(forgotBinding.tilEmail.value(true))
                .addOnCompleteListener {
                    builder.dismiss()
                    if (it.isSuccessful) {
                        displayPopUp(
                            getString(R.string.confirmation),
                            getString(R.string.confirmation_email_has_been_sent)
                        )
                    } else {
                        displayPopUp(
                            getString(R.string.error),
                            it.exception?.message
                        )
                    }
                }
        }
    }

}

fun FragmentActivity.displayPopUp(
    title: String,
    subTitle: String?,
    onClick: GeneralListener? = null
) {
    cancelLoading()
    val binding =
        com.mpressavicenna.app.databinding.ItemGeneralAlertDialogBinding.inflate(layoutInflater)
    val builder = AlertDialog.Builder(this)
    builder.setView(binding.root)
    val dialog = builder.create()
    dialog.window?.decorView?.setBackgroundResource(R.drawable.alert_dialog_background)
    dialog.show()

    binding.tvTitlePopUp.text = title
    binding.tvSubTitlePopUp.text = subTitle

    when (title) {
        getString(R.string.error) -> {
            binding.btnOkPopUp.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.holo_red_light
                )
            )
            binding.civAlert.setColorFilter(
                ContextCompat.getColor(this, R.color.holo_red_light),
                android.graphics.PorterDuff.Mode.MULTIPLY
            )
            binding.civAlert.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_failure
                )
            )
        }
        getString(R.string.alert) -> {
            binding.tvTitlePopUp.text = resources.getString(R.string.alert)
            binding.btnOkPopUp.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.black
                )
            )
            binding.civAlert.setColorFilter(
                ContextCompat.getColor(this, R.color.black),
                android.graphics.PorterDuff.Mode.MULTIPLY
            )
            binding.civAlert.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_success
                )
            )
        }
        else -> {
            binding.btnOkPopUp.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.holo_green_light
                )
            )
            binding.civAlert.setColorFilter(
                ContextCompat.getColor(this, R.color.holo_green_light),
                android.graphics.PorterDuff.Mode.MULTIPLY
            )
            binding.civAlert.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    R.drawable.ic_success
                )
            )
        }
    }

    binding.btnOkPopUp.setOnClickListener {
        if (onClick == null) {
            dialog.cancel()
        } else {
            onClick.buttonClick(true)
            dialog.cancel()
        }
    }

}

fun FragmentActivity.displayPopUpOptions(
    onClick: GeneralListener? = null,
    message: String = "",
    isCancelable: Boolean = true
) {
    val binding = ItemDialogUserOptionBinding.inflate(layoutInflater)
    val builder = AlertDialog.Builder(this)
    builder.setCancelable(isCancelable)
    builder.setView(binding.root)
    val dialog = builder.create()
    dialog.window?.decorView?.setBackgroundResource(R.drawable.alert_dialog_background)
    dialog.show()

    if (message == this.getString(R.string.are_you_sure_to_delete_profile)) {
        binding.civAlert.visibility = View.GONE
        binding.tvSubTitlePopUp.text = message
    } else {
        binding.tvSubTitlePopUp.text = getString(R.string.are_you_sure_to_add_profile)
    }

    if (message.isNotEmpty()) {
        binding.tvSubTitlePopUp.text = message
    }

    binding.btnOkPopUp.setOnClickListener {
        dialog.cancel()
        onClick?.let { onClick.buttonClick(true) }
    }

    binding.btnCancel.setOnClickListener { dialog.cancel() }

}

fun FragmentActivity.drawable(icon: Int): Drawable? {
    return ResourcesCompat.getDrawable(resources, icon, null)
}

fun FragmentActivity.isOnline(): Boolean {
    val connectivityManager =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    if (capabilities != null) {
        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                return true
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                return true
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                return true
            }
        }
    }
    return false
}

fun FragmentActivity.getUserData(getData: GeneralListener? = null) {
    if (!isOnline()) {
        displayPopUp(
            getString(R.string.error),
            getString(R.string.no_internet_access)
        )
        return
    }

    val key = if (Paper.book().read<User>(Constant.k_activeUser)?.id?.isNotEmpty() == true) {
        Paper.book().read<User>(Constant.k_activeUser)?.id
    } else {
        Paper.book().read<String>(Constant.k_authId)
    }

    key?.let { authId ->
        Constant.rootRef.child(Constant.k_tableUser)
            .child(authId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        try {
                            snapshot.getValue(User::class.java)?.let {

                                if (it.isDeleted) {
                                    displayPopUp(
                                        getString(R.string.error),
                                        getString(R.string.account_disabled)
                                    )
                                    return
                                }

                                Paper.book().write(Constant.k_activeUser, it)
                                saveCheckedLinks()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        getData?.buttonClick(true)
                    } else {
                        getData?.buttonClick(false)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    displayPopUp(
                        getString(R.string.error),
                        error.message
                    )
                }
            })
    }

}

fun saveCheckedLinks() {

    Constant.mListSocialLinks.forEach { it.value = "" }

    val user = Paper.book().read<User>(Constant.k_activeUser)!!
    if (user.links.isNotEmpty()) {
        for (i in user.links.indices) {
            for (j in Constant.mListSocialLinks.indices) {
                if (user.links[i]?.name == Constant.mListSocialLinks[j].name) {
                    Constant.mListSocialLinks[j].value =
                        user.links[i]?.value
                    break
                }
            }
        }
    }
}

fun FragmentActivity.displayBottomSheet(
    name: String?,
    image: Drawable?,
    value: String?,
    selection: GeneralListener,
    isDisabled: Boolean = false
) {

    val binding = ItemSocialLinkBsBinding.inflate(layoutInflater)
    val builder = BottomSheetDialog(this, R.style.bottomSheetStyle)
    builder.setContentView(binding.root)
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

    binding.tvTitle.text = name
    binding.tilSocialLink.hint = name
    binding.ivSocialLogo.setImageDrawable(image)

    if (value?.isNotEmpty() == true) {
        binding.tilSocialLink.editText?.setText(value)
    }

    binding.ivDown.setOnClickListener {
        builder.dismiss()
    }

    binding.btnOpen.setOnClickListener {

        if (binding.tilSocialLink.value().isEmpty()) {
            binding.tilSocialLink.error = resources.getString(R.string.editTextEmptyFieldError)
            return@setOnClickListener
        }

        selection.bottomSheetListener("open", binding.tilSocialLink.value())
        builder.dismiss()
    }

    binding.btnDelete.setOnClickListener {
        if (!isDisabled) {
            if (binding.tilSocialLink.value().isEmpty()) {
                binding.tilSocialLink.error = getString(R.string.nothing_to_delete)
                return@setOnClickListener
            }

            selection.bottomSheetListener("delete", binding.tilSocialLink.value())
            builder.dismiss()
        }
    }

    binding.btnSave.setOnClickListener {
        if (!isDisabled) {

            if (binding.tilSocialLink.value().isEmpty()) {
                binding.tilSocialLink.error = getString(R.string.editTextEmptyFieldError)
                return@setOnClickListener
            }

            selection.bottomSheetListener("save", binding.tilSocialLink.value())
            builder.dismiss()
        }
    }

    binding.btnOpen.requestFocus()

    if (isDisabled) {
        binding.civQuestion.visibility = View.GONE
    }

    val instructionsBuilder = AlertDialog.Builder(this)
    lateinit var dialog: AlertDialog

    for (i in getInstructionsMessage().indices) {
        if (name == getInstructionsMessage()[i].name) {
            instructionsBuilder.setTitle("Instructions")
            instructionsBuilder.setMessage(getInstructionsMessage()[i].message)
            instructionsBuilder.setPositiveButton("Ok") { instructionDialog, _ ->
                instructionDialog.cancel()
            }
            dialog = instructionsBuilder.create()
            break
        }
    }

    binding.civQuestion.setOnClickListener {
        dialog.show()
    }

}

fun FragmentActivity.getInstructionsMessage() = mutableListOf(
    Instruction(
        "Add your phone number including your country code (eg: +6581234567)",
        getString(R.string.phone)
    ),
    Instruction(
        "1: Open up your Instagram app and log into your account.\n" +
                "2: Click on your profile picture at the bottom right corner.\n" +
                "3: Your username will be shown at the very top of your profile (above your profile" +
                "picture).\n" +
                "4: Paste your username into the Instagram URL field.",
        getString(R.string.instagram)
    ),
    Instruction(
        "If you are using the Facebook app on a mobile phone:\n" +
                "1: Open up your Facebook app and log into your Facebook account.\n" +
                "2: From the home page, click on the menu icon at the bottom right corner (It looks like" +
                "three horizontal lines.)\n" +
                "3: Click on your profile picture to go to your profile page.\n" +
                "4: Click on the Profile settings tab (three dots).\n" +
                "5: Click on Copy Link to copy your full Facebook profile link.\n" +
                "6: Paste the link into the Facebook URL field. (e.g. www.facebook.com/your_fb_id).\n",
        getString(R.string.facebook)
    ),
    Instruction(
        "1. Open your TikTok app and log into your account.\n" +
                "2. Click on profile located at the bottom right corner.\n" +
                "3. Your username will be shown under your profile picture.\n" +
                "4. Copy and paste your username into the TikTok URL field.",
        getString(R.string.tiktok)
    ),
    Instruction(
        "Add your phone number including your country code (e.g. +6581234567)",
        getString(R.string.whatsapp)
    ),
    Instruction(
        "If you are using the LinkedIn app on a mobile phone:\n" +
                "1: Open your LinkedIn app and log into your account.\n" +
                "2: From the home page, click on the profile picture in the top left.\n" +
                "3: Click on the right side menu button (three dots).\n" +
                "4: Select Share via…\n" +
                "5: Select Copy.\n" +
                "6: Paste the copied link into the LinkedIn URL field.\n",
        getString(R.string.linkedIn)
    ),
    Instruction(
        "1. Open your Telegram app and log into your account.\n" +
                "2. Click on settings located at the bottom right corner.\n" +
                "3. Your username will be shown under your contact number.\n" +
                "4. Copy and paste your username into the Telegram URL field.",
        getString(R.string.telegram)
    ),
    Instruction(
        "1: Open your Snapchat app and log into your account.\n" +
                "2: Tap into your profile icon at the top left corner of the screen.\n" +
                "3: Your username is shown next to your Snapchat score.\n" +
                "4: Copy and paste the username into the Snapchat URL field.\n",
        getString(R.string.snapchat)
    ),
    Instruction(
        "1. Open your Twitter app and log into your account.\n" +
                "2. Click on your profile picture located at the top left corner.\n" +
                "3. Your username will be shown under your profile picture.\n" +
                "4. Copy and paste your username into the Twitter URL field.",
        getString(R.string.twitter)
    ),
    Instruction(
        "1. Sign in to your YouTube Studio account.\n" +
                "2. From the Menu, select Customisation Basic Info.\n" +
                "3. Click into the Channel URL and copy the link.\n" +
                "4. Paste the copied link into the YouTube URL field.",
        getString(R.string.youtube)
    ),
    Instruction(
        "1. Search for your favourite Artist or Albums.\n" +
                "2. Click on the three dots menu selection and select share.\n" +
                "3. Select Copy Link.\n" +
                "4. Paste the copied link into the Spotify URL Field.",
        getString(R.string.spotify)
    ),
    Instruction(
        "Input your email address.",
        getString(R.string.email)
    ),
    Instruction(
        "1: Go to www.paypal.com and log in.\n" +
                "2: Under your profile, select Get Paypal.me\n" +
                "3: Create a Paypal.me profile.\n" +
                "4: Copy your created Paypal.me link and paste it into the Paypal URL field.\n",
        getString(R.string.paypal)
    ),
    Instruction(
        "1: Open your Pinterest app and log into your account.\n" +
                "2: Click onto your profile picture located at the bottom right corner.\n" +
                "3: Click into the three dots menu located at the top right corner.\n" +
                "4: Select copy profile link.\n" +
                "5: Paste the copied link into the Pinterest URL field.",
        getString(R.string.pinterest)
    ),
    Instruction(
        "1: Open up your Calendly app and log into your account.\n" +
                "2: Copy the URL link below your name.\n" +
                "3: Paste the copied link into the Calendly URL field.\n",
        getString(R.string.calendly)
    )
)

fun FragmentActivity.handleSocialLinkClick(
    source: String,
    name: String,
    value: String?,
    ivSelected: ImageView?,
    item: SocialLink?
) {
    when (source) {
        "open" -> {
            openSocialApp(item, value)
        }
        "delete" -> {
            handleSaveOrDelete(name, "", ivSelected)
        }
        "save" -> {
            handleSaveOrDelete(name, value, ivSelected)
        }
        else -> {
            displayPopUp(
                getString(R.string.error),
                getString(R.string.error)
            )
        }
    }
}

fun FragmentActivity.openSocialApp(item: SocialLink?, userId: String?) {

    if (item?.packageName?.isNotEmpty() == true) {
        if (isPackageInstalled(item.packageName!!)) {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("${item.baseUrl}$userId")
                this.startActivity(intent)
            } catch (e: Exception) {
                Log.e("Utils", "openSocialApp: ${e.message}")
            }
        } else {
            try {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data =
                    Uri.parse("https://play.google.com/store/apps/details?id=${item.packageName}")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.startActivity(intent)
            } catch (e: Exception) {
                Log.e("Utils", "openSocialApp: ${e.message}")
            }
        }
    } else {
        when (item?.baseUrl) {
            "phone" -> {
                try {
                    val intent = Intent(Intent.ACTION_DIAL)
                    intent.data = Uri.parse("tel:$userId")
                    this.startActivity(intent)
                } catch (e: Exception) {
                    Log.e("Utils", "openSocialApp: ${e.message}")
                }
            }
            "web" -> {
                try {
                    val webpage = Uri.parse(userId)
                    val myIntent = Intent(Intent.ACTION_VIEW, webpage)
                    this.startActivity(myIntent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        this,
                        "No application can handle this request. Please install a web browser or check your URL.",
                        Toast.LENGTH_LONG
                    ).show()
                    e.printStackTrace()
                }
            }
            "message" -> {
                try {
                    val sendIntent = Intent(Intent.ACTION_VIEW)
                    sendIntent.data = Uri.parse("sms:")
                    sendIntent.putExtra("sms_body", userId)
                    sendIntent.type = "vnd.android-dir/mms-sms"
                    this.startActivity(sendIntent)
                } catch (e: Exception) {
                    Log.e("Utils", "openSocialApp: ${e.message}")
                }
            }
            "email" -> {
                try {
                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$userId")
                    }
                    this.startActivity(Intent.createChooser(emailIntent, "Send Mail"))
                } catch (e: Exception) {
                    Log.e("Utils", "openSocialApp: ${e.message}")
                }
            }
            else -> {
                displayPopUp(
                    getString(R.string.error),
                    getString(R.string.no_package_name)
                )
            }
        }
    }
}

fun Context.isPackageInstalled(packageName: String): Boolean {
    val packageManager = this.packageManager
    val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
    val list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
    return list.size > 0
}

fun handleSaveOrDelete(name: String?, value: String?, ivSelected: ImageView?) {
    for (i in Constant.mListSocialLinks.indices) {
        if (Constant.mListSocialLinks[i].name == name) {
            Constant.mListSocialLinks[i].value = value
            ivSelected?.visibility = if (value == "") {
                View.GONE
            } else {
                View.VISIBLE
            }
            break
        }
    }
}

fun FragmentActivity.handleDelete(name: String?, result: GeneralListener) {
    showLoading()
    val query =
        Constant.rootRef.child(Constant.k_tableUser)
            .child(Paper.book().read<String>(Constant.k_authId)!!)
            .child("links").orderByChild("name").equalTo(name)

    query.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                for (ds in snapshot.children) {
                    ds.ref.removeValue()
                }
                for (i in Constant.mListSocialLinks.indices) {
                    if (Constant.mListSocialLinks[i].name == name) {
                        Constant.mListSocialLinks[i].value = ""
                        break
                    }
                }
                result.buttonClick(true)
                cancelLoading()
            } else {
                cancelLoading()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            this@handleDelete.displayPopUp(
                getString(R.string.error),
                error.message
            )
        }

    })

}

fun FragmentActivity.handleSave(
    name: String?,
    value: String?,
    result: GeneralListener
) {
    showLoading()
    val query =
        Constant.rootRef.child(Constant.k_tableUser)
            .child(Paper.book().read<String>(Constant.k_authId)!!)
            .child("links").orderByChild("name").equalTo(name)

    query.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.exists()) {
                for (ds in snapshot.children) {
                    ds.child("value").ref.setValue(value)
                }
                for (i in Constant.mListSocialLinks.indices) {
                    if (Constant.mListSocialLinks[i].name == name) {
                        Constant.mListSocialLinks[i].value = value
                        break
                    }
                }
                result.buttonClick(true)
                cancelLoading()
            } else {
                cancelLoading()
            }
        }

        override fun onCancelled(error: DatabaseError) {
            this@handleSave.displayPopUp(
                resources.getString(R.string.error),
                error.message
            )
        }

    })
}

fun loadImage(image: String?, imageView: ImageView) {

    image?.let {
        if (it.contains("gs://") || it.contains("http")) {
            FirebaseStorage.getInstance()
                .getReferenceFromUrl(it).downloadUrl.addOnSuccessListener { uri ->
                    try {
                        Glide.with(imageView.context)
                            .load(uri)
                            .placeholder(R.drawable.ic_image_placeholder)
                            .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                            .into(imageView)
                    } catch (e: Exception) {
                        Log.e(TAG, "loadImage: ${e.printStackTrace()}")
                    }
                }
            return
        }
    }

    try {
        Glide.with(imageView.context)
            .load(image)
            .placeholder(R.drawable.ic_image_placeholder)
            .into(imageView)
    } catch (e: Exception) {
        Log.e(TAG, "loadImage: ${e.printStackTrace()}")
    }

}

fun TextView.copyOnHold(customText: String? = null) {
    setOnClickListener {
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", customText ?: text)
        clipboardManager.setPrimaryClip(clip)
        it.context.toast { "Link Copied" }
    }
}

fun FragmentActivity.openBrowser(url: String) {
    if (url.startsWith("https://") || url.startsWith("http://")) {
        val uri: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    } else {
        toast { "Invalid Url" }
    }
}

fun FragmentActivity.contactUs() {
    val email = "support@tappze.com"
    val subject = "Support"
    val body = "Please send us your issue in detail, we're happy to learn from you"

    val selectorIntent = Intent(Intent.ACTION_SENDTO)
    val urlString =
        "mailto:" + Uri.encode(email) + "?subject=" + Uri.encode(subject) + "&body=" + Uri.encode(
            body
        )
    selectorIntent.data = Uri.parse(urlString)

    val emailIntent = Intent(Intent.ACTION_SEND)
    emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
    emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
    emailIntent.putExtra(Intent.EXTRA_TEXT, body)
    emailIntent.selector = selectorIntent

    startActivity(Intent.createChooser(emailIntent, "Send email"))
}

fun FragmentActivity.signOut() {
    if (Constant.mAuth.currentUser != null) {
        Constant.mAuth.signOut()
        Paper.book().destroy()
        openActivity(IntroActivity::class.java)
    }
}

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

fun FragmentActivity.displayBottomSheetForBio(
    value: String?,
    buttonClicked: GeneralListener
) {

    val binding = ItemBioBsBinding.inflate(layoutInflater)
    val builder = BottomSheetDialog(this, R.style.bottomSheetStyle)
    builder.setContentView(binding.root)
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

    if (value?.isNotEmpty() == true) {
        binding.tilBio.editText?.text = value.toEditable()
    }

    binding.ivDown.setOnClickListener {
        builder.dismiss()
    }

    binding.btnSave.setOnClickListener {
        buttonClicked.bottomSheetListener("save", binding.tilBio.value())
        builder.dismiss()
    }

}

fun FragmentActivity.getDate(result: GeneralListener) {
    val cal = Calendar.getInstance()
    val dpd = DatePickerDialog(
        this, R.style.datePickerDialog, { _, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val myFormat = "dd.MM.yyyy"
            val sdf = SimpleDateFormat(myFormat, Locale.US)

            result.pickDate(sdf.format(cal.time))
        },
        cal.get(Calendar.YEAR),
        cal.get(Calendar.MONTH),
        cal.get(Calendar.DAY_OF_MONTH)
    )
    dpd.show()
}

fun randInt(min: Int, max: Int) = Random().nextInt(max - min + 1) + min

fun FragmentActivity.openWebsite() {
    try {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse("https://tappze.com/")
        startActivity(i)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
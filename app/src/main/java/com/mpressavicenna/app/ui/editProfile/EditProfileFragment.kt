package com.mpressavicenna.app.ui.editProfile

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.mpressavicenna.app.R
import com.mpressavicenna.app.databinding.FragmentEditProfileBinding
import com.mpressavicenna.app.model.User
import com.mpressavicenna.app.ui.HomeActivity
import com.mpressavicenna.app.ui.purchase.PurchaseActivity
import com.mpressavicenna.app.util.*
import com.mpressavicenna.app.util.Loading.cancelLoading
import com.mpressavicenna.app.util.Loading.showLoading
import id.zelory.compressor.Compressor
import io.paperdb.Paper
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.io.File
import java.lang.Exception

class EditProfileFragment : Fragment(R.layout.fragment_edit_profile) {

    private lateinit var binding: FragmentEditProfileBinding
    lateinit var mListGender: MutableList<String>
    var updateUser = User()
    var profileImage: Uri? = null
    var coverImage: Uri? = null
        private val startForProfileImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val fileUri = data?.data!!
                        profileImage = fileUri
                        binding.civUser.setImageURI(fileUri)
                    }

                    ImagePicker.RESULT_ERROR -> {
                        toast { ImagePicker.getError(data) }
                    }

                    else -> {
                        toast { "Task Cancelled" }
                    }
                }
            }

    private val startForQrImgResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data

            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    Constant.k_itemQrImgUri.value = fileUri
                }

                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(
                        requireActivity(),
                        ImagePicker.getError(data),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> {
                    Toast.makeText(requireActivity(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val startForCoverImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
            val data = result.data
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val fileUri = data?.data!!
                    coverImage = fileUri
                    binding.civCoverPhoto.setImageURI(fileUri)
                }

                ImagePicker.RESULT_ERROR -> {
                    toast { ImagePicker.getError(data) }
                }

                else -> {
                    toast { "Task Cancelled" }
                }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentEditProfileBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE
        Constant.k_viewLifeCycleOwner = viewLifecycleOwner

        binding.tvBio.setOnClickListener {
            requireActivity().displayBottomSheetForBio(
                updateUser.bio.ifEmpty { "" },
                object : GeneralListener {
                    override fun bottomSheetListener(
                        source: String,
                        value: String?,
                        uri: String?,
                        linkedId: Int,
                        title: String?
                    ) {
                        super.bottomSheetListener(source, value, uri, linkedId, title)
                        value?.let { bio ->
                            updateUser.bio = bio
                            binding.tvBio.text = bio
                        }
                    }
                })
        }

        binding.btnCancel.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            if (binding.tvFullName.text.toString().trim().isNotEmpty()) {
                requireActivity().showLoading()
                val job = CoroutineScope(Dispatchers.IO).launch { collectValues() }
                if (job.isCancelled) {
                    requireActivity().cancelLoading()
                }
            }
        }

        initializeFields()
        setUserData()

    }

    private fun initializeFields() {

        mListGender = mutableListOf(
            resources.getString(R.string.male),
            resources.getString(R.string.female),
            resources.getString(R.string.undefined)
        )
        binding.spinnerGender.setItems(mListGender)
        binding.spinnerGender.setOnSpinnerItemSelectedListener<String> { _, _, _, newItem ->
            updateUser.gender = newItem
        }
        binding.tvSelectDate.setOnClickListener {
            requireActivity().getDate(
                object : GeneralListener {
                    override fun pickDate(value: String) {
                        binding.tvSelectDate.text = value
                        updateUser.dob = value
                    }
                })
        }
        binding.civUser.setOnClickListener {

            /*if (Paper.book()
                    .read<User>(Constant.k_activeUser)?.subscription != "yearly" || Paper.book()
                    .read<User>(Constant.k_activeUser)?.subscription == "lifeTime"
            ) {
                requireActivity().displayPopUpOptions(
                    object : GeneralListener {
                        override fun buttonClick(clicked: Boolean) {
                            requireActivity().openActivity(PurchaseActivity::class.java, false)
                        }
                    },
                    "Do you want to subscribe to Yearly or Life Time?",
                )
                return@setOnClickListener
            }*/

            ImagePicker.with(this)
                .cropSquare()
                .compress(1024)
                .maxResultSize(1080, 650)
                .createIntent {
                    startForProfileImageResult.launch(it)
                }
        }

        binding.civCoverPhoto.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .crop(16f, 9f)
                .compress(1024)
                .maxResultSize(1080, 650)
                .createIntent {
                    startForCoverImageResult.launch(it)
                }
        }

        binding.addCoverPhoto.setOnClickListener {
            ImagePicker.with(this)
                .cropSquare()
                .crop(16f, 9f)
                .compress(1024)
                .maxResultSize(1080, 650)
                .createIntent {
                    startForCoverImageResult.launch(it)
                }
        }

    }

    private fun setUserData() {

        try {
            updateUser = Paper.book().read<User>(Constant.k_activeUser)!!

            if (updateUser.name.isNotEmpty()) {
                binding.tvFullName.text = updateUser.name.toEditable()
            }

            if (updateUser.phone?.isNotEmpty() == true) {
                binding.etPhone.text = updateUser.phone?.toEditable()
            }

            if (updateUser.bio.isNotEmpty()) {
                binding.tvBio.text = updateUser.bio.toEditable()
            }

            if (updateUser.dob.isNotEmpty()) {
                binding.tvSelectDate.text = updateUser.dob.toEditable()
            }

            if (updateUser.company.isNotEmpty()) {
                binding.etCompany.text = updateUser.company.toEditable()
            }
        } catch (_: NullPointerException) {

        }

        if (updateUser.gender?.isNotEmpty() == true) {
            for (i in mListGender.indices) {
                if (mListGender[i].lowercase() == updateUser.gender?.lowercase()) {
                    binding.spinnerGender.selectItemByIndex(i)
                    break
                }
            }
        }
        if (profileImage != null) {
            binding.civUser.setImageURI(profileImage)
        } else {
            try {
                loadImage(updateUser.profileUrl, binding.civUser)
            } catch (e: Exception) {

            }
        }

        if (coverImage != null) {
            binding.civCoverPhoto.setImageURI(coverImage)
        } else {
            try {
                if (updateUser.coverUrl.isNotEmpty()) {
                    loadImage(updateUser.coverUrl, binding.civCoverPhoto)
                }
            } catch (e: Exception) {

            }
        }

        binding.rvProfile.apply {
            adapter = EditProfileAdapter(
                requireActivity(),
                Constant.mListSocialLinks.distinctBy { it.name }
                    .toMutableList(),
                startForQrImgResult)
        }

    }

    private fun collectValues() = CoroutineScope(Dispatchers.IO).launch {

        binding.tvFullName.text.trim().let { updateUser.name = it.toString() }
        binding.etPhone.text.trim().let { updateUser.phone = it.toString() }
        binding.tvBio.text.toString().trim().let { updateUser.bio = it }
        binding.etCompany.text.toString().trim().let { updateUser.company = it }
        //updateUser.dob = binding.tvSelectDate.text.toString()
        if (profileImage != null) {
            val job = async { uploadImage("profilePic", profileImage!!) }
            updateUser.profileUrl =
                if (job.await().contains("%3A")) job.await().replace("%3A", ":") else job.await()
            job.join()
        }

        if (coverImage != null) {
            val job = async { uploadImage("coverPic", coverImage!!) }
            updateUser.coverUrl =
                if (job.await().contains("%3A")) job.await().replace("%3A", ":") else job.await()
            job.join()
        }

        updateUser.leadMode = Paper.book().read<User>(Constant.k_activeUser)?.leadMode!!
        updateUser.links =
            Constant.mListSocialLinks.filter { it.value != "" }.toMutableList()

        Log.e("ProfileFragment", "collectValues: $updateUser")

        /*if (socialLinksValidation()) {

        } else {
            requireActivity().cancelLoading()
        }*/

        Constant.rootRef.child(Constant.k_tableUser)
            .child(Paper.book().read<User>(Constant.k_activeUser)?.id!!)
            .setValue(updateUser)
            .addOnSuccessListener {
                Constant.rootRef.child(Constant.k_tableUser)
                    .child(Paper.book().read<User>(Constant.k_activeUser)?.id!!)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                Paper.book().write(
                                    Constant.k_activeUser,
                                    snapshot.getValue(User::class.java)!!
                                )
                                saveCheckedLinks()
                                requireActivity().displayPopUp(
                                    getString(R.string.success),
                                    resources.getString(R.string.profile_updated_success),
                                    object : GeneralListener {
                                        override fun buttonClick(clicked: Boolean) {
                                            requireActivity().openActivity(
                                                HomeActivity::class.java,
                                                true
                                            )
                                        }
                                    })
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            requireActivity().displayPopUp(
                                resources.getString(R.string.error),
                                error.message,
                                null
                            )
                        }
                    })
            }
            .addOnFailureListener {
                requireActivity().displayPopUp(
                    resources.getString(R.string.error),
                    it.message,
                    null
                )
            }

    }

    private suspend fun uploadImage(source: String, image: Uri): String {
        val path = "$source:${Paper.book().read<User>(Constant.k_activeUser)?.id}.${
            MimeTypeMap.getFileExtensionFromUrl(
                image.toString()
            )
        }"

        val imageRef = Constant.mStorage.child(path)
        return withContext(Dispatchers.IO) {
            imageRef
                .putFile(Uri.fromFile(Compressor.compress(requireContext(), File(image.path!!))))
                .await()
                .storage
                .metadata
                .await()
                .reference
                .toString()
        }
    }

    private fun socialLinksValidation(): Boolean {

        if (updateUser.links.isEmpty()) {
            return true
        }

        val numberOfLinks = if (updateUser.isSubscribed) {
            if (Paper.book().read<User>(Constant.k_activeUser)?.isProVersion == false) {
                10
            } else {
                200
            }
        } else {
            6
        }

        Log.e("EditProfileFragment", "numberOfLinks: $numberOfLinks")
        Log.e("EditProfileFragment", "userLinks: ${updateUser.links.size}")

        if (updateUser.links.size > numberOfLinks) {
            binding.root.snackBar("You can add up-to $numberOfLinks links in your subscription.")
            requireActivity().displayPopUpOptions(
                object : GeneralListener {
                    override fun buttonClick(clicked: Boolean) {
                        requireActivity().openActivity(
                            PurchaseActivity::class.java,
                            false
                        )
                    }
                },
                "You can add up-to $numberOfLinks of links in your subscription.\nDo you want to subscribe to pro version?",
            )
        } else {
            return true
        }


        return false
    }

    fun openPurchaseActivity() {
        requireActivity().openActivity(PurchaseActivity::class.java, false)
    }

}
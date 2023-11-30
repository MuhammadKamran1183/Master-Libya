package com.masterlibya.app.ui.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.masterlibya.app.R
import com.masterlibya.app.databinding.FragmentProfileBinding
import com.masterlibya.app.model.User
import com.masterlibya.app.util.*
import com.masterlibya.app.util.Loading.cancelLoading
import com.masterlibya.app.util.Loading.showLoading
import io.paperdb.Paper

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private var updateUser = User()

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentProfileBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        /*requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility =
            View.VISIBLE*/

        Constant.k_viewLifeCycleOwner = viewLifecycleOwner
//        binding.btnProfileSwitch.isChecked = Paper.book().read<User>(Constant.k_activeUser)?.leadMode!!

//        binding.civEdit.setOnClickListener { findNavController().navigate(R.id.nav_edit_profile) }
//        binding.btnAddLink.setOnClickListener { findNavController().navigate(R.id.nav_edit_profile) }
//        binding.cvTopProfile.setOnClickListener { findNavController().navigate(R.id.nav_edit_profile) }

        binding.ivInfoLead.setOnClickListener {
            if (binding.btnProfileSwitch.isChecked) {
                requireActivity().displayPopUp(
                    getString(R.string.alert), getString(R.string.leaad_capture_is_on)
                )
            } else {
                requireActivity().displayPopUp(
                    getString(R.string.alert), getString(R.string.leaad_capture_is_off)
                )
            }
        }

            binding.btnProfileSwitch.setOnTouchListener { view, event ->

            if (event.action == MotionEvent.ACTION_UP) {

                if (!binding.btnProfileSwitch.isChecked) {

                    Constant.rootRef.child(Constant.k_tableUser)
                        .child(Paper.book().read<User>(Constant.k_activeUser)?.id!!)
                        .child("leadMode")
                        .setValue(true)
                        .addOnSuccessListener {
                            updateUser = Paper.book().read<User>(Constant.k_activeUser)!!
                            updateUser?.leadMode = true
                            Paper.book().write(Constant.k_activeUser, updateUser)
                            binding.btnProfileSwitch.isChecked = true
                        }
                } else {
                    Constant.rootRef.child(Constant.k_tableUser)
                        .child(Paper.book().read<User>(Constant.k_activeUser)?.id!!)
                        .child("leadMode")
                        .setValue(false)
                        .addOnSuccessListener {
                            updateUser = Paper.book().read<User>(Constant.k_activeUser)!!
                            updateUser?.leadMode = false
                            Paper.book().write(Constant.k_activeUser, updateUser)
                            binding.btnProfileSwitch.isChecked = false
                        }
                }

            }
            true
        }

        if (Paper.book().read<User>(Constant.k_activeUser)?.profileOn == 1) {
            binding.btnToggleProfile.check(R.id.btnProfileOn)
        } else {
            binding.btnToggleProfile.check(R.id.btnProfileOff)
        }

        binding.btnProfileSwitchDirect.isChecked =
            Paper.book().read<User>(Constant.k_activeUser)?.profileOn == 1

        binding.btnProfileSwitchDirect.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {

                if (!binding.btnProfileSwitchDirect.isChecked) {
                    changeProfileStatus(1)
                } else {
                    changeProfileStatus(0)
                }

            }
            true
        }

        binding.ivInfoDirect.setOnClickListener {
            if (!binding.btnProfileSwitchDirect.isChecked) {
                requireActivity().displayPopUp(
                    resources.getString(R.string.alert),
                    resources.getString(R.string.tag_turned_off)
                )
            } else {
                requireActivity().displayPopUp(
                    resources.getString(R.string.alert),
                    resources.getString(R.string.tag_turned_on)
                )
            }
        }

//        loadUserData()

    }

    private fun changeProfileStatus(profile: Int) {
        requireActivity().showLoading()
        Constant.rootRef.child(Constant.k_tableUser)
            .child(Paper.book().read<User>(Constant.k_activeUser)?.id!!)
            .child("profileOn").setValue(profile)
            .addOnSuccessListener {
                requireActivity().cancelLoading()
                val msg = if (profile == 0) {
                    binding.btnProfileSwitchDirect.isChecked = false
                    getString(R.string.tag_turned_off)
                } else {
                    binding.btnProfileSwitchDirect.isChecked = true
                    getString(R.string.tag_turned_on)
                }
                /*requireActivity().displayPopUp(
                    resources.getString(R.string.the_status_of_profile),
                    msg
                )*/
            }
    }

    private fun loadUserData() {

        val user = Paper.book().read<User>(Constant.k_activeUser)

        if (user?.profileUrl?.isNotEmpty() == true) {
            try {
                loadImage(
                    Paper.book().read<User>(Constant.k_activeUser)?.profileUrl!!,
                    binding.civProfile
                )
            } catch (e: Exception) {

            }
        }

        if (user?.coverUrl?.isNotEmpty() == true) {
            try {
                loadImage(
                    Paper.book().read<User>(Constant.k_activeUser)?.coverUrl!!,
                    binding.civCoverPhoto
                )
            } catch (e: Exception) {

            }
        }

        Log.e("ProfileFragment", "loadUserData: $user")
        binding.tvFullName.text = user?.name
        binding.tvEmail.text = user?.email

        if (user != null) {
            if (user.company != null && user.company.isNotEmpty()) {
                binding.tvCompany.visibility = View.VISIBLE
                binding.tvCompany.text = user?.company
            }
        }

        binding.tvUserName.text = user?.username
        /*if (user.phone?.isEmpty() == true) {
            binding.tvMobile.visibility = View.GONE
        } else {
//            binding.tvMobile.visibility = View.VISIBLE
            binding.tvMobile.text = user?.phone
        }*/

        val selectedLinksList = Constant.mListSocialLinks
            .filter { it.value?.isNotEmpty() == true }
            .distinctBy { it.name }.toMutableList()

        if (selectedLinksList.isNotEmpty()) {
            binding.tvPlaceHolder.visibility = View.GONE
            binding.rvHome.visibility = View.VISIBLE
            binding.rvHome.apply {
                adapter = ProfileAdapter(requireActivity(), selectedLinksList, startForQrImgResult)
            }
        } else {
            binding.tvPlaceHolder.visibility = View.VISIBLE
            binding.rvHome.visibility = View.GONE
        }

    }

}
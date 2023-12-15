package com.masterlibya.app.ui.settings

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.masterlibya.app.R
import com.masterlibya.app.databinding.FragmentSettingsBinding
import com.masterlibya.app.databinding.ItemAllProfilesBinding
import com.masterlibya.app.model.User
import com.masterlibya.app.ui.SplashActivity
import com.masterlibya.app.ui.purchase.PurchaseActivity
import com.masterlibya.app.util.*
import com.masterlibya.app.util.Loading.cancelLoading
import com.masterlibya.app.util.Loading.showLoading
import io.paperdb.Paper
import com.masterlibya.app.util.GeneralListener as GeneralListener1

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private lateinit var binding: FragmentSettingsBinding
    private var updateUser = User()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentSettingsBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        val mList = listOf(
            /*getString(R.string.how_to_use),
            getString(R.string.contact_us),
            getString(R.string.refer_a_friend),
            getString(R.string.add_profile),
            getString(R.string.change_profile),*/
            getString(R.string.my_business_network),
            getString(R.string.change_password),
            /*getString(R.string.affliliate_marketing),
            getString(R.string.subscription_plan),*/
            getString(R.string.sign_out),
            getString(R.string.delete_account),
            //getString(R.string.purchase_items)
        )

        val mListIcons = listOf(
            R.drawable.ic_global_network,
            R.drawable.ic_lock_24,
            R.drawable.ic_logout_24,
            R.drawable.ic_delete_24
        )

        val settingAdapter = SettingAdapter(requireActivity(), mList, mListIcons)

        binding.rvSettings.apply { adapter = settingAdapter }

        settingAdapter.itemClickListener = { item ->
            when (item) {
                getString(R.string.subscription_plan) -> {
                    requireActivity().openActivity(PurchaseActivity::class.java, false)
                }
                getString(R.string.purchase_items) -> {
                    requireActivity().openWebsite()
                }
                getString(R.string.add_profile) -> {
                    addProfile()
                }
                getString(R.string.change_profile) -> {
                    getUserProfiles()
                }
                getString(R.string.change_password) -> {
                    requireActivity().forgotPassword()
                }
                getString(R.string.contact_us) -> {
                    requireActivity().contactUs()
                }
                getString(R.string.sign_out) -> {
                    requireActivity().signOut()
                }
                getString(R.string.my_business_network) -> {
                    findNavController().navigate(R.id.businessNetworkFragment)
                }
                getString(R.string.delete_account) -> {
                    requireActivity().displayPopUpOptions(
                        object : com.masterlibya.app.util.GeneralListener {
                            override fun buttonClick(clicked: Boolean) {
                                super.buttonClick(clicked)
                                if (clicked) {
                                    Constant.mAuth.currentUser?.delete()
                                        ?.addOnCompleteListener { task ->
                                            if (task.isSuccessful) {

                                                Constant.rootRef.child(Constant.k_tableUser).child(
                                                    Paper.book()
                                                        .read<User>(Constant.k_activeUser)?.id!!
                                                ).removeValue()

                                                val gso =
                                                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                                        .build()
                                                val googleSignInClient =
                                                    GoogleSignIn.getClient(requireActivity(), gso)
                                                googleSignInClient.signOut()
                                                Paper.book().destroy()
                                                requireActivity().openActivity(SplashActivity::class.java)
                                            } else {
                                                requireActivity().displayPopUp(
                                                    getString(R.string.error),
                                                    task.exception?.message.toString()
                                                )
                                            }
                                        }
                                }
                            }
                        },
                        getString(R.string.are_you_sure_to_delete_profile)
                    )
                }
            }
        }

        if (Paper.book().read<User>(Constant.k_activeUser)?.profileOn == 1) {
            binding.btnToggleProfile.check(R.id.btnProfileOn)
        } else {
            binding.btnToggleProfile.check(R.id.btnProfileOff)
        }

        binding.btnToggleProfile.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btnProfileOn) {
                    changeProfileStatus(1)
                } else if (checkedId == R.id.btnProfileOff) {
                    changeProfileStatus(0)
                }
            }
        }

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

    }

    private fun changeProfileStatus(profile: Int) {
        requireActivity().showLoading()
        Constant.rootRef.child(Constant.k_tableUser)
            .child(Paper.book().read<User>(Constant.k_activeUser)?.id!!)
            .child("profileOn").setValue(profile)
            .addOnSuccessListener {
                val msg = if (profile == 0) {
                    getString(R.string.tag_turned_off)
                } else {
                    getString(R.string.tag_turned_on)
                }
                requireActivity().cancelLoading()
                /*requireActivity().displayPopUp(
                    resources.getString(R.string.the_status_of_profile),
                    msg
                )*/
            }
    }

    private fun addProfile() {
        requireActivity().displayPopUpOptions(
            object : GeneralListener1 {
                override fun buttonClick(clicked: Boolean) {
                    if (clicked) {
                        requireActivity().showLoading()
                        val user = User()
                        user.name = getString(R.string.full_name)
                        user.email = Paper.book().read<User>(Constant.k_activeUser)?.email
                        user.fcmToken = Paper.book().read<User>(Constant.k_activeUser)?.fcmToken
                        user.parentId = Paper.book().read<User>(Constant.k_activeUser)?.parentId!!
                        user.username =
                            "${Paper.book().read<User>(Constant.k_activeUser)?.username}${
                                randInt(
                                    0,
                                    9999
                                )
                            }"
                        val key = Constant.rootRef.child(Constant.k_tableUser)
                            .push()
                            .key
                        user.id = key

                        Constant.rootRef.child(Constant.k_tableUser)
                            .child(key!!)
                            .setValue(user)
                            .addOnSuccessListener {
                                requireActivity().cancelLoading()
                                getUserProfiles()
                            }
                    }
                }
            },
            "",
            false
        )
    }

    private fun getUserProfiles() {

        requireActivity().showLoading()
        val getUserProfiles: Query =
            Constant.rootRef.child(Constant.k_tableUser).orderByChild("parentID")
                .equalTo(Paper.book().read<User>(Constant.k_activeUser)?.parentId!!)

        getUserProfiles.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requireActivity().cancelLoading()
                val mListUser = mutableListOf<User>()
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

                displayProfiles(mListUser)

            }

            override fun onCancelled(error: DatabaseError) {
                requireActivity().cancelLoading()
                requireActivity().displayPopUp(
                    getString(R.string.error),
                    error.message
                )
            }
        })

    }

    private fun displayProfiles(mListUser: MutableList<User>) {
        val profileBinding = ItemAllProfilesBinding.inflate(layoutInflater)
        val builder = BottomSheetDialog(requireContext(), R.style.bottomSheetStyle)
        builder.setContentView(profileBinding.root)
        builder.also {
            val bottomSheet =
                it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = getBottomSheetDialogDefaultHeight()
        }
        builder.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
        builder.show()

        profileBinding.toolbar.setNavigationOnClickListener {
            builder.dismiss()
        }

        val profileAdapter = UserProfilesAdapter(requireActivity(), mListUser)
        profileBinding.rvUserProfiles.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = profileAdapter
        }
        val swipeGesture = object : SwipeGesture(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        if (profileAdapter.itemCount == 1) {
                            toast { "You can't delete your last profile." }
                            return
                        } else {
                            deleteProfile(
                                profileAdapter,
                                viewHolder.absoluteAdapterPosition,
                                builder
                            )
                        }
                    }
                }
            }
        }
        val touchHelper = ItemTouchHelper(swipeGesture)
        touchHelper.attachToRecyclerView(profileBinding.rvUserProfiles)
        profileAdapter.itemClickListener = { user ->
            Paper.book().write(Constant.k_activeUser, user)
            saveCheckedLinks()
            requireActivity().findNavController(R.id.nav_host_fragment_activity_home)
                .navigate(R.id.nav_profile)
            builder.dismiss()
        }
    }

    private fun deleteProfile(
        profileAdapter: UserProfilesAdapter,
        position: Int,
        builder: BottomSheetDialog
    ) {
        requireActivity().displayPopUp(
            resources.getString(R.string.error),
            "Are you sure to delete your profile?",
            object : GeneralListener1 {
                override fun buttonClick(clicked: Boolean) {
                    Constant.rootRef.child(Constant.k_tableUser)
                        .child(profileAdapter.getUser(position).id!!)
                        .removeValue().addOnSuccessListener {
                            profileAdapter.deleteItem(position)

                            requireActivity().getUserData(object : GeneralListener1 {
                                override fun buttonClick(clicked: Boolean) {
                                    builder.dismiss()
                                    requireActivity().cancelLoading()
                                    findNavController().navigate(R.id.nav_profile)
                                }
                            })

                        }
                }
            }
        )
    }

    private fun getBottomSheetDialogDefaultHeight(): Int {
        val displayMetrics = DisplayMetrics()
        (context as Activity?)?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.heightPixels * 80 / 100
    }

}
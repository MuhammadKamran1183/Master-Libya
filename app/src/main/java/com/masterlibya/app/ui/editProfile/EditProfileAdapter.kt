package com.masterlibya.app.ui.editProfile

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.masterlibya.app.databinding.ItemSocialLinkBinding
import com.masterlibya.app.model.SocialLink
import com.masterlibya.app.util.*

class EditProfileAdapter(
    val activity: FragmentActivity,
    private var mList: MutableList<SocialLink>,
    private var startForQrImgResult: ActivityResultLauncher<Intent>
) : RecyclerView.Adapter<EditProfileAdapter.EditProfileVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = EditProfileVH(
        ItemSocialLinkBinding.inflate(LayoutInflater.from(activity), parent, false)
    )

    override fun onBindViewHolder(holder: EditProfileVH, position: Int) = holder.run {
        holder.bind(mList[position])
    }

    override fun getItemCount() = mList.size

    inner class EditProfileVH(val binding: ItemSocialLinkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SocialLink) {
            binding.ivSocial.setImageDrawable(item.socialIcon)
            binding.tvIconName.text = item.name

            if (item.linkID == 50 || item.linkID == 51 || item.linkID == 52) {
                if (item.image!!.isNotEmpty()) {
                    loadImage(item.image!!, binding.ivSocial)
                }
            }

            binding.ivSelected.visibility =
                if (item.value?.isNotEmpty() == true) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                displayBottomSheet(item, binding, position)/*if (Paper.book()
                        .read<User>(Constant.k_activeUser)?.subscription == "monthly"
                ) {
                    if (mList.size >= 3) {
                        activity.displayPopUpOptions(
                            object : GeneralListener {
                                override fun buttonClick(clicked: Boolean) {
                                    activity.openActivity(PurchaseActivity::class.java, false)
                                }
                            },
                            "Do you want to subscribe to Yearly or Life Time?",
                        )
                    } else {
                        displayBottomSheet(item, binding)
                    }
                    return@setOnClickListener
                } else if (Paper.book()
                        .read<User>(Constant.k_activeUser)?.subscription == "yearly"
                ) {
                    if (mList.size >= 6) {
                        activity.displayPopUpOptions(
                            object : GeneralListener {
                                override fun buttonClick(clicked: Boolean) {
                                    activity.openActivity(PurchaseActivity::class.java, false)
                                }
                            },
                            "Do you want to subscribe to Life Time?",
                        )
                    } else {
                        displayBottomSheet(item, binding)
                    }
                } else if (Paper.book()
                        .read<User>(Constant.k_activeUser)?.subscription == "lifeTime"
                ) {
                    displayBottomSheet(item, binding)
                } else {
                    activity.displayPopUpOptions(
                        object : GeneralListener {
                            override fun buttonClick(clicked: Boolean) {
                                activity.openActivity(PurchaseActivity::class.java, false)
                            }
                        },
                        "Do you want to subscribe to Monthly, Yearly or Life Time?",
                    )
                }*/
            }

        }

    }

    private fun displayBottomSheet(
        item: SocialLink, binding: ItemSocialLinkBinding, position: Int
    ) {
        activity.displayBottomSheet(
            item.name,
            item.socialIcon,
            item.value,
            item.linkID,
            item.image,
            startForQrImgResult,
            object : GeneralListener {
                override fun bottomSheetListener(
                    source: String, value: String?, uri: String?, linkedId: Int, title: String?
                ) {
                    super.bottomSheetListener(source, value, uri, linkedId, title)
                    activity.handleSocialLinkClick(source,
                        item.name!!,
                        value,
                        binding.ivSelected,
                        item,
                        uri,
                        linkedId,
                        title,
                        object : GeneralListener {
                            override fun buttonClick(clicked: Boolean) {
                                if (clicked) {
                                    notifyItemChanged(position)
                                }
                            }

                            override fun bottomSheetListener(
                                source: String,
                                value: String?,
                                uri: String?,
                                linkedId: Int,
                                title: String?
                            ) {
                                super.bottomSheetListener(source, value, uri, linkedId, title)
                            }
                        })
                }
            },
            false
        )
    }

}
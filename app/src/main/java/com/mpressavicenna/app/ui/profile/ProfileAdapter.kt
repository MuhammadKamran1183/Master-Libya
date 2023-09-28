package com.mpressavicenna.app.ui.profile

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mpressavicenna.app.databinding.ItemSocialLinkBinding
import com.mpressavicenna.app.model.SocialLink
import com.mpressavicenna.app.util.*

class ProfileAdapter(
    val activity: FragmentActivity,
    private val mList: MutableList<SocialLink>,
    private val startForQrImgResult: ActivityResultLauncher<Intent>
) : RecyclerView.Adapter<ProfileAdapter.ProfileVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProfileVH(
        ItemSocialLinkBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ProfileVH, position: Int) = holder.run {
        holder.bind(mList[position])
    }

    override fun getItemCount() = mList.size

    inner class ProfileVH(val binding: ItemSocialLinkBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SocialLink) {

            binding.ivSocial.setImageDrawable(item.socialIcon)
            binding.tvIconName.text = item.name

            if (item.linkID == 50 || item.linkID == 51 || item.linkID == 52) {

                if (item.image!!.isNotEmpty()) {
                    loadImage(item.image!!, binding.ivSocial)
                }
            }

            itemView.setOnClickListener {
                activity.displayBottomSheet(
                    item.name,
                    item.socialIcon,
                    item.value,
                    item.linkID,
                    item.image,
                    startForQrImgResult,
                    object : GeneralListener {
                        override fun bottomSheetListener(
                            source: String,
                            value: String?,
                            uri: String?,
                            linkedId: Int,
                            title: String?
                        ) {
                            super.bottomSheetListener(source, value, uri, linkedId, title)
                            when (source) {
                                "open" -> {
                                    activity.openSocialApp(item, value, uri)
                                }

                                "delete" -> {
                                    activity.handleDelete(
                                        item.linkID,
                                        item.name,
                                        object : GeneralListener {
                                            override fun buttonClick(clicked: Boolean) {
                                                mList.removeAt(absoluteAdapterPosition)
                                                notifyItemRemoved(absoluteAdapterPosition)
                                            }
                                        })
                                }

                                "save" -> {
                                    activity.handleSave(
                                        binding.ivSocial,
                                        item.name,
                                        item.linkID,
                                        value,
                                        uri,
                                        item.image,
                                        title,
                                        object : GeneralListener {
                                            override fun buttonClick(clicked: Boolean) {
                                                try {
                                                    if (clicked){
                                                        //notifyDataSetChanged()
                                                        //notifyItemChanged(position)
                                                    }
                                                    mList[absoluteAdapterPosition].value = value
                                                    mList[absoluteAdapterPosition].image = uri
                                                    notifyItemChanged(position)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        })
                                }
                            }
                        }
                    },
                    false
                )
            }
        }
    }

}
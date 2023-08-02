package com.mpressavicenna.app.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mpressavicenna.app.databinding.ItemSocialLinkBinding
import com.mpressavicenna.app.model.SocialLink
import com.mpressavicenna.app.util.*

class ProfileAdapter(
    val activity: FragmentActivity,
    private val mList: MutableList<SocialLink>
) : RecyclerView.Adapter<ProfileAdapter.ProfileVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ProfileVH(
        ItemSocialLinkBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ProfileVH, position: Int) = holder.run {
        holder.bind(mList[position])
    }

    override fun getItemCount() = mList.size

    inner class ProfileVH(val binding: ItemSocialLinkBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SocialLink) {

            binding.ivSocial.setImageDrawable(item.socialIcon)
            binding.tvIconName.text = item.name

            itemView.setOnClickListener {
                activity.displayBottomSheet(
                    item.name,
                    item.socialIcon,
                    item.value,
                    object : GeneralListener {
                        override fun bottomSheetListener(source: String, value: String?) {
                            when (source) {
                                "open" -> { activity.openSocialApp(item, value) }
                                "delete" -> {
                                    activity.handleDelete(
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
                                        item.name,
                                        value,
                                        object : GeneralListener {
                                            override fun buttonClick(clicked: Boolean) {
                                                try {
                                                    mList[absoluteAdapterPosition].value = value
                                                }catch (e: Exception){
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
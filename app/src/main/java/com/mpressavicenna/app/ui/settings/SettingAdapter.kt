package com.mpressavicenna.app.ui.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.mpressavicenna.app.R
import com.mpressavicenna.app.databinding.ItemSettingsBinding

class SettingAdapter(
    val activity: FragmentActivity,
    private val mList: List<String>
) : RecyclerView.Adapter<SettingAdapter.SettingVH>() {

    var itemClickListener: ((item: String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = SettingVH(
        ItemSettingsBinding
            .inflate(LayoutInflater.from(activity), parent, false)
    )

    override fun onBindViewHolder(holder: SettingVH, position: Int) = holder.run {
        holder.bind(mList[position])
        holder.itemClick = itemClickListener
    }

    override fun getItemCount() = mList.size

    inner class SettingVH(val binding: ItemSettingsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var itemClick: ((item: String) -> Unit)? = null

        fun bind(item: String) {

            binding.tvSettings.text = item

            if (item == activity.getString(R.string.delete_account)) {
                binding.tvSettings.setTextColor(
                    ContextCompat.getColor(
                        activity,
                        R.color.holo_red_light
                    )
                )
            }

            itemView.setOnClickListener { itemClick?.invoke(item) }

        }

    }

}
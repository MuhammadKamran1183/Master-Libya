package com.masterlibya.app.ui.analytics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.masterlibya.app.R
import com.masterlibya.app.databinding.ItemAnalyticsBinding
import com.masterlibya.app.model.SocialLinkAnalytic
import com.masterlibya.app.util.displayPopUp
import com.masterlibya.app.util.loadImage

class AnalyticsAdapter(
    val activity: FragmentActivity,
    private var mList: MutableList<SocialLinkAnalytic>
) : RecyclerView.Adapter<AnalyticsAdapter.AnalyticsVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = AnalyticsVH(
        ItemAnalyticsBinding
            .inflate(LayoutInflater.from(activity), parent, false)
    )

    override fun onBindViewHolder(holder: AnalyticsVH, position: Int) = holder.run {
        holder.bind(mList[position])
    }

    override fun getItemCount() = mList.size

    inner class AnalyticsVH(val binding: ItemAnalyticsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SocialLinkAnalytic) {

            binding.civHomeIcon.setImageDrawable(item.image)
            binding.tvIconName.text = item.name
            binding.tvTotalCount.text = "${item.clicks}"

            if (item.linkId == 50 || item.linkId == 51 || item.linkId == 52) {
                loadImage(item.customImage, binding.civHomeIcon)
            }

            itemView.setOnClickListener {
                activity.displayPopUp(
                    activity.resources.getString(R.string.alert),
                    activity.resources.getString(R.string.each_link_individual)
                )
            }

        }

    }

}
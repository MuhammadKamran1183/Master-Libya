package com.masterlibya.app.ui.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.masterlibya.app.databinding.ItemUserProfileBinding
import com.masterlibya.app.model.User
import com.masterlibya.app.util.Constant
import com.masterlibya.app.util.loadImage
import io.paperdb.Paper

class UserProfilesAdapter(
    val activity: FragmentActivity,
    private val mList: MutableList<User>
) : RecyclerView.Adapter<UserProfilesAdapter.UserProfileVH>() {

    var itemClickListener: ((item: User) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = UserProfileVH(
        ItemUserProfileBinding
            .inflate(LayoutInflater.from(activity), parent, false)
    )

    override fun onBindViewHolder(holder: UserProfileVH, position: Int) = holder.run {
        holder.bind(mList[position])
        holder.itemClick = itemClickListener
    }

    override fun getItemCount() = mList.size

    fun deleteItem(position: Int) {
        mList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun getUser(position: Int) = mList[position]

    inner class UserProfileVH(val binding: ItemUserProfileBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var itemClick: ((item: User) -> Unit)? = null

        fun bind(item: User) {

            if (item.id == Paper.book().read<User>(Constant.k_activeUser)?.id){
                binding.ivSelected.visibility = View.VISIBLE
            } else {
                binding.ivSelected.visibility = View.GONE
            }

            item.name.let { binding.tvProfileName.text = it }
            if (item.profileUrl.isNotEmpty()){
                loadImage(item.profileUrl, binding.ivUserContact)
            }

            item.username.let { binding.tvProfileEmail.text = it }

            itemView.setOnClickListener { itemClick?.invoke(item) }

        }

    }

}
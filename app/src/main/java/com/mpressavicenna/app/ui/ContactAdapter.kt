package com.mpressavicenna.app.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.mpressavicenna.app.databinding.ItemContactsBinding
import com.mpressavicenna.app.model.Contact
import java.text.SimpleDateFormat
import java.util.*

class ContactAdapter(
    val activity: FragmentActivity, var mList: MutableList<Contact>
) : RecyclerView.Adapter<ContactAdapter.ContactVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ContactVH(
        ItemContactsBinding.inflate(LayoutInflater.from(activity), parent, false)
    )

    override fun onBindViewHolder(holder: ContactVH, position: Int) = holder.run {
        holder.bind(mList[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchList(items: MutableList<Contact>) {
        mList = items
        notifyDataSetChanged()
    }

    fun deleteItem(position: Int) {
        mList.removeAt(position)
        notifyItemRemoved(position)
    }

    override fun getItemCount() = mList.size

    inner class ContactVH(val binding: ItemContactsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Contact) {

            itemView.setOnClickListener {
                /*Constants.mListContactDetail.clear()
                Constants.mListContactDetail.add(item)
                activity.findNavController(R.id.nav_host_fragment_content_home)
                    .navigate(R.id.contactDetailFragment)*/
            }

            item.name?.let { binding.tvName.text = it }
            item.date?.let { binding.tvDate.text = getDateTime(it) }
            item.email?.let { binding.tvEmail.text = it }
            item.phone?.let { binding.tvPhone.text = it }
            item.job?.let { binding.tvJob.text = it }
            item.message?.let { binding.tvNote.text = it }
        }

        private fun getDateTime(s: Long): String {
            val sdf = SimpleDateFormat("dd, MMMM yyyy hh:mm a", Locale.US)
            val netDate = Date(s * 1000)
            return sdf.format(netDate)
        }

    }

}
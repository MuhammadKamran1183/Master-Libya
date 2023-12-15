package com.masterlibya.app.ui

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.masterlibya.app.R
import com.masterlibya.app.databinding.FragmentBusinessNetworkBinding
import com.masterlibya.app.model.Contact
import com.masterlibya.app.model.User
import com.masterlibya.app.util.Constant
import com.masterlibya.app.util.Loading.cancelLoading
import com.masterlibya.app.util.Loading.showLoading
import com.masterlibya.app.util.displayPopUp
import com.masterlibya.app.util.isOnline
import io.paperdb.Paper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BusinessNetworkFragment : Fragment() {
    private lateinit var binding: FragmentBusinessNetworkBinding
    var mListContacts = mutableListOf<Contact>()
    var filterListContacts = mutableListOf<Contact>()
    private lateinit var searchList: ArrayList<Contact>
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_business_network, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentBusinessNetworkBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        binding.ivBack.setOnClickListener { requireActivity().onBackPressed() }

        getContacts(Paper.book().read<User>(Constant.k_activeUser)!!.id)

        searchUser()
    }

    private fun searchUser() {

        binding.etSearchContact.setText("")

        binding.etSearchContact.addTextChangedListener { editable ->
            MainScope().launch {
                delay(400)
                editable?.let {
                    filterSearchData(editable.toString())
                }
            }
        }
    }

    private fun filterSearchData(str: String) {
        searchList = arrayListOf()
        searchList.clear()
        mListContacts.forEach {
            if (it.name!!.toLowerCase().contains(str.toLowerCase())) {
                searchList.add(it)
            }
        }
        if (mListContacts.isNotEmpty()) {
            contactAdapter.searchList(searchList)
        }

    }

    private fun getContacts(userId: String?) {
        binding.rvContacts.adapter = null
        userId?.let {
            if (!requireActivity().isOnline()) {
                requireActivity().displayPopUp(
                    getString(R.string.error), resources.getString(R.string.no_internet_access)
                )
                return
            }
            requireActivity().showLoading()

            val getContacts: Query =
                Constant.rootRef.child(Constant.k_tableContact).orderByChild("userid").equalTo(it)

            getContacts.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    requireActivity().cancelLoading()

                    if (snapshot.exists()) {
                        mListContacts.clear()
                        filterListContacts.clear()

                        snapshot.children.forEach { contact ->
                            try {
                                contact.getValue(Contact::class.java)
                                    ?.let { it1 -> mListContacts.add(it1) }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }

                        //filterListContacts = mListContacts

                        for (i in mListContacts.indices) {
                            for (j in mListContacts.indices) {
                                if (mListContacts[i].date!! == mListContacts[j].date && mListContacts[i].name!! == mListContacts[j].name) {
                                    filterListContacts.add(mListContacts[i])
                                    break
                                }
                            }
                        }

                        loadContacts(filterListContacts)
                        binding.etSearchContact.visibility = View.VISIBLE
                    } else {
                        binding.rvContacts.visibility = View.GONE
                        binding.tvPlaceholder.visibility = View.VISIBLE
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    requireActivity().displayPopUp(
                        resources.getString(R.string.error), error.message
                    )
                }
            })

        }

    }

    private fun loadContacts(mList: MutableList<Contact>) {

        Log.e("ContactFragment", "loadContacts: $mList")

        if (mList.isNotEmpty()) {
            binding.rvContacts.visibility = View.VISIBLE
            binding.etSearchContact.visibility = View.VISIBLE
            binding.tvPlaceholder.visibility = View.GONE
            contactAdapter = ContactAdapter(
                requireActivity(), mList.reversed().distinctBy { it.name }.toMutableList()
            )
            binding.rvContacts.apply { adapter = contactAdapter }
        } else {
            binding.rvContacts.visibility = View.GONE
            binding.tvPlaceholder.visibility = View.VISIBLE
        }
    }

}
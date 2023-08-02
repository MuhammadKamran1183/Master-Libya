package com.mpressavicenna.app.ui.analytics

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.mpressavicenna.app.R
import com.mpressavicenna.app.databinding.FragmentAnalyticsBinding
import com.mpressavicenna.app.model.Analytics
import com.mpressavicenna.app.model.SocialLinkAnalytic
import com.mpressavicenna.app.model.User
import com.mpressavicenna.app.ui.purchase.PurchaseActivity
import com.mpressavicenna.app.util.*
import com.mpressavicenna.app.util.Loading.cancelLoading
import com.mpressavicenna.app.util.Loading.showLoading
import io.paperdb.Paper

class AnalyticsFragment : Fragment(R.layout.fragment_analytics) {

    private lateinit var binding: FragmentAnalyticsBinding
    var analytics: Analytics? = Analytics()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding = FragmentAnalyticsBinding.bind(view)
        super.onViewCreated(view, savedInstanceState)

        getAnalytics()

    }

    private fun getAnalytics() {
        requireActivity().showLoading()
        val getAnalytics: Query =
            Constant.rootRef.child(Constant.k_tableAnalytic)
                .orderByChild("userid")
                .equalTo(Paper.book().read<User>(Constant.k_activeUser)?.id)

        getAnalytics.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requireActivity().cancelLoading()

                if (snapshot.exists()) {
                    snapshot.children.forEach {
                        analytics = it.getValue(Analytics::class.java)
                        return@forEach
                    }
                }

                populateAnalytics()

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

    private fun populateAnalytics() {
        loadImage(Paper.book().read<User>(Constant.k_activeUser)?.profileUrl, binding.civUser)
        binding.tvViewCounts.text = "${analytics?.totalClicks}"

        setRecycler()
    }

    private fun setRecycler() {
        val mList = Constant.mListSocialLinks
            .filter { it.value?.isNotEmpty() == true }
            .distinctBy { it.name }.toMutableList()

        val mListAnalytics = mutableListOf<SocialLinkAnalytic>()
        mList.forEach { userLinks ->
            analytics?.socialLinkAnalytic?.forEach { analyticLinks ->
                if (userLinks.name?.lowercase() == analyticLinks.name?.lowercase()) {
                    val analytic = SocialLinkAnalytic(
                        analyticLinks.clicks,
                        userLinks.socialIcon,
                        analyticLinks.name
                    )
                    mListAnalytics.add(analytic)
                }
            }
        }

        binding.rvAnalytics.apply {
            adapter = AnalyticsAdapter(requireActivity(), mListAnalytics)
        }

    }

}
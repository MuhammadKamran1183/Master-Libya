package com.masterlibya.app.ui.analytics

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import com.masterlibya.app.R
import com.masterlibya.app.databinding.FragmentAnalyticsBinding
import com.masterlibya.app.model.Analytics
import com.masterlibya.app.model.SocialLinkAnalytic
import com.masterlibya.app.model.User
import com.masterlibya.app.util.*
import com.masterlibya.app.util.Loading.cancelLoading
import com.masterlibya.app.util.Loading.showLoading
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
        try {
            loadImage(Paper.book().read<User>(Constant.k_activeUser)?.profileUrl, binding.civUser)
            binding.tvViewCounts.text = "${analytics?.totalClicks}"
        } catch (_: Exception) {

        }

        setRecycler()
    }

    private fun setRecycler() {
        val mList = Constant.mListSocialLinks
            .distinctBy { it.name }.toMutableList()

        val mListAnalytics = mutableListOf<SocialLinkAnalytic>()
        mList.forEach { userLinks ->
            analytics?.socialLinkAnalytic?.forEach { analyticLinks ->
                if (userLinks.name?.lowercase() == analyticLinks.name?.lowercase()) {
                    val analytic = SocialLinkAnalytic(
                        analyticLinks.clicks,
                        userLinks.socialIcon,
                        analyticLinks.name,
                        customImage = userLinks.image,
                        linkId = userLinks.linkID,
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
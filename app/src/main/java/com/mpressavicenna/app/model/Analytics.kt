package com.mpressavicenna.app.model

import com.google.firebase.database.PropertyName

data class Analytics(

    @set:PropertyName("id")
    @get:PropertyName("id")
    var id: String? = "",

    @set:PropertyName("links")
    @get:PropertyName("links")
    var socialLinkAnalytic: MutableList<SocialLinkAnalytic> = mutableListOf(),

    @set:PropertyName("linksEngCrntWk")
    @get:PropertyName("linksEngCrntWk")
    var linksEngCurrentWk: Int = 0,

    @set:PropertyName("linksEngPastWk")
    @get:PropertyName("linksEngPastWk")
    var linksEngPastWk: Int = 0,

    @set:PropertyName("tContactsMeCrntWk")
    @get:PropertyName("tContactsMeCrntWk")
    var tContactsMeCurrentWk: Int = 0,

    @set:PropertyName("tContactsMePastWk")
    @get:PropertyName("tContactsMePastWk")
    var tContactsMePastWk: Int = 0,

    @set:PropertyName("totalClicks")
    @get:PropertyName("totalClicks")
    var totalClicks: Int = 0,

    @set:PropertyName("userid")
    @get:PropertyName("userid")
    var userId: String? = ""
)
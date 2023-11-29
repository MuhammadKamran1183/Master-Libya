package com.mpressavicenna.app.model

import com.google.firebase.database.PropertyName

data class User(

    @set:PropertyName("address")
    @get:PropertyName("address")
    var address: String? = "",

    @set:PropertyName("bio")
    @get:PropertyName("bio")
    var bio: String = "",

    @set:PropertyName("tagUid")
    @get:PropertyName("tagUid")
    var tagUid: String = "",

    @set:PropertyName("leadMode")
    @get:PropertyName("leadMode")
    var leadMode: Boolean = false,

    @set:PropertyName("company")
    @get:PropertyName("company")
    var company: String = "",

    @set:PropertyName("dob")
    @get:PropertyName("dob")
    var dob: String = "",

    @set:PropertyName("email")
    @get:PropertyName("email")
    var email: String? = "",

    @set:PropertyName("fcmToken")
    @get:PropertyName("fcmToken")
    var fcmToken: String? = "",

    @set:PropertyName("gender")
    @get:PropertyName("gender")
    var gender: String? = "",

    @set:PropertyName("id")
    @get:PropertyName("id")
    var id: String? = "",

    @set:PropertyName("isDeleted")
    @get:PropertyName("isDeleted")
    var isDeleted: Boolean = false,

    @set:PropertyName("isProVersion")
    @get:PropertyName("isProVersion")
    var isProVersion: Boolean = false,

    @set:PropertyName("isSubscribed")
    @get:PropertyName("isSubscribed")
    var isSubscribed: Boolean = false,

    @set:PropertyName("isCardPurchased")
    @get:PropertyName("isCardPurchased")
    var isCardPurchased: Boolean = false,

    @set:PropertyName("isReqByMe")
    @get:PropertyName("isReqByMe")
    var isReqByMe: Boolean = false,

    @set:PropertyName("isReqByOther")
    @get:PropertyName("isReqByOther")
    var isReqByOther: Boolean = false,

    @set:PropertyName("links")
    @get:PropertyName("links")
    var links: MutableList<SocialLink?> = mutableListOf(),

    @set:PropertyName("name")
    @get:PropertyName("name")
    var name: String = "",

    @set:PropertyName("parentID")
    @get:PropertyName("parentID")
    var parentId: String = "",

    @set:PropertyName("phone")
    @get:PropertyName("phone")
    var phone: String? = "",

    @set:PropertyName("platform")
    @get:PropertyName("platform")
    var platform: String = "android",

    @set:PropertyName("profileOn")
    @get:PropertyName("profileOn")
    var profileOn: Int = 1,

    @set:PropertyName("profileUrl")
    @get:PropertyName("profileUrl")
    var profileUrl: String = "",

    @set:PropertyName("coverUrl")
    @get:PropertyName("coverUrl")
    var coverUrl: String = "",

    @set:PropertyName("subscription")
    @get:PropertyName("subscription")
    var subscription: String = "none",

    @set:PropertyName("subscriptionExpiryDate")
    @get:PropertyName("subscriptionExpiryDate")
    var subscriptionExpiryDate: String = "",

    @set:PropertyName("subscriptionPurchaseDate")
    @get:PropertyName("subscriptionPurchaseDate")
    var subscriptionPurchaseDate: String = "",

    @set:PropertyName("username")
    @get:PropertyName("username")
    var username: String? = ""

)

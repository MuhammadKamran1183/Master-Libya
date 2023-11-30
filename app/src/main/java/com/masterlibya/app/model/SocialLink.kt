package com.masterlibya.app.model

import android.graphics.drawable.Drawable
import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName

data class SocialLink(

    @set:PropertyName("image")
    @get:PropertyName("image")
    var image: String? = "",

    @set:PropertyName("linkID")
    @get:PropertyName("linkID")
    var linkID: Int = 0,

    @set:PropertyName("name")
    @get:PropertyName("name")
    var name: String? = "",

    @set:PropertyName("value")
    @get:PropertyName("value")
    var value: String? = "",

    @Exclude @set:Exclude @get:Exclude var socialIcon: Drawable? = null,

    @set:PropertyName("customLinkImg")
    @get:PropertyName("customLinkImg")
    var customLinkImg: String? = "",

   /* @set:PropertyName("isShared")
    @get:PropertyName("isShared")
    var isShared: Boolean? = false,*/

    @Exclude @set:Exclude @get:Exclude var packageName: String? = "",

    @Exclude @set:Exclude @get:Exclude var baseUrl: String? = "",

    @Exclude @set:Exclude @get:Exclude var title: Int = 0,

    @Exclude @set:Exclude @get:Exclude var directOn: Boolean = false

)

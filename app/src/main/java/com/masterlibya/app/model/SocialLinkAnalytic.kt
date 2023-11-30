package com.masterlibya.app.model

import android.graphics.drawable.Drawable
import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName

data class SocialLinkAnalytic(

    @set:PropertyName("clicks")
    @get:PropertyName("clicks")
    var clicks: Int = 0,

    @Exclude @set:Exclude @get:Exclude var image: Drawable? = null,

    @set:PropertyName("name")
    @get:PropertyName("name")
    var name: String? = "",

    @set:PropertyName("customImage")
    @get:PropertyName("customImage")
    var customImage: String? = "",

    @set:PropertyName("linkId")
    @get:PropertyName("linkId")
    var linkId: Int = 0

)

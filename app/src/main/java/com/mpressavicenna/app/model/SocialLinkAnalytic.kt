package com.mpressavicenna.app.model

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
    var name: String? = ""
)

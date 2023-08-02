package com.mpressavicenna.app.model

import android.graphics.drawable.Drawable
import com.google.firebase.database.Exclude
import com.google.firebase.database.PropertyName

data class SocialLink(

    @set:PropertyName("name")
    @get:PropertyName("name")
    var name: String? = "",

    @Exclude @set:Exclude @get:Exclude var socialIcon: Drawable? = null,

    @set:PropertyName("image")
    @get:PropertyName("image")
    var image: String? = "",

    @set:PropertyName("value")
    @get:PropertyName("value")
    var value: String? = "",

    @Exclude @set:Exclude @get:Exclude var packageName: String? = "",

    @Exclude @set:Exclude @get:Exclude var baseUrl: String? = "",

    @Exclude @set:Exclude @get:Exclude var title: Int = 0,

    @Exclude @set:Exclude @get:Exclude var directOn: Boolean = false

)

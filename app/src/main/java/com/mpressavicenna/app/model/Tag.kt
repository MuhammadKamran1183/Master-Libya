package com.mpressavicenna.app.model

import com.google.firebase.database.PropertyName

data class Tag(

    @set:PropertyName("id")
    @get:PropertyName("id")
    var id: String = "",

    @set:PropertyName("isDeleted")
    @get:PropertyName("isDeleted")
    var isDeleted: Boolean = false,

    @set:PropertyName("status")
    @get:PropertyName("status")
    var status: Boolean = false,

    @set:PropertyName("tagId")
    @get:PropertyName("tagId")
    var tagId: String = "",

    @set:PropertyName("username")
    @get:PropertyName("username")
    var username: String = "",

)

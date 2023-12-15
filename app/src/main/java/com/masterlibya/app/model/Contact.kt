package com.masterlibya.app.model

import com.google.firebase.database.PropertyName

data class Contact(

    @set:PropertyName("date")
    @get:PropertyName("date")
    var date: Long? = null,

    @set:PropertyName("email")
    @get:PropertyName("email")
    var email: String? = "",

    @set:PropertyName("id")
    @get:PropertyName("id")
    var id: String? = "",

    @set:PropertyName("job")
    @get:PropertyName("job")
    var job: String? = "",

    @set:PropertyName("message")
    @get:PropertyName("message")
    var message: String? = "",

    @set:PropertyName("name")
    @get:PropertyName("name")
    var name: String? = "",

    @set:PropertyName("phone")
    @get:PropertyName("phone")
    var phone: String? = "",

    @set:PropertyName("userid")
    @get:PropertyName("userid")
    var userid: String? = "",

)

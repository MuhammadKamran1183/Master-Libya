package com.masterlibya.app

import android.app.Application
import io.paperdb.Paper

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Paper.init(this)
    }

}
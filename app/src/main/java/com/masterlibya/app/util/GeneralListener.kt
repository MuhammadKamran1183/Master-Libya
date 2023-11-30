package com.masterlibya.app.util

interface GeneralListener {
    fun buttonClick(clicked: Boolean) {}
    fun bottomSheetListener(source: String, value: String?, uri: String?, linkedId: Int, title: String?) {}
    fun pickDate(value: String) {}
}
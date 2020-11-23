package com.supcon.supbeacon.event

import com.supcon.scanwriter.BaseEvent

class HttpEvent : BaseEvent() {
    var requestCode = -1
    var data: Any? = null
    var code : Int = -1
    var msg : String = ""
}
package com.techno.tron.print.utills

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Util {
     fun charRepeater(str:String, time:Int = 32):String{
        val strBuffer = StringBuffer()
        for(i in 1..time){
            strBuffer.append(str)
        }
        return strBuffer.toString()
    }

    fun dateFormatter(date:Date):String{
        val sdf = SimpleDateFormat("dd-MMM-yyyy hh:mm aa", Locale.getDefault())
        return sdf.format(date).toString()
    }
}
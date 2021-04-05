package com.brandon.hangtime

import com.google.firebase.Timestamp
import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


object FirebaseDataObjects {

    data class User(
            val UUID: String = "",
            val email: String = "",
            val name: String? = null
    ) : Serializable {
        override fun toString(): String = email
    }


    data class Event(
            val name: String = "",
            val startDateTime: Timestamp? = null,
            val endDateTime: Timestamp? =  null,
            val owner: String = "",
            val description: String? = null,
            val participants: List<String>? = null,
            val group: String? = null
            ) : Serializable {
        override fun toString(): String = name
    }




    data class Group(
            val groupName: String = "",
            val members: List<String>? = null,
            val owner: String = ""
    ) : Serializable {
        override fun toString():String = groupName
    }

    // this data class is not used directly with fire base
    // but it is used for the algorithm to draw events
    // in the group calendar
    data class EventTimeComponent(
            val hour : Int,
            val minute : Int,
            val isStart : Boolean
    )


    fun toTimestamp(ldt:LocalDateTime):Timestamp {
        return Timestamp(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()))
    }

    fun toLocalDateTime(ts:Timestamp):LocalDateTime{
        return LocalDateTime.ofInstant(ts.toDate().toInstant(), ZoneId.systemDefault())
    }


}
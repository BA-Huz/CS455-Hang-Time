package com.brandon.hangtime

import java.io.Serializable
import java.time.LocalDateTime


class FirebaseDataObjects {

    data class User(
            val UUID: String = "",
            val email: String = "",
            val name: String? = null
    ) : Serializable {
        override fun toString(): String = email
    }


    data class Event(
            val name: String? = null,
            val startDateTime: LocalDateTime,
            val endDateTime: LocalDateTime,
            val owner: String = "",
            val description: String? = null,
            val participants: List<String>? = null
            ) : Serializable

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

}
package com.brandon.hangtime

import java.time.LocalDateTime


class FirebaseDataObjects {

    data class User(
            val UUID: String = "",
            val email: String = "",
            val name: String? = null
    ) {
        override fun toString(): String = email
    }

    data class Event(
            val name: String?,
            val startDateTime: LocalDateTime,
            val endDateTime: LocalDateTime,
            val owner: String,
            val description: String? = null,
            val participants: List<String>? = null
            )
}
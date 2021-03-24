package com.brandon.hangtime

class FirebaseDataObjects {

    data class User(
            val UUID: String = "",
            val email: String = "",
            val name: String? = null
    ) {
        override fun toString(): String = email
    }
}
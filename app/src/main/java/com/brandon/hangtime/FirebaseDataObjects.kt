package com.brandon.hangtime

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import java.io.Serializable
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.security.auth.callback.Callback


object FirebaseDataObjects {

    data class User(
            val UUID: String = "",
            val email: String = "",
            val name: String = ""
    ) : Serializable {
        override fun toString(): String = email
    }


    data class Event(
            val name: String = "",
            val startDateTime: Timestamp = Timestamp(0,0),
            val endDateTime: Timestamp =  Timestamp(0,0),
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
            val owner: String = "",
            val id: String = ""
    ) : Serializable {
        override fun toString():String = groupName
    }

    // this data class is not used directly with fire base
    // but it is used for the algorithm to draw events
    // in the group calendar
    data class EventTimeComponent(
            val hour : Int,
            val minute : Int,
            val isStart : Boolean,
            val isGroupEvent : Boolean
    )


    fun toTimestamp(ldt:LocalDateTime):Timestamp {
        return Timestamp(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()))
    }

    fun toLocalDateTime(ts:Timestamp):LocalDateTime{
        return LocalDateTime.ofInstant(ts.toDate().toInstant(), ZoneId.systemDefault())
    }


    fun getUsers(userId:String, callback: (List<User>) -> Unit){
        getUsers(listOf(userId)){ user -> callback(user)}
    }

    fun getUsers(userId:List<String>, callback: (List<User>) -> Unit){

        if (userId.isEmpty()) callback(listOf())

        val db = Firebase.firestore.collection("users")
        db.whereIn("UUID", userId).get().addOnSuccessListener { result ->
            val users = result!!.map { snapshot ->
                snapshot.toObject<User>()
            }
            callback(users)
        }
    }


}
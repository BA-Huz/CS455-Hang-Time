package com.brandon.hangtime;

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_group_create.*

public class GroupCreate : AppCompatActivity() {

        private lateinit var saveButton: Button
        private lateinit var cancelButton: Button
        private lateinit var groupName: EditText
        private lateinit var errorTextView: TextView

        override fun onCreate(savedInstanceState: Bundle?)
        {
                super.onCreate(savedInstanceState)
                supportFragmentManager.beginTransaction()
                        .setReorderingAllowed(true)
                        .add( R.id.user_select_fragment,UserSelectFragment())
                        .commit()

                setContentView(R.layout.activity_group_create)


                setWidgets()
                setEventListeners()
                setAutoComplete()



        }


        private fun setAutoComplete(){
                val usersDB = Firebase.firestore.collection("users")


                usersDB.get().addOnSuccessListener {  result ->
                        val users = result!!.map { snapshot ->
                        snapshot.toObject<FirebaseDataObjects.User>()
                        }
                        UserSelectFragment().setAutoCompleteSource(users)

                }
                .addOnFailureListener { exception ->
                        Log.d(TAG, "Error getting documents: ", exception)
                }

        }


        private fun setWidgets(){
                saveButton = findViewById(R.id.createGroupButton)
                cancelButton = findViewById(R.id.cancelButton)
                groupName = findViewById(R.id.groupName)
                errorTextView = findViewById(R.id.error)
        }

        private fun setEventListeners()
        {
                saveButton.setOnClickListener {

                        if( groupName.text == null)
                        {
                                // tells the user to enter info in all the fields
                                errorTextView.text = getString(R.string.allFieldsError)
                        }
                        else
                        {
                                addGroup(groupName.text.toString())
                                val intent = Intent(this, GroupList::class.java)
                                startActivity(intent)

                        }
                }
                cancelButton.setOnClickListener{
                        val intent = Intent(this, GroupList::class.java)
                        startActivity(intent)
                }

        }



         private fun addGroup(name: String){
                 val db = Firebase.firestore
                 val id = db.collection("groups").document().id
                 val group:FirebaseDataObjects.Group = FirebaseDataObjects.Group(
                         name,
                         UserSelectFragment().getSelectedUsers().map{it.UUID },
                         Firebase.auth.currentUser.uid,
                         id
                 )

                 db.collection("groups").document(id).set(group).addOnSuccessListener { documentReference ->
                         Log.d(TAG, "DocumentSnapshot added with ID: $documentReference")


                 }
                         .addOnFailureListener { e ->
                                 Log.w(TAG, "Error adding document", e)
                         }
         }



        companion object {
                private const val TAG = "GroupCreate"
        }


}

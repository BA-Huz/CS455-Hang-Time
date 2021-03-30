package com.brandon.hangtime;

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Text
import java.util.ArrayList

public class GroupCreate : AppCompatActivity() {

        private lateinit var saveButton: Button
        private lateinit var cancelButton: Button
        private lateinit var groupName: EditText
        private lateinit var userSelect: AutoCompleteTextView
        private lateinit var groupMembers: TextView
        private lateinit var errorTextView: TextView

        override fun onCreate(savedInstanceState: Bundle?)
        {
                super.onCreate(savedInstanceState)
                setContentView(R.layout.activity_group_create)


                setWidgets()
                setEventListeners()
                setAutoComplete()



        }

        var uuid:List<FirebaseDataObjects.User> = listOf()

        private fun setAutoComplete(){
                val users = Firebase.firestore.collection("users")

                var names:List<FirebaseDataObjects.User> = listOf()


                users.get().addOnSuccessListener {  result ->
                        names = result!!.map { snapshot ->
                        snapshot.toObject<FirebaseDataObjects.User>()
                        }

                        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, names)
                        userSelect.setAdapter(adapter)

                        userSelect.onItemClickListener = AdapterView.OnItemClickListener{
                                parent,view,position,id->
                                val selectedItem: FirebaseDataObjects.User = parent.getItemAtPosition(position) as FirebaseDataObjects.User

                                groupMembers.text = selectedItem.toString() + "\n"
                                uuid += selectedItem

                        }
                }
                .addOnFailureListener { exception ->
                        Log.d(TAG, "Error getting documents: ", exception)
                }

        }


        private fun setWidgets(){
                saveButton = findViewById(R.id.createGroupButton)
                cancelButton = findViewById(R.id.cancelButton)
                groupName = findViewById(R.id.groupName)
                userSelect = findViewById(R.id.userSelectText)
                groupMembers = findViewById(R.id.usersInGroupText)
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
                                addGroup(groupName.text.toString(), uuid)
                                val intent = Intent(this, GroupList::class.java)
                                startActivity(intent)

                        }
                }
                cancelButton.setOnClickListener{
                        val intent = Intent(this, GroupList::class.java)
                        startActivity(intent)
                }

        }



         private fun addGroup(name: String, members: List<FirebaseDataObjects.User>){
                 val db = Firebase.firestore

                 val group:FirebaseDataObjects.Group = FirebaseDataObjects.Group(
                        name,
                        members.map{ it.UUID},
                        Firebase.auth.currentUser.uid
                 )


                 db.collection("groups").document().set(group).addOnSuccessListener { documentReference ->
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

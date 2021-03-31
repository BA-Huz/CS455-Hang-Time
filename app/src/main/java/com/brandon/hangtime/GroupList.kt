package com.brandon.hangtime

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class GroupList : AppCompatActivity()
{

    private lateinit var newGroupButton : Button
    private lateinit var personalScheduleButton : Button
    private lateinit var logOutButton : Button

    private lateinit var groupList : ListView

    private var userGroups:List<FirebaseDataObjects.Group> = listOf()



    // start of call back overrides   **********   start of call back overrides   **********   start of call back overrides   **********
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_list)

        setButtons()
        setListView()
        setButtonListeners()

        loadUserGroups(Firebase.auth.currentUser.uid)
    }

    override fun onStart()
    {
        super.onStart()
    }

    override fun onResume()
    {
        super.onResume()
    }

    override fun onPause()
    {
        super.onPause()
    }

    override fun onStop()
    {
        super.onStop()
    }

    override fun onDestroy()
    {
        super.onDestroy()
    }
    // end of call back overrides   **********   end of call back overrides   **********   end of call back overrides   **********


    private fun setButtons()
    {
        newGroupButton = findViewById(R.id.newGroupButton)
        personalScheduleButton = findViewById(R.id.personalScheduleButton)
        logOutButton = findViewById(R.id.logOutButton)
    }

    private fun setListView()
    {
        groupList = findViewById((R.id.groupList))

        groupList.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, id ->
                val selectedGroup = userGroups[position]
                val intent = Intent(this,GroupCalendar::class.java)
                intent.putExtra("group", selectedGroup)
                startActivity(intent)
            }
    }

    private fun setButtonListeners()
    {
        newGroupButton.setOnClickListener{
            val intent = Intent(this, GroupCreate::class.java)
            startActivity(intent)
        }

        personalScheduleButton.setOnClickListener{
            val intent = Intent(this, PersonalSchedule::class.java)
            startActivity(intent)
        }

        logOutButton.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


    private fun loadUserGroups(uuid:String){

        val groups = Firebase.firestore.collection("groups")

        groups.whereArrayContains("members",uuid).get().addOnSuccessListener {  result ->
            var userGroups = result!!.map { snapshot ->
                snapshot.toObject<FirebaseDataObjects.Group>()
            }
            addGroupsToList(userGroups)
        }
            .addOnFailureListener { exception ->
                Log.d(GroupList.TAG, "Error getting documents: ", exception)
            }

        groups.whereEqualTo("owner",uuid).get().addOnSuccessListener {  result ->
            var userGroups = result!!.map { snapshot ->
                snapshot.toObject<FirebaseDataObjects.Group>()
            }
            addGroupsToList(userGroups)
        }
            .addOnFailureListener { exception ->
                Log.d(GroupList.TAG, "Error getting documents: ", exception)
            }


    }

    private fun addGroupsToList(groups:List<FirebaseDataObjects.Group>){
        userGroups += groups
        val adapter = ArrayAdapter(
                this,
                android.R.layout.simple_list_item_1, userGroups
        )
        groupList.adapter = adapter
        adapter.notifyDataSetChanged()
    }


    companion object {
        private const val TAG = "GroupList"
    }

}
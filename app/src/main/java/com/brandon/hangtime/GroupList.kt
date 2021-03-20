package com.brandon.hangtime

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ListView

class GroupList : AppCompatActivity()
{

    private lateinit var newGroupButton : Button
    private lateinit var personalScheduleButton : Button
    private lateinit var logOutButton : Button

    private lateinit var groupList : ListView

    // start of call back overrides   **********   start of call back overrides   **********   start of call back overrides   **********
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_list)

        setButtons()
        setListView()
        setButtonListeners()
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
    }

    private fun setButtonListeners()
    {
        newGroupButton.setOnClickListener{
            // put code here for what will happen when you click on the new group button ***************************************************
        }

        personalScheduleButton.setOnClickListener{
            val intent = Intent(this, PersonalSchedule::class.java)
            startActivity(intent)
        }

        logOutButton.setOnClickListener{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
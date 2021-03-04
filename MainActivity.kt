package com.brandon.hangtime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.util.Log

class MainActivity : AppCompatActivity()
{
    private lateinit var loginButton : Button
    private lateinit var newUserButton : Button

    private lateinit var usernameEditText : EditText
    private lateinit var passwordEditText : EditText

    // start of call back overrides   **********   start of call back overrides   **********   start of call back overrides   **********
    // onCreate will also call the set functions for thr activity_main.xml widgets
    // and the set functions for event listeners
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setButtons()
        setEditTexts()

        setButtonListeners()
        Log.d("CHEESE   ", "HGKGJVHJHJHVGFCYVUOHBKJFUIGIU")
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
        loginButton = findViewById(R.id.logInButton)
        newUserButton = findViewById(R.id.newUserButton)
    }

    private fun setEditTexts()
    {
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById((R.id.passwordEditText))
    }


    // gets the username text from the username EditText widget
    private fun getUsernameText() : String
    {
        return usernameEditText.text.toString()
    }

    // gets the password text from the password EditText widget
    private fun getPasswordText() : String
    {
        return passwordEditText.text.toString()
    }


    // Code inside of here  ***********************************************************************
    private fun setButtonListeners()
    {
        loginButton.setOnClickListener{
            val userName = getUsernameText()
            val password = getPasswordText()
            // put code here ***********************************************************************

        }

        newUserButton.setOnClickListener{
            // and here ****************************************************************************
        }
    }
}
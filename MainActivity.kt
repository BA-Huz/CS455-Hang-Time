package com.brandon.hangtime

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.util.Log
import android.widget.TextView

class MainActivity : AppCompatActivity()
{
    private lateinit var loginButton : Button
    private lateinit var newUserButton : Button

    private lateinit var usernameEditText : EditText
    private lateinit var passwordEditText : EditText

    private lateinit var invalidLogin : TextView

    // start of call back overrides   **********   start of call back overrides   **********   start of call back overrides
    // onCreate will also call the set functions for thr activity_main.xml widgets
    // and the set functions for event listeners
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setButtons()
        setEditTexts()
        setInvalidLogin()

        setListeners()
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
    // end of call back overrides   **********   end of call back overrides   **********   end of call back overrides



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

    private fun setInvalidLogin()
    {
        invalidLogin = findViewById(R.id.invalidLogin)
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


    private fun setListeners()
    {
        loginButton.setOnClickListener{
            val userName = getUsernameText()
            val password = getPasswordText()

            // if the user has valid login credentials we will move the the group list activity
            if(validLoginVerification())
            {
                // move to the grouplist activity
                val intent = Intent(this, GroupList::class.java)
                startActivity(intent)
            }
            else
            {
                // display invalid login
                passwordEditText.setText("")
                invalidLogin.visibility = TextView.VISIBLE
            }

        }

        newUserButton.setOnClickListener{
            // add code here ******************************************************************************************
        }

        // Making the invalid login text no longer visible when you start typing in your
        // password was strangely difficult because addTextChangedListener is more complicated
        // then you'd think. The following code snippet comes from
        // https://www.tutorialkart.com/kotlin-android/android-edittext-on-text-change/
        passwordEditText.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                invalidLogin.visibility = TextView.INVISIBLE // this line is our own
                Log.d("HELLO", passwordEditText.text.toString())
            }
        })
    }


    private fun validLoginVerification() : Boolean
    {
        if(getPasswordText() == "" || getUsernameText() == "")
            return false
        else
            return true // change this later to see if we get a valid login or not ************************************************************

    }
}
package com.brandon.hangtime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class SignUp : AppCompatActivity()
{
    // lateinits for the widgets in activity sign up
    private lateinit var errorTextView : TextView
    private lateinit var nameEditText : EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText : EditText
    private lateinit var reenterPasswordEditText : EditText
    private lateinit var signUpButton : Button

    // start of call back overrides   **********   start of call back overrides   **********   start of call back overrides
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        setWidgets()
        setSignUpButtonListener()


        val message = savedInstanceState?.getString("Error Message")
        if (message != null)
            errorTextView.text = message
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

    // puts the text in errorTextView into the saved instance state
    // so that when the screen is flipped we can reset the text to what it was
    override fun onSaveInstanceState(savedInstanceState : Bundle)
    {
        super.onSaveInstanceState(savedInstanceState)

        savedInstanceState.putString("Error Message", errorTextView.text.toString())
    }

    // sets the widget objects to reference the widgets in activity_sign_up
    private fun setWidgets()
    {
        errorTextView = findViewById(R.id.errorTextView)
        nameEditText = findViewById(R.id.userNameSignUpEditText)
        emailEditText = findViewById(R.id.emailSignUpEditText)
        passwordEditText = findViewById(R.id.passwordSignUpEditText)
        reenterPasswordEditText = findViewById(R.id.reenterPasswordSignUpEditText)
        signUpButton = findViewById(R.id.signUpButton)
    }

    //sets the sign up button to have an on click listener
    private fun setSignUpButtonListener()
    {
        signUpButton.setOnClickListener {

            if( ! allEditTextsFilled())
            {
                // tells the user to enter info in all the fields
                errorTextView.text = getString(R.string.allFieldsError)
            }
            else if ( ! passwordsAreEqual())
            {
                // tells the user their passwords do not match
                errorTextView.text = getString(R.string.passwordsDontMatchError)
                passwordEditText.setText("")
                reenterPasswordEditText.setText("")
            }
            else if (userInfoAlreadyExists())
            {
                // tells the user that their sign in info already exists
                errorTextView.text = getString(R.string.infoAlreadyExistsError)
            }
            else
                submitSignUpInfo()
        }
    }

    // tests if the passwords are the same
    private fun passwordsAreEqual() : Boolean
    {
        return (passwordEditText.toString() == reenterPasswordEditText.toString())
    }

    // tests to see if all of the EditTexts are filled and not empty
    private fun allEditTextsFilled() : Boolean
    {
        return !((nameEditText.text.toString() == ""|| emailEditText.text.toString() == "") || (passwordEditText.text.toString() == "" || reenterPasswordEditText.text.toString() == ""))
    }

    // *****************************************************************************************
    //These two functions are for you Viktor
    // returns true if that username and password already exist
    private fun userInfoAlreadyExists() : Boolean
    {
        emailEditText.text.toString()
        passwordEditText.text.toString()
        return true
    }


    private fun submitSignUpInfo()
    {
        nameEditText.text.toString()
        emailEditText.text.toString()
        passwordEditText.text.toString()
    }

}
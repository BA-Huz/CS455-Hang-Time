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
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity()
{
    // late inits for xml widgets
    private lateinit var loginButton : Button
    private lateinit var newUserButton : Button

    private lateinit var usernameEditText : EditText
    private lateinit var passwordEditText : EditText

    private lateinit var invalidLogin : TextView

    private lateinit var auth: FirebaseAuth

    // I have a text change listener however it is always fires whenever
    // we create the activity which breaks the program logic so this bool
    // acts as a fix for that to stop the listener from doing stuff when the activity is created
    private var textChangeFirstCall = true


    // start of call back overrides   **********   start of call back overrides   **********   start of call back overrides
    // onCreate will also call the set functions for thr activity_main.xml widgets
    // and the set functions for event listeners
    override fun onCreate(savedInstanceState: Bundle?)
    {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth
        // setup all widget references and event listeners
        setButtons()
        setEditTexts()
        setInvalidLogin()
        setListeners()

        // grabs the previous instance state to appropriatly adjust the invalid login TextView visibility
        // the user input within the text edits automatically works to not loose the input
        val v = savedInstanceState?.getBoolean("InvalidLoginVisibility", false)
        if (v != null) // based off of the saved instance state ensures the invalid login message has proper visibility
            setInvalidLoginVisibility(v)
        else
            setInvalidLoginVisibility(false)

    }

    override fun onStart()
    {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            reload();
        }
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

    // when we kill the activity when going between portrait or landscape
    // we will stash the visibility of the invalid login textview
    override fun onSaveInstanceState(savedInstanceState : Bundle)
    {

        super.onSaveInstanceState(savedInstanceState)
        if(invalidLogin.visibility == TextView.VISIBLE)
            savedInstanceState.putBoolean("InvalidLoginVisibility", true)
        else
            savedInstanceState.putBoolean("InvalidLoginVisibility", false)
    }


    // sets the button objects to reference the xml widgets
    private fun setButtons()
    {
        loginButton = findViewById(R.id.logInButton)
        newUserButton = findViewById(R.id.newUserButton)
    }

    // sets the TextEdit objects to reference the xml widgets
    private fun setEditTexts()
    {
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById((R.id.passwordEditText))
    }

    // sets the invalidLogin TextView object to reference the widget
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

    // Sets the visibility of the invalid login message
    private fun setInvalidLoginVisibility(v : Boolean)
    {

        if(v)
            invalidLogin.visibility = TextView.VISIBLE
        else
            invalidLogin.visibility = TextView.INVISIBLE
    }

    // sets all the event listeners
    private fun setListeners()
    {
        // when clicked the login button will test to see if
        // the login credentials are correct and if they are
        // we will then go to the grouplist activity
        loginButton.setOnClickListener{

            auth.signInWithEmailAndPassword(getUsernameText(), getPasswordText())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
        }

        // when clicked the login button take us to the sign up page
        newUserButton.setOnClickListener{
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }


        // this listener will fire whenever the password edittext is altered
        // its used to remove the invalid login message
        /*********************************************/
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
                // For some reason when switching between portrait and landscape this function is called.
                // this is problematic for our invalidLogin message as it will always set it to
                // invisible. to solve this we will use a boolean to ensure that this listener only calls
                // setInvalidLoginVisibility on the second or more call
                // essentially if this will always be called once after we recreate the activity
                // then it will always ignore that first call
                if(textChangeFirstCall)
                    textChangeFirstCall = false
                else
                    setInvalidLoginVisibility(false)
            }
        })
    }

    // This function will test if the inputed username and password are valid and
    // corrospond to a user in the back end
    private fun validLoginVerification() : Boolean
    {
        if(getPasswordText() == "" || getUsernameText() == "")
            return false
        else
            return true // change this later to see if we get a valid login or not ************************************************************

    }

    private fun reload(){
    }


    private fun updateUI(user: FirebaseUser?) {

        if (user != null){
            // move to the grouplist activity
            val intent = Intent(this, GroupList::class.java)
            startActivity(intent)
        }
        else {
            // display invalid login
            passwordEditText.setText("")
            setInvalidLoginVisibility(true)
        }
    }


    companion object {
        private const val TAG = "EmailPassword"
    }



}
package com.brandon.hangtime

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.security.acl.Group

class SignUp : AppCompatActivity()
{
    // lateinits for the widgets in activity sign up
    private lateinit var errorTextView : TextView
    private lateinit var nameEditText : EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText : EditText
    private lateinit var reenterPasswordEditText : EditText
    private lateinit var signUpButton : Button
    private lateinit var auth: FirebaseAuth

    // start of call back overrides   **********   start of call back overrides   **********   start of call back overrides
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        setWidgets()
        setSignUpButtonListener()

        auth = Firebase.auth

        val message = savedInstanceState?.getString("Error Message")
        if (message != null)
            errorTextView.text = message
    }

    override fun onStart()
    {
        super.onStart()
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

            errorTextView.text = ""
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
            /*else if (userInfoAlreadyExists()) //No longer needed because of how google auth handles this.
            {
                // tells the user that their sign in info already exists
                errorTextView.text = getString(R.string.infoAlreadyExistsError)
            }*/
            else
                submitSignUpInfo()
        }
    }

    // tests if the passwords are the same
    private fun passwordsAreEqual() : Boolean
    {
        return (passwordEditText.text.toString() == reenterPasswordEditText.text.toString())
    }

    // tests to see if all of the EditTexts are filled and not empty
    private fun allEditTextsFilled() : Boolean
    {
        return !((nameEditText.text.toString() == ""|| emailEditText.text.toString() == "") || (passwordEditText.text.toString() == "" || reenterPasswordEditText.text.toString() == ""))
    }

    // *****************************************************************************************

    private fun submitSignUpInfo()
    {
        nameEditText.text.toString()

        auth.createUserWithEmailAndPassword(emailEditText.text.toString(), passwordEditText.text.toString())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }
            }


    }


    //Runs if user is already signed in. Should redirect them to MainActivity
    private fun reload(){

    }


    private fun updateUI(user: FirebaseUser?) {
        if(user != null){
            val intent = Intent(this, GroupList::class.java)
            startActivity(intent)
        }
    }


    companion object {
        private const val TAG = "EmailPassword"
    }

}
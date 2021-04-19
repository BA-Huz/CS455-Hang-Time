package com.brandon.hangtime

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.google.firebase.auth.ktx.auth
import java.util.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime


/*
    Help for implementing pop ups was taken from Tutorialspoint.com
    it taught me how to make pop ups and that classes in kotlin can
    extend a specific abstract function of another class in order to override
    it
 */


                    // The Personal schedule class will override this function from DatePickerDialog and TimePickerDialog
class PersonalSchedule : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
{

    private lateinit var swapButton : Button
    private var isShowingCalendar = true
    private lateinit var topFrame: FrameLayout

    private lateinit var personalCalendarFragment : FragmentPersonalCalendar
    private lateinit var eventEditorFragment : FragmentEventEditor

    // this variable will tell us if we are creating an event start time or end time
    private var startTimePicker = true

    // used when making events
    private lateinit var eventDate: LocalDateTime

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_schedule)

        setLateInits()
        setListeners()

        //.apply is a scope function so we can write code without adding in blah.blah.blah. to make use of the functions lik replace
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.personalCalendarTopFragment, personalCalendarFragment, "TOP_FRAG_TAG")
            commit()
        }
    }

    // sets the late init widgets and has the fragment objects equal to the correct fragment classes
    private fun setLateInits()
    {
        topFrame = findViewById(R.id.personalCalendarTopFragment)
        swapButton = findViewById(R.id.personalEventCalendarSwapButton)
        swapButton.text = "Add Event"

        personalCalendarFragment = FragmentPersonalCalendar()
        eventEditorFragment = FragmentEventEditor()
    }


    //the function name is self explanitory
    private fun setListeners()
    {
        swapButton.setOnClickListener{
            if(isShowingCalendar)
            {
                supportFragmentManager.beginTransaction().apply {
                    // set the top FrameLayout to hold the event editor fragments
                    replace(R.id.personalCalendarTopFragment, eventEditorFragment, "TOP_FRAG_TAG")
                    commit()
                }
                eventEditorFragment.setParent(FragmentEventEditor.Parent.PERSONALSCHEDULE)

                swapButton.text = "Back to calendar"
                isShowingCalendar = false
            }
            else
            {
                supportFragmentManager.beginTransaction().apply {
                    // set the top FrameLayout to hold the event editor fragments
                    replace(R.id.personalCalendarTopFragment, personalCalendarFragment, "TOP_FRAG_TAG")
                    commit()
                }
                swapButton.text = "Add Event"
                isShowingCalendar = true
            }
        }
    }

    // An internal method that will be called by fragments of this activity
    // it creates the pop ups for selecting dates and times
    internal fun makeTimePopUp(startTimePicker : Boolean)
    {
        this.startTimePicker = startTimePicker

        val now: LocalDate = LocalDate.now()
        //give the info to the dialog constructor as well as a reference to our version of its onDateSet
        val dpd = DatePickerDialog(this, this, now.year, now.monthValue-1 , now.dayOfMonth)//now.monthValue-1
        dpd.datePicker.minDate = Calendar.getInstance().timeInMillis //Later on we can change this so that if this is the end time it sets the min to be the start time and date
        dpd.show()
    // show the dialog pop up
    }

    // overridden member of the datePickerDialog class
    // we set it to also make a timePickerDialog
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int)
    {
        val now: LocalTime = LocalTime.now()
        eventDate = LocalDateTime.of(year, month+1,dayOfMonth,0,0)//dayOfMonth+1

        // create a timePicker Dialog
        val tpd = TimePickerDialog(this,this, now.hour, now.minute, false)
        tpd.show()
    }

    // overridden member of the datePickerDialog class
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int)
    {
        eventDate = eventDate.plusHours(hourOfDay.toLong()).plusMinutes(minute.toLong())

        //now we also need to send the proper info back to the fragment
        setFragmentEventEditorEditTexts()
    }

    // this function will bundle up the info to do with user selected time and date for events
    // and sends it into the FragmentEventEditor
    private fun setFragmentEventEditorEditTexts()
    {
        (supportFragmentManager.findFragmentByTag("TOP_FRAG_TAG") as FragmentEventEditor).setEdits(startTimePicker, eventDate)
    }

    internal fun submitNewEvent(event:FirebaseDataObjects.Event)
    {
        val db = Firebase.firestore

        val eventId :FirebaseDataObjects.Event =
                if (event.participants != null) {
                    event.copy(participants = event.participants!!.plus(Firebase.auth.currentUser.uid ).distinct())
                }
                else {
                    event.copy(participants = listOf(Firebase.auth.currentUser.uid))
                }


        db.collection("events").document().set(eventId).addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot added with ID: $documentReference")
            Toast.makeText(this, "${eventId.name} has been submitted", Toast.LENGTH_SHORT).show()
            (supportFragmentManager.findFragmentByTag("TOP_FRAG_TAG") as FragmentEventEditor).setSaveButtonClickability(true)
            supportFragmentManager.beginTransaction().apply {
                // set the top FrameLayout to hold the event editor fragments
                replace(R.id.personalCalendarTopFragment, personalCalendarFragment, "TOP_FRAG_TAG")
                commit()
            }
            swapButton.text = "Add Event"
            isShowingCalendar = true
        }
        .addOnFailureListener { e ->
            Log.w(TAG, "Error adding document", e)
            Toast.makeText(this, "${event.name} was not submitted successfully", Toast.LENGTH_SHORT).show()
            (supportFragmentManager.findFragmentByTag("TOP_FRAG_TAG") as FragmentEventEditor).setSaveButtonClickability(true)
        }

    }

    companion object {
        private const val TAG = "PersonalSchedule"
    }

}
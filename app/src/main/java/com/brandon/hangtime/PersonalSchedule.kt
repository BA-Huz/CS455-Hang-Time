package com.brandon.hangtime

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import java.util.*
import android.widget.DatePicker
import android.widget.TimePicker

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
    private lateinit var bottomFrame : FrameLayout

    private lateinit var personalCalendarFragment : FragmentPersonalCalendar
    private lateinit var eventEditorFragment : FragmentEventEditor
    private lateinit var bottomFragment : FragmentPersonalEventList

    // this variable will determine which fragment to load when we swap fragments
    private var startTimePicker = true

    // these wll be filled by a calendar instance
    private var day = 0
    private var month = 0
    private var year = 0
    private var hour = 0
    private var minute = 0

    // s for saved, these values will be filled by the users input
    private var sday = 0
    private var smonth = 0
    private var syear = 0
    private var shour = 0
    private var sminute = 0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_schedule)

        setLateInits()
        setListeners()

        //.apply is a scope function so we can write code without adding in blah.blah.blah. to make use of the functions lik replace
        supportFragmentManager.beginTransaction().apply {
            // set the two FrameLayouts to hold these 2 fragments
            replace(R.id.personalCalendarTopFragment, personalCalendarFragment, "TOP_FRAG_TAG")
            replace(R.id.personalEventListFragment, bottomFragment, "BOTTOM_FRAG_TAG")
            commit()
        }
    }

    // sets the late init widgets and has the fragment objects equal to the correct fragment classes
    private fun setLateInits()
    {
        topFrame = findViewById(R.id.personalCalendarTopFragment)
        bottomFrame = findViewById(R.id.personalEventListFragment)
        swapButton = findViewById(R.id.personalEventCalendarSwapButton)
        swapButton.text = "Add Event"

        personalCalendarFragment = FragmentPersonalCalendar()
        eventEditorFragment = FragmentEventEditor()
        bottomFragment = FragmentPersonalEventList()
    }

    // grabs the current time from a calendar instance
    private fun setCalendarVals()
    {
        val c = Calendar.getInstance()
        day = c.get(Calendar.DAY_OF_MONTH)
        month = c.get(Calendar.MONTH)
        year = c.get(Calendar.YEAR)
        hour = c.get(Calendar.HOUR)
        minute = c.get(Calendar.MINUTE)
    }

    //the function name is self explanitory
    private fun setListeners()
    {
        swapButton.setOnClickListener{
            if(isShowingCalendar)
            {
                supportFragmentManager.beginTransaction().apply {
                    // set the two FrameLayouts to hold these 2 fragments
                    replace(R.id.personalCalendarTopFragment, eventEditorFragment, "TOP_FRAG_TAG")
                    commit()
                }

                swapButton.text = "Back to calendar"
                isShowingCalendar = false
               // makePopUp(true)
            }
            else
            {
                supportFragmentManager.beginTransaction().apply {
                    // set the two FrameLayouts to hold these 2 fragments
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
        setCalendarVals()

        //give the info to the dialog constructor as well as a reference to our version of its onDateSet
        val dpd = DatePickerDialog(this, this, year, month, day)
        dpd.datePicker.minDate = Calendar.getInstance().timeInMillis
        dpd.show()
    // show the dialog pop up
    }

    // overridden member of the datePickerDialog class
    // we set it to also make a timePickerDialog
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int)
    {
        sday = dayOfMonth
        syear = year
        smonth = month

        setCalendarVals()
        // create a timePicker Dialog
        var tpd = TimePickerDialog(this, 3,this, hour, minute, false)
        tpd.show()
    }

    // overridden member of the datePickerDialog class
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int)
    {
        shour = hourOfDay
        sminute = minute

        //now we also need to send the proper info back to the fragment
        setFragmentEventEditorEditTexts()
    }

    // this function will bundle up the info to do with user selected time and date for events
    // and sends it into the FragmentEventEditor
    private fun setFragmentEventEditorEditTexts()
    {
        var m = "AM"
        if(shour > 12)
        {
            m = "PM"
            shour -= 12
        }
        // I seperated each variable by a unique character
        var msg = "$smonth-$sday*$year;$shour:$sminute#$m"

        (supportFragmentManager.findFragmentByTag("TOP_FRAG_TAG") as FragmentEventEditor).setEdits(startTimePicker, msg)
    }

    internal fun submitNewEvent(startMsg : String, endMsg : String, eventName : String, whichRepeating : Int)
    {
        //  *****************      HEY VIKTOR      ***********************
        //  *****************      IVE HAD TOO MUCH COFFEE TODAY      ***********************
        //  *****************      NOW LET ME EXPLAIN HOW TO USE THESE PARAMETERS      ***********************
        /*
            the startMsg and endMsg hold all of the info to do with date and time for when the event starts and ends
            you dont need to check if they are valid input, ive already done that, at this stage there good
            whichRepeating shows which radio button is checked
            0 is non repetitive
            1 is daily event
            2 is weekly event

            the information is numbers with the exception of AM or PM that are seperated by unigue characters

      ***      msg = "$smonth-$sday*$year;$shour:$sminute#$m"  take a look at the above function to get an idea     ***

            if you want to extract the hour out of startMsg you can do this
            val hour = startMsg.substring(startMessage.indexOf(';')+1, startMessage.indexOf(':'))
            this is because before the hour is the unique char ; and after is :

            however this will not account for am or pm, its in 12 hour not 24 hour time. so to determine if its
            10:00 in 24 hour time or 22:00 in 24 hour time you can
            if(startMsg.substring(startMessage.indexOf('#')+1) == "AM")
                // your in the morning

         */
    }
}
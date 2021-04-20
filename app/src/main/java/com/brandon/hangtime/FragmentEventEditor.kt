package com.brandon.hangtime

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime


class FragmentEventEditor : Fragment()
{
    // when the user clicks on either edit text it will actually open pop up windows
    // the edit text is used because it affords clicking
    private lateinit var startTimeWidget : TextView
    private lateinit var endTimeWidget: TextView
    private lateinit var eventNameWidget : EditText
    private lateinit var saveButton : Button
    private lateinit var nonRepeatingRadio : RadioButton
    private lateinit var dailyRepeatingRadio : RadioButton
    private lateinit var weeklyRepeatingRadio : RadioButton

    private lateinit var startDate: LocalDateTime
    private lateinit var endDate: LocalDateTime

    internal enum class Parent{
        GROUPCALENDAR, PERSONALSCHEDULE
    }
    private var myParent : Parent = Parent.GROUPCALENDAR



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val v = inflater.inflate(R.layout.fragment_event_editor, container, false)


        setLateInits(v)
        setListeners()

        return v
    }

    internal fun setParent(p : Parent)
    {
        myParent = p
    }

    // sets up the late inits
    private fun setLateInits(v : View)
    {
        startTimeWidget = v.findViewById(R.id.personalEventStartTimeEditText)
        // make it so you cannot enter anything into the edit text
        startTimeWidget.inputType = InputType.TYPE_NULL

        endTimeWidget = v.findViewById(R.id.personalEventEndTimeEditText)
        endTimeWidget.inputType = InputType.TYPE_NULL

        eventNameWidget = v.findViewById(R.id.personalEventNameEditText)

        saveButton = v.findViewById((R.id.savePersonalEventButton))

        nonRepeatingRadio = v.findViewById(R.id.personalEventNonRepeatingRadio)
        nonRepeatingRadio.isChecked = true
        dailyRepeatingRadio = v.findViewById(R.id.personalEventDailyRadio)
        weeklyRepeatingRadio = v.findViewById(R.id.personalEventWeeklyRadio)
    }

    // sets all the event listeners
    private fun setListeners()
    {
        //set the startTime edittext so when clicked on the parent activity will throw
        //the correct pop up
        startTimeWidget.setOnClickListener{
            if(myParent == Parent.PERSONALSCHEDULE)
                (activity as PersonalSchedule).makeTimePopUp(true)
            else
                (activity as GroupCalendar).makeTimePopUp(true)
        }

        endTimeWidget.setOnClickListener{
            if(myParent == Parent.PERSONALSCHEDULE)
                (activity as PersonalSchedule).makeTimePopUp(false)
            else
                (activity as GroupCalendar).makeTimePopUp(false)
        }

        saveButton.setOnClickListener{
            // the all fields function will already make a toast if its false
            if(allFieldsFilled())
            {
                if(endDate<startDate) {
                    if(myParent == Parent.PERSONALSCHEDULE)
                        Toast.makeText(activity as PersonalSchedule, "The event must end after it starts", Toast.LENGTH_SHORT).show()
                    else
                        Toast.makeText(activity as GroupCalendar, "The event must end after it starts", Toast.LENGTH_SHORT).show()
                } else{
                    saveButton.isClickable = false // so the button cant be spammed while the database is being accessed

                    val event = FirebaseDataObjects.Event(
                            eventNameWidget.text.toString(),
                            FirebaseDataObjects.toTimestamp(startDate),
                            FirebaseDataObjects.toTimestamp(endDate),
                            Firebase.auth.currentUser!!.uid,
                            "",
                            null
                    )
                    if(myParent == Parent.PERSONALSCHEDULE)
                        (activity as PersonalSchedule).submitNewEvent(event)
                    else
                        (activity as GroupCalendar).submitNewEvent(event)
                }
            }
        }
    }

    internal fun setSaveButtonClickability(clickable : Boolean)
    {
        saveButton.isClickable = clickable
    }

    // this function is intended to be called by the personal schedule for when we are inserting
    // time into the edit texts
    internal fun setEdits(startTimeEdit : Boolean, eventTime:LocalDateTime)
    {
        val hour: String
        val period: String

        when {
            eventTime.hour == 0 -> {// time is 12 am
                hour = "12"
                period = "AM"
            }
            eventTime.hour > 12 -> {  // time is between 10pm and 12pm
                hour = "${eventTime.hour - 12}"
                period = "PM"
            }
            else -> { // time is between 10 am and 11 am
                hour = "${eventTime.hour}"
                period = "AM"
            }
        }

        val minute: String = if(eventTime.minute < 10)
            "0${eventTime.minute}"
        else
            "${eventTime.minute}"

        if(startTimeEdit)
        {
            startTimeWidget.text = "${eventTime.month} ${eventTime.dayOfMonth} at $hour:$minute $period"
            startDate = eventTime
        }
        else
        {
            endTimeWidget.text = "${eventTime.month} ${eventTime.dayOfMonth} at $hour:$minute $period"
            endDate = eventTime
        }
    }

    // returns true if all the fields are filled
    private fun allFieldsFilled() : Boolean
    {
        return if(eventNameWidget.text.toString().isBlank() || (startTimeWidget.text.toString().isBlank() || endTimeWidget.text.toString().isBlank() ))
        {
            if(myParent == Parent.PERSONALSCHEDULE)
                Toast.makeText(activity as PersonalSchedule, "All fields must be filled", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(activity as GroupCalendar, "All fields must be filled", Toast.LENGTH_SHORT).show()
            false
        }
        else
            true
    }

}
package com.brandon.hangtime

import android.os.Bundle
import android.text.InputType
import android.text.format.Time
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.util.*


class FragmentEventEditor : Fragment()
{
    // when the user clicks on either edit text it will actually open pop up windows
    // the edit text is used because it affords clicking
    private lateinit var startTimeWidget : EditText
    private lateinit var endTimeWidget: EditText
    private lateinit var eventNameWidget : EditText
    private lateinit var eventDescriptionWidget : EditText
    //private lateinit var repetitiveRadioGroup : RadioGroup
    private lateinit var saveButton : Button
    private lateinit var nonRepeatingRadio : RadioButton
    private lateinit var dailyRepeatingRadio : RadioButton
    private lateinit var weeklyRepeatingRadio : RadioButton

    private lateinit var startDate: LocalDateTime
    private lateinit var endDate: LocalDateTime



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val v = inflater.inflate(R.layout.fragment_event_editor, container, false)

        setLateInits(v)
        setListeners()

        return v
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
        //eventDescriptionWidget = v.findViewById(R.id.DescriptionEditText)

       // repetitiveRadioGroup = v.findViewById(R.id.repetitiveRadios)

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
            (activity as PersonalSchedule).makeTimePopUp(true)
        }

        endTimeWidget.setOnClickListener{
            (activity as PersonalSchedule).makeTimePopUp(false)
        }

        saveButton.setOnClickListener{
            // the all fields function will already make a toast if its false
            if(allFieldsFilled())
            {
                if(endDate<startDate) {
                    Toast.makeText(activity as PersonalSchedule, "The event must end after it starts", Toast.LENGTH_SHORT).show()
                } else{

                    val event = FirebaseDataObjects.Event(
                            eventNameWidget.text.toString(),
                            FirebaseDataObjects.toTimestamp(startDate),
                            FirebaseDataObjects.toTimestamp(endDate),
                            Firebase.auth.currentUser.uid,
                            ""//eventDescriptionWidget.text.toString()

                    )
                    (activity as PersonalSchedule).submitNewEvent(event)
                }
            }
        }
    }

    // this function is intended to be called by the personal schedule for when we are inserting
    // time into the edit texts
    internal fun setEdits(startTimeEdit : Boolean, eventTime:LocalDateTime)
    {
        if(startTimeEdit)
        {
            startTimeWidget.setText(eventTime.toString())
            startDate = eventTime
        }
        else
        {
            endTimeWidget.setText(eventTime.toString())
            endDate = eventTime
        }
    }

    // returns true if all the fields are filled
    private fun allFieldsFilled() : Boolean
    {
        return if(eventNameWidget.text.toString().isNullOrBlank() || (startTimeWidget.text.toString().isNullOrBlank() || endTimeWidget.text.toString().isNullOrBlank() ))
        {
            Toast.makeText(activity as PersonalSchedule, "All fields must be filled", Toast.LENGTH_SHORT).show()
            false
        }
        else
            true
    }

}
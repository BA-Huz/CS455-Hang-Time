package com.brandon.hangtime


import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.lang.StringBuilder


class FragmentEventEditor : Fragment()
{
    // when the user clicks on either edit text it will actually open pop up windows
    // the edit text is used because it affords clicking
    private lateinit var startTimeWidget : EditText
    private lateinit var endTimeWidget: EditText
    private lateinit var eventNameWidget : EditText
    //private lateinit var repetitiveRadioGroup : RadioGroup
    private lateinit var saveButton : Button
    private lateinit var nonRepeatingRadio : RadioButton
    private lateinit var dailyRepeatingRadio : RadioButton
    private lateinit var weeklyRepeatingRadio : RadioButton

    private lateinit var startMessage : String
    private lateinit var endMessage : String


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
                if(!endTimeIsGreater())
                    Toast.makeText(activity as PersonalSchedule, "The event must end after it starts", Toast.LENGTH_SHORT).show()
                else // we can submit the info to the database!!!
                    submitEvent()
            }
        }
    }

    // this function is intended to be called by the personal schedule for when we are inserting
    // time into the edit texts
    internal fun setEdits(startTimeEdit : Boolean, timeMsg : String)
    {
        if(startTimeEdit)
        {
            startTimeWidget.setText(timeParser(timeMsg))
            startMessage = timeMsg
        }
        else
        {
            endTimeWidget.setText(timeParser(timeMsg))
            endMessage = timeMsg
        }
    }

    // this function takes the bundled info to do with date ans time and turns it into a dispalyable readable string
    private fun timeParser(s : String) : String {
        val b = StringBuilder()
        b.append(s)
        var toReturn: String

        if (b[0] == '0') {
            toReturn = "JAN "
        } else if (b[0] == '1' && b[1] == '-') {
            toReturn = "FEB "
        } else if (b[0] == '2') {
            toReturn = "MAR "
        } else if (b[0] == '3') {
            toReturn = "APR "
        } else if (b[0] == '4') {
            toReturn = "MAY "
        } else if (b[0] == '5') {
            toReturn = "JUN "
        } else if (b[0] == '6') {
            toReturn = "JUL "
        } else if (b[0] == '7') {
            toReturn = "AUG "
        } else if (b[0] == '8') {
            toReturn = "SEP "
        } else if (b[0] == '9') {
            toReturn = "OCT "
        } else if (b[0] == '1' && b[1] == '0') {
            toReturn = "NOV "
        } else {
            toReturn = "DEC "
        }

        //after - is day
        var i = b.indexOf('-')
        toReturn += b[i + 1]
        if (b[i + 2] != '*')
            toReturn += b[i + 2]
        toReturn += "   "

        //after * is year
        i = b.indexOf('*')
        toReturn += b.substring(i + 1, i + 5).toString() + "    "


        //after ; is hour
        //toReturn += b[i+7]
        if (b[i + 7] == ':')
            toReturn += b[i + 6] + ":"
        else {
            toReturn += b[i + 6]
            toReturn += b[i + 7] + ":"
        }

        // after : is minute
        i = b.indexOf(':')
        if (b[i + 2] != '#') {
            toReturn += b[i + 1]
            toReturn += b[i + 2]
        }
        else
        {
            toReturn += "0"
            toReturn += b[i + 1]
        }

        // after # is am or pm
        i = b.indexOf('#')
        toReturn += " " + b.substring(i+1, i+3)

        return toReturn
    }

    // returns true if all the fields are filled
    private fun allFieldsFilled() : Boolean
    {
        if(eventNameWidget.text.toString() == "" || (startTimeWidget.text.toString() == "" || endTimeWidget.text.toString() == ""))
        {
            Toast.makeText(activity as PersonalSchedule, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return false
        }
        else
            return true
    }

    // returns true if the start time of an event is before the end time
    private fun endTimeIsGreater() : Boolean
    {
        val start = startTimeWidget.text.toString()
        val end = endTimeWidget.text.toString()

        // first lets compare the years
        // if the start year is less then the end year
        if(startMessage.substring(startMessage.indexOf('*')+1, startMessage.indexOf(';'))
                < endMessage.substring(endMessage.indexOf('*')+1, endMessage.indexOf(';')))
                    return true
        // if the start year is greater then the end year
        else if (startMessage.substring(startMessage.indexOf('*')+1, startMessage.indexOf(';'))
                > endMessage.substring(endMessage.indexOf('*')+1, endMessage.indexOf(';')))
                    return false

        // now we know the years are equal so lets compare month
        // if the start month is less then the end month
        if(startMessage.substring(0, startMessage.indexOf('-'))
                < endMessage.substring(0, endMessage.indexOf('-')))
            return true
        // if the start month is greater then the end month
        else if (startMessage.substring(0, startMessage.indexOf('-'))
                > endMessage.substring(0, endMessage.indexOf('-')))
            return false

        // now we know the months are equal so lets compare days
        // if the start day is less then the end day
        if(startMessage.substring(startMessage.indexOf('-')+1, startMessage.indexOf('*'))
                < endMessage.substring(endMessage.indexOf('-')+1, endMessage.indexOf('*')))
            return true
        // if the start day is greater then the end day
        else if (startMessage.substring(startMessage.indexOf('-')+1, startMessage.indexOf('*'))
                > endMessage.substring(endMessage.indexOf('-')+1, endMessage.indexOf('*')))
            return false

        // now we know the days are equal so lets compare AM, PM
        // if the start day is less then the end day
        if(startMessage.indexOf('#') + 1 < (endMessage.indexOf('#') + 1))
            return true
        else if(startMessage.indexOf('#') + 1 > (endMessage.indexOf('#') + 1))
            return false

        // now we move onto hour
        // if the start hour is less then the end hour
        if(startMessage.substring(startMessage.indexOf(';')+1, startMessage.indexOf(':'))
                < endMessage.substring(endMessage.indexOf(';')+1, endMessage.indexOf(':')))
            return true
        // if the start hour is greater then the end hour
        else if (startMessage.substring(startMessage.indexOf(';')+1, startMessage.indexOf(':'))
                > endMessage.substring(endMessage.indexOf(';')+1, endMessage.indexOf(':')))
            return false

        // deep breath, almost there, finally minute
        // if the start minute is less then the end minute
        if(startMessage.substring(startMessage.indexOf(':')+1, startMessage.indexOf('#'))
                < endMessage.substring(endMessage.indexOf(':')+1, endMessage.indexOf('#')))
            return true
        // if the start minute is greater then the end minute
        else if (startMessage.substring(startMessage.indexOf(':')+1, startMessage.indexOf('#'))
                > endMessage.substring(endMessage.indexOf(':')+1, endMessage.indexOf('#')))
            return false

        // if we get to here both dates and times are equal
        return false
    }

    // passes nessicary info up to the activity to be submitted to the data bases
    private fun submitEvent()
    {
        if(nonRepeatingRadio.isChecked)
            (activity as PersonalSchedule).submitNewEvent(startMessage, endMessage, eventNameWidget.text.toString(), 0)
        else if(dailyRepeatingRadio.isChecked)
            (activity as PersonalSchedule).submitNewEvent(startMessage, endMessage, eventNameWidget.text.toString(), 1)
        else
            (activity as PersonalSchedule).submitNewEvent(startMessage, endMessage, eventNameWidget.text.toString(), 2)
    }

}
package com.brandon.hangtime


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.Toast
import androidx.fragment.app.Fragment



class FragmentPersonalCalendar : Fragment()
{
    private lateinit var calendar : CalendarView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        var v = inflater.inflate(R.layout.fragment_personal_calendar, container, false)

        setUpCalendar(v)

        return v
    }

    private fun setUpCalendar(v : View)
    {
        calendar = v.findViewById(R.id.personalCalendar) as CalendarView
        calendar?.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // This function detects when a user selects a different date on the calendar
            // later on we can put code here to load the correct events into the personal event list

        }

    }

}
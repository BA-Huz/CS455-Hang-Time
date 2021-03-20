package com.brandon.hangtime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CalendarView

class PersonalSchedule : AppCompatActivity()
{
    private lateinit var calendarView : CalendarView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_schedule)

        calendarView = findViewById(R.id.personalCalendar)
    }


}
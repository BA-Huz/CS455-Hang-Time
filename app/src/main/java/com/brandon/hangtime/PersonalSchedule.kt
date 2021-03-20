package com.brandon.hangtime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment


class PersonalSchedule : AppCompatActivity()
{
    private lateinit var calendarFragment : Fragment
    private lateinit var eventListFragment : Fragment
    private lateinit var eventEditorFragment : Fragment

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_schedule)

    }


}
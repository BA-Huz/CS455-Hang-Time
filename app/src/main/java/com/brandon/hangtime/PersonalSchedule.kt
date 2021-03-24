package com.brandon.hangtime

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment

class PersonalSchedule : AppCompatActivity()
{
    private lateinit var swapButton : Button
    private var isShowingCalendar = true

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_schedule)

        val personalCalendarFragment = FragmentPersonalCalendar()
        val eventEditorFragment = FragmentEventEditor()
        val bottomFragment = FragmentPersonalEventList()

        //.apply is a scope function so we can write code without adding in blah.blah.blah. to make use of the functions lik replace
        supportFragmentManager.beginTransaction().apply {
            // set the two FrameLayouts to hold these 2 fragments
            replace(R.id.personalCalendarTopFragment, personalCalendarFragment)
            replace(R.id.personalEventListFragment, bottomFragment)
            commit()
        }

        swapButton = findViewById(R.id.personalEventCalendarSwapButton)
        swapButton.setOnClickListener{
            if(isShowingCalendar)
            {
                supportFragmentManager.beginTransaction().apply {
                    // set the two FrameLayouts to hold these 2 fragments
                    replace(R.id.personalCalendarTopFragment, eventEditorFragment)
                    commit()
                }

                isShowingCalendar = false
            }
            else
            {
                supportFragmentManager.beginTransaction().apply {
                    // set the two FrameLayouts to hold these 2 fragments
                    replace(R.id.personalCalendarTopFragment, personalCalendarFragment)
                    commit()
                }

                isShowingCalendar = true
            }
        }
    }


}
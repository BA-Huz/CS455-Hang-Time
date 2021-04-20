package com.brandon.hangtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import java.time.LocalDateTime

class TwoDayViewFragment : Fragment()
{
    private lateinit var leftDay : TextView
    private lateinit var rightDay : TextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val v = inflater.inflate(R.layout.fragment_two_day_view, container, false)

        setLateInits(v)

        return v
    }

    private fun setLateInits(v : View)
    {
        leftDay = v.findViewById(R.id.day1Date)
        rightDay = v.findViewById(R.id.day2Date)
        setDisplayedDays((activity as GroupCalendar).displayedLeftDay)
    }

    internal fun setDisplayedDays(leftDate : LocalDateTime)
    {
        leftDay.text = "${leftDate.month}   ${leftDate.dayOfMonth}"
        rightDay.text = "${leftDate.plusDays(1).month}   ${leftDate.plusDays(1).dayOfMonth}"
    }
}
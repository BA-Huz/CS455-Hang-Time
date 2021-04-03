package com.brandon.hangtime

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.lang.Math.abs
import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

class GroupCalendar : AppCompatActivity()
{
    // scalar is used to line up the event regions with the hours
    // each hour is 60dp apart in xml and in  kotlin code 180 apart
    private val scalar = 180

    // the number of users in the current group
    // will be given a new value in get events
    private var numberInGroup = 0

    // a list of events that we will grab from the db. each event
    // is personal events of group members
    private lateinit var events : List<FirebaseDataObjects.Event>

    // the date of whichever day is displayed in the left column
    // when the first page loads it is set to the current day
    private var displayedLeftDay = LocalDateTime.now()

    private var touchDownX = -1f
    private var touchUpX = -1f
    private var touchDownY = -1f
    private var touchUpY = -1f
    private var lastMove = 0
    private var nowMove = 0


    // these 2 bitmaps and canvases will be used to draw rectangles
    // they also cannot be initialized fully until after completion of onCreate()
    // that's why they have their own init function that isn't setLateinits
    // and have an accompanying boolean so the function to init them will be called
    // from loadColumns only once
    private lateinit var day1BitMap : Bitmap
    private lateinit var day1Canvas : Canvas
    private lateinit var day2BitMap : Bitmap
    private lateinit var day2Canvas : Canvas
    private var createdBitMapsAndCanvases = false

    private lateinit var scrollView : ScrollView
    private lateinit var day1ImageView : ImageView
    private lateinit var day2ImageView : ImageView

    private lateinit var leftDay : TextView
    private lateinit var rightDay : TextView


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_calendar)

        setLateinits()
        setListeners()

        var temp = findViewById<LinearLayout>(R.id.dayBar)
        temp.setOnClickListener{
            //drawRegion(true, 5f, 8.9f, groupAvailibilityColour(50, 3))
            //drawRegion(false, 7.6f, 10.4f, groupAvailibilityColour(50, 16))
            loadColumns()
        }

        getEvents()
    }


    private fun setLateinits()
    {
        scrollView = findViewById(R.id.scrollView)

        day1ImageView = findViewById(R.id.groupCalendarDay1)
        day2ImageView = findViewById(R.id.groupCalendarDay2)

        leftDay = findViewById(R.id.day1Date)
        leftDay.text = "${displayedLeftDay.month.toString()}   ${displayedLeftDay.dayOfMonth}"
        rightDay = findViewById(R.id.day2Date)
        rightDay.text = "${displayedLeftDay.plusDays(1).month.toString()}   ${displayedLeftDay.plusDays(1).dayOfMonth}"
    }

    private fun setListeners()
    {
        /*swipable.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, m: MotionEvent): Boolean {
                // Perform tasks here
                return true
            }
        })*/
        scrollView.setOnTouchListener {v: View, m: MotionEvent ->
            detectedTouch(m)
            true
        }
    }

    private fun detectedTouch(m : MotionEvent)
    {
        if(m.action == MotionEvent.ACTION_MOVE)
        {
            lastMove = nowMove
            nowMove = m.y.toInt()
            // this if ensures smooth scrolling of the scrollview
            if (lastMove != 0)
            {
                val difference = nowMove - lastMove
                scrollView.scrollY -= difference
            }
        }
        else if(m.action == MotionEvent.ACTION_DOWN)
        {
            touchDownX = m.x
            touchUpX = 0f
            touchDownY = m.y
            touchUpY = 0f
            lastMove = 0
            nowMove = 0
        }
        else if(m.action == MotionEvent.ACTION_UP)
        {
            lastMove = 0
            nowMove = 0
            touchUpX = m.x
            touchUpY = m.x
            if(touchUpX > touchDownX + 250) //&& kotlin.math.abs(touchUpX-touchDownX) > kotlin.math.abs(touchUpY-touchDownY))
            {
                // swipe right
                swipe(false)
            }
            else if(touchUpX < touchDownX - 250)// && kotlin.math.abs(touchUpX) > kotlin.math.abs(touchUpY))
            {
                // swipe left
                swipe(true)
            }
        }
    }

    // Grabs events from the database that happen on the 31st of march and puts them into the events list
    private fun getEvents()
    {
        val startDate1 = LocalDateTime.of(2021, Month.APRIL, 1, 3, 0)
        val endDate1 = LocalDateTime.of(2021, Month.APRIL, 2, 6, 5)
        val startDate2 = LocalDateTime.of(2021, Month.APRIL, 2, 5, 0)
        val endDate2 = LocalDateTime.of(2021, Month.APRIL, 2, 9, 30)
        val startDate3 = LocalDateTime.of(2021, Month.APRIL, 2, 15, 0)
        val endDate3 = LocalDateTime.of(2021, Month.APRIL, 2, 16, 40)
        val startDate4 = LocalDateTime.of(2021, Month.APRIL, 2, 12, 0)
        val endDate4 = LocalDateTime.of(2021, Month.APRIL, 3, 5, 10)


        var e1 = FirebaseDataObjects.Event("e1", startDate1, endDate1, "Alex","")
        var e2 = FirebaseDataObjects.Event("e2", startDate2, endDate2, "Alex","")
        var e3 = FirebaseDataObjects.Event("e3", startDate3, endDate3, "Alex","")
        var e4 = FirebaseDataObjects.Event("e4", startDate4, endDate4, "Alex","")

        events = listOf<FirebaseDataObjects.Event>(e1, e2, e3, e4)
        numberInGroup = 4
    }

    // this function is called before the first region is drawn
    // the values in here cannot be assigned until after onCreate has finished
    // this is because the imageView.height variables are 0 until after on create finishes
    private fun setBitMapsAndCanvases()
    {
        createdBitMapsAndCanvases = true
        day1BitMap = Bitmap.createBitmap(day1ImageView.width, day1ImageView.height, Bitmap.Config.ARGB_8888)
        day1Canvas = Canvas(day1BitMap)
        day2BitMap = Bitmap.createBitmap(day2ImageView.width, day2ImageView.height, Bitmap.Config.ARGB_8888)
        day2Canvas = Canvas(day2BitMap)

        day1ImageView.background = BitmapDrawable(resources, day1BitMap)
        day2ImageView.background = BitmapDrawable(resources, day2BitMap)
    }

    //draws lines to coraspond with each hour
    private fun drawHourLines()
    {
        val paint = Paint()
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6F
        paint.isAntiAlias = true

        for(i in 1..24)
        {
            day1Canvas.drawLine(0.toFloat(), (i * scalar).toFloat(), day1ImageView.width.toFloat(), (i * scalar).toFloat(), paint)
            day2Canvas.drawLine(0.toFloat(), (i * scalar).toFloat(), day2ImageView.width.toFloat(), (i * scalar).toFloat(), paint)
        }

        paint.strokeWidth = 3F
        for(i in 1..24)
        {
            day1Canvas.drawLine(0.toFloat(), (i*scalar - scalar/2).toFloat(), day1ImageView.width.toFloat(), (i*scalar - scalar/2).toFloat(), paint)
            day2Canvas.drawLine(0.toFloat(), (i*scalar - scalar/2).toFloat(), day2ImageView.width.toFloat(), (i*scalar - scalar/2).toFloat(), paint)
        }
    }

    // this function will fill the columns with all the appropriate regions
    private fun loadColumns()
    {
        if( ! createdBitMapsAndCanvases)
            setBitMapsAndCanvases()
        else
        {
            // wipe the canvases clean
            day1Canvas.drawColor(Color.WHITE)
            day2Canvas.drawColor(Color.WHITE)
        }

        //now we want to parse the events such that it properly can draw rectangles
        parseAndDrawEvents()


        //drawRegion(true, 5f, 8.9f, groupAvailibilityColour(50, 3))
        //drawRegion(false, 7.6f, 10.4f, groupAvailibilityColour(50, 16))
        drawHourLines()

    }

    private fun parseAndDrawEvents()
    {
        var parsedEventsLeftDay = mutableListOf<FirebaseDataObjects.EventTimeComponent>()
        var parsedEventsRightDay = mutableListOf<FirebaseDataObjects.EventTimeComponent>()

        // put nessicary info into the left and right days
        for(e in events)
        {
            // put start time in left day
            if(e.startDateTime.dayOfMonth == displayedLeftDay.dayOfMonth)
            {
                val toAdd = FirebaseDataObjects.EventTimeComponent(e.startDateTime.hour, e.startDateTime.minute, true)
                parsedEventsLeftDay.add(toAdd)
                // if it ends after left day
                if (e.endDateTime.dayOfMonth > displayedLeftDay.dayOfMonth)
                {
                    val toAdd = FirebaseDataObjects.EventTimeComponent(24, 0, false)
                    parsedEventsLeftDay.add(toAdd)
                }
            }
            // put start time in right day
            else if(e.startDateTime.dayOfMonth == displayedLeftDay.plusDays(1).dayOfMonth)
            {
                val toAdd = FirebaseDataObjects.EventTimeComponent(e.startDateTime.hour, e.startDateTime.minute, true)
                parsedEventsRightDay.add(toAdd)
                // if it ends after right day
                if (e.endDateTime.dayOfMonth > displayedLeftDay.plusDays(1).dayOfMonth)
                {
                    val toAdd = FirebaseDataObjects.EventTimeComponent(24, 0, false)
                    parsedEventsRightDay.add(toAdd)
                }
            }

            // put end time in left day
            if(e.endDateTime.dayOfMonth == displayedLeftDay.dayOfMonth)
            {
                val toAdd = FirebaseDataObjects.EventTimeComponent(e.endDateTime.hour, e.endDateTime.minute, false)
                parsedEventsLeftDay.add(toAdd)
                // if it starts before left day
                if (e.startDateTime.dayOfMonth < displayedLeftDay.dayOfMonth)
                {
                    val toAdd = FirebaseDataObjects.EventTimeComponent(0, 0, true)
                    parsedEventsLeftDay.add(toAdd)
                }
            }
            // put end time in right day
            else if(e.endDateTime.dayOfMonth == displayedLeftDay.plusDays(1).dayOfMonth)
            {
                val toAdd = FirebaseDataObjects.EventTimeComponent(e.endDateTime.hour, e.endDateTime.minute, false)
                parsedEventsRightDay.add(toAdd)
                // if it starts before right day
                if (e.startDateTime.dayOfMonth < displayedLeftDay.plusDays(1).dayOfMonth)
                {
                    val toAdd = FirebaseDataObjects.EventTimeComponent(0, 0, true)
                    parsedEventsRightDay.add(toAdd)
                }
            }

            //if an event starts before and ends after the left day
            if(e.startDateTime.dayOfMonth < displayedLeftDay.dayOfMonth && e.endDateTime.dayOfMonth > displayedLeftDay.dayOfMonth)
            {
                var toAdd = FirebaseDataObjects.EventTimeComponent(0, 0, true)
                parsedEventsLeftDay.add(toAdd)
                toAdd = FirebaseDataObjects.EventTimeComponent(24, 0, false)
                parsedEventsLeftDay.add(toAdd)
            }

            //if an event starts before and ends after the right day
            if(e.startDateTime.dayOfMonth < displayedLeftDay.plusDays(1).dayOfMonth && e.endDateTime.dayOfMonth > displayedLeftDay.plusDays(1).dayOfMonth)
            {
                var toAdd = FirebaseDataObjects.EventTimeComponent(0, 0, true)
                parsedEventsRightDay.add(toAdd)
                toAdd = FirebaseDataObjects.EventTimeComponent(24, 0, false)
                parsedEventsRightDay.add(toAdd)
            }

        }


        // sort all of the components by time
        parsedEventsLeftDay.sortBy{it.hour * 100 + it.minute}
        parsedEventsRightDay.sortBy{it.hour * 100 + it.minute}

        var numberBusy = 0
        var i = 0
        while (i < parsedEventsLeftDay.size)
        {
            // if it is a start time
            if (parsedEventsLeftDay[i].isStart)
            {
                numberBusy++
                drawRegion(true, timeToFloat(parsedEventsLeftDay[i].hour, parsedEventsLeftDay[i].minute), timeToFloat(parsedEventsLeftDay[i+1].hour, parsedEventsLeftDay[i+1].minute), groupAvailibilityColour(numberBusy))
            }
            // else it is an end time
            else
            {
                numberBusy--
                if(numberBusy > 0)
                    drawRegion(true, timeToFloat(parsedEventsLeftDay[i].hour, parsedEventsLeftDay[i].minute), timeToFloat(parsedEventsLeftDay[i+1].hour, parsedEventsLeftDay[i+1].minute), groupAvailibilityColour(numberBusy))
            }
            i++
        }


        numberBusy = 0
        i = 0
        while (i < parsedEventsRightDay.size)
        {
            // if it is a start time
            if (parsedEventsRightDay[i].isStart)
            {
                numberBusy++
                drawRegion(false, timeToFloat(parsedEventsRightDay[i].hour, parsedEventsRightDay[i].minute), timeToFloat(parsedEventsRightDay[i+1].hour, parsedEventsRightDay[i+1].minute), groupAvailibilityColour(numberBusy))
            }
            // else it is an end time
            else
            {
                numberBusy--
                if(numberBusy > 0)
                    drawRegion(false, timeToFloat(parsedEventsRightDay[i].hour, parsedEventsRightDay[i].minute), timeToFloat(parsedEventsRightDay[i+1].hour, parsedEventsRightDay[i+1].minute), groupAvailibilityColour(numberBusy))
            }
            i++
        }
    }

    // Draws a single region on either day1 column or day 2 column
    private fun drawRegion(isFirstDay : Boolean, startTime : Float, endTime : Float, colour : String )
    {
        var region = ShapeDrawable(RectShape())

        if(isFirstDay)
        {
            // if two users have events at the same time, dont draw a flat region, ei start time == end time
            if(startTime != endTime)
            {
                region.setBounds(0, (scalar * startTime).toInt(), day1ImageView.width, (scalar * endTime).toInt())
                region.paint.color = Color.parseColor(colour)
                region.draw(day1Canvas)
            }
        }
        else
        {
            // if two users have events at the same time, dont draw a flat region, ei start time == end time
            if(startTime != endTime)
            {
                region.setBounds(0, (scalar * startTime).toInt(), day2ImageView.width, (scalar * endTime).toInt())
                region.paint.color = Color.parseColor(colour)
                region.draw(day2Canvas)
            }
        }

        //region2.setBounds(day1ImageView.width, 180*2, day2ImageView.width, 180*3)
       // region2.paint.color = Color.parseColor("#009944")
       // region2.draw(canvas)

        // this is a temp location for the call
    }

    // takes the number of users in a group and the number of those users that are busy
    // and calculates a shade of grey to represent overall group availibility
    // darker grey for more avalible, lighter for less
    private fun groupAvailibilityColour(numberBusy : Int) : String
    {
        var busyPercentage = 0f
        if (numberBusy > 0)
            busyPercentage = numberBusy.toFloat()/numberInGroup.toFloat()
        else
            busyPercentage = numberBusy.toFloat()/numberInGroup.toFloat() * -1

        if(busyPercentage > 0.5f)
            return "#463E3F" // black eel
        else if(busyPercentage > 0.25f)
            return "#666362" // ash grey
        else if(busyPercentage >= 0.1f)
            return "#B6B6B4" // grey cloud
        else
            return "#E5E4E2" // platinum
    }

    private fun timeToFloat(hour : Int, minute : Int) : Float
    {
        return hour.toFloat() + (minute.toFloat() / 60f)
    }

    private fun swipe(isLeftSwipe : Boolean)
    {
        Toast.makeText(this,"swiped", Toast.LENGTH_SHORT).show()
        if(isLeftSwipe)
            displayedLeftDay = displayedLeftDay.plusDays(2)
        else
            displayedLeftDay = displayedLeftDay.plusDays(-2)

        leftDay.text = "${displayedLeftDay.month.toString()}   ${displayedLeftDay.dayOfMonth}"
        rightDay.text = "${displayedLeftDay.plusDays(1).month.toString()}   ${displayedLeftDay.plusDays(1).dayOfMonth}"
        getEvents()
        loadColumns()
    }
}
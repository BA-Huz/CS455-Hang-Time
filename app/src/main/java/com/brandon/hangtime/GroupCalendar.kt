package com.brandon.hangtime

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RectShape
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.brandon.hangtime.FirebaseDataObjects.toLocalDateTime
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*


class GroupCalendar : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener
{
    private lateinit var currentGroup : FirebaseDataObjects.Group

    // this variable will tell us if we are creating an event start time or end time
    private var startTimePicker = true

    // used when making events
    private lateinit var eventDate: LocalDateTime

    // scalar is used to line up the event regions with the hours
    // each hour is 60dp apart in xml and in  kotlin code 180 apart
    private var scalar = 0
    private var setScaler = false

    // the number of users in the current group
    // will be given a new value in get events
    private var numberInGroup = 0

    // a list of events that we will grab from the db. each event
    // is personal events of group members
    private lateinit var events : List<FirebaseDataObjects.Event>

    // the date of whichever day is displayed in the left column
    // when the first page loads it is set to the current day
    internal var displayedLeftDay = LocalDateTime.now()

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

    private lateinit var eventButton : Button

    private lateinit var twoDayViewFragment: TwoDayViewFragment
    private lateinit var eventEditorFragment : FragmentEventEditor
    private var showingTwoDayFragment = true

    //private lateinit var loadColsJob : Job


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_calendar)

        currentGroup = intent.getSerializableExtra("group") as FirebaseDataObjects.Group

        setLateInits()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.topFrame, twoDayViewFragment, "TOP_FRAG")
            commit()
        }

        setListeners()

        getEvents(LocalDate.now().atStartOfDay())

        // We cannot draw on the screen until after onCreate has finished
        // and we also cannot touch the view from a thread that did not create
        // the view. So to solve this we start a new coroutine  that waits half a second
        // and then orders the main routine to execute the function chain that does the drawing
        GlobalScope.launch{
            Thread.sleep(500)
            Handler(Looper.getMainLooper()).post(Runnable { loadColumns() })
        }
    }


    private fun setLateInits()
    {
        scrollView = findViewById(R.id.scrollView)

        day1ImageView = findViewById(R.id.groupCalendarDay1)
        day2ImageView = findViewById(R.id.groupCalendarDay2)



        eventButton = findViewById(R.id.GroupEventButton)

        twoDayViewFragment = TwoDayViewFragment()
        eventEditorFragment = FragmentEventEditor()
    }

    private fun setListeners()
    {
        eventButton.setOnClickListener {
            if(eventButton.text == "Schedule a Group Event")
            {
                eventButton.text = "Back To Day View"
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.topFrame, eventEditorFragment, "TOP_FRAG")   // eventEditorFragment throwing exception uninitialized property access exception
                    commit()
                }
                showingTwoDayFragment = false
                eventEditorFragment.setParent(FragmentEventEditor.Parent.GROUPCALENDAR)
            }
            else
            {
                eventButton.text = "Schedule a Group Event"
                supportFragmentManager.beginTransaction().apply{
                    replace(R.id.topFrame, twoDayViewFragment, "TOP_FRAG")
                    commit()
                }
                showingTwoDayFragment = true
            }
        }

        scrollView.setOnTouchListener { v: View, m: MotionEvent ->
            detectedTouch(m)
            true
        }
    }

    private fun detectedTouch(m: MotionEvent)
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
    private fun getEvents(firstDate: LocalDateTime)
    {
        val eventsColl = Firebase.firestore.collection("events")

        eventsColl
                .whereGreaterThanOrEqualTo("endDateTime", FirebaseDataObjects.toTimestamp(firstDate))
                //Replace listOf<String><() with a list of the uuid of all memebers within the group.
                //.whereArrayContainsAny("participants", listOf<String>())
                .get().addOnSuccessListener { result ->
                    events = result!!.map { snapshot ->
                        snapshot.toObject<FirebaseDataObjects.Event>()
                    }
                //Successful retrieval listener code goes here
                    Log.d(TAG, events.toString())
        }
         .addOnFailureListener { exception ->
             Log.d(TAG, "Error getting documents: ", exception)
         }

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

    //draws lines to corespond with each hour
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
            day1Canvas.drawLine(0.toFloat(), (i * scalar - scalar / 2).toFloat(), day1ImageView.width.toFloat(), (i * scalar - scalar / 2).toFloat(), paint)
            day2Canvas.drawLine(0.toFloat(), (i * scalar - scalar / 2).toFloat(), day2ImageView.width.toFloat(), (i * scalar - scalar / 2).toFloat(), paint)
        }
    }

    // this function will fill the columns with all the appropriate regions
    private fun loadColumns()
    {
        if(!setScaler)
        {
            setScaler = true
            findViewById<LinearLayout>(R.id.groupCalendarTimeLayout).apply { scalar = getChildAt(1).top - getChildAt(0).top }
        }

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

        drawHourLines()

    }

    private fun parseAndDrawEvents()
    {
        var parsedEventsLeftDay = mutableListOf<FirebaseDataObjects.EventTimeComponent>()
        var parsedEventsRightDay = mutableListOf<FirebaseDataObjects.EventTimeComponent>()

        // put nessicary info into the left and right days
        for(e in events)
        {
            val start = toLocalDateTime(e.startDateTime!!)
            val end = toLocalDateTime(e.endDateTime!!)

            // put start time in left day
            if(start.dayOfMonth == displayedLeftDay.dayOfMonth)
            {

                val toAdd = FirebaseDataObjects.EventTimeComponent(start.hour, start.minute, true, (e.group != null && e.group == currentGroup.id))
                parsedEventsLeftDay.add(toAdd)
                // if it ends after left day
                if (end.dayOfMonth > displayedLeftDay.dayOfMonth)
                {
                    val toAdd = FirebaseDataObjects.EventTimeComponent(24, 0, false, (e.group != null && e.group == currentGroup.id))
                    parsedEventsLeftDay.add(toAdd)
                }
            }
            // put start time in right day
            else if(start.dayOfMonth == displayedLeftDay.plusDays(1).dayOfMonth)
            {
                val toAdd = FirebaseDataObjects.EventTimeComponent(start.hour, start.minute, true, (e.group != null && e.group == currentGroup.id))
                parsedEventsRightDay.add(toAdd)
                // if it ends after right day
                if (end.dayOfMonth > displayedLeftDay.plusDays(1).dayOfMonth)
                {
                    val toAdd = FirebaseDataObjects.EventTimeComponent(24, 0, false, (e.group != null && e.group == currentGroup.id))
                    parsedEventsRightDay.add(toAdd)
                }
            }

            // put end time in left day
            if(end.dayOfMonth == displayedLeftDay.dayOfMonth)
            {
                val toAdd = FirebaseDataObjects.EventTimeComponent(end.hour, end.minute, false, (e.group != null && e.group == currentGroup.id))
                parsedEventsLeftDay.add(toAdd)
                // if it starts before left day
                if (start.dayOfMonth < displayedLeftDay.dayOfMonth)
                {
                    val toAdd = FirebaseDataObjects.EventTimeComponent(0, 0, true, (e.group != null && e.group == currentGroup.id))
                    parsedEventsLeftDay.add(toAdd)
                }
            }
            // put end time in right day
            else if(end.dayOfMonth == displayedLeftDay.plusDays(1).dayOfMonth)
            {
                val toAdd = FirebaseDataObjects.EventTimeComponent(end.hour, end.minute, false, (e.group != null && e.group == currentGroup.id))
                parsedEventsRightDay.add(toAdd)
                // if it starts before right day
                if (start.dayOfMonth < displayedLeftDay.plusDays(1).dayOfMonth)
                {
                    val toAdd = FirebaseDataObjects.EventTimeComponent(0, 0, true, (e.group != null && e.group == currentGroup.id))
                    parsedEventsRightDay.add(toAdd)
                }
            }

            //if an event starts before and ends after the left day
            if(start.dayOfMonth < displayedLeftDay.dayOfMonth && end.dayOfMonth > displayedLeftDay.dayOfMonth)
            {
                var toAdd = FirebaseDataObjects.EventTimeComponent(0, 0, true, (e.group != null && e.group == currentGroup.id))
                parsedEventsLeftDay.add(toAdd)
                toAdd = FirebaseDataObjects.EventTimeComponent(24, 0, false, (e.group != null && e.group == currentGroup.id))
                parsedEventsLeftDay.add(toAdd)
            }

            //if an event starts before and ends after the right day
            if(start.dayOfMonth < displayedLeftDay.plusDays(1).dayOfMonth && end.dayOfMonth > displayedLeftDay.plusDays(1).dayOfMonth)
            {
                var toAdd = FirebaseDataObjects.EventTimeComponent(0, 0, true, (e.group != null && e.group == currentGroup.id))
                parsedEventsRightDay.add(toAdd)
                toAdd = FirebaseDataObjects.EventTimeComponent(24, 0, false, (e.group != null && e.group == currentGroup.id))
                parsedEventsRightDay.add(toAdd)
            }

        }


        // sort all of the components by time
        parsedEventsLeftDay.sortBy{it.hour * 100 + it.minute}
        parsedEventsRightDay.sortBy{it.hour * 100 + it.minute}

        var numberBusy = 0
        var i = 0
        var gCounter = 0
        while (i < parsedEventsLeftDay.size)
        {
            // if it is a start time
            if (parsedEventsLeftDay[i].isStart)
            {
                if(parsedEventsLeftDay[i].isGroupEvent)
                    gCounter++
                else
                    numberBusy++
                drawRegion(true, timeToFloat(parsedEventsLeftDay[i].hour, parsedEventsLeftDay[i].minute), timeToFloat(parsedEventsLeftDay[i + 1].hour, parsedEventsLeftDay[i + 1].minute), groupAvailibilityColour(numberBusy, gCounter > 0))
            }
            // else it is an end time
            else
            {
                if(parsedEventsLeftDay[i].isGroupEvent)
                    gCounter--
                else
                    numberBusy--
                if(numberBusy > 0)
                    drawRegion(true, timeToFloat(parsedEventsLeftDay[i].hour, parsedEventsLeftDay[i].minute), timeToFloat(parsedEventsLeftDay[i + 1].hour, parsedEventsLeftDay[i + 1].minute), groupAvailibilityColour(numberBusy, gCounter > 0))
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
                if(parsedEventsRightDay[i].isGroupEvent)
                    gCounter++
                else
                    numberBusy++
                drawRegion(false, timeToFloat(parsedEventsRightDay[i].hour, parsedEventsRightDay[i].minute), timeToFloat(parsedEventsRightDay[i + 1].hour, parsedEventsRightDay[i + 1].minute), groupAvailibilityColour(numberBusy, gCounter > 0))
            }
            // else it is an end time
            else
            {
                if(parsedEventsRightDay[i].isGroupEvent)
                    gCounter--
                else
                    numberBusy--
                if(numberBusy > 0)
                    drawRegion(false, timeToFloat(parsedEventsRightDay[i].hour, parsedEventsRightDay[i].minute), timeToFloat(parsedEventsRightDay[i + 1].hour, parsedEventsRightDay[i + 1].minute), groupAvailibilityColour(numberBusy, gCounter > 0))
            }
            i++
        }
    }

    // Draws a single region on either day1 column or day 2 column
    private fun drawRegion(isFirstDay: Boolean, startTime: Float, endTime: Float, colour: String)
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
    }

    // takes the number of users in a group and the number of those users that are busy
    // and calculates a shade of grey to represent overall group availibility
    // darker grey for more avalible, lighter for less
    private fun groupAvailibilityColour(numberBusy: Int, hasGroupEventAtThisTime: Boolean) : String
    {
        var busyPercentage = 0f
        if (numberBusy > 0)
            busyPercentage = numberBusy.toFloat()/numberInGroup.toFloat()
        else
            busyPercentage = numberBusy.toFloat()/numberInGroup.toFloat() * -1

        if(! hasGroupEventAtThisTime)
        {
            if (busyPercentage > 0.5f)
                return "#463E3F" // black eel
            else if (busyPercentage > 0.25f)
                return "#666362" // ash grey
            else if (busyPercentage >= 0.1f)
                return "#B6B6B4" // grey cloud
            else
                return "#E5E4E2" // platinum
        }
        else
        {
            if (busyPercentage > 0.5f)
                return "#000066"
            else if (busyPercentage > 0.25f)
                return "#000099" // Lapis Blue
            else if (busyPercentage >= 0.1f)
                return "#0000b3" // grey cloud
            else
                return "#0000cc" // platinum
        }
    }

    private fun timeToFloat(hour: Int, minute: Int) : Float
    {
        return hour.toFloat() + (minute.toFloat() / 60f)
    }

    private fun swipe(isLeftSwipe: Boolean)
    {
        if(isLeftSwipe)
            displayedLeftDay = displayedLeftDay.plusDays(2)
        else
            displayedLeftDay = displayedLeftDay.plusDays(-2)

        if(showingTwoDayFragment)
            (supportFragmentManager.findFragmentByTag("TOP_FRAG") as TwoDayViewFragment).setDisplayedDays(displayedLeftDay)

        getEvents(LocalDateTime.now())//This should be changed to whatever the leftmost displayed day is. Or maybe that minus 1
        loadColumns()
    }

    // An internal method that will be called by fragments of this activity
    // it creates the pop ups for selecting dates and times
    internal fun makeTimePopUp(startTimePicker : Boolean)
    {
        this.startTimePicker = startTimePicker

        val now: LocalDate = LocalDate.now()
        //give the info to the dialog constructor as well as a reference to our version of its onDateSet
        val dpd = DatePickerDialog(this, this, now.year, now.monthValue-1 , now.dayOfMonth)//now.monthValue-1
        dpd.datePicker.minDate = Calendar.getInstance().timeInMillis //Later on we can change this so that if this is the end time it sets the min to be the start time and date
        dpd.show()
        // show the dialog pop up
    }

    // overridden member of the datePickerDialog class
    // we set it to also make a timePickerDialog
    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int)
    {
        val now: LocalTime = LocalTime.now()
        eventDate = LocalDateTime.of(year, month+1,dayOfMonth,0,0)//dayOfMonth+1

        // create a timePicker Dialog
        val tpd = TimePickerDialog(this,this, now.hour, now.minute, false)
        tpd.show()
    }

    // overridden member of the datePickerDialog class
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int)
    {
        eventDate = eventDate.plusHours(hourOfDay.toLong()).plusMinutes(minute.toLong())

        //now we also need to send the proper info back to the fragment
        setFragmentEventEditorEditTexts()
    }

    // this function will bundle up the info to do with user selected time and date for events
    // and sends it into the FragmentEventEditor
    private fun setFragmentEventEditorEditTexts()
    {
        (supportFragmentManager.findFragmentByTag("TOP_FRAG") as FragmentEventEditor).setEdits(startTimePicker, eventDate)
    }

    internal fun submitNewEvent(event:FirebaseDataObjects.Event) // ***********************************************  Viktor can you make sure the code here will work
    {
        //Toast.makeText(this, "We called it boys", Toast.LENGTH_SHORT).show() // you can get rid of this lol

        val db = Firebase.firestore

        val eventId = event.copy(group = currentGroup.id)

        db.collection("events").document().set(eventId).addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot added with ID: $documentReference")
        }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
            }

    }


    companion object {
        private const val TAG = "GroupCalendar"
    }
}
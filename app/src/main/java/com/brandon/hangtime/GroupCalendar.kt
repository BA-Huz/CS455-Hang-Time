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
    private var setScalar = false

    // the number of users in the current group
    // will be given a new value in get events
    private var numberInGroup = 0

    // a list of events that we will grab from the db. each event
    // is personal events of group members
    private var events : List<FirebaseDataObjects.Event> = listOf()
    private var usersInGroup: List<FirebaseDataObjects.User> = listOf()
    private var usersFetched = false

    // the date of whichever day is displayed in the left column
    // when the first page loads it is set to the current day
    internal var displayedLeftDay = LocalDateTime.now()

    // hold flags are for holding a touch on event regions
    // they help to handle some minor threading
    // holdFlag1 stops a thread from being multi instantiated
    // holdFlag2 is used to test if the user has lifted their finger
    // while the thread sleeps
    private var holdFlag1 = false
    private var holdFlag2 = false

    // these values are used with the scrollview on touch Listener
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

    // widgets not attached to a fragment
    private lateinit var scrollView : ScrollView
    private lateinit var day1ImageView : ImageView
    private lateinit var day2ImageView : ImageView
    private lateinit var eventButton : Button
    private lateinit var addMemberButton : Button

    // the two fragments used by this activity and a bool to represent which is active
    private lateinit var twoDayViewFragment : TwoDayViewFragment
    private lateinit var eventEditorFragment : FragmentEventEditor
    private lateinit var userSelectFragment: UserSelectFragment
    private var showingTwoDayFragment = true


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_calendar)

        currentGroup = intent.getSerializableExtra("group") as FirebaseDataObjects.Group

        updateUsersInGroup()

        setLateInits()

        supportFragmentManager.beginTransaction().apply {
            replace(R.id.topFrame, twoDayViewFragment, "TOP_FRAG")
            commit()
        }

        setListeners()

        // grabs the events from the db
        getEvents(LocalDate.now().atStartOfDay())
    }

    private fun updateUsersInGroup() {
        val usersDB = Firebase.firestore.collection("users")

        usersDB.whereIn("UUID", currentGroup.members!!.toList()).get().addOnSuccessListener { result ->
            val users = result!!.map { snapshot ->
                snapshot.toObject<FirebaseDataObjects.User>()
            }
            usersInGroup = users
        }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
    }


    private fun setLateInits()
    {
        scrollView = findViewById(R.id.scrollView)

        day1ImageView = findViewById(R.id.groupCalendarDay1)
        day2ImageView = findViewById(R.id.groupCalendarDay2)

        eventButton = findViewById(R.id.GroupEventButton)
        addMemberButton = findViewById(R.id.addGroupMember)

        twoDayViewFragment = TwoDayViewFragment()
        eventEditorFragment = FragmentEventEditor()
        userSelectFragment = UserSelectFragment()
    }

    private fun setListeners()
    {
        eventButton.setOnClickListener {
            if(showingTwoDayFragment)
            {
                eventButton.text = "Back To Day View"
                addMemberButton.isClickable = false
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
                addMemberButton.isClickable = true
                supportFragmentManager.beginTransaction().apply{
                    replace(R.id.topFrame, twoDayViewFragment, "TOP_FRAG")
                    commit()
                }
                showingTwoDayFragment = true
            }
        }

        addMemberButton.setOnClickListener {
            if (!usersFetched){
                val usersDB = Firebase.firestore.collection("users")

                usersDB.get().addOnSuccessListener {  result ->
                    val users = result!!.map { snapshot ->
                        snapshot.toObject<FirebaseDataObjects.User>()
                    }
                    userSelectFragment.setAutoCompleteSource(users)
                    usersFetched = true
                }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "Error getting documents: ", exception)
                        }
            }
            if(showingTwoDayFragment)
            {
                eventButton.text = "Back To Day View"
                addMemberButton.text = "Add Member"
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.topFrame, userSelectFragment, "TOP_FRAG")   // eventEditorFragment throwing exception uninitialized property access exception
                    commit()
                }
                showingTwoDayFragment = false
                eventEditorFragment.setParent(FragmentEventEditor.Parent.GROUPCALENDAR)
            }
            else {
                addMemberButton.text = "Add A New Member"
                eventButton.text = "Schedule a Group Event"
                addMemberButton.isClickable = true

                val users = userSelectFragment.getSelectedUsers()

                val newGroup = currentGroup.copy(members = currentGroup.members!!.plus(users.map{it.UUID}).distinct())

                val db = Firebase.firestore

                db.collection("groups").document(currentGroup.id).set(newGroup).addOnSuccessListener { documentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: $documentReference")
                    Toast.makeText(this, "Users has been added", Toast.LENGTH_SHORT).show()

                }.addOnFailureListener { e ->
                            Log.w(TAG, "Error adding document", e)
                            Toast.makeText(this, "Users were not added successfully", Toast.LENGTH_SHORT).show()
                        }
                supportFragmentManager.beginTransaction().apply{
                    replace(R.id.topFrame, twoDayViewFragment, "TOP_FRAG")
                    commit()
                }
                showingTwoDayFragment = true
            }

        }


        scrollView.setOnTouchListener { _: View, m: MotionEvent ->
            detectedTouch(m)
            true
        }


    }

    // our implementation of the scrollview's on touch listener
    // calls this function
    private fun detectedTouch(m: MotionEvent)
    {
        // this first snippet works to test if the user is holding a touch on an event region
        // hold flag 1 stops the thread from multi instantiating itself
        // hold flag 2 stops the toast from happening if the user lifted their finger up during the threads sleep
        if(!holdFlag1 && !holdFlag2)
        {
            holdFlag1 = true
            holdFlag2 = true
            GlobalScope.launch{
                Thread.sleep(500)
                if(holdFlag2) // if we were holding this whole time
                {
                    if(m.x >= day1ImageView.left && m.x <= day1ImageView.right)
                        Handler(Looper.getMainLooper()).post { toastBusyMembersAtTime(clickPositionToTime(m.y, true)) }
                    else if(m.x >= day2ImageView.left && m.x <= day2ImageView.right)
                        Handler(Looper.getMainLooper()).post { toastBusyMembersAtTime(clickPositionToTime(m.y, false)) }
                }
                holdFlag1 = false
            }
        }

        // if this was a move action then scroll the scrollview
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
        // else if this was an action down then store values
        else if(m.action == MotionEvent.ACTION_DOWN)
        {
            touchDownX = m.x
            touchUpX = 0f
            touchDownY = m.y
            touchUpY = 0f
            lastMove = 0
            nowMove = 0
        }
        // else if this was an action up then see if the values of the down and up validate a swipe
        else if(m.action == MotionEvent.ACTION_UP)
        {
            holdFlag2 = false
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

    // makes a toast listing all the members that are busy with events at the time given
    private fun toastBusyMembersAtTime(clickedTime : LocalDateTime)
    {
        // build an array of the names of members busy
        var busyMembers = mutableListOf<String>()

        val eventsInInterval = events.filter { event -> toLocalDateTime(event.startDateTime) < clickedTime && toLocalDateTime(event.endDateTime) > clickedTime}

        for (e in eventsInInterval) {
            if (e.group == null || e.group != currentGroup.id) // else and this is a group event of another group
                busyMembers.addAll(e.participants!!) //.plus(e.participants)
                //busyMembers = busyMembers.plus(e.participants!!.filter { usersInGroup.map { x -> x.UUID  }.contains(it) }.map{id -> usersInGroup.find{ user -> user.UUID == id }!!.name })

        }

        // put those names in a string then Toast those names
        var message = ""
        for (m in busyMembers.distinct()) {
            message += if (message == "")
                usersInGroup.find { user -> user.UUID == m }?.name
            else
                ", ${usersInGroup.find { user -> user.UUID == m }?.name}"
        }
        if (message != "")
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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
                loadColumns()
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

    //draws lines to correspond with each hour
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
        if(!setScalar)
        {
            setScalar = true
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

    // this function takes the list of events and extracts the start and end times of each
    // it then sorts theses times for drawing rectangles we called regions
    private fun parseAndDrawEvents()
    {
        val parsedEventsLeftDay = mutableListOf<FirebaseDataObjects.EventTimeComponent>()
        val parsedEventsRightDay = mutableListOf<FirebaseDataObjects.EventTimeComponent>()

        // put necessary info into the left and right days
        for(e in events)
        {
            val start = toLocalDateTime(e.startDateTime)
            val end = toLocalDateTime(e.endDateTime)

            // put start time in left day
            if(start.dayOfMonth == displayedLeftDay.dayOfMonth)
            {

                parsedEventsLeftDay.add(FirebaseDataObjects.EventTimeComponent(start.hour, start.minute, true, (e.group != null && e.group == currentGroup.id)))
                // if it ends after left day
                if (end.dayOfMonth > displayedLeftDay.dayOfMonth)
                {
                    parsedEventsLeftDay.add(FirebaseDataObjects.EventTimeComponent(24, 0, false, (e.group != null && e.group == currentGroup.id)))
                }
            }
            // put start time in right day
            else if(start.dayOfMonth == displayedLeftDay.plusDays(1).dayOfMonth)
            {
                parsedEventsRightDay.add(FirebaseDataObjects.EventTimeComponent(start.hour, start.minute, true, (e.group != null && e.group == currentGroup.id)))
                // if it ends after right day
                if (end.dayOfMonth > displayedLeftDay.plusDays(1).dayOfMonth)
                {
                    parsedEventsRightDay.add(FirebaseDataObjects.EventTimeComponent(24, 0, false, (e.group != null && e.group == currentGroup.id)))
                }
            }

            // put end time in left day
            if(end.dayOfMonth == displayedLeftDay.dayOfMonth)
            {
                parsedEventsLeftDay.add(FirebaseDataObjects.EventTimeComponent(end.hour, end.minute, false, (e.group != null && e.group == currentGroup.id)))
                // if it starts before left day
                if (start.dayOfMonth < displayedLeftDay.dayOfMonth)
                {
                    parsedEventsLeftDay.add(FirebaseDataObjects.EventTimeComponent(0, 0, true, (e.group != null && e.group == currentGroup.id)))
                }
            }
            // put end time in right day
            else if(end.dayOfMonth == displayedLeftDay.plusDays(1).dayOfMonth)
            {
                parsedEventsRightDay.add(FirebaseDataObjects.EventTimeComponent(end.hour, end.minute, false, (e.group != null && e.group == currentGroup.id)))
                // if it starts before right day
                if (start.dayOfMonth < displayedLeftDay.plusDays(1).dayOfMonth)
                {
                    parsedEventsRightDay.add(FirebaseDataObjects.EventTimeComponent(0, 0, true, (e.group != null && e.group == currentGroup.id)))
                }
            }

            //if an event starts before and ends after the left day
            if(start.dayOfMonth < displayedLeftDay.dayOfMonth && end.dayOfMonth > displayedLeftDay.dayOfMonth)
            {
                parsedEventsLeftDay.add(FirebaseDataObjects.EventTimeComponent(0, 0, true, (e.group != null && e.group == currentGroup.id)))
                parsedEventsLeftDay.add(FirebaseDataObjects.EventTimeComponent(24, 0, false, (e.group != null && e.group == currentGroup.id)))
            }

            //if an event starts before and ends after the right day
            if(start.dayOfMonth < displayedLeftDay.plusDays(1).dayOfMonth && end.dayOfMonth > displayedLeftDay.plusDays(1).dayOfMonth)
            {
                parsedEventsRightDay.add(FirebaseDataObjects.EventTimeComponent(0, 0, true, (e.group != null && e.group == currentGroup.id)))
                parsedEventsRightDay.add(FirebaseDataObjects.EventTimeComponent(24, 0, false, (e.group != null && e.group == currentGroup.id)))
            }

        }


        // sort all of the components by time
        parsedEventsLeftDay.sortBy{it.hour * 100 + it.minute}
        parsedEventsRightDay.sortBy{it.hour * 100 + it.minute}

    // draw the regions between each time in the lists
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
                drawRegion(true, timeToFloat(parsedEventsLeftDay[i].hour, parsedEventsLeftDay[i].minute), timeToFloat(parsedEventsLeftDay[i + 1].hour, parsedEventsLeftDay[i + 1].minute), groupAvailabilityColour(numberBusy, gCounter > 0))
            }
            // else it is an end time
            else
            {
                if(parsedEventsLeftDay[i].isGroupEvent)
                    gCounter--
                else
                    numberBusy--
                if(numberBusy > 0)
                    drawRegion(true, timeToFloat(parsedEventsLeftDay[i].hour, parsedEventsLeftDay[i].minute), timeToFloat(parsedEventsLeftDay[i + 1].hour, parsedEventsLeftDay[i + 1].minute), groupAvailabilityColour(numberBusy, gCounter > 0))
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
                drawRegion(false, timeToFloat(parsedEventsRightDay[i].hour, parsedEventsRightDay[i].minute), timeToFloat(parsedEventsRightDay[i + 1].hour, parsedEventsRightDay[i + 1].minute), groupAvailabilityColour(numberBusy, gCounter > 0))
            }
            // else it is an end time
            else
            {
                if(parsedEventsRightDay[i].isGroupEvent)
                    gCounter--
                else
                    numberBusy--
                if(numberBusy > 0)
                    drawRegion(false, timeToFloat(parsedEventsRightDay[i].hour, parsedEventsRightDay[i].minute), timeToFloat(parsedEventsRightDay[i + 1].hour, parsedEventsRightDay[i + 1].minute), groupAvailabilityColour(numberBusy, gCounter > 0))
            }
            i++
        }
    }

    // Draws a single region on either day1 column or day 2 column
    private fun drawRegion(isFirstDay: Boolean, startTime: Float, endTime: Float, colour: String)
    {
        val region = ShapeDrawable(RectShape())

        if(isFirstDay)
        {
            // if two users have events at the same time, don't draw a flat region, ei start time == end time
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
    // and calculates a shade of grey to represent overall group availability
    // darker grey for more available, lighter for less
    // blue for this groups events, darker blue if this groups event that members are already busy for
    private fun groupAvailabilityColour(numberBusy: Int, hasGroupEventAtThisTime: Boolean) : String
    {
        val busyPercentage: Float = if (numberBusy > 0)
            numberBusy.toFloat()/numberInGroup.toFloat()
        else
            numberBusy.toFloat()/numberInGroup.toFloat() * -1

        if(!hasGroupEventAtThisTime)
        {
            return when {
                busyPercentage > 0.5f -> "#463E3F" // black eel
                busyPercentage > 0.25f -> "#666362" // ash grey
                busyPercentage >= 0.1f -> "#B6B6B4" // grey cloud
                else -> "#E5E4E2"
            } // platinum
        }
        else
        {
            return when {
                busyPercentage > 0.5f -> "#000066"
                busyPercentage > 0.25f -> "#000099" // Lapis Blue
                busyPercentage >= 0.1f -> "#0000b3" // grey cloud
                else -> "#0000cc"
            } // platinum
        }
    }

    // converts an hour and minute into a single float
    private fun timeToFloat(hour: Int, minute: Int) : Float
    {
        return hour.toFloat() + (minute.toFloat() / 60f)
    }

    // changes the displayed days to adjacent days depending on swipe direction
    private fun swipe(isLeftSwipe: Boolean)
    {
        displayedLeftDay = if(isLeftSwipe)
            displayedLeftDay.plusDays(2)
        else
            displayedLeftDay.plusDays(-2)

        if(showingTwoDayFragment)
            (supportFragmentManager.findFragmentByTag("TOP_FRAG") as TwoDayViewFragment).setDisplayedDays(displayedLeftDay)

        // get the events for the next day
        getEvents(LocalDate.now().atStartOfDay())
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

    // submits a group event into the db
    internal fun submitNewEvent(event:FirebaseDataObjects.Event)
    {
        val db = Firebase.firestore

        val eventId = event.copy(group = currentGroup.id, participants = currentGroup.members)

        db.collection("events").document().set(eventId).addOnSuccessListener { documentReference ->
            Log.d(TAG, "DocumentSnapshot added with ID: $documentReference")
            Toast.makeText(this, "${event.name} has been submitted", Toast.LENGTH_SHORT).show()
            (supportFragmentManager.findFragmentByTag("TOP_FRAG") as FragmentEventEditor).setSaveButtonClickability(true)
            eventButton.text = "Schedule a Group Event"
            supportFragmentManager.beginTransaction().apply{
                replace(R.id.topFrame, twoDayViewFragment, "TOP_FRAG")
                commit()
            }
            showingTwoDayFragment = true
        }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error adding document", e)
                Toast.makeText(this, "${event.name} was not submitted successfully", Toast.LENGTH_SHORT).show()
                (supportFragmentManager.findFragmentByTag("TOP_FRAG") as FragmentEventEditor).setSaveButtonClickability(true)
            }

    }

    // determines the date time associated with the location on the scrollview that the user clicked on
    private fun clickPositionToTime(y : Float, isLeftDay : Boolean) : LocalDateTime
    {
        val time = (y - scalar.toFloat())/scalar.toFloat() + scrollView.scrollY/scalar.toFloat()
        val hour = time - (time % 1) - 1
        var minute = ((time % 1)/1.67f)
        minute -= (minute % 0.01f)
        minute *= 100

        return if(isLeftDay)
            LocalDateTime.of(displayedLeftDay.year, displayedLeftDay.month, displayedLeftDay.dayOfMonth, hour.toInt(), minute.toInt())
        else
            LocalDateTime.of(displayedLeftDay.plusDays(1).year, displayedLeftDay.plusDays(1).month, displayedLeftDay.plusDays(1).dayOfMonth, hour.toInt(), minute.toInt())
    }

    companion object {
        private const val TAG = "GroupCalendar"
    }
}
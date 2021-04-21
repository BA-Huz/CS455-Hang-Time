package com.brandon.hangtime


import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brandon.hangtime.FirebaseDataObjects.toLocalDateTime
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_personal_calendar.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class FragmentPersonalCalendar : Fragment()
{
    private lateinit var calendar : CalendarView
    private var eventsList: List<FirebaseDataObjects.Event> = listOf()
    private var subEventsList: MutableList<FirebaseDataObjects.Event> = mutableListOf()

    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter:EventAdapter? =  null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val v = inflater.inflate(R.layout.fragment_personal_calendar, container, false)

        adapter = EventAdapter(subEventsList)
        layoutManager = LinearLayoutManager(activity)
        setUpCalendar(v)
        getEvents()

        return v
    }

    //Initializes the calendars DateChange function to display the events for that day in the Recycler view.
    private fun setUpCalendar(v : View)
    {
        calendar = v.findViewById(R.id.personalCalendar) as CalendarView
        calendar.setOnDateChangeListener { _, year, month, dayOfMonth ->

            val selectedDate = LocalDateTime.of(year,month+1,dayOfMonth,0,0)
            subEventsList.clear()
            subEventsList.addAll(eventsList.filter {
                toLocalDateTime(it.endDateTime) > selectedDate
                        && toLocalDateTime(it.startDateTime) < selectedDate.plusDays(1)
            })

            subEventsList.sortBy { it.startDateTime}

            adapter!!.notifyDataSetChanged()

        }

    }

    //Get the events for the current user from today forwards.
    //Update the RecyclerView to show todays events
    private fun getEvents(){
        val db = Firebase.firestore.collection("events")

        val date = FirebaseDataObjects.toTimestamp(
            LocalDate.now().atStartOfDay())

        db.whereArrayContains("participants", Firebase.auth.currentUser!!.uid)
            .whereGreaterThanOrEqualTo("endDateTime", date)
            .get().addOnSuccessListener {  result ->
                eventsList = result!!.map { snapshot ->
                    snapshot.toObject<FirebaseDataObjects.Event>()
                }

                val selectedDate = LocalDate.now().atStartOfDay()

                subEventsList.clear()
                subEventsList.addAll(eventsList.filter { event ->
                    toLocalDateTime(event.endDateTime) > selectedDate &&
                            toLocalDateTime(event.startDateTime) < selectedDate.plusDays(1) })

                eventRecycler.layoutManager = layoutManager
                eventRecycler.adapter = adapter
                adapter!!.notifyDataSetChanged()

            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "Error getting documents: ", exception)
            }
    }

    companion object {
        private const val TAG = "FragmentPersonalCalendar"
    }
}



//RecyclerView Adapter for Events
class EventAdapter (private val mEvents: List<FirebaseDataObjects.Event>) : RecyclerView.Adapter<EventAdapter.ViewHolder>(){

    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        val eventNameTextView: TextView = itemView.findViewById(R.id.eventName)
        val eventStartTextView: TextView = itemView.findViewById(R.id.eventStart)
        val eventEndTextView: TextView = itemView.findViewById(R.id.eventEnd)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val eventView = inflater.inflate(R.layout.item_event, parent, false)
        return ViewHolder(eventView)
    }

    override fun onBindViewHolder(viewHolder: EventAdapter.ViewHolder, position: Int) {
        val event: FirebaseDataObjects.Event = mEvents[position]

        viewHolder.eventNameTextView.text = event.name
        viewHolder.eventStartTextView.text = "Start: ${toLocalDateTime(event.startDateTime).format(
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"
        viewHolder.eventEndTextView.text = "End: ${toLocalDateTime(event.endDateTime).format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}"
    }

    override fun getItemCount(): Int {
        return mEvents.size
    }


}
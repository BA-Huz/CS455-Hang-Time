package com.brandon.hangtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_user_select.*


class UserSelectFragment : Fragment() {

    private var autoCompleteUsers: List<FirebaseDataObjects.User> = listOf()
    private var selectedUsers:MutableList<FirebaseDataObjects.User> = mutableListOf()
    private lateinit var userAutoComplete: AutoCompleteTextView
    private lateinit var rvUsers: RecyclerView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        val v = inflater.inflate(R.layout.fragment_user_select, container, false)


        setLateInits(v)

        // Lookup the recyclerview in activity layout


        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        setAdapters()
        setAutoCompleteAdapter()
    }

    private fun setLateInits(v : View)
    {
        userAutoComplete = v.findViewById(R.id.userSelectText)

        rvUsers = v.findViewById(R.id.rvUsers) as RecyclerView


    }

    fun setAdapters(){
        // Initialize contacts
        // Create adapter passing in the sample user data
        val adapter = UsersAdapter(selectedUsers)
        // Attach the adapter to the recyclerview to populate items
        rvUsers.adapter = adapter
        // Set layout manager to position the items
        rvUsers.layoutManager = LinearLayoutManager(activity)
    }

    public fun setAutoCompleteSource(users:List<FirebaseDataObjects.User>){
        autoCompleteUsers = users
        rvUsers.adapter?.notifyDataSetChanged()
    }

    public fun getSelectedUsers(): List<FirebaseDataObjects.User> {
        return selectedUsers
    }


    private fun setAutoCompleteAdapter(){

        val adapter = ArrayAdapter(view!!.context, android.R.layout.simple_list_item_1, autoCompleteUsers)
        userAutoComplete.setAdapter(adapter)

        userAutoComplete.onItemClickListener = AdapterView.OnItemClickListener{
                parent, _, position, _->
            val selectedItem: FirebaseDataObjects.User = parent.getItemAtPosition(position) as FirebaseDataObjects.User

            selectedUsers.add(selectedItem)
            rvUsers.adapter?.notifyItemInserted(selectedUsers.size - 1)
        }


    }

}




class UsersAdapter (private val mUsers: MutableList<FirebaseDataObjects.User>) : RecyclerView.Adapter<UsersAdapter.ViewHolder>(){
    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(listItemView: View) : RecyclerView.ViewHolder(listItemView) {
        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val nameTextView: TextView = itemView.findViewById(R.id.user_name)
        val removeButton: Button = itemView.findViewById(R.id.remove_button)
    }


    // ... constructor and member variables
    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val contactView = inflater.inflate(R.layout.item_user, parent, false)
        // Return a new holder instance
        return ViewHolder(contactView)
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(viewHolder: UsersAdapter.ViewHolder, position: Int) {
        // Get the data model based on position
        val user: FirebaseDataObjects.User = mUsers[position]
        // Set item views based on your views and data model
        val textView = viewHolder.nameTextView
        textView.text = user.name
        val button = viewHolder.removeButton
        button.setOnClickListener {
            mUsers.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    // Returns the total count of items in the list
    override fun getItemCount(): Int {
        return mUsers.size
    }
}
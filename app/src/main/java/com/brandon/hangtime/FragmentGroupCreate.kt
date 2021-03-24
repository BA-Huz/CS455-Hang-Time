package com.brandon.hangtime;

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

public class FragmentGroupCreate : Fragment() {
        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
        {
                return inflater!!.inflate(R.layout.fragment_event_editor, container, false)
        }



}

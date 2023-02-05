package com.suzuki.fragment.faqFragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import com.suzuki.R;
import com.suzuki.adapter.ExpandableListAdptr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Rides extends Fragment {

    ExpandableListAdapter listAdapter;
    ExpandableListView listView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    public Rides() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_rides, container, false);
        listView = view.findViewById(R.id.expandLv);

        prepareListData();

//        listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
        listAdapter = new ExpandableListAdptr(getContext(), listDataHeader, listDataChild);


        // setting list adapter
        listView.setAdapter(listAdapter);

        return view;
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding parent data
        listDataHeader.add(getString(R.string.question1));
        listDataHeader.add(getString(R.string.question2));
        listDataHeader.add(getString(R.string.question3));

        // Adding child data
        List<String> general = new ArrayList<String>();
        general.add(getString(R.string.answer1));
        List<String> contact = new ArrayList<String>();
        contact.add(getString(R.string.answer2));
        List<String> legal = new ArrayList<String>();
        legal.add(getString(R.string.answer1));


        listDataChild.put(listDataHeader.get(0), general); // Header, Child data
        listDataChild.put(listDataHeader.get(1), contact);
        listDataChild.put(listDataHeader.get(2), legal);
    }
}

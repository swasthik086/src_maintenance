package com.suzuki.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;

import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;


import com.suzuki.R;
import com.suzuki.adapter.ExpandableListAdptr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class UserguideActivity extends Activity {
    static boolean flag = true;
    Button view;
    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    private int lastExpandedGroupPosition = -1;
    HashMap<String, List<String>> listDataChild;
//    int[] img_res = {R.drawable.about_us_icon};
//    ImageView ivCont;
    ImageView arrow;
    RelativeLayout rlUserGuide;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.userguide_faq);

        arrow = (ImageView) findViewById(R.id.ivUserGuide);
        rlUserGuide = (RelativeLayout) findViewById(R.id.rlUserGuide);
        rlUserGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        view = (Button) findViewById(R.id.btUserbtn);

        findViewById(R.id.faqBtn).setOnClickListener(v -> startActivity(new Intent(UserguideActivity.this, FaqOptionsActivity.class)));

        view.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), IntroscreenActivity.class);
            i.putExtra("help", "help");

            startActivity(i);
        });

        expListView = (ExpandableListView) findViewById(R.id.expan);

        prepareListData();

        listAdapter = new ExpandableListAdptr(this, listDataHeader, listDataChild);

        expListView.setAdapter(listAdapter);

        expListView.setOnGroupExpandListener(groupPosition -> { });

        expListView.setOnGroupCollapseListener(groupPosition -> { });

        expListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> false);
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

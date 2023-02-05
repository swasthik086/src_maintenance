package com.suzuki.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.suzuki.R;
import com.suzuki.adapter.ExpandableListAdptr;
import com.suzuki.utils.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.suzuki.activity.HomeScreenActivity.TOAST_DURATION;

public class FaqDescriptionActivity extends AppCompatActivity {

    ExpandableListView expandableListView;
    ExpandableListAdapter expandableListAdapter;
    private String title = "FAQ's";
    ArrayList<String> tempQuestionList, questionList;
    HashMap<String, List<String>> mainList, tempMainList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq_description);

        if (!getDataFromIntent()) {
            new Common(this).showToast("Issue in parsing data", TOAST_DURATION);
            return;
        }

        TextView titleTv = findViewById(R.id.titleTv);
        EditText searchEt = findViewById(R.id.searchEt);
        titleTv.setText(title);
        expandableListView = findViewById(R.id.descExpandableLv);
        expandableListAdapter = new ExpandableListAdptr(this, questionList, mainList);
        expandableListView.setAdapter(expandableListAdapter);

        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                tempQuestionList = questionList;
                tempMainList = mainList;
                if (s.length() > 0) {
                    tempQuestionList = new ArrayList<>();
                    tempMainList = new HashMap<>();
                    for (String question : questionList) {
                        if (question.toLowerCase().contains(s.toString().toLowerCase())) {
                            tempQuestionList.add(question);
                            tempMainList.put(question, mainList.get(question));
                        }
                    }
                }
                expandableListAdapter = new ExpandableListAdptr(FaqDescriptionActivity.this, tempQuestionList, tempMainList);
                expandableListView.setAdapter(expandableListAdapter);
            }
        });

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private boolean getDataFromIntent() {
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra(getString(R.string.question_list))) {
                title = intent.getStringExtra(getString(R.string.title));
                questionList = intent.getStringArrayListExtra(getString(R.string.question_list));
                mainList = (HashMap<String, List<String>>) intent.getSerializableExtra(getString(R.string.main_list));
            }
            return true;

        } else return false;
    }
}

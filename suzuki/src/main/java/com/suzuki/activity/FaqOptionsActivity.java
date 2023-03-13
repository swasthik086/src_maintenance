package com.suzuki.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.suzuki.R;
import com.suzuki.faqmodel.AboutFaq;
import com.suzuki.faqmodel.BatteryFaq;
import com.suzuki.faqmodel.BluetoothFaq;
import com.suzuki.faqmodel.GeneralFaq;
import com.suzuki.faqmodel.HelpFaq;
import com.suzuki.faqmodel.LastParkedLocationFaq;
import com.suzuki.faqmodel.NavigationFaq;
import com.suzuki.faqmodel.RideFaq;
import com.suzuki.faqmodel.SettingsFaq;
import com.suzuki.faqmodel.WelcomeFaq;
import com.suzuki.model.QuestionAndAnswer;
import com.suzuki.faqmodel.ProfileFaq;

public class FaqOptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq_options);

        /*Ride Faq*/
        findViewById(R.id.option00).setOnClickListener(v -> {
            Intent intent = new Intent(FaqOptionsActivity.this, FaqDescriptionActivity.class);
            QuestionAndAnswer questionAndAnswer = new RideFaq().getQuestionAnswer();
            intent.putExtra(getString(R.string.title),"Trips"+ " FAQ's");
            intent.putExtra(getString(R.string.question_list), questionAndAnswer.getQuestionList());
            intent.putExtra(getString(R.string.main_list), questionAndAnswer.getMainList());
            startActivity(intent);
        });

        /*Welcome*/
        findViewById(R.id.option01).setOnClickListener(v -> {
            Intent intent = new Intent(FaqOptionsActivity.this, FaqDescriptionActivity.class);
            QuestionAndAnswer questionAndAnswer = new WelcomeFaq().getQuestionAnswer();
            intent.putExtra(getString(R.string.title),getString(R.string.welcome)+" FAQ's");
            intent.putExtra(getString(R.string.question_list), questionAndAnswer.getQuestionList());
            intent.putExtra(getString(R.string.main_list), questionAndAnswer.getMainList());
            startActivity(intent);
        });

        /*About*/
        findViewById(R.id.option10).setOnClickListener(v -> {
            Intent intent = new Intent(FaqOptionsActivity.this, FaqDescriptionActivity.class);
            QuestionAndAnswer questionAndAnswer = new AboutFaq().getQuestionAnswer();
            intent.putExtra(getString(R.string.title),getString(R.string.about)+" FAQ's");
            intent.putExtra(getString(R.string.question_list), questionAndAnswer.getQuestionList());
            intent.putExtra(getString(R.string.main_list), questionAndAnswer.getMainList());
            startActivity(intent);
        });

        /*General*/
        findViewById(R.id.option11).setOnClickListener(v -> {
            Intent intent = new Intent(FaqOptionsActivity.this, FaqDescriptionActivity.class);
            QuestionAndAnswer questionAndAnswer = new GeneralFaq().getQuestionAnswer();
            intent.putExtra(getString(R.string.title),getString(R.string.general)+" FAQ's");
            intent.putExtra(getString(R.string.question_list), questionAndAnswer.getQuestionList());
            intent.putExtra(getString(R.string.main_list), questionAndAnswer.getMainList());
            startActivity(intent);
        });

        /*Settings*/
        findViewById(R.id.option20).setOnClickListener(v -> {
            Intent intent = new Intent(FaqOptionsActivity.this, FaqDescriptionActivity.class);
            QuestionAndAnswer questionAndAnswer = new SettingsFaq().getQuestionAnswer();
            intent.putExtra(getString(R.string.title),getString(R.string.setting)+" FAQ's");
            intent.putExtra(getString(R.string.question_list), questionAndAnswer.getQuestionList());
            intent.putExtra(getString(R.string.main_list), questionAndAnswer.getMainList());
            startActivity(intent);
        });

        /*profile settings faq*/
        findViewById(R.id.option21).setOnClickListener(v -> {
            Intent intent = new Intent(FaqOptionsActivity.this, FaqDescriptionActivity.class);
            QuestionAndAnswer questionAndAnswer = new ProfileFaq().getQuestionAnswer();
            intent.putExtra(getString(R.string.title),"Profile"+ " FAQ's");
            intent.putExtra(getString(R.string.question_list), questionAndAnswer.getQuestionList());
            intent.putExtra(getString(R.string.main_list), questionAndAnswer.getMainList());
            startActivity(intent);
        });

        /*Bluetooth*/
        findViewById(R.id.option30).setOnClickListener(v -> {
            Intent intent = new Intent(FaqOptionsActivity.this, FaqDescriptionActivity.class);
            QuestionAndAnswer questionAndAnswer = new BluetoothFaq().getQuestionAnswer();
            intent.putExtra(getString(R.string.title),getString(R.string.bluetooth)+" FAQ's");
            intent.putExtra(getString(R.string.question_list), questionAndAnswer.getQuestionList());
            intent.putExtra(getString(R.string.main_list), questionAndAnswer.getMainList());
            startActivity(intent);
        });

        /*Navigation*/
        findViewById(R.id.option31).setOnClickListener(v -> {
            Intent intent = new Intent(FaqOptionsActivity.this, FaqDescriptionActivity.class);
            QuestionAndAnswer questionAndAnswer = new NavigationFaq().getQuestionAnswer();
            intent.putExtra(getString(R.string.title),getString(R.string.navigation)+" FAQ's");
            intent.putExtra(getString(R.string.question_list), questionAndAnswer.getQuestionList());
            intent.putExtra(getString(R.string.main_list), questionAndAnswer.getMainList());
            startActivity(intent);
        });

        /*Help*/
        findViewById(R.id.option41).setOnClickListener(v -> {
            Intent intent = new Intent(FaqOptionsActivity.this, FaqDescriptionActivity.class);
            QuestionAndAnswer questionAndAnswer = new HelpFaq().getQuestionAnswer();
            intent.putExtra(getString(R.string.title),getString(R.string.help)+" FAQ's");
            intent.putExtra(getString(R.string.question_list), questionAndAnswer.getQuestionList());
            intent.putExtra(getString(R.string.main_list), questionAndAnswer.getMainList());
            startActivity(intent);
        });

        /*Last Parked Location*/
        findViewById(R.id.option50).setOnClickListener(v -> {
            Intent intent = new Intent(FaqOptionsActivity.this, FaqDescriptionActivity.class);
            QuestionAndAnswer questionAndAnswer = new LastParkedLocationFaq().getQuestionAnswer();
            intent.putExtra(getString(R.string.title),getString(R.string.last_parked_location)+" FAQ's");
            intent.putExtra(getString(R.string.question_list), questionAndAnswer.getQuestionList());
            intent.putExtra(getString(R.string.main_list), questionAndAnswer.getMainList());
            startActivity(intent);
        });

        /*Battery*/
        findViewById(R.id.option40).setOnClickListener(v -> {
            Intent intent = new Intent(FaqOptionsActivity.this, FaqDescriptionActivity.class);
            QuestionAndAnswer questionAndAnswer = new BatteryFaq().getQuestionAnswer();
            intent.putExtra(getString(R.string.title),getString(R.string.battery)+" FAQ's");
            intent.putExtra(getString(R.string.question_list), questionAndAnswer.getQuestionList());
            intent.putExtra(getString(R.string.main_list), questionAndAnswer.getMainList());
            startActivity(intent);
        });

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }
}

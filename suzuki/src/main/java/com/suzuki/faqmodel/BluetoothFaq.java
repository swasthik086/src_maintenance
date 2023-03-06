package com.suzuki.faqmodel;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BluetoothFaq {

    public BluetoothFaq(){

    }

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/
        questionList.add("Can I connect Suzuki Ride Connect Application with any other Bluetooth device?");
        answerList.add("• The application will only show the nearby available Suzuki vehicle to pair with it.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("Does Application Connect Automatically after the First connection?");
        answerList.add("• Yes. The application will Automatically Connect with previously paired vehicle.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("I am unable to view the Vehicle ID on Application’s Bluetooth scan list?");
        answerList.add("• Vehicle ID is advertise for 120 seconds only. You can press the Mode button on Instrument Cluster to advertise the Vehicle ID again..");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("Application doesn’t auto connect with vehicle instrument cluster while phone is locked.");
        answerList.add("From version 8.0 (Oreo) and above, Android OS restricts any applications which is running Bluetooth scan in background for more than 30 seconds to conserve the Phone’s Battery. In case your phone has been locked for more than 1 min, Suzuki Ride Connect application might not connect automatically. However, unlocking the phone while the application is still running will initiate auto-connect with your Suzuki two wheeler instrument cluster.");
        mainList.put(questionList.get(que),answerList);
        que++;


        answerList = new ArrayList<>();
        questionList.add("Will the mobile application run, if the Bluetooth is OFF?");
        answerList.add("No. The application will disconnect from the vehicle instrument cluster.");
        mainList.put(questionList.get(que),answerList);
        que++;

        return new QuestionAndAnswer(questionList, mainList);
    }

}

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
        questionList.add("Can I connect Suzuki Ride Connect Application with other Bluetooth devices?");
        answerList.add("No, the Application will only show nearby available and compatible Suzuki vehicles to pair with it.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("Does the Application connect automatically after the First connection?");
        answerList.add("Yes, if the previously paired vehicle is nearby and Bluetooth is ON, the Application will automatically connect if opened.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("Why am I unable to view the vehicle ID on the Application's Bluetooth Scan list??");
        answerList.add("Vehicle ID is advertised/displayed on the instrument cluster for 120 seconds only. If unable to view the ID, press the Mode Button on Instrument cluster to advertise the Vehicle ID again.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("Application doesn't auto-connect with the vehicle's instrument cluster when the phone is locked.");
        answerList.add("From Android 8.0 (Oreo)and above, Android OS restricts any application from running a Bluetooth Scan in the background for more than 30 secs to conserve the phone's battery. If the phone is locked for more than 1 minute, Suzuki Ride Connect application may not connect automatically. However, if the phone is unlocked with Application running, auto-connect will be initiated with Instrument Cluster.\n" +
                "\n" +
                "*Ensure Background activity is allowed and Bluetooth is ON");
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

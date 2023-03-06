package com.suzuki.faqmodel;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GeneralFaq {

    public GeneralFaq(){

    }

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/
        questionList.add("Which Android OS version is compatible for Suzuki Ride Connect Application?");
        answerList.add("It supports Android OS version 8.0 and above.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("Is internet mandatory for Application to function?");
        answerList.add("\"Yes, Internet connectivity is mandatory for navigation-related features. However, other features like Call/SMS notifications will function without internet connectivity.\n" +
                "\n" +
                "*For Data Enabled Applications, certain VOIP services are restricted due to dependency on the Mobile handset and Android OS\"");
       // answerList.add("For Data Enabled Application and certain VOIP services are restricted due to dependency on Mobile Handset and Android OS.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("What should I do if navigation information is not getting updated regularly on the speedometer screen");
        answerList = new ArrayList<>();
        answerList.add("For proper operation of the Navigation features, user should enable and allow the following permissions to Suzuki Ride Connect Application\n");
        answerList.add("1. Bluetooth ON\n" +
                "2. Location Permission with Allow all the time\n" +
                "3. Give Background activity permission to the Application.\n" +
                "4. Mobile Data (Internet Services)\n" +
                "5. Allow Notification access to receive alerts during Navigation (Call/SMS/WhatsApp)");

        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Unable to disconnect the application from vehicle’s instrument cluster.");
        answerList = new ArrayList<>();
        answerList.add("\"For Android, after successful auto-pairing, user can disconnect the application from the cluster through application's \"\"Tap to Disconnect\"\" function.\n" +
                "If unable to do so, user either can restart the application or perform Ignition OFF and ON.\"");
        mainList.put(questionList.get(que),answerList);
        que++;

        return new QuestionAndAnswer(questionList, mainList);
    }

}

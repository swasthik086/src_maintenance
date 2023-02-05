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
        answerList.add("It supports Android OS Version 6.0 and above.");
        answerList.add("Note: Certain old smartphones may not be compatible.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("Is internet mandatory for Application to function?");
        answerList.add("Yes, internet connectivity is mandatory for navigation related features. However other features such as Call/SMS/WhatsApp Call/Messages notifications will function without the internet connectivity.");
        answerList.add("For Data Enabled Application and certain VOIP services are restricted due to dependency on Mobile Handset and Android OS.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("What should I do if navigation information is not getting updated regularly on the speedometer screen");
        answerList = new ArrayList<>();
        answerList.add("For proper operation of the Navigation features, user should enable and allow permissions to the Suzuki Ride Connect application for the following:");
        answerList.add("• Bluetooth");
        answerList.add("• GPS (Location Services)");
        answerList.add("• Mobile Data (Internet Services)");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Unable to disconnect the application from vehicle’s instrument cluster.");
        answerList = new ArrayList<>();
        answerList.add("In case of Android application. After successful auto-pairing, if user wants to disconnect the application from Cluster through application's \"Tap to Disconnect\" function, user either has to restart the application or perform the Ignition OFF and then ON the vehicle.");
        mainList.put(questionList.get(que),answerList);
        que++;

        return new QuestionAndAnswer(questionList, mainList);
    }

}

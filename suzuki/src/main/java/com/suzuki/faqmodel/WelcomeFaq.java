package com.suzuki.faqmodel;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WelcomeFaq {

    public WelcomeFaq(){

    }

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/
        questionList.add("How can I change the name displayed on Welcome Screen of the Application?");
        answerList.add("Go to Rider’s Profile Section");
        answerList.add("Click on Edit icon and you can change/edit the name.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("What is the “ODO meter” reading?");
        answerList.add("ODO meter reading is the Total Distance Travelled by the vehicle");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("What is the maximum reading of Trip-A and Trip-B?");
        answerList = new ArrayList<>();
        answerList.add("Maximum reading of Trip A & Trip B is 99999.9km.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Why does the Fuel Bar blink on the Welcome Screen?");
        answerList = new ArrayList<>();
        answerList.add("The blinking bar in Amber colour indicates the Low Fuel Level in the Vehicle.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Is the vehicle data on the welcome screen such as Odometer, Trip-A and Trip-B data get reset when Application disconnects from the Vehicle?");
        answerList = new ArrayList<>();
        answerList.add("No. These data will not be reset. The application will show the last connected vehicle’s information while not connected with the vehicle.");
        mainList.put(questionList.get(que),answerList);

        return new QuestionAndAnswer(questionList, mainList);
    }

}

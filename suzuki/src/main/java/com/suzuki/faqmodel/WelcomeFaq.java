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
        questionList.add("How can I change the name displayed on the Welcome Screen of the Application?");
        answerList.add("1) Go to the \"Last Sync Data\" tab\n" +
                "2) Go to 'Rider's Profile' Section\n" +
                "3) Click on the Edit icon to change/edit the name");
        //answerList.add("Click on Edit icon and you can change/edit the name.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("What is the ODOmeter reading?");
        answerList.add("ODOmeter reading is the Total Distance Travelled by the vehicle");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("What is the maximum reading of Trip-A and Trip-B?");
        answerList = new ArrayList<>();
        answerList.add("Maximum reading of Trip A & Trip B is 99999.9km");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Why does the Fuel Bar blink on the Welcome Screen?");
        answerList = new ArrayList<>();
        answerList.add("The blinking bar in Amber Colour indicates the Low Fuel Level in the vehicle");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Is the vehicle data on the Welcome Screen i.e. ODOmeter, Trip-A and Trip-B data reset when the Application disconnects from the vehicle?");
        answerList = new ArrayList<>();
        answerList.add("No. These data will not be reset. The application will show the last connected vehicle's information even if the vehicle is not connected");
        mainList.put(questionList.get(que),answerList);

        return new QuestionAndAnswer(questionList, mainList);
    }

}

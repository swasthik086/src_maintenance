package com.suzuki.faqmodel;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FeaturesFaq {

    public FeaturesFaq(){

    }

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/
        questionList.add("Can I get nearby Hospitals in the navigation screen?");
        answerList.add("Yes. You can get to the nearest Hospitals by clicking on the ‘+’(POI)" +
                "symbol in navigation screen. And click on the hospital it will show the nearest Hospitals.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("Can I get to the nearby Suzuki Service centre on the navigation screen?");
        answerList.add("Yes. You can get to the nearest Suzuki Service centre by clicking on the " +
                "‘+’(POI) symbol in navigation screen. And click on the Suzuki Service option, " +
                "it will show the nearest Suzuki Service centre.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Can I get nearby Suzuki Sales on the navigation screen?");
        answerList = new ArrayList<>();
        answerList.add("Yes. You can get the nearest Suzuki Sales by clicking on the ‘+’(POI) " +
                "symbol in navigation screen. And click on Suzuki Sales it will show the nearest Suzuki Sale's shop.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Can I get near the ATM in the navigation screen?");
        answerList = new ArrayList<>();
        answerList.add("Yes. You can get the nearest ATM by clicking on the ‘+’ (POI) symbol in " +
                "navigation screen. And click on Banks and ATMs it will show the nearest Banks and ATM.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Can I get nearby Restaurants in the navigation screen?");
        answerList = new ArrayList<>();
        answerList.add("Yes. You can get the nearest Restaurants by clicking on the ‘+’(POI) symbol " +
                "in navigation screen. And click on Food and Restaurants it will show the nearest Restaurant.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Can I navigate from the RECENT trips?");
        answerList = new ArrayList<>();
        answerList.add("Yes. You can navigate if a recent trip is available.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("How can I navigate through my RECENT trip?");
        answerList = new ArrayList<>();
        answerList.add("Open the application");
        answerList.add("Click on the last tab “MORE”.");
        answerList.add("Click on RECENT.");
        answerList.add("Click on the available recent trip, which you want to re-navigate.");
        answerList.add("In the trip details screen, you will get a Navigate button.");
        answerList.add("Click on the Navigate button you will re-navigate to the location.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("How can I navigate through my FAVOURITE trip?");
        answerList = new ArrayList<>();
        answerList.add("Open the application.");
        answerList.add("Click on the last tab “MORE”.");
        answerList.add("Click on Trips.");
        answerList.add("Click on FAVOURITE (if you made it favourite).");
        answerList.add("Click on the available favourite trip, which you want to re-navigate.");
        answerList.add("In the trip details screen, you will get a Navigate button.");
        answerList.add("Click on the Navigate button you will re-navigate to the location.");
        mainList.put(questionList.get(que),answerList);

        return new QuestionAndAnswer(questionList, mainList);
    }

}

package com.suzuki.faqmodel;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RideFaq {

    public RideFaq(){

    }

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/
        questionList.add("How can I pair the application with vehicle?");
        answerList.add("In Ride Connect Application, click on \"PAIR with SUZUKI\" option on Welcome Screen. \n" +
                "Find the desired vehicle ID and connect. \n" +
                "Sample IDs - SAS010000001 and SBM0000001");
       // answerList.add("Select to pair on Suzuki Vehicle ID.");
        //answerList.add("Sample IDs SAS010000001 and SBS010000001.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("How can I record my trips?");
        answerList.add("Open Suzuki Ride Connect Application.\n" +
                "Go to the \"Settings\" tab. \n" +
                "Enable 'Save All Trips'.");
       // answerList.add("Go to the “Settings” tab.");
       // answerList.add("Enable ‘Save All Trips’ option.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("How many trips can be saved?");
        answerList = new ArrayList<>();
        answerList.add("You can save a maximum of 10 Recent Trips and 10 Favourite Trips");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("What is Trip Count?");
        answerList = new ArrayList<>();
        answerList.add("Trip Count indicates the total number of Saved Trips while the Navigation is Active");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("How can I save a Recent Trip as a Favourite Trip?");
        answerList = new ArrayList<>();
        answerList.add("1. Go to \"Last Sync Data\" tab.\n" +
                "2. Click on \"Trips\", \"Recent\" Trips tab will open.\n" +
                "3. Click on Favourite \"Heart \" Icon of that Trip.\n" +
                "4. The colour of the Favourite Icon will change to blue heart which indicates it as a favourite Trip.");
        // answerList.add("Click on “Trips”, “Recent” Trips tab will open,");
       // answerList.add("Click on Favourite “♡” Icon of that Trip.");
       // answerList.add("The Colour of the Favourite Icon will change to Blue “💙” which indicates it as a Favourite Trip.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("If I save a Recent Trip as a Favourite Trip, will it disappear from the Recent Trip list?");
        answerList = new ArrayList<>();
        answerList.add("No, the Recent Trip list will remain the same");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("If I delete all Recent Trips, will it affect the Favourite Trips?");
        answerList = new ArrayList<>();
        answerList.add("No, it will not affect the Favourite Trips.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("What if the number of Recent Trips exceeds the maximum limit?");
        answerList = new ArrayList<>();
        answerList.add("Recent Trips can store up to 10 trips. Every new trip will replace the oldest trip automatically.");
        mainList.put(questionList.get(que),answerList);

        return new QuestionAndAnswer(questionList, mainList);
    }

}

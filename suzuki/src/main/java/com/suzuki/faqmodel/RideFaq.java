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
        questionList.add("How to Pair Application to with Vehicle?");
        answerList.add("In Ride Connect Application, Click on “PAIR WITH SUZUKI” option on  welcome screen.");
        answerList.add("Select to pair on Suzuki Vehicle ID.");
        answerList.add("Sample IDs SAS010000001 and SBS010000001.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("How to Record the Trips?");
        answerList.add("Open the Suzuki Ride Connect Application.");
        answerList.add("Go to the “Settings” tab.");
        answerList.add("Enable ‘Save All Trips’ option.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("How Many Trips can be Saved?");
        answerList = new ArrayList<>();
        answerList.add("You can save 10 Recent Trips and 10 Favourite Trips.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("What is Ride Count?");
        answerList = new ArrayList<>();
        answerList.add("Ride Count indicates the total number of Saved Rides while Navigation is Active.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("How can I save recent Trip into a Favourite Trip?");
        answerList = new ArrayList<>();
        answerList.add("Go to “Last Sync Data” tab.");
        answerList.add("Click on “Trips”, “Recent” Trips tab will open,");
        answerList.add("Click on Favourite “♡” Icon of that Trip.");
        answerList.add("The Colour of the Favourite Icon will change to Blue “💙” which indicates it as a Favourite Trip.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("If I change a Recent Trip to Favourite Trip, will it disappear from Recent Trip list?");
        answerList = new ArrayList<>();
        answerList.add("No, the Recent Trip list will remain the same.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("If I delete all Recent Trips, will it affect the Favourite Trips?");
        answerList = new ArrayList<>();
        answerList.add("No. It will not affect the Favourite Trips.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("What if the number of Recent Trips exceeds the maximum limit?");
        answerList = new ArrayList<>();
        answerList.add("The Recent Trip can store up to 10 Trips. The new trip will replace the oldest trip automatically.");
        mainList.put(questionList.get(que),answerList);

        return new QuestionAndAnswer(questionList, mainList);
    }

}

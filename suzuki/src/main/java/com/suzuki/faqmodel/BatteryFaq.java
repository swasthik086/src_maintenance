package com.suzuki.faqmodel;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BatteryFaq {

    public BatteryFaq(){

    }

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/

        questionList.add("Why is my WhatsApp notification not being displayed on the vehicle’s instrument cluster?");
        answerList.add("Older versions of Android (6 and 7) does not allow WhatsApp notifications when Battery Saver mode is turned ON. The Battery Saver mode need to be disabled on your smartphone to get the notifications on instrument cluster.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("What will happen if the Battery optimization permission is not enabled?");
        answerList.add("The application will disconnect from  vehicle instrument cluster");
        mainList.put(questionList.get(que),answerList);
        que++;



        return new QuestionAndAnswer(questionList, mainList);
    }

}

package com.suzuki.faqmodel;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HelpFaq {

    public HelpFaq(){

    }

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/
        questionList.add("Can user send any Feedback to Suzuki Motorcycle India within the Application?");
        answerList.add("Users can share their feedbacks within the application from:");
        answerList.add("• Go to Help Section in Last Sync Data tab of the application,");
        answerList.add("• Click on Contact,");
        answerList.add("• Select either Call/Mail/Feedback option.");
        mainList.put(questionList.get(que),answerList);
        que++;

        return new QuestionAndAnswer(questionList, mainList);
    }

}

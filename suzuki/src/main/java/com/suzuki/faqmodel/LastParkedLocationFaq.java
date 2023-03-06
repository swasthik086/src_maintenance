package com.suzuki.faqmodel;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LastParkedLocationFaq {

    public LastParkedLocationFaq(){

    }

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/
        questionList.add("Why application is not displaying the Last Parked Location information?");
        answerList.add("For last parked location, you have to use navigation feature at least once.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("How to locate the Last Parked Vehicle?");
        answerList.add("• To locate the last parked.");
        answerList.add("• If the parked Vehicle is within 500m, then the Application gives Pedestrian Route Guidance (Dotted Lines) to the Last Parked Location.");
        answerList.add("• If your parked Vehicle is more than 500m, then it will show Navigation route to reach to the parked vehicle location.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("Can I share the last parked location to others?");
        answerList.add("Yes, you can share your last parked location with others via social networking services.");
        mainList.put(questionList.get(que),answerList);
        que++;

        return new QuestionAndAnswer(questionList, mainList);
    }

}

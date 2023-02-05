package com.suzuki.faqmodel;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileSettingsFaq {

    public ProfileSettingsFaq(){

    }

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/
        questionList.add("First question");
        answerList.add("This is first solution");
        answerList.add("This is second solution");
        answerList.add("This is third solution");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("Second question");
        answerList.add("This is first solution");
        answerList.add("This is second solution");
        answerList.add("This is third solution");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Third question");
        answerList = new ArrayList<>();
        answerList.add("This is first solution");
        answerList.add("This is second solution");
        answerList.add("This is third solution");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Fourth question");
        answerList = new ArrayList<>();
        answerList.add("This is first solution");
        answerList.add("This is second solution");
        answerList.add("This is third solution");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Fifth question. This is a big question for testing multiple lines in user interface");
        answerList = new ArrayList<>();
        answerList.add("This is first solution for this question for testing multiple lines in user interface");
        answerList.add("This is second solution");
        answerList.add("This is third solution");
        mainList.put(questionList.get(que),answerList);

        return new QuestionAndAnswer(questionList, mainList);
    }

}

package com.suzuki.faqmodel;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AboutFaq {

    public AboutFaq(){

    }

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/
        questionList.add("Is it going to affect application behaviour if required permissions are not provided during installation of Application?");
        answerList.add("If you don’t allow all permissions, this will affect the certain features in application. However, you can always Enable the Permissions, by following steps:");
        answerList.add("• Go to the Apps & Notifications under Android Settings.");
        answerList.add("• Select Suzuki Ride Connect Application and click on Permissions.");
        answerList.add("• Enable all the permissions. [Android settings may vary for OS version to version].");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("What happened if I force close the Application?");
        answerList.add("The application will disconnect from the Vehicle. It may also lead to loss of information of the Current Ride.");
        mainList.put(questionList.get(que),answerList);
        que++;

        return new QuestionAndAnswer(questionList, mainList);
    }

}

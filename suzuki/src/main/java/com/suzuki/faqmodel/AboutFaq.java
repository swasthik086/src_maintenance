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
        questionList.add("Will the Application behaviour be affected if the required permissions are not provided during installation of Application?");
        answerList.add("Please check the following permissions.To check- Settings>App Management>Suzuki Ride Connect\n");
        answerList.add("1.Permissions>Location>Allow All the Time \n");
        answerList.add("2.Battery Usage>Allow Background Activity \n");
        answerList.add("3.Unused Apps>Turn OFF (Battery>Optimize Battery Use>Don't Optimize)\n");
        answerList.add("4. Allow Notification Access in App \n");
        answerList.add("If unresolved, mail to customer.queries@suzukimotorcycle.in");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("What happened if I force close the Application?");
        answerList.add("The application will disconnect from the vehicle. It may also lead to a loss of information about the Current Trip.");
        mainList.put(questionList.get(que),answerList);
        que++;

        return new QuestionAndAnswer(questionList, mainList);
    }

}

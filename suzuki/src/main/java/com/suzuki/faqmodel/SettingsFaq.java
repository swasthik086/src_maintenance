package com.suzuki.faqmodel;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SettingsFaq {

    public SettingsFaq(){

    }

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/
        questionList.add("Why I am unable to see notifications on speedometer?");
        answerList.add("You should enable the Notifications in Settings of the application. The notifications can be enabled or disabled as per your preference.");
        answerList.add("All notifications which are enabled will be displayed on speedometer.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("Why I cannot see SMS Notification or WhatsApp notification on speedometer though it is enabled in notification settings?");
        answerList.add("For unsaved contacts, SMS and WhatsApp Notification are not supported, to avoid the unnecessary distraction while riding.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Does the application support notifications for Contacts Saved in Other than English Language?");
        answerList = new ArrayList<>();
        answerList.add("The application currently supports English language only. The speedometer will display the Caller Number in case of non-supported languages.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Not getting Calls over Smartphone while it is connected with vehicle?");
        answerList = new ArrayList<>();
        answerList.add("Make sure that “Auto-Reply SMS” feature is disabled in application’s Settings to get calls on smartphone.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("How does the Speed Warning feature work?");
        answerList = new ArrayList<>();
        answerList.add("• The User can set the speed for Speed Warning in application’s Settings Screen.");
        answerList.add("• The Text box or Slider can be used for setting the Speed.");
        answerList.add("• The user will get the Over Speed Warning message on vehicle’s Instrument Custer when vehicle crosses the set speed limit.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("How does “Auto Reply SMS” work?");
        answerList = new ArrayList<>();
        answerList.add("• The User has to enable the “Auto-Reply SMS” option under Application’s Settings.");
        answerList.add("• The User can select one of the predefined text options or can enter custom Text.");
        answerList.add("• After enabling this feature, user will not get any call alert/notification on and caller will receive the defined message.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Does Application support WhatsApp Video Call notification?");
        answerList = new ArrayList<>();
        answerList.add("No, Application does not support WhatsApp Video Call Notification.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Does Application support WhatsApp Group Message Notification?");
        answerList = new ArrayList<>();
        answerList.add("Yes. It supports.. The Group Name will be displayed over the Instrument Cluster.");
        mainList.put(questionList.get(que),answerList);
        que++;

        return new QuestionAndAnswer(questionList, mainList);
    }

}

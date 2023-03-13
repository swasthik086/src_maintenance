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
        questionList.add("Why am I unable to see notifications on the Instrument cluster?");
        answerList.add("Ensure the notifications are enabled in Setting Tab of the application. \n" +
                "Also, allow Notification access to Suzuki Ride Connect\n" +
                "\n" +
                "*Notifications can be enabled/disabled as per user preference. All Notifications which are enabled will be displayed on the Instrument Cluster.");
      //  answerList.add("All notifications which are enabled will be displayed on speedometer.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("Why can I not see SMS notifications or WhatsApp notifications on the instrument cluster even though it is enabled in Notification Settings?");
        answerList.add("For any unsaved contacts, SMS & WhatsApp notifications are not supported to avoid any unnecessary distractions while riding");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Does the application support notifications for Contacts Saved other than English Language?");
        answerList = new ArrayList<>();
        answerList.add("The application currently supports English language only. The Instrument cluster will display the caller in case of non-supported languages.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Why am I unable to get any calls over my smartphone while it is connected with the vehicle?");
        answerList = new ArrayList<>();
        answerList.add("Make sure that \"Auto-Reply SMS\" feature is disabled in Application's Settings tab to get calls on the smartphone");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("How does the Speed Exceeding feature work?");
        answerList = new ArrayList<>();
        answerList.add("Change Speed Warning/ OverSpeed Warning to Speed Exceeding everywhere");
      //  answerList.add("• The Text box or Slider can be used for setting the Speed.");
     //   answerList.add("• The user will get the Over Speed Warning message on vehicle’s Instrument Custer when vehicle crosses the set speed limit.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("How does the Auto-Reply SMS work?");
        answerList = new ArrayList<>();
        answerList.add("Change Application's settings to Settings Tab");
      //  answerList.add("• The User can select one of the predefined text options or can enter custom Text.");
      //  answerList.add("• After enabling this feature, user will not get any call alert/notification on and caller will receive the defined message.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Does the Application support WhatsApp Video call notification?");
        answerList = new ArrayList<>();
        answerList.add("No, Application doesn't support WhatsApp Video Call Notification");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Does the Application support WhatsApp Group message notification?");
        answerList = new ArrayList<>();
        answerList.add("Yes, WhatsApp Group Notifications are received, but displayed is the sender name not group name.");
        mainList.put(questionList.get(que),answerList);
        que++;

        return new QuestionAndAnswer(questionList, mainList);
    }

}

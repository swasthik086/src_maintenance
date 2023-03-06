package com.suzuki.faqmodel;

import android.os.Parcel;
import android.os.Parcelable;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProfileFaq implements Parcelable {

    public ProfileFaq() {
    }

    private ProfileFaq(Parcel in) {
    }

    public static final Creator<ProfileFaq> CREATOR = new Creator<ProfileFaq>() {
        @Override
        public ProfileFaq createFromParcel(Parcel in) {
            return new ProfileFaq(in);
        }

        @Override
        public ProfileFaq[] newArray(int size) {
            return new ProfileFaq[size];
        }
    };

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/
        questionList.add("How to Remove/Modify the Rider Profile picture?");
        answerList.add("\"Go to \"\"Last Sync Data\"\"tab and select the \"\"Profile\"\" option\n" +
                "Click on the existing Profile picture\n" +
                "User will get options to \"\"Remove\"\" or \"\"Replace\"\"(from Gallery/Camera) will appear\"");
    //    answerList.add("• Click on the existing Profile picture.");
    //    answerList.add("• Options to “Delete” or Replace (from “Gallery” or “Camera”) will appear.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("What is the Maximum Character Size for Rider’s Name and Location?");
        answerList.add("The maximum characters limit to input Name and Location is 20 characters. Only English Alphabets are  allowed.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("How to feed the Location information in Profile Page?");
        answerList = new ArrayList<>();
        answerList.add("• Rider can enter the Location name manually or ");
        answerList.add("• Click on icon “⌖” to get current location automatically from GPS location information.");
        mainList.put(questionList.get(que),answerList);
        que++;

        return new QuestionAndAnswer(questionList, mainList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}

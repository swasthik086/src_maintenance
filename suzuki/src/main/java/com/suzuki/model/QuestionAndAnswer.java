package com.suzuki.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class QuestionAndAnswer implements Parcelable {
    private ArrayList<String> questionList;
    private HashMap<String, List<String>> mainList;

    public QuestionAndAnswer() {
    }

    public QuestionAndAnswer(ArrayList<String> questionList, HashMap<String, List<String>> mainList) {
        this.questionList = questionList;
        this.mainList = mainList;
    }

    private QuestionAndAnswer(Parcel in) {
        questionList = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(questionList);
        dest.writeMap(mainList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<QuestionAndAnswer> CREATOR = new Creator<QuestionAndAnswer>() {
        @Override
        public QuestionAndAnswer createFromParcel(Parcel in) {
            return new QuestionAndAnswer(in);
        }

        @Override
        public QuestionAndAnswer[] newArray(int size) {
            return new QuestionAndAnswer[size];
        }
    };

    public ArrayList<String> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(ArrayList<String> questionList) {
        this.questionList = questionList;
    }

    public HashMap<String, List<String>> getMainList() {
        return mainList;
    }

    public void setMainList(HashMap<String, List<String>> mainList) {
        this.mainList = mainList;
    }
}

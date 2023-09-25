package com.suzuki.faqmodel;

import com.suzuki.model.QuestionAndAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NavigationFaq {

    public NavigationFaq(){

    }

    public QuestionAndAnswer getQuestionAnswer(){
        /*init all required objects*/
        ArrayList<String> questionList = new ArrayList<>();
        ArrayList<String> answerList = new ArrayList<>();
        HashMap<String, List<String>> mainList = new HashMap<>();
        int que = 0;
        /*first question*/
        questionList.add("Can I use the Navigation feature without connecting Application to the vehicle?");
        answerList.add("No, you have to connect application with vehicle's Instrument Cluster for using Navigation feature.");
        mainList.put(questionList.get(que),answerList);
        que++;

        /*2nd question*/
        answerList = new ArrayList<>();
        questionList.add("How to use Navigation features?");
        answerList.add("1) Open Suzuki Ride connect Application\n" +
                "2) Turn ON the vehicle and Establish Bluetooth Connection.\n" +
                "3) Open the Navigation in smart phone and enter the Destination location or Tap on Map to set the destination.\n" +
                "4) Start the navigation by clicking on Navigate option.");
       // answerList.add("• Turn ON the Vehicle and Establish Bluetooth Connection.");
     //   answerList.add("• Open the Navigation in smartphone and enter the Destination location or Tap on Map to set the destination.");
     //   answerList.add("• Start the Navigation by clicking on Navigate option.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Is Location sharing possible while riding?");
        answerList = new ArrayList<>();
        answerList.add("No, the location information cannot be shared by this Application.");
        mainList.put(questionList.get(que),answerList);
        que++;

        questionList.add("Can Trip information be shared with others?");
        answerList = new ArrayList<>();
        answerList.add("Yes, you can share the trip information with others Trip screen, the icon     be used to share the Trip information with others.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("What does the + symbol indicate in the Navigation screen?");
        answerList.add("The “+” Symbol indicates Point of Interests (POI). You can search for nearby point of interest from this list. The pre-defined Point of Interests are:");
        answerList.add("- Suzuki Service");
        answerList.add("- Suzuki Sales");
        answerList.add("- Fuel Stations");
        answerList.add("- Hospitals");
        answerList.add("- Banks and ATM");
        answerList.add("- Food and Restaurants");
        answerList.add("- Tyre Repair Shops");
        answerList.add("- Medical Stores");
        answerList.add("- Parking");
        answerList.add("- Convenience Stores");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("Is “Tap and Hold” on the Map Supported for Selecting the Destination location?");
        answerList.add("Yes, user can select the destination by Tap and Hold on map.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("Can “Via Points” be added in the Navigation?");
        answerList.add("Yes. By clicking on Add icon “⨁” in Navigation search field, Rider can add up to 10 Intermediate Destination Points.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("Will the Navigation get Re-routed when Rider takes different route?");
        answerList.add("• Yes. It will be automatically Re-routed.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("Will there be any Notification after reaching a Via-Point?");
        answerList.add("Yes, the Notification “Via Point Reached” will be displayed on the vehicle’s Instrument Cluster.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("Are there any Quick Access options for Home and Work location?");
        answerList.add("Yes, there are \"Home\" and \"Work\" icons available for Quick Access to predefined address location for Navigation.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("Is there a way to precisely assign the Location information to Home and Work?");
        answerList.add("Yes, the “Tap and Hold” option can be used to link the location to these icons.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("Can Rider use Recent Trips for Navigation?");
        answerList.add("Yes, it is supported.");
        answerList.add("Go to the Recent Trips option in Application’s Last Sync Data screen,");
        answerList.add("Click on the “Navigate” icon on Route Screen Section to start the Navigation.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add("Can Rider Quickly Search for Nearby Suzuki Service Stations, Hospitals, ATMs…etc?");
        answerList.add("Yes, these are part of POI (Point of Interest) feature. There are different POIs available such nearby Hospitals, Suzuki Service Stations, Suzuki Sales, ATMs, Restaurants…etc.");
        mainList.put(questionList.get(que),answerList);
        que++;

        answerList = new ArrayList<>();
        questionList.add(" On completing my ride, will I get any notification on my phone ?");
        answerList.add("Yes, you will get pop message stating that \"Destination reached, you want to exit the navigation\" ?");
        mainList.put(questionList.get(que),answerList);
        que++;

        return new QuestionAndAnswer(questionList, mainList);
    }

}

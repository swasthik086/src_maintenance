package com.suzuki.pojo;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SettingsPojo extends RealmObject {

    private boolean syncClockTime;
    @PrimaryKey
    private int id;
    private int speedAlert;
    private  boolean speedSet;
    private boolean autoReplySMS;
    private String message;


    private boolean callYouback;
    private boolean Imbusy;
    private boolean imRiding;
    private boolean customMsg;
    private boolean callsms;

    private boolean incomingCall;
    private boolean incomingSMS;
    private boolean whatsappCall;
    private boolean whatsappMSG;
    private boolean SaveTrips;


    public SettingsPojo() {
    }

    public boolean isSpeedSet() {
        return speedSet;
    }

    public void setSpeedSet(boolean speedSet) {
        this.speedSet = speedSet;
    }

    public boolean isSaveTrips() {
        return SaveTrips;
    }

    public void setSaveTrips(boolean saveTrips) {
        SaveTrips = saveTrips;
    }

    public boolean isWhatsappCall() {
        return whatsappCall;
    }

    public void setWhatsappCall(boolean whatsappCall) {
        this.whatsappCall = whatsappCall;
    }

    public boolean isWhatsappMSG() {
        return whatsappMSG;
    }

    public void setWhatsappMSG(boolean whatsappMSG) {
        this.whatsappMSG = whatsappMSG;
    }

    public boolean isIncomingSMS() {
        return incomingSMS;
    }

    public void setIncomingSMS(boolean incomingSMS) {
        this.incomingSMS = incomingSMS;
    }

    public boolean isIncomingCall() {
        return incomingCall;
    }

    public void setIncomingCall(boolean incomingCall) {
        this.incomingCall = incomingCall;
    }

    public boolean isCallsms() {
        return callsms;
    }

    public void setCallsms(boolean callsms) {
        this.callsms = callsms;
    }

    public boolean isCallYouback() {
        return callYouback;
    }

    public void setCallYouback(boolean callYouback) {
        this.callYouback = callYouback;
    }

    public boolean isImbusy() {
        return Imbusy;
    }

    public void setImbusy(boolean imbusy) {
        Imbusy = imbusy;
    }

    public boolean isImRiding() {
        return imRiding;
    }

    public void setImRiding(boolean imRiding) {
        this.imRiding = imRiding;
    }

    public boolean isCustomMsg() {
        return customMsg;
    }

    public void setCustomMsg(boolean customMsg) {
        this.customMsg = customMsg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSyncClockTime() {
        return syncClockTime;
    }

    public void setSyncClockTime(boolean syncClockTime) {
        this.syncClockTime = syncClockTime;
    }

    public int getSpeedAlert() {
        return speedAlert;
    }

    public void setSpeedAlert(int speedAlert) {
        this.speedAlert = speedAlert;
    }

    public boolean isAutoReplySMS() {
        return autoReplySMS;
    }

    public void setAutoReplySMS(boolean autoReplySMS) {
        this.autoReplySMS = autoReplySMS;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

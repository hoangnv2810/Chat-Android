package com.example.chatclone.utils;

import java.util.HashMap;

public class Constants {

    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";
    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_INVITATION_CANCEL = "cancel";
    public static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";



    public static HashMap<String, String> getRemoteMessageHeaders(){
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Authorization", "key=AAAAufIdU7E:APA91bGL42PAyPnJPPbT6eZlNgJpEvySTJlwJp25zQsx2x4l7ROUc5pRRohkRoX2gconLOZauiJ-bVyNW1WOlK7RHf46cVbj6n10n_J_ea2qQAanFaU7cBe15B23GaEu9p11OyTbU90S");
        headers.put("Content-Type", "application/json");
        return headers;
    }
}

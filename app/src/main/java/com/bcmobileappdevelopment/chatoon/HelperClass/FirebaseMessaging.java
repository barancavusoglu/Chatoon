package com.bcmobileappdevelopment.chatoon.HelperClass;

import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("tarja","yeni mesaj geldi");
        if (remoteMessage.getData().size() > 0) {
            Message msg = new Message();
            msg.setAuthor(new Author());
            String dialogID = "";
            for (Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (key.equals("messageID"))
                    msg.setId(value);
                else if (key.equals("senderUserProfilePic"))
                    msg.getUser().setAvatar(value);
                else if (key.equals("senderUserID"))
                    msg.getUser().setId(value);
                else if (key.equals("content"))
                    msg.setText(value);
                else if (key.equals("imageURL"))
                    msg.setImageURL(value);
                else if (key.equals("senderUserName"))
                    msg.getUser().setName(value);
                else if (key.equals("chatID"))
                    dialogID = value;
                //Log.d("tarja", "key, " + key + " value " + value);
            }
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent("new_message_received");
            intent.putExtra("messageData",remoteMessage);
            intent.putExtra("messageID",msg.getId());
            intent.putExtra("senderUserProfilePic",msg.getUser().getAvatar());
            intent.putExtra("senderUserID",msg.getUser().getId());
            intent.putExtra("content",msg.getText());
            intent.putExtra("imageURL",msg.getImageUrl());
            intent.putExtra("senderUserName", msg.getUser().getName());
            intent.putExtra("dialogID", dialogID);
            sendBroadcast(intent);
        }
        if (remoteMessage.getNotification() != null) {
            //Log.d("tarja", "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }
    }

    @Override
    public void onNewToken(String s) {
        Intent intent = new Intent("new_token");
        intent.putExtra("new_token",s);
        sendBroadcast(intent);
        super.onNewToken(s);
    }
}

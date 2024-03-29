package com.bcmobileappdevelopment.chatoon.GsonResponse;

import java.util.List;

public class LoadMoreMessagesResponse {

    private boolean isSuccess;
    private String message;
    private List<MessagesBean> messages;

    public boolean isIsSuccess() {
        return isSuccess;
    }

    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<MessagesBean> getMessages() {
        return messages;
    }

    public void setMessages(List<MessagesBean> messages) {
        this.messages = messages;
    }

    public static class MessagesBean {

        private String text;
        private String ID;
        private String imageURL;
        private String date;
        private int fromUserID;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public String getImageURL() {
            return imageURL;
        }

        public void setImageURL(String imageURL) {
            this.imageURL = imageURL;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getFromUserID() {
            return fromUserID;
        }

        public void setFromUserID(int fromUserID) {
            this.fromUserID = fromUserID;
        }
    }
}

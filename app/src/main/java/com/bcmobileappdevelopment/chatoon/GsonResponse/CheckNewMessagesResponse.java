package com.bcmobileappdevelopment.chatoon.GsonResponse;

import java.util.List;

public class CheckNewMessagesResponse {

    /**
     * isSuccess : true
     * message : success
     * totalUnreadCount : 2
     * maxID : 5
     * dialogs : [{"unreadCount":1,"photo":"https://firebasestorage.googleapis.com/v0/b/votidea-ef6e2.appspot.com/o/images%2Fturk%2Fchatoon_logo2-5.png?alt=media&token=f05185c1-d85b-43f7-aeaa-9384f94a33a2","fromUsername":"Anonymous#1001","ID":"2","fromUserID":"70","messages":[{"text":"text","ID":"3","imageURL":"https://images-na.ssl-images-amazon.com/images/I/51ueQMCe40L._SX425_.jpg","date":"1993-02-01T00:00:00","fromUserID":70}]},{"unreadCount":1,"photo":"https://firebasestorage.googleapis.com/v0/b/votidea-ef6e2.appspot.com/o/images%2Fturk%2Fchatoon_logo2-5.png?alt=media&token=f05185c1-d85b-43f7-aeaa-9384f94a33a2","fromUsername":"Anonymous#1002","ID":"3","fromUserID":"71","messages":[{"text":"VGhhbmtz\n","ID":"5","imageURL":null,"date":"1993-02-01T00:00:00","fromUserID":71}]}]
     */

    private boolean isSuccess;
    private String message;
    private int totalUnreadCount;
    private int maxID;
    private List<DialogsBean> dialogs;

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

    public int getTotalUnreadCount() {
        return totalUnreadCount;
    }

    public void setTotalUnreadCount(int totalUnreadCount) {
        this.totalUnreadCount = totalUnreadCount;
    }

    public int getMaxID() {
        return maxID;
    }

    public void setMaxID(int maxID) {
        this.maxID = maxID;
    }

    public List<DialogsBean> getDialogs() {
        return dialogs;
    }

    public void setDialogs(List<DialogsBean> dialogs) {
        this.dialogs = dialogs;
    }

    public static class DialogsBean {
        /**
         * unreadCount : 1
         * photo : https://firebasestorage.googleapis.com/v0/b/votidea-ef6e2.appspot.com/o/images%2Fturk%2Fchatoon_logo2-5.png?alt=media&token=f05185c1-d85b-43f7-aeaa-9384f94a33a2
         * fromUsername : Anonymous#1001
         * ID : 2
         * fromUserID : 70
         * messages : [{"text":"text","ID":"3","imageURL":"https://images-na.ssl-images-amazon.com/images/I/51ueQMCe40L._SX425_.jpg","date":"1993-02-01T00:00:00","fromUserID":70}]
         */

        private int unreadCount;
        private String photo;
        private String fromUsername;
        private String ID;
        private String fromUserID;
        private List<MessagesBean> messages;

        public int getUnreadCount() {
            return unreadCount;
        }

        public void setUnreadCount(int unreadCount) {
            this.unreadCount = unreadCount;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public String getFromUsername() {
            return fromUsername;
        }

        public void setFromUsername(String fromUsername) {
            this.fromUsername = fromUsername;
        }

        public String getID() {
            return ID;
        }

        public void setID(String ID) {
            this.ID = ID;
        }

        public String getFromUserID() {
            return fromUserID;
        }

        public void setFromUserID(String fromUserID) {
            this.fromUserID = fromUserID;
        }

        public List<MessagesBean> getMessages() {
            return messages;
        }

        public void setMessages(List<MessagesBean> messages) {
            this.messages = messages;
        }

        public static class MessagesBean {
            /**
             * text : text
             * ID : 3
             * imageURL : https://images-na.ssl-images-amazon.com/images/I/51ueQMCe40L._SX425_.jpg
             * date : 1993-02-01T00:00:00
             * fromUserID : 70
             */

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
}

package com.bcmobileappdevelopment.chatoon.Activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bcmobileappdevelopment.chatoon.GsonResponse.BasicResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.GetLastSeenDateResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.GetUserDetailsResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.LoadMoreMessagesResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.LoginResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.SendMessageResponse;
import com.bcmobileappdevelopment.chatoon.HelperClass.DialogItem;
import com.bcmobileappdevelopment.chatoon.HelperClass.MasterChatObject;
import com.bcmobileappdevelopment.chatoon.HelperClass.ImagePager;
import com.bumptech.glide.Glide;
import com.bcmobileappdevelopment.chatoon.HelperClass.Author;
import com.bcmobileappdevelopment.chatoon.HelperClass.Message;
import com.bcmobileappdevelopment.chatoon.R;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.fxn.pix.Pix;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;
import com.yalantis.ucrop.UCrop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import es.dmoral.toasty.Toasty;
import me.relex.circleindicator.CircleIndicator;

public class DialogActivity extends AppCompatActivity implements MessageInput.InputListener{//, BaseSliderView.OnSliderClickListener {

    MessagesListAdapter<Message> adapter;
    Author me;
    MessagesList messagesList;
    MessageInput input;
    String dialogID, fromUsername, senderAvatar, senderID,destinationPath,fileName, uploadedImageURL = "";
    int minID = -1;
    ArrayList<MasterChatObject> masterChatObjects;
    Gson gson;
    SendMessageResponse sendMessageResponse;
    LoginResponse loggedUser;
    Boolean dialogExists = false, needToRefresh = false;
    BroadcastReceiver newMessageReceivedReceiver;
    ArrayList<String> returnValue;
    UCrop.Options options;
    StorageReference mStorageRef;
    LoadMoreMessagesResponse loadMoreMessagesResponse;
    ImageView btBack, ivProfilePic, btProfile, ivCountry;
    RequestOptions requestOptions;
    TextView tvUsername, tvLastSeen, tvGenderAgeCountry, tvDialogUsername, btBackReportDialog, btSendReportDialog, btReportUser;
    Dialog photoSliderDialog, reportDialog;
    GetUserDetailsResponse getUserDetailsResponse;
    CircleIndicator circleIndicator;
    ViewPager viewPager;
    ImagePager imagePager;
    GetLastSeenDateResponse getLastSeenDateResponse;
    int interval = 60000;
    Handler lastSeenHandler;
    Locale enUsLocale = new Locale("en_US");
    SimpleDateFormat UTCDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    EditText etReasonReportDialog;
    BasicResponse reportResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme2);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Initialize();
        InitializeListeners();
        ResetUnreadCount();
        PopulateMessages();
        InitializeBackground();
        //GetLastSeenDate(false);
        //LastSeenDateHandler();
    }

    private void InitializeBackground() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        try {
            Date morning = dateFormat.parse("06:00");
            Date noon  = dateFormat.parse("12:00");
            Date night  = dateFormat.parse("20:00");
            Date currentTime = dateFormat.parse(dateFormat.format(new Date()));

            if (currentTime.after(night)){
                // night
                messagesList.setBackground(getResources().getDrawable(R.drawable.night));
                input.setBackgroundColor(getResources().getColor(R.color.black));
                input.getInputEditText().setTextColor(getResources().getColor(R.color.white));
            }
            else if(currentTime.after(noon)){
                //noon
                messagesList.setBackground(getResources().getDrawable(R.drawable.default_chat_background));
            }
            else if(currentTime.after(morning)){
                //morning
                messagesList.setBackground(getResources().getDrawable(R.drawable.morning));
            }
            else {
                //night
                messagesList.setBackground(getResources().getDrawable(R.drawable.night));
                input.setBackgroundColor(getResources().getColor(R.color.black));
                input.getInputEditText().setTextColor(getResources().getColor(R.color.white));
            }
        }
        catch (Exception e){

        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(newMessageReceivedReceiver);
        lastSeenHandler.removeCallbacksAndMessages(null);
        Log.d("tarja","onDestroy");
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        lastSeenHandler.removeCallbacksAndMessages(null);
        Log.d("tarja","onPause");
        super.onPause();
    }

    @Override
    protected void onResume() {
        LastSeenDateHandler();
        super.onResume();
    }

    private void LastSeenDateHandler() {
        lastSeenHandler = new Handler();
        lastSeenHandler.removeCallbacksAndMessages(null);
        lastSeenHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GetLastSeenDate(false);
                lastSeenHandler.postDelayed(this,interval);
            }
        },0);
    }

    private void setPhotoSliderDialog(){
        photoSliderDialog = new Dialog(this);
        photoSliderDialog.setContentView(R.layout.dialog_photo_slider);
        Window window = photoSliderDialog.getWindow();
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setGravity(Gravity.CENTER);
        circleIndicator = photoSliderDialog.findViewById(R.id.circle);
        viewPager = photoSliderDialog.findViewById(R.id.viewPager);
        tvDialogUsername = photoSliderDialog.findViewById(R.id.tvDialogUsername);
        btReportUser = photoSliderDialog.findViewById(R.id.btReportUser);
        btReportUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setReportDialog();
            }
        });
        imagePager = new ImagePager(getBaseContext(),(ArrayList<String>) getUserDetailsResponse.getUserImages());
        viewPager.setAdapter(imagePager);
        circleIndicator.setViewPager(viewPager);
        tvGenderAgeCountry = photoSliderDialog.findViewById(R.id.tvGenderAgeCountry);
        ivCountry = photoSliderDialog.findViewById(R.id.ivCountry);
        String gender;
        if (getUserDetailsResponse.getGender().equals("M"))
            gender = getResources().getString(R.string.male);
        else
            gender = getResources().getString(R.string.female);
        tvGenderAgeCountry.setText(gender+", "+getUserDetailsResponse.getAge()+"\n"+getUserDetailsResponse.getCountryName());
        tvDialogUsername.setText(fromUsername);
        Glide.with(DialogActivity.this).load(getResources().getIdentifier(getUserDetailsResponse.getFlagCode().toLowerCase(enUsLocale), "drawable", getPackageName())).into(ivCountry);
        photoSliderDialog.show();
    }

    private void setReportDialog(){
        reportDialog = new Dialog(this);
        reportDialog.setContentView(R.layout.dialog_report);
        reportDialog.setCancelable(true);
        Window window = reportDialog.getWindow();
        window.setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

        btSendReportDialog = reportDialog.findViewById(R.id.btSend);
        btBackReportDialog = reportDialog.findViewById(R.id.btBack);
        etReasonReportDialog = reportDialog.findViewById(R.id.etReason);
        btSendReportDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etReasonReportDialog.getText().length() > 200){
                    Toasty.warning(DialogActivity.this, getResources().getString(R.string.invalid_length,getResources().getString(R.string.report_reason)), Toast.LENGTH_LONG, true).show();
                }
                else if (!etReasonReportDialog.getText().toString().matches("[a-zA-Z0-9_.\\-]+") && !etReasonReportDialog.getText().toString().equals("")){
                    Toasty.warning(DialogActivity.this, getResources().getString(R.string.invalid_input,getResources().getString(R.string.report_reason)), Toast.LENGTH_LONG, true).show();
                }
                else {
                    Report(false);
                }
            }
        });
        btSendReportDialog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                btSendReportDialog.setBackgroundColor(getResources().getColor(R.color.transparent_green));
                if (event.getAction() == MotionEvent.ACTION_UP)
                    btSendReportDialog.setBackgroundColor(getResources().getColor(R.color.transparent_green_pressed));
                return false;
            }
        });

        btBackReportDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportDialog.dismiss();
            }
        });
        btBackReportDialog.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                btBackReportDialog.setBackgroundColor(getResources().getColor(R.color.transparent_red));
                if (event.getAction() == MotionEvent.ACTION_UP)
                    btBackReportDialog.setBackgroundColor(getResources().getColor(R.color.transparent_red_pressed));
                return false;
            }
        });


        reportDialog.show();
    }

    private void InitializeListeners() {
        input.setAttachmentsListener(new MessageInput.AttachmentsListener() {
            @Override
            public void onAddAttachments() {
                if (loggedUser.getUser().getAccountType().equals("Anonymous")){
                    Toasty.error(DialogActivity.this, getResources().getString(R.string.anonymous_cant_send_photo), Toast.LENGTH_LONG, true).show();
                }
                else if(!loggedUser.getUser().isIsEmailApproved()){
                    Toasty.error(DialogActivity.this, getResources().getString(R.string.verificate_mail_to_send_photo), Toast.LENGTH_LONG, true).show();
                }
                else
                    Pix.start(DialogActivity.this,1);
            }
        });
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        btProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetUserImages(false);
            }
        });
        ivProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btProfile.callOnClick();
            }
        });
        tvUsername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btProfile.callOnClick();
            }
        });
        tvLastSeen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btProfile.callOnClick();
            }
        });
    }

    private void Initialize() {
        Hawk.init(DialogActivity.this).build();
        loggedUser = Hawk.get("loggedUser");
        dialogID = getIntent().getStringExtra("DialogID");
        fromUsername = getIntent().getStringExtra("FromUsername");
        senderAvatar = getIntent().getStringExtra("ProfilePic");
        senderID = getIntent().getStringExtra("SenderID");
        UTCDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        me = new Author();
        me.setAvatar("");
        me.setId("0");
        me.setName("me");

        requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CircleCrop());

        btBack = findViewById(R.id.btBack);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        Glide.with(getApplication()).load(senderAvatar).apply(requestOptions).into(ivProfilePic);
        tvUsername = findViewById(R.id.tvUsername);
        tvLastSeen = findViewById(R.id.tvLastSeen);
        tvUsername.setText(fromUsername);
        btProfile = findViewById(R.id.btProfile);

        messagesList = findViewById(R.id.messagesList);
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Glide.with(getApplication()).load(url).into(imageView);
            }
        };

        adapter = new MessagesListAdapter<>("0", imageLoader);
        messagesList.setAdapter(adapter);
        adapter.setLoadMoreListener(new MessagesListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                LoadMoreMessages(false);
                Log.d("tarja",page+" "+ totalItemsCount);
            }
        });
        adapter.setOnMessageClickListener(new MessagesListAdapter.OnMessageClickListener<Message>() {
            @Override
            public void onMessageClick(Message message) {
                if (message.getImageUrl() != null && !message.getImageUrl().equals(""))
                {
                    Intent myIntent = new Intent(DialogActivity.this, ImageActivity.class);
                    myIntent.putExtra("imageURL", message.getImageUrl());
                    DialogActivity.this.startActivity(myIntent);
                }
            }
        });
        adapter.setDateHeadersFormatter(new DateFormatter.Formatter() {
            @Override
            public String format(Date date) {
                if (DateFormatter.isToday(date)) {
                    return getResources().getString(R.string.today);
                }
                else if (DateFormatter.isYesterday(date)) {
                    return getResources().getString(R.string.yesterday);
                }
                else if (DateFormatter.isCurrentYear(date)) {
                    return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH);
                }
                else {
                    return DateFormatter.format(date, DateFormatter.Template.STRING_DAY_MONTH_YEAR);
                }
            }
        });
        input = findViewById(R.id.input);
        input.setInputListener(this);

        newMessageReceivedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                NewMessageReceived(intent);
            }

        };
        registerReceiver(newMessageReceivedReceiver,new IntentFilter("new_message_received"));

        options = new UCrop.Options();
        options.setCompressionQuality(50);
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        options.setToolbarTitle(getResources().getString(R.string.edit_photo));
        options.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        options.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        options.setActiveWidgetColor(getResources().getColor(R.color.chatoon_orange_medium));
        options.setFreeStyleCropEnabled(true);
    }

    private void NewMessageReceived(Intent intent) {
        if (senderID.equals(intent.getStringExtra("senderUserID"))){
            com.bcmobileappdevelopment.chatoon.HelperClass.Message msg = new com.bcmobileappdevelopment.chatoon.HelperClass.Message();
            msg.setAuthor(new Author());
            msg.setId(intent.getStringExtra("messageID"));
            msg.getUser().setAvatar(intent.getStringExtra("senderUserProfilePic"));
            msg.getUser().setId( intent.getStringExtra("senderUserID"));

            if (intent.getStringExtra("imageURL") != null){
                msg.setImageURL(intent.getStringExtra("imageURL"));
            }
            else {
                byte[] data = Base64.decode(intent.getStringExtra("content"), Base64.DEFAULT);
                try {
                    String emojiMessage = new String(data, "UTF-8");
                    msg.setText(emojiMessage);
                }catch (Exception e){

                }
            }
            msg.getUser().setName(intent.getStringExtra("senderUserName"));
            msg.setDate(new Date());
            adapter.addToStart(msg,true);
            tvLastSeen.setText(getResources().getString(R.string.online));
        }
    }

    private void PopulateMessages() {
        masterChatObjects = Hawk.get("master_chat_objects");
        for (MasterChatObject item:masterChatObjects){
            if (item.getDialogItem().getFromUserID().equals(senderID)){
                //senderAvatar = item.getDialogItem().getDialogPhoto();
                dialogID = item.getDialogItem().getId();
                for (com.bcmobileappdevelopment.chatoon.HelperClass.Message message:item.getMessages()){
                    com.bcmobileappdevelopment.chatoon.HelperClass.Message messageToAdd = new com.bcmobileappdevelopment.chatoon.HelperClass.Message();
                    messageToAdd.setId(message.getId());
                    messageToAdd.setAuthor(message.getUser());
                    messageToAdd.setDate(message.getCreatedAt());
                    if (message.getImageUrl() != null && !message.getImageUrl().equals(""))
                        messageToAdd.setImageURL(message.getImageUrl());
                    else
                        messageToAdd.setText(message.getText());
                    //Log.d("tarja","asdasd"+message.getImageUrl());
                    adapter.addToStart(messageToAdd,true);
                    if (Integer.parseInt(messageToAdd.getId()) != 0 && (minID == -1 || minID > Integer.parseInt(messageToAdd.getId()))){
                        minID = Integer.parseInt(messageToAdd.getId());
                    }
                }
            }
        }
    }

    @Override
    public boolean onSubmit(CharSequence input) {
        if (!needToRefresh)
            needToRefresh = true;
        try {
            String contentWithEmoji,content;
            //Log.d("tarja","girilen input -> "+ input.toString());
            //content = input.toString().replaceAll("\\s","").replaceAll("\\n","");
            content = input.toString().replaceAll(" +", " ").replaceAll("\\n","");
            //Log.d("tarja","content -> "+ content);

            if (!content.equals("") && content.length() <= 250){
                byte[] data = input.toString().replaceAll("\\s\\s","").replaceAll("\\n","").getBytes("UTF-8");
                contentWithEmoji = android.util.Base64.encodeToString(data, android.util.Base64.DEFAULT);

                Message newMessage = new Message();
                newMessage.setText(content);
                newMessage.setDate(new Date());
                newMessage.setId("0");
                newMessage.setAuthor(me);
                adapter.addToStart(newMessage,true);
                SendMessage(content,contentWithEmoji,false);
            }else
                return false; //TODO tost göster
        }
        catch (Exception e){
            return false; // TODO tost göster
        }
        return true;
    }

    private void SendMessage(final String content, final String contentWithEmoji, final boolean useBackup) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(DialogActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL+getResources().getString(R.string.ws_SendMessage), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<SendMessageResponse>() {
                }.getType();
                sendMessageResponse = gson.fromJson(response, myType);
                Log.d("tarja","SendMessage");
                if (sendMessageResponse.isIsSuccess()){
                    SubmitMessage(sendMessageResponse.getMessageID(),sendMessageResponse.getChatID(),content);

                    if (minID == -1)
                        minID = Integer.parseInt(sendMessageResponse.getMessageID());
                    Log.d("tarja","minID -> "+minID);
                }
                else if(sendMessageResponse.getMessage().equals("user_banned")){
                    Toasty.warning(DialogActivity.this, getResources().getString(R.string.user_banned), Toast.LENGTH_LONG, true).show();
                    Logout();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    SendMessage(content,contentWithEmoji,true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("token", loggedUser.getToken());
                params.put("fromUserID", String.valueOf(loggedUser.getUser().getID()));
                params.put("toUserID", senderID);
                params.put("messageContent", contentWithEmoji);
                params.put("messageContentMeaning", content);
                params.put("imageURL", uploadedImageURL);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(10000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void SubmitMessage(String messageID, String chatID, String content) {
        Log.d("tarja","SubmitMessage");
        dialogExists = false;
        masterChatObjects = Hawk.get("master_chat_objects");
        for (MasterChatObject item:masterChatObjects){
            if (item.getDialogItem().getFromUserID().equals(senderID)){
                Message newMessage = new Message();
                if (uploadedImageURL != "")
                    newMessage.setText(getResources().getString(R.string.image));
                else {
                    newMessage.setText(content);
                }
                Log.d("tarja","content->" +content);
                newMessage.setDate(new Date());
                newMessage.setId(messageID);
                newMessage.setAuthor(me);
                newMessage.setImageURL(uploadedImageURL);
                uploadedImageURL = "";

                item.getMessages().add(newMessage);
                item.getDialogItem().setLastMessage(newMessage);
                dialogExists = true;
            }
        }
        if (!dialogExists){
            Log.d("tarja","2 girdim");
            MasterChatObject masterChatObjectAdd = new MasterChatObject();
            DialogItem dialogItemAdd = new DialogItem();
            dialogItemAdd.setUnreadCount(0);
            dialogItemAdd.setPhoto(senderAvatar);
            dialogItemAdd.setID(chatID);
            dialogItemAdd.setFromUsername(fromUsername);
            dialogItemAdd.setFromUserID(senderID);

            List<Author> authorListAdd = new ArrayList<>();
            Author authorAdd = new Author();
            authorAdd.setName(fromUsername);
            authorAdd.setAvatar(senderAvatar);
            authorAdd.setId(senderID);
            authorListAdd.add(authorAdd);
            dialogItemAdd.setUserList(authorListAdd);

            Message messageToAdd = new Message();
            ArrayList<Message> messageListAdd = new ArrayList<>();
            messageToAdd.setImageURL(uploadedImageURL);
            if (uploadedImageURL != "")
                messageToAdd.setText(getResources().getString(R.string.image));
            else
                messageToAdd.setText(content);
            uploadedImageURL = "";

            messageToAdd.setAuthor(me);
            messageToAdd.setId(messageID);
            try{
                messageToAdd.setDate(new Date());
            }catch (Exception e){
                messageToAdd.setDate(new Date());
            }
            messageListAdd.add(messageToAdd);

            dialogItemAdd.setLastMessage(messageToAdd);
            masterChatObjectAdd.setDialogItem(dialogItemAdd);
            masterChatObjectAdd.setMessages(messageListAdd);
            masterChatObjects.add(masterChatObjectAdd);
        }
        Hawk.put("master_chat_objects",masterChatObjects);
        //SendMessage(newMessage.getText());
    }

    private void ResetUnreadCount() {
        //hawk ile lastIncomingMessageID güncellenmeli veya o tarz bişyler
        masterChatObjects = Hawk.get("master_chat_objects");
        for (MasterChatObject item:masterChatObjects){
            if (item.getDialogItem().getId().equals(dialogID)){
                item.getDialogItem().setUnreadCount(0);
            }
        }
        Hawk.put("master_chat_objects",masterChatObjects);
    }

    @Override
    public void onBackPressed() {
        //if (touchImage.getVisibility() == View.VISIBLE){
        //    touchImage.setVisibility(View.GONE);
        //    return;
        //}TODO
        ResetUnreadCount();
        Intent intent = new Intent("refresh");
        sendBroadcast(intent);
        this.finish();
        super.onBackPressed();
    }

    private void CheckPermissions(){
        //TODO
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK ){
            returnValue = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            Uri source = Uri.fromFile(new File(returnValue.get(0)));
            destinationPath = DialogActivity.this.getFilesDir().toString()+CreateFileName();
            Uri destination = Uri.fromFile(new File(destinationPath));
            //Log.d("Tarja","dest= "+destinationPath);
            UCrop.of(source, destination)
                    .withMaxResultSize(1200,1200)
                    .withOptions(options)
                    .start(DialogActivity.this,2);

        }
        else if(requestCode == 2 && resultCode == RESULT_OK){
            UploadImage(destinationPath,fileName);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private String CreateFileName(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss:SSS");
        fileName = "/u"+String.valueOf(loggedUser.getUser().getID())+"_"+df.format(calendar.getTime())+".jpg";
        return  fileName;
    }

    private void UploadImage(String imagePath, String imageName){
        mStorageRef = FirebaseStorage.getInstance().getReference();
        Uri file = Uri.fromFile(new File(imagePath));
        final StorageReference riversRef = mStorageRef.child("images/"+loggedUser.getUser().getFlagCode()+imageName);
        riversRef.putFile(file).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
            }
        })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUrl = uri;
                                uploadedImageURL = downloadUrl.toString();
                                SendMessage("","",false);
                                Message newMessage = new Message();
                                newMessage.setText("");
                                newMessage.setDate(new Date());
                                newMessage.setImageURL(uploadedImageURL);
                                newMessage.setId("0");
                                newMessage.setAuthor(me);
                                adapter.addToStart(newMessage,true);
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception exception) {
                        //progressDialog.dismiss(); TODO
                        Log.d("Tarja","UPLOAD FAILURE");
                    }
                });
    }

    private void LoadMoreMessages(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(DialogActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL+getResources().getString(R.string.ws_LoadMoreMessages), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<LoadMoreMessagesResponse>() {
                }.getType();
                loadMoreMessagesResponse = gson.fromJson(response, myType);
                if (loadMoreMessagesResponse.isIsSuccess()) {
                    String comment;
                    byte[] data;

                    List<Message> messages = new ArrayList<>();
                    for (LoadMoreMessagesResponse.MessagesBean messageItem: loadMoreMessagesResponse.getMessages()){
                        Message messageToAdd = new Message();
                        Author messageAuthor = new Author();
                        messageAuthor.setId(String.valueOf(messageItem.getFromUserID()));
                        messageAuthor.setAvatar(senderAvatar);
                        messageAuthor.setName("");
                        messageToAdd.setAuthor(messageAuthor);
                        try{
                            messageToAdd.setDate(UTCDateFormat.parse(messageItem.getDate()));
                        }catch (Exception e){
                            messageToAdd.setDate(new Date());
                        }
                        messageToAdd.setId(messageItem.getID());
                        messageToAdd.setImageURL(messageItem.getImageURL());
                        //messageToAdd.setText(messageItem.getText());
                        if (messageItem.getImageURL() == null)
                        {
                            comment = messageItem.getText();
                            data = Base64.decode(comment, Base64.DEFAULT);
                            try {
                                String emojiMessage = new String(data, "UTF-8");
                                messageToAdd.setText(emojiMessage);
                            }catch (Exception e){
                            }
                        }
                        messages.add(messageToAdd);
                        if (Integer.parseInt(messageToAdd.getId()) != 0 && (minID == -1 || minID > Integer.parseInt(messageToAdd.getId()))){
                            minID = Integer.parseInt(messageToAdd.getId());
                        }
                    }
                    adapter.addToEnd(messages,false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    LoadMoreMessages(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                params.put("token", loggedUser.getToken());
                params.put("chatID", dialogID);
                params.put("minID", String.valueOf(minID));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void GetUserImages(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(DialogActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_GetUserDetails);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL+webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<GetUserDetailsResponse>() {
                }.getType();
                getUserDetailsResponse = gson.fromJson(response, myType);
                if (getUserDetailsResponse.isIsSuccess()) {
                    setPhotoSliderDialog();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    GetUserImages(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                params.put("token", loggedUser.getToken());
                params.put("detailUserID", senderID);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void Report(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(DialogActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_Report);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL+webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<BasicResponse>() {
                }.getType();
                reportResponse = gson.fromJson(response, myType);
                if (reportResponse.isIsSuccess()) {
                    reportDialog.dismiss();
                    Toasty.success(DialogActivity.this, getResources().getString(R.string.user_reported), Toast.LENGTH_LONG, true).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    Report(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("token", loggedUser.getToken());
                params.put("reporterUserID", String.valueOf(loggedUser.getUser().getID()));
                params.put("reportedUserID", senderID);
                params.put("reason", etReasonReportDialog.getText().toString());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void GetLastSeenDate(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL+getResources().getString(R.string.ws_GetLastSeenDate), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<GetLastSeenDateResponse>() {
                }.getType();
                getLastSeenDateResponse = gson.fromJson(response, myType);
                if (getLastSeenDateResponse.isIsSuccess()){
                    Log.d("tarja","last seen "+getLastSeenDateResponse.getLastSeenDate());
                    ShowLastSeenDate();
                    //Handler handler = new Handler();
                    //handler.postDelayed(new Runnable() {
                    //    public void run() {
                    //
                    //    }
                    //}, 0);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    GetLastSeenDate(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("token", loggedUser.getToken());
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                params.put("lastSeenUserID", senderID);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void ShowLastSeenDate(){
        try {
            //Locale current = getResources().getConfiguration().locale;
            //DateFormat dateFormat = SimpleDateFormat.getDateInstance(DateFormat.DEFAULT, current);
            //dateFormat.setTimeZone(TimeZone.getDefault());
            Date myDate = UTCDateFormat.parse(getLastSeenDateResponse.getLastSeenDate());
            //String date = dateFormat.format(myDate); // 8 Eki 2018, Oct 8, 2018
            String time = new SimpleDateFormat(" HH:mm").format(myDate); //08:18

            Calendar cal = Calendar.getInstance();
            //Date nowDate=cal.getTime();
            //DateFormat dateFormat2 = new SimpleDateFormat("HH:mm");
            //String nowTime=dateFormat2.format(nowDate);
            //Log.d("tarja","şuanki saat: "+nowTime);

            cal.add(Calendar.MINUTE,-1);
            Date beforeDate=cal.getTime();
            //String beforeTime=new SimpleDateFormat("HH:mm").format(beforeDate);
            //Log.d("tarja","before saat: "+beforeTime);

            boolean isOnline = false;

            String lastSeenString = getResources().getString(R.string.last_seen);
            if (DateFormatter.isToday(myDate)) {
                if (myDate.after(beforeDate)){
                    isOnline = true;
                    lastSeenString = getResources().getString(R.string.online);
                }
                else {
                    lastSeenString += getResources().getString(R.string.today_last_seen);
                }
            }
            else if (DateFormatter.isYesterday(myDate)) {
                lastSeenString += getResources().getString(R.string.yesterday_last_seen);
            }
            else {
                String longDate = new SimpleDateFormat(" dd.MM.yyyy").format(myDate);
                isOnline = true; //yalancıktan :D
                lastSeenString += longDate;
            }
            if (!isOnline)
                lastSeenString += time;
            tvLastSeen.setText(lastSeenString);
            if (tvLastSeen.getVisibility() != View.VISIBLE)
                tvLastSeen.setVisibility(View.VISIBLE);
        }
        catch (Exception e){

        }
    }

    public void Logout() {
        FirebaseAuth.getInstance().signOut();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                } catch (IOException e) {
                    Log.d("tarja",e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
        Hawk.deleteAll();
        Log.d("tarja","silinen token->"+FirebaseInstanceId.getInstance().getToken());

        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        Intent myIntent = new Intent(DialogActivity.this, SplashActivity.class);
        DialogActivity.this.startActivity(myIntent);
        DialogActivity.this.finish();
    }
}
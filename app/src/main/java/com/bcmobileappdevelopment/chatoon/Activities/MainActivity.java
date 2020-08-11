package com.bcmobileappdevelopment.chatoon.Activities;

import android.app.Dialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.MenuItem;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bcmobileappdevelopment.chatoon.Adapters.PremiumAdapter;
import com.bcmobileappdevelopment.chatoon.Fragments.SearchCredentialsFragment;
import com.bcmobileappdevelopment.chatoon.GsonResponse.BasicResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.CheckNewMessagesResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.FindNewChatResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.GetMessagesFirstTimeResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.GetPremiumInfoResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.GetPremiumListResponse;
import com.bcmobileappdevelopment.chatoon.HelperClass.MasterChatObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.florent37.tutoshowcase.TutoShowcase;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;
import com.bcmobileappdevelopment.chatoon.GsonResponse.LoginResponse;
import com.bcmobileappdevelopment.chatoon.HelperClass.Author;
import com.bcmobileappdevelopment.chatoon.HelperClass.DialogItem;
import com.bcmobileappdevelopment.chatoon.HelperClass.Message;
import com.bcmobileappdevelopment.chatoon.R;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;
import com.stfalcon.chatkit.utils.DateFormatter;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import es.dmoral.toasty.Toasty;
import ru.whalemare.sheetmenu.SheetMenu;
import spencerstudios.com.ezdialoglib.EZDialog;
import spencerstudios.com.ezdialoglib.EZDialogListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    DialogsList dialogsList;
    DialogsListAdapter dialogsListAdapter;
    AdView bannerAd;
    ImageView ivProfile, ivLoading;
    TextView tvUsername, tvGenderValue, tvAgeRangeValue;
    LoginResponse loggedUser;
    View navHeader;
    RequestOptions requestOptions;
    Gson gson;
    BasicResponse insertFirebaseIDResponse, deleteChatResponse, addPremiumResponse;
    BroadcastReceiver newTokenReceiver, newMessageReceivedReceiver, refreshReceiver, updateProfilePicReceiver, searchCredentialsUpdatedReceiver, personalInfoUpdatedReceiver;
    int lastIncomingMessageID = 0;
    GetMessagesFirstTimeResponse getMessagesFirstTimeResponse;
    CheckNewMessagesResponse checkNewMessagesResponse;
    FindNewChatResponse findNewChatResponse;
    FloatingActionButton btFindNewChat, btPremium;
    SheetMenu chatDialogMenu;
    SimpleDateFormat UTCDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    InterstitialAd interstitialAd;
    RecyclerView rvPremium;
    GetPremiumListResponse getPremiumListResponse;
    BaseQuickAdapter rvPremiumAdapter;
    RewardedVideoAd premiumVideoAd;
    GetPremiumInfoResponse getPremiumInfoResponse = new GetPremiumInfoResponse();
    Dialog loadingDialog;
    ConstraintLayout mainContainer;
    int showcaseNumber = 0;
    boolean needShowcase = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d("tarja","onCreate");
        Initialize();
        InitializeAds();
        InitializeListeners();
        ChatInitialize();
        GetPremiumList(false);
        GetPremiumInfo(false);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent myIntent = new Intent(MainActivity.this, ProfileActivity.class);
            MainActivity.this.startActivity(myIntent);
            //Hawk.delete("master_chat_objects");
            //Hawk.delete("last_incoming_message_ID");
        } else if (id == R.id.nav_edit_profile) {
            SearchCredentialsFragment fragment = new SearchCredentialsFragment(loggedUser);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container,fragment,"main_container")
                    .addToBackStack(null)
                    .commit();
        } else if (id == R.id.nav_settings) {
            Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(myIntent);
        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void Showcase(int viewID, int layoutID){
        TutoShowcase.from(MainActivity.this)
                .setListener(new TutoShowcase.Listener() {
                    @Override
                    public void onDismissed() {
                        Log.d("tarja","dismissed");
                        switch (showcaseNumber)
                        {
                            case 1:
                                Showcase(R.id.btPremium, R.layout.showcase_premium);
                                break;
                        }
                    }
                })
                .setContentView(layoutID)
                .on(viewID) //a view in actionbar
                .addCircle()
                .withBorder()
                .show();
        showcaseNumber++;
    }

    private void InitializeAds(){
        btPremium.hide();
        MobileAds.initialize(this, getResources().getString(R.string.ad_app_unit_id));

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.ad_interstitial_unit_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                FindNewChat(false);
                Log.d("tarja","onAdClosed");
                super.onAdClosed();
            }
        });

        bannerAd = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        bannerAd.loadAd(adRequest);

        premiumVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        premiumVideoAd.loadAd(getResources().getString(R.string.ad_premium_unit_id),
                new AdRequest.Builder().build());
        premiumVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                //Log.d("tarja","onRewardedVideoAdLoaded");
                btPremium.show();
                if (needShowcase)
                    Showcase(R.id.btFindNewChat, R.layout.showcase_find_new_chat);
            }

            @Override
            public void onRewardedVideoAdOpened() {
                Log.d("tarja","onRewardedVideoAdOpened");
            }

            @Override
            public void onRewardedVideoStarted() {
                //Log.d("tarja","onRewardedVideoStarted");
            }

            @Override
            public void onRewardedVideoAdClosed() {
                premiumVideoAd.loadAd(getResources().getString(R.string.ad_premium_unit_id),
                        new AdRequest.Builder().build());
                //Log.d("tarja","onRewardedVideoAdClosed");
            }

            @Override
            public void onRewarded(RewardItem rewardItem) {
                AddPremium(false);
               //Log.d("tarja","onRewarded");
            }

            @Override
            public void onRewardedVideoAdLeftApplication() {
                //Log.d("tarja","onRewardedVideoAdLeftApplication");
            }

            @Override
            public void onRewardedVideoAdFailedToLoad(int i) {
                premiumVideoAd.loadAd(getResources().getString(R.string.ad_premium_unit_id),
                        new AdRequest.Builder().build());
                //Log.d("tarja","onRewardedVideoAdFailedToLoad");
            }

            @Override
            public void onRewardedVideoCompleted() {
                //Log.d("tarja","onRewardedVideoCompleted");
            }
        });
    }


    @Override
    protected void onResume() {
        NotificationManager nManager = ((NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE));
        nManager.cancelAll();
        super.onResume();
    }

    private void Initialize() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        btFindNewChat = findViewById(R.id.btFindNewChat);
        btPremium = findViewById(R.id.btPremium);
        UTCDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        chatDialogMenu = new SheetMenu();
        chatDialogMenu.setMenu(R.menu.chat_dialog);
        chatDialogMenu.setAutoCancel(true);

        mainContainer = findViewById(R.id.main_container);

        requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new RoundedCorners(50));

        dialogsList = findViewById(R.id.dialogsList);

        navHeader = navigationView.getHeaderView(0);
        ivProfile = navHeader.findViewById(R.id.ivProfile);
        tvUsername = navHeader.findViewById(R.id.tvUsername);
        tvGenderValue = navHeader.findViewById(R.id.tvGenderValue);
        tvAgeRangeValue = navHeader.findViewById(R.id.tvAgeRangeValue);
        PopulateUser();
        RegisterFirebaseInstance();

        if (Hawk.contains("showcase")){
            needShowcase = Hawk.get("showcase");
            Hawk.delete("showcase");
        }

        newTokenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String newToken = intent.getStringExtra("new_token");
                if (Hawk.contains("firebase_instance_id"))
                    InsertFirebaseID(newToken, false);
            }
        };
        registerReceiver(newTokenReceiver, new IntentFilter("new_token"));

        newMessageReceivedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Integer.valueOf(intent.getStringExtra("messageID")) > lastIncomingMessageID){
                    lastIncomingMessageID = Integer.valueOf(intent.getStringExtra("messageID"));
                    Hawk.put("last_incoming_message_ID", lastIncomingMessageID);
                    NewMessageReceived(intent);
                }
            }
        };
        registerReceiver(newMessageReceivedReceiver, new IntentFilter("new_message_received"));

        refreshReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                RefreshList();
            }
        };
        registerReceiver(refreshReceiver, new IntentFilter("refresh"));

        updateProfilePicReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                UpdateProfilePic();
            }
        };
        registerReceiver(updateProfilePicReceiver,new IntentFilter("update_profile_pic"));

        searchCredentialsUpdatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                UpdateSearchCredentials();
            }
        };
        registerReceiver(searchCredentialsUpdatedReceiver,new IntentFilter("search_credentials_updated"));

        personalInfoUpdatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                UpdatePersonalInfo();
            }
        };
        registerReceiver(personalInfoUpdatedReceiver,new IntentFilter("personal_info_updated"));

        dialogsListAdapter = new DialogsListAdapter(new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Glide.with(getApplication()).load(url).into(imageView);
            }
        });

        dialogsListAdapter.setDatesFormatter(new DateFormatter.Formatter() {
            @Override
            public String format(Date date) {
                if (DateFormatter.isToday(date)) {
                    return DateFormatter.format(date, DateFormatter.Template.TIME);
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

        dialogsList.setAdapter(dialogsListAdapter);

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ivLoading = loadingDialog.findViewById(R.id.ivLoading);
        Glide.with(MainActivity.this).load(R.drawable.loading).into(ivLoading);
    }

    private void InitializePremiumRecyclerView() {
        rvPremium = findViewById(R.id.rvPremium);
        rvPremium.setLayoutManager(new LinearLayoutManager(this,RecyclerView.HORIZONTAL,false));
        rvPremiumAdapter = new PremiumAdapter(R.layout.list_item_premium, getPremiumListResponse.getPremiumList());
        rvPremiumAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        rvPremiumAdapter.setEnableLoadMore(true);
        rvPremiumAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                GetPremiumListResponse.PremiumListBean clickedUser = getPremiumListResponse.getPremiumList().get(position);
                if (clickedUser.getUserID() == loggedUser.getUser().getID()){
                    String title;
                    if (loggedUser.getUser().getGender().equals("M"))
                        title = getResources().getString(R.string.whos_that_handsome);
                    else
                        title = getResources().getString(R.string.whos_that_chick);
                    new EZDialog.Builder(MainActivity.this)
                            .setTitle(title)
                            .setMessage(getResources().getString(R.string.oh_its_me_for_sure))
                            .setNeutralBtnText(getResources().getString(R.string.ok))
                            .OnNeutralClicked(new EZDialogListener() {
                                @Override
                                public void OnClick() {

                                }
                            })
                            .setCancelableOnTouchOutside(false)
                            .build();
                }
                else
                {
                    OpenDialog("0",clickedUser.getUsername(),clickedUser.getProfilePicURL(),String.valueOf(clickedUser.getUserID()));
                }


                //Log.d("tarja",clickedUser.getUsername()+clickedUser.getProfilePicURL()+" "+String.valueOf(clickedUser.getUserID()));
            }
        });
        rvPremium.setAdapter(rvPremiumAdapter);
    }

    private void InitializeListeners() {
        dialogsListAdapter.setOnDialogClickListener(new DialogsListAdapter.OnDialogClickListener<DialogItem>() {
            @Override
            public void onDialogClick(DialogItem dialog) {
                OpenDialog(dialog.getId(),dialog.getFromUsername(),dialog.getDialogPhoto(),dialog.getFromUserID());
            }
        });
        dialogsListAdapter.setOnDialogLongClickListener(new DialogsListAdapter.OnDialogLongClickListener<DialogItem>() {
            @Override
            public void onDialogLongClick(final DialogItem dialog) {
                SheetMenu.with(MainActivity.this)
                        .setTitle("")
                        .setMenu(R.menu.chat_dialog)
                        .setAutoCancel(true)
                        .setClick(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DeleteChat(false,dialog.getFromUserID());
                                return false;
                            }
                        }).show();
            }
        });
        btFindNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!interstitialAd.isLoaded())
                    interstitialAd.loadAd(new AdRequest.Builder().build());

                int prefFindNewChatAd = Hawk.get("prefFindNewChatAd");
                if (prefFindNewChatAd > 0){
                    int findNewChatAdCount = Hawk.get("findNewChatAdCount",0);
                    if ((findNewChatAdCount % 5) == 0 && interstitialAd.isLoaded() && findNewChatAdCount > 0){
                        interstitialAd.show();
                    }
                    else
                        FindNewChat(false);
                    Hawk.put("findNewChatAdCount",++findNewChatAdCount);
                }
                else{
                    loadingDialog.show();
                    FindNewChat(false);
                }
            }
        });
        btPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPremiumInfoResponse.getMessage().equals("premium")){
                    String timeLeft = "";
                    try{
                        Date now = new Date();
                        Date premiumDate = UTCDateFormat.parse(getPremiumInfoResponse.getPremiumInfo().getDate());
                        //Log.d("tarja",premiumDate+" premium");
                        Calendar c = Calendar.getInstance();
                        c.setTime(premiumDate);
                        c.add(Calendar.HOUR_OF_DAY, Integer.parseInt(Hawk.get("prefPremiumHour").toString()));
                        premiumDate = c.getTime();
                        long different = -now.getTime() + premiumDate.getTime();

                        long minutesInMilli = 1000 * 60;
                        long hoursInMilli = minutesInMilli * 60;

                        long elapsedHours = different / hoursInMilli;
                        different = different % hoursInMilli;

                        long elapsedMinutes = different / minutesInMilli;
                        if (elapsedMinutes < 10)
                            timeLeft = elapsedHours+":0"+elapsedMinutes;
                        else
                            timeLeft = elapsedHours+":"+elapsedMinutes;
                    }catch (Exception e){

                    }

                    new EZDialog.Builder(MainActivity.this)
                            .setTitle(getResources().getString(R.string.highlights))
                            .setMessage(getResources().getString(R.string.already_in_highlights)+timeLeft)
                            .setNeutralBtnText(getResources().getString(R.string.ok))
                            .OnNeutralClicked(new EZDialogListener() {
                                @Override
                                public void OnClick() {

                                }
                            })
                            .setCancelableOnTouchOutside(false)
                            .build();
                }
                else {
                    if (!premiumVideoAd.isLoaded())
                        premiumVideoAd.loadAd(getResources().getString(R.string.ad_premium_unit_id),new AdRequest.Builder().build());
                    new EZDialog.Builder(MainActivity.this)
                            .setTitle(getResources().getString(R.string.highlights))
                            .setMessage(getResources().getString(R.string.highlights_message, Hawk.get("prefPremiumHour")))
                            .setPositiveBtnText(getResources().getString(R.string.ok))
                            .setNegativeBtnText(getResources().getString(R.string.cancel))
                            .setCancelableOnTouchOutside(false)
                            .OnPositiveClicked(new EZDialogListener() {
                                @Override
                                public void OnClick() {
                                    premiumVideoAd.show();
                                }
                            })
                            .OnNegativeClicked(new EZDialogListener() {
                                @Override
                                public void OnClick() {
                                    //todo
                                }
                            })
                            .build();
                }
            }
        });
    }

    private void RegisterFirebaseInstance() {
        if (Hawk.contains("firebase_instance_id")) {
            //Log.d("tarja","token->"+FirebaseInstanceId.getInstance().getToken());
            if (!FirebaseInstanceId.getInstance().getToken().equals(Hawk.get("firebase_instance_id"))) {
                InsertFirebaseID(FirebaseInstanceId.getInstance().getToken(), false);
                Log.d("tarja", "Inserting new FirebaseInstanceID");
            }
        } else {
            InsertFirebaseID(FirebaseInstanceId.getInstance().getToken(), false);
            Log.d("tarja", "Inserting new FirebaseInstanceID");
        }
    }

    private void InsertFirebaseID(final String token, final boolean useBackup) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_InsertFirebaseInstanceID);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<BasicResponse>() {
                }.getType();
                insertFirebaseIDResponse = gson.fromJson(response, myType);
                if (insertFirebaseIDResponse.isIsSuccess()) {
                    Log.d("tarja", "Inserting new FirebaseInstanceID SUCCESS");
                    Hawk.put("firebase_instance_id", FirebaseInstanceId.getInstance().getToken());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    InsertFirebaseID(token, true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                params.put("token", loggedUser.getToken());
                params.put("firebaseInstanceID", token);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 10, 1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void GetPremiumInfo(final boolean useBackup) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_GetPremiumInfo);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<GetPremiumInfoResponse>() {
                }.getType();
                getPremiumInfoResponse = gson.fromJson(response, myType);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    GetPremiumInfo(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("token", loggedUser.getToken());
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 10, 1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void GetPremiumList(final boolean useBackup) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_GetPremiumList);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<GetPremiumListResponse>() {
                }.getType();
                getPremiumListResponse = gson.fromJson(response, myType);
                if (getPremiumListResponse.isIsSuccess()) {
                    InitializePremiumRecyclerView();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    GetPremiumList(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("token", loggedUser.getToken());
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 10, 1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void PopulateUser() {
        loggedUser = Hawk.get("loggedUser");
        if (loggedUser != null) {
            Glide.with(getApplicationContext()).load(loggedUser.getUser().getProfilePicURL()).apply(requestOptions).into(ivProfile);
            tvUsername.setText(loggedUser.getUser().getUsername());
            tvAgeRangeValue.setText(loggedUser.getUser().getLookingAgeRange());
            switch (loggedUser.getUser().getLookingGender()) {
                case "F":
                    tvGenderValue.setText(getResources().getString(R.string.female));
                    break;
                case "M":
                    tvGenderValue.setText(getResources().getString(R.string.male));
                    break;
                case "MF":
                    tvGenderValue.setText(getResources().getString(R.string.male_or_female));
                    break;
            }
        } else {
            //TODO logine at gitsin
        }
    }
    
    private void UpdateSearchCredentials(){
        loggedUser = Hawk.get("loggedUser");
        tvAgeRangeValue.setText(loggedUser.getUser().getLookingAgeRange());
        switch (loggedUser.getUser().getLookingGender()) {
            case "F":
                tvGenderValue.setText(getResources().getString(R.string.female));
                break;
            case "M":
                tvGenderValue.setText(getResources().getString(R.string.male));
                break;
            case "MF":
                tvGenderValue.setText(getResources().getString(R.string.male_or_female));
                break;
        }
    }

    private void UpdatePersonalInfo(){
        loggedUser = Hawk.get("loggedUser");
        tvUsername.setText(loggedUser.getUser().getUsername());
    }

    private void UpdateProfilePic(){
        loggedUser = Hawk.get("loggedUser");
        Glide.with(getApplicationContext()).load(loggedUser.getUser().getProfilePicURL()).apply(requestOptions).into(ivProfile);
    }

    @Override
    protected void onDestroy() {
        UnregisterReceivers();
        super.onDestroy();
    }

    private void UnregisterReceivers(){
        unregisterReceiver(newTokenReceiver);
        unregisterReceiver(newMessageReceivedReceiver);
        unregisterReceiver(refreshReceiver);
        unregisterReceiver(updateProfilePicReceiver);
        unregisterReceiver(searchCredentialsUpdatedReceiver);
        unregisterReceiver(personalInfoUpdatedReceiver);
    }

    //private void LetsCreateDialog() {
    //    DialogItem dialogItem = new DialogItem();
    //    dialogItem.setID("1");
    //    dialogItem.setFromUsername("Elon");
    //    dialogItem.setPhoto("https://static.wixstatic.com/media/9715b6_0578c9021b6f44f3a08b19631e5d3851~mv2.jpeg/v1/fit/w_498%2Ch_373%2Cq_90/file.jpg");
    //    dialogItem.setUnreadCount(1);
    //    List<Author> userList = new ArrayList<>();
    //    Author author = new Author();
    //    author.setId("1");
    //    author.setName("Elon");
    //    author.setAvatar("https://static.wixstatic.com/media/9715b6_0578c9021b6f44f3a08b19631e5d3851~mv2.jpeg/v1/fit/w_498%2Ch_373%2Cq_90/file.jpg");
    //    userList.add(author);
    //    dialogItem.setUserList(userList);
    //    Message message = new Message();
    //    message.setId("5");
    //    message.setAuthor(author);
    //    message.setDate(new Date());
    //    message.setText("Selam");
    //    dialogItem.setLastMessage(message);
//
    //    DialogItem dialogItem2 = new DialogItem();
    //    dialogItem2.setID("2");
    //    dialogItem2.setFromUsername("Baran");
    //    dialogItem2.setPhoto("https://pbs.twimg.com/profile_images/378800000544769000/5d88a19ec1f5955c6a53b17dd6988687_400x400.jpeg");
    //    dialogItem2.setUnreadCount(0);
    //    userList = new ArrayList<>();
    //    author = new Author();
    //    author.setId("2");
    //    author.setName("Baran");
    //    author.setAvatar("https://i.ytimg.com/vi/TuL4bTIGXpc/hqdefault.jpg");
    //    userList.add(author);
    //    dialogItem2.setUserList(userList);
    //    message = new Message();
    //    message.setId("6");
    //    message.setAuthor(author);
    //    message.setDate(new Date(2018, 05, 5, 5, 47));
    //    message.setText("Hey");
    //    dialogItem2.setLastMessage(message);
//
    //    //dialogsListAdapter = new DialogsListAdapter(dialogsList, new ImageLoader() {
    //    //    @Override
    //    //    public void loadImage(ImageView imageView, @Nullable String url) {
    //    //        Glide.with(ChatFragment.this).load(url).into(imageView);
    //    //    }
    //    //});
//
    //    //dialogsListAdapter = new DialogsListAdapter<>(R.layout.fragment_chat, new ImageLoader() {
    //    //    @Override
    //    //    public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
    //    //        Glide.with(ChatFragment.this).load(url).into(imageView);
    //    //    }
    //    //});
//
    //    dialogsListAdapter = new DialogsListAdapter(new ImageLoader() {
    //        @Override
    //        public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
    //            Glide.with(getApplication()).load(url).into(imageView);
    //        }
    //    });
//
    //    dialogsList.setAdapter(dialogsListAdapter);
    //    dialogsListAdapter.addItem(dialogItem);
    //    dialogsListAdapter.addItem(dialogItem2);
    //}

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (mainContainer != null && mainContainer.getChildCount() > 0)
            getSupportFragmentManager().popBackStack();
        else if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            MainActivity.this.finish();
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    //@Override
    //public boolean onOptionsItemSelected(MenuItem item) {
    //    // Handle action bar item clicks here. The action bar will
    //    // automatically handle clicks on the Home/Up button, so long
    //    // as you specify a parent activity in AndroidManifest.xml.
    //    int id = item.getItemId();
//
    //    //noinspection SimplifiableIfStatement
    //    if (id == R.id.action_settings) {
    //        return true;
    //    }
//
    //    return super.onOptionsItemSelected(item);
    //}

    private void ChatInitialize() {
        Log.d("tarja","ChatInitialize");
        //Hawk.delete("master_chat_objects");
        //Hawk.delete("last_incoming_message_ID");
        if (!Hawk.contains("master_chat_objects")) {
            ArrayList<MasterChatObject> masterChatObjects = new ArrayList<>();
            Hawk.put("master_chat_objects", masterChatObjects);
        }
        if (!Hawk.contains("last_incoming_message_ID")) {
            lastIncomingMessageID = 0;
            Hawk.put("last_incoming_message_ID", lastIncomingMessageID);
            GetMessagesFirstTime(false);
        } else {
            lastIncomingMessageID = Hawk.get("last_incoming_message_ID");
            CheckNewMessages(false);
        }
    }

    private String EmojiMessage(String text){
        String emojiString = "";
        byte[] data = Base64.decode(text, Base64.DEFAULT);
        try {
            emojiString = new String(data, "UTF-8");
        } catch (Exception e) {

        }
        return emojiString;
    }

    private Date FormatDate(String date){
        Date formattedDate = new Date();
        try{
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date newDate = dateFormat.parse(date);
            formattedDate = newDate;
        }
        catch (Exception e){

        }
        return formattedDate;
    }

    private void GetMessagesFirstTime(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL+getResources().getString(R.string.ws_GetMessagesFirstTime), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<GetMessagesFirstTimeResponse>() {
                }.getType();
                getMessagesFirstTimeResponse = gson.fromJson(response, myType);
                if (getMessagesFirstTimeResponse.isIsSuccess()){
                    lastIncomingMessageID = getMessagesFirstTimeResponse.getMaxID();
                    Hawk.put("last_incoming_message_ID",lastIncomingMessageID);
                    if(getMessagesFirstTimeResponse.getDialogs().size()>0){
                        ArrayList<MasterChatObject> masterChatObjects = new ArrayList<>();
                        MasterChatObject masterChatObject;
                        for (GetMessagesFirstTimeResponse.DialogsBean item: getMessagesFirstTimeResponse.getDialogs()) {
                            masterChatObject = new MasterChatObject();
                            Message lastMessage = new Message();
                            Author author = new Author();
                            List<Author> authorList = new ArrayList<>();
                            ArrayList<Message> messages = new ArrayList<>();
                            author.setName(item.getFromUsername());
                            author.setId(item.getFromUserID());
                            author.setAvatar(item.getPhoto());
                            authorList.add(author);
                            lastMessage.setAuthor(author);
                            if (item.getMessages().get(item.getMessages().size()-1).getImageURL() != null)
                                lastMessage.setText(getResources().getString(R.string.image));
                            else
                            {
                                String comment = item.getMessages().get(item.getMessages().size()-1).getText();
                                byte[] data = Base64.decode(comment, Base64.DEFAULT);
                                try {
                                    String emojiMessage = new String(data, "UTF-8");
                                    lastMessage.setText(emojiMessage);
                                }catch (Exception e){

                                }
                            }
                            lastMessage.setId(item.getMessages().get(item.getMessages().size()-1).getID());

                            try{
                                lastMessage.setDate(UTCDateFormat.parse(item.getMessages().get(item.getMessages().size()-1).getDate()));
                            }
                            catch (Exception e){
                                lastMessage.setDate(new Date());
                            }
                            masterChatObject.getDialogItem().setUnreadCount(item.getUnreadCount());
                            masterChatObject.getDialogItem().setLastMessage(lastMessage);
                            masterChatObject.getDialogItem().setPhoto(item.getPhoto());
                            masterChatObject.getDialogItem().setFromUsername(item.getFromUsername());
                            masterChatObject.getDialogItem().setID(item.getID());
                            masterChatObject.getDialogItem().setFromUserID(item.getFromUserID());
                            masterChatObject.getDialogItem().setUserList(authorList);
                            for (GetMessagesFirstTimeResponse.DialogsBean.MessagesBean messageItem: item.getMessages()){
                                Message messageToAdd = new Message();
                                Author messageAuthor = new Author();
                                messageAuthor.setId(String.valueOf(messageItem.getFromUserID()));
                                messageAuthor.setAvatar(author.getAvatar());
                                messageAuthor.setName("");
                                messageToAdd.setAuthor(messageAuthor);
                                try{
                                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                    dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                    Date newDate = dateFormat.parse(messageItem.getDate());
                                    messageToAdd.setDate(newDate);
                                }catch (Exception e){
                                    messageToAdd.setDate(new Date());
                                }
                                messageToAdd.setId(messageItem.getID());
                                if (messageItem.getImageURL() != null)
                                    messageToAdd.setImageURL(messageItem.getImageURL());
                                else
                                {
                                    String comment = messageItem.getText();
                                    byte[] data = Base64.decode(comment, Base64.DEFAULT);
                                    try {
                                        String emojiMessage = new String(data, "UTF-8");
                                        messageToAdd.setText(emojiMessage);
                                    }catch (Exception e){

                                    }
                                }
                                messages.add(messageToAdd);
                            }
                            masterChatObject.setMessages(messages);
                            masterChatObjects.add(masterChatObject);
                        }
                        Hawk.put("master_chat_objects",masterChatObjects);
                        PopulateDialogs();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tarja","onErrorResponse,GetMessagesFirstTime");
                if (!useBackup)
                    GetMessagesFirstTime(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("token", loggedUser.getToken());
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void CheckNewMessages(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL+getResources().getString(R.string.ws_CheckNewMessages), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<CheckNewMessagesResponse>() {
                }.getType();
                checkNewMessagesResponse = gson.fromJson(response, myType);
                if (checkNewMessagesResponse.isIsSuccess()) {
                    if(checkNewMessagesResponse.getTotalUnreadCount() > 0){
                        lastIncomingMessageID = checkNewMessagesResponse.getMaxID();
                        Hawk.put("last_incoming_message_ID",lastIncomingMessageID);
                        ArrayList<MasterChatObject> masterChatObjects = Hawk.get("master_chat_objects");
                        Boolean dialogExists;

                        for (CheckNewMessagesResponse.DialogsBean responseItem: checkNewMessagesResponse.getDialogs()) {
                            dialogExists = false;
                            for (MasterChatObject localItem: masterChatObjects)
                            {
                                if (localItem.getDialogItem().getId().equals("-1")){
                                    if(localItem.getDialogItem().getFromUsername().equals(responseItem.getFromUsername())){
                                        localItem.getDialogItem().setID(responseItem.getID());
                                    }
                                }
                                if(localItem.getDialogItem().getId().equals(responseItem.getID())){
                                    dialogExists = true;
                                    ArrayList<Message> localMessages = localItem.getMessages();
                                    Message messageToAdd = new Message();
                                    for (CheckNewMessagesResponse.DialogsBean.MessagesBean messageItem: responseItem.getMessages()){
                                        if (messageItem.getID().equals(localMessages.get(localMessages.size()-1).getId()) ){
                                            Log.d("tarja","duplicate");
                                        }
                                        messageToAdd = new Message();
                                        Author messageAuthor = new Author();
                                        messageAuthor.setId(String.valueOf(messageItem.getFromUserID()));
                                        messageAuthor.setAvatar(responseItem.getPhoto());
                                        messageAuthor.setName("");
                                        messageToAdd.setAuthor(messageAuthor);
                                        try{
                                            //DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                            //dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                            //Date newDate = dateFormat.parse(messageItem.getDate());
                                            //messageToAdd.setDate(newDate);
                                            messageToAdd.setDate(UTCDateFormat.parse(messageItem.getDate()));
                                        }catch (Exception e){
                                            messageToAdd.setDate(new Date());
                                        }
                                        messageToAdd.setId(messageItem.getID());

                                        if (messageItem.getImageURL() != null){
                                            messageToAdd.setImageURL(messageItem.getImageURL());
                                            messageToAdd.setText(getResources().getString(R.string.image));
                                        }
                                        else
                                        {
                                            String comment = messageItem.getText();
                                            byte[] data = Base64.decode(comment, Base64.DEFAULT);
                                            try {
                                                String emojiMessage = new String(data, "UTF-8");
                                                messageToAdd.setText(emojiMessage);
                                            }catch (Exception e){

                                            }
                                        }
                                        localMessages.add(messageToAdd);
                                    }
                                    localItem.setMessages(localMessages);
                                    localItem.getDialogItem().setLastMessage(messageToAdd);
                                    localItem.getDialogItem().setUnreadCount(localItem.getDialogItem().getUnreadCount() + responseItem.getUnreadCount());
                                }
                            }
                            if (!dialogExists){
                                MasterChatObject masterChatObjectAdd = new MasterChatObject();
                                DialogItem dialogItemAdd = new DialogItem();
                                dialogItemAdd.setUnreadCount(responseItem.getUnreadCount());
                                dialogItemAdd.setPhoto(responseItem.getPhoto());
                                dialogItemAdd.setID(responseItem.getID());
                                dialogItemAdd.setFromUserID(responseItem.getFromUserID());
                                dialogItemAdd.setFromUsername(responseItem.getFromUsername());
                                List<Author> authorListAdd = new ArrayList<>();
                                Author authorAdd = new Author();
                                authorAdd.setName(responseItem.getFromUsername());
                                authorAdd.setAvatar(responseItem.getPhoto());
                                authorAdd.setId(responseItem.getFromUserID());
                                authorListAdd.add(authorAdd);
                                dialogItemAdd.setUserList(authorListAdd);
                                Message messageToAdd = new Message();
                                ArrayList<Message> messageListAdd = new ArrayList<>();
                                for (CheckNewMessagesResponse.DialogsBean.MessagesBean messageItemAdd: responseItem.getMessages()){
                                    messageToAdd = new Message();
                                    messageToAdd.setAuthor(authorAdd);
                                    messageToAdd.setId(messageItemAdd.getID());
                                    try{
                                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                                        Date newDate = dateFormat.parse(messageItemAdd.getDate());
                                        messageToAdd.setDate(newDate);
                                    }catch (Exception e){
                                        messageToAdd.setDate(new Date());
                                    }

                                    if (messageItemAdd.getImageURL() != null){
                                        messageToAdd.setImageURL(messageItemAdd.getImageURL());
                                        messageToAdd.setText(getResources().getString(R.string.image));
                                    }
                                    else
                                    {
                                        String comment = messageItemAdd.getText();
                                        byte[] data = Base64.decode(comment, Base64.DEFAULT);
                                        try {
                                            String emojiMessage = new String(data, "UTF-8");
                                            messageToAdd.setText(emojiMessage);
                                        }catch (Exception e){

                                        }
                                    }
                                    messageListAdd.add(messageToAdd);
                                }
                                dialogItemAdd.setLastMessage(messageToAdd);
                                masterChatObjectAdd.setDialogItem(dialogItemAdd);
                                masterChatObjectAdd.setMessages(messageListAdd);
                                masterChatObjects.add(masterChatObjectAdd);
                            }
                        }
                        Hawk.put("master_chat_objects",masterChatObjects);
                    }
                }
                PopulateDialogs();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    CheckNewMessages(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("token", loggedUser.getToken());
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                params.put("lastIncomingMessageID",String.valueOf(lastIncomingMessageID));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void NewMessageReceived(Intent intent) {
        String messageID = intent.getStringExtra("messageID");
        String senderUserProfilePic = intent.getStringExtra("senderUserProfilePic");
        String senderUserID = intent.getStringExtra("senderUserID");
        String content = intent.getStringExtra("content");
        String senderUserName = intent.getStringExtra("senderUserName");
        String dialogID = intent.getStringExtra("dialogID");
        String imageURL = intent.getStringExtra("imageURL");
        Boolean dialogExists = false;

        ArrayList<MasterChatObject> masterChatObjects = Hawk.get("master_chat_objects");
        for (MasterChatObject masterChatObject: masterChatObjects){
            if (masterChatObject.getDialogItem().getId().equals(dialogID)){

                dialogExists = true;
                masterChatObject.getDialogItem().setUnreadCount(masterChatObject.getDialogItem().getUnreadCount()+1);
                ArrayList<Message> localMessages = masterChatObject.getMessages();
                Message messageToAdd = new Message();

                if (!localMessages.get(localMessages.size()-1).getId().equals(messageID)){ // duplicate kontrolü
                    messageToAdd.setAuthor(masterChatObject.getDialogItem().getUserList().get(0));
                    try{
                        messageToAdd.setDate(new Date());
                    }catch (Exception e){
                        messageToAdd.setDate(new Date());
                    }
                    messageToAdd.setId(messageID);

                    if (imageURL != null){
                        messageToAdd.setImageURL(imageURL);
                        messageToAdd.setText(getResources().getString(R.string.image));
                    }
                    else
                    {
                        try {
                            byte[] data = Base64.decode(content, Base64.DEFAULT);
                            String emojiMessage = new String(data, "UTF-8");
                            messageToAdd.setText(emojiMessage);
                        }catch (Exception e){
                            messageToAdd.setText("");
                        }
                    }
                    localMessages.add(messageToAdd);
                    masterChatObject.setMessages(localMessages);
                    masterChatObject.getDialogItem().setLastMessage(messageToAdd);
                }
            }
        }
        if (!dialogExists){
            MasterChatObject masterChatObjectAdd = new MasterChatObject();
            DialogItem dialogItemAdd = new DialogItem();
            dialogItemAdd.setUnreadCount(1);
            dialogItemAdd.setPhoto(senderUserProfilePic);
            dialogItemAdd.setID(dialogID);
            dialogItemAdd.setFromUserID(senderUserID);
            dialogItemAdd.setFromUsername(senderUserName);
            List<Author> authorListAdd = new ArrayList<>();
            Author authorAdd = new Author();
            authorAdd.setName(senderUserName);
            authorAdd.setAvatar(senderUserProfilePic);
            authorAdd.setId(senderUserID);
            authorListAdd.add(authorAdd);
            dialogItemAdd.setUserList(authorListAdd);
            Message messageToAdd = new Message();
            ArrayList<Message> messageListAdd = new ArrayList<>();

            messageToAdd = new Message();
            messageToAdd.setAuthor(authorAdd);
            messageToAdd.setId(messageID);
            try{
                messageToAdd.setDate(new Date());
            }catch (Exception e){
                messageToAdd.setDate(new Date());
            }
            byte[] data = Base64.decode(content, Base64.DEFAULT);
            try {
                String emojiMessage = new String(data, "UTF-8");
                messageToAdd.setText(emojiMessage);
            }catch (Exception e){

            }
            messageListAdd.add(messageToAdd);

            dialogItemAdd.setLastMessage(messageToAdd);
            masterChatObjectAdd.setDialogItem(dialogItemAdd);
            masterChatObjectAdd.setMessages(messageListAdd);
            masterChatObjects.add(masterChatObjectAdd);
            Log.d("tarja","yeni dialog eklendi");
        }
        Hawk.put("master_chat_objects",masterChatObjects);
        lastIncomingMessageID = Integer.parseInt(messageID);
        Hawk.put("last_incoming_message_ID",lastIncomingMessageID);
        RefreshList();
    }

    private void PopulateDialogs(){
        List<MasterChatObject> masterChatObjects;
        masterChatObjects = Hawk.get("master_chat_objects");
        if (masterChatObjects != null){
            Collections.sort(masterChatObjects, new Comparator<MasterChatObject>() {
                @Override
                public int compare(MasterChatObject o1, MasterChatObject o2) {
                    return o2.getDialogItem().getLastMessage().getCreatedAt().compareTo(o1.getDialogItem().getLastMessage().getCreatedAt());
                }
            });
            for (MasterChatObject masterItem:masterChatObjects){
                CreateDialog(masterItem);
            }
        }
    }

    private void CreateDialog(MasterChatObject masterItem) {
        DialogItem dialogItem = new DialogItem();
        dialogItem.setID(masterItem.getDialogItem().getId());
        dialogItem.setUserList(masterItem.getDialogItem().getUserList());
        dialogItem.setFromUsername(masterItem.getDialogItem().getFromUsername());
        dialogItem.setPhoto(masterItem.getDialogItem().getDialogPhoto());
        //Log.d("tarja",masterItem.getDialogItem().getLastMessage().getCreatedAt().toString());
        dialogItem.setLastMessage(masterItem.getDialogItem().getLastMessage());
        dialogItem.setUnreadCount(masterItem.getDialogItem().getUnreadCount());
        dialogItem.setFromUserID(masterItem.getDialogItem().getFromUserID());
        dialogsListAdapter.addItem(dialogItem);
    }

    public void RefreshList(){
        dialogsListAdapter.clear();
        PopulateDialogs();
    }

    private void OpenDialog(String dialogID, String fromUsername, String profilePic, String senderID){
        Intent myIntent = new Intent(MainActivity.this, DialogActivity.class);
        myIntent.putExtra("DialogID", dialogID);
        myIntent.putExtra("FromUsername", fromUsername);
        myIntent.putExtra("ProfilePic", profilePic);
        myIntent.putExtra("SenderID", senderID);
        MainActivity.this.startActivity(myIntent);
    }

    private void FindNewChat(final boolean useBackup) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_FindNewChat);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<FindNewChatResponse>() {
                }.getType();
                findNewChatResponse = gson.fromJson(response, myType);
                if (findNewChatResponse.isIsSuccess()) {
                    OpenDialog("0",findNewChatResponse.getUsername(),findNewChatResponse.getProfilePic(),String.valueOf(findNewChatResponse.getUserID()));
                }
                else if(findNewChatResponse.getMessage().equals("chat_not_found")){
                    new EZDialog.Builder(MainActivity.this)
                            .setTitle(getResources().getString(R.string.we_re_sorry))
                            .setMessage(getResources().getString(R.string.chat_not_found))
                            .setNeutralBtnText(getResources().getString(R.string.ok))
                            .OnNeutralClicked(new EZDialogListener() {
                                @Override
                                public void OnClick() {

                                }
                            })
                            .setCancelableOnTouchOutside(false)
                            .build();
                }
                loadingDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    FindNewChat(true);
                else
                    loadingDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("token", loggedUser.getToken());
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 10, 1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void DeleteChat(final boolean useBackup,final String deleteUserID) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_DeleteChat);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<BasicResponse>() {
                }.getType();
                deleteChatResponse = gson.fromJson(response, myType);
                if (deleteChatResponse.isIsSuccess()) {
                    ArrayList<MasterChatObject> masterChatObjects = Hawk.get("master_chat_objects");
                    for (MasterChatObject item:masterChatObjects){
                        if (item.getDialogItem().getFromUserID().equals(deleteUserID)){
                            masterChatObjects.remove(item);
                            Hawk.put("master_chat_objects",masterChatObjects);
                            RefreshList();
                            break;
                        }
                    }
                    Toasty.success(MainActivity.this, getResources().getString(R.string.dialog_deleted), Toast.LENGTH_LONG, true).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    DeleteChat(true, deleteUserID);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("token", loggedUser.getToken());
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                params.put("deleteUserID", deleteUserID);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 10, 1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void AddPremium(final boolean useBackup) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_AddPremium);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<BasicResponse>() {
                }.getType();
                addPremiumResponse = gson.fromJson(response, myType);
                if (addPremiumResponse.isIsSuccess()) {
                    GetPremiumList(false);
                    btPremium.hide();
                    new EZDialog.Builder(MainActivity.this)
                            .setTitle(getResources().getString(R.string.highlights))
                            .setMessage(getResources().getString(R.string.premium_activated, Hawk.get("prefPremiumHour")))
                            .setNeutralBtnText(getResources().getString(R.string.ok))
                            .OnNeutralClicked(new EZDialogListener() {
                                @Override
                                public void OnClick() {

                                }
                            })
                            .setCancelableOnTouchOutside(false)
                            .build();
                    try{
                        GetPremiumInfoResponse.PremiumInfoBean premiumInfo = new GetPremiumInfoResponse.PremiumInfoBean();
                        premiumInfo.setDate(UTCDateFormat.format(new Date()));
                        getPremiumInfoResponse.setPremiumInfo(premiumInfo);
                        getPremiumInfoResponse.setMessage("premium");
                    }
                    catch (Exception e){
                    }
                }
                else if(addPremiumResponse.getMessage().equals("already_premium")){
                    GetPremiumInfo(false);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    AddPremium(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("token", loggedUser.getToken());
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 10, 1.0f));
        mRequestQueue.add(stringRequest);
    }
}

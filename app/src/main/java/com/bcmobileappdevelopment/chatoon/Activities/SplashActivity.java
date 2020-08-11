package com.bcmobileappdevelopment.chatoon.Activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bcmobileappdevelopment.chatoon.GsonResponse.GetPreferencesResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;
import com.bcmobileappdevelopment.chatoon.BuildConfig;
import com.bcmobileappdevelopment.chatoon.GsonResponse.LoginResponse;
import com.bcmobileappdevelopment.chatoon.R;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;
import spencerstudios.com.ezdialoglib.EZDialog;
import spencerstudios.com.ezdialoglib.EZDialogListener;

public class SplashActivity extends AppCompatActivity {

    LoginResponse loginResponse, loggedUser;
    Gson gson;
    FirebaseAuth mAuth;
    GetPreferencesResponse getPreferencesResponse;
    int prefVersion, prefMaintenance;
    BroadcastReceiver finishActivityReceiver;
    boolean stop = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Initialize();
        if (isNetworkConnected())
            GetPreferences(false);
        else{
            new EZDialog.Builder(SplashActivity.this)
                    .setTitle(getResources().getString(R.string.no_internet_title))
                    .setMessage(getResources().getString(R.string.no_internet_message))
                    .setPositiveBtnText(getResources().getString(R.string.ok))
                    .setCancelableOnTouchOutside(false)
                    .OnPositiveClicked(new EZDialogListener() {
                        @Override
                        public void OnClick() {
                            SplashActivity.this.finish();
                        }
                    })
                    .build();
        }

        //Handler handler = new Handler();
        //handler.postDelayed(new Runnable() {
        //    public void run() {
        //        Intent myIntent = new Intent(SplashActivity.this, LoginActivity.class);
        //        SplashActivity.this.startActivity(myIntent);
        //        SplashActivity.this.finish();
        //    }
        //}, 500);
    }

    private void KeyHash() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.bcmobileappdevelopment.chatoon",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("tarja", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.d("tarja",e.getMessage());

        }
        catch (NoSuchAlgorithmException e) {
            Log.d("tarja",e.getMessage());
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(finishActivityReceiver);
        super.onDestroy();
    }

    private void Initialize() {
        mAuth = FirebaseAuth.getInstance();
        Hawk.init(SplashActivity.this).build();

        finishActivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SplashActivity.this.finish();
            }
        };
        registerReceiver(finishActivityReceiver,new IntentFilter("finish_activity"));
    }

    private void GetPreferences(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(SplashActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String method = getResources().getString(R.string.ws_GetPreferences);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + method, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<GetPreferencesResponse>() {
                }.getType();
                getPreferencesResponse = gson.fromJson(response, myType);
                if (getPreferencesResponse.isIsSuccess()){
                    for (GetPreferencesResponse.PreferencesBean item: getPreferencesResponse.getPreferences()){
                        switch (item.getPreference()){
                            case "Version":
                                prefVersion = Integer.parseInt(item.getValue());
                                if (prefVersion > BuildConfig.VERSION_CODE){
                                    stop = true;
                                    new EZDialog.Builder(SplashActivity.this)
                                            .setTitle(getResources().getString(R.string.update_app_title))
                                            .setMessage(getResources().getString(R.string.update_app_message))
                                            .setPositiveBtnText(getResources().getString(R.string.ok))
                                            .setNegativeBtnText(getResources().getString(R.string.cancel))
                                            .setCancelableOnTouchOutside(false)
                                            .OnPositiveClicked(new EZDialogListener() {
                                                @Override
                                                public void OnClick() {
                                                    SplashActivity.this.finish();
                                                    try {
                                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.bcmobileappdevelopment.chatoon")));
                                                    } catch (android.content.ActivityNotFoundException anfe) {
                                                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.bcmobileappdevelopment.chatoon")));
                                                    }
                                                }
                                            })
                                            .OnNegativeClicked(new EZDialogListener() {
                                                @Override
                                                public void OnClick() {
                                                    SplashActivity.this.finish();
                                                }
                                            })
                                            .build();
                                }
                                break;
                            case "Maintenance":
                                prefMaintenance = Integer.parseInt(item.getValue());
                                if (prefMaintenance > 0){
                                    stop = true;
                                    new EZDialog.Builder(SplashActivity.this)
                                            .setTitle(getResources().getString(R.string.maintenance_title))
                                            .setMessage(getResources().getString(R.string.maintenance_message))
                                            .setPositiveBtnText(getResources().getString(R.string.ok))
                                            .setCancelableOnTouchOutside(false)
                                            .OnPositiveClicked(new EZDialogListener() {
                                                @Override
                                                public void OnClick() {
                                                    SplashActivity.this.finish();
                                                }
                                            })
                                            .build();
                                }
                                break;
                            case "FindNewChatAd":
                                Hawk.put("prefFindNewChatAd",Integer.parseInt(item.getValue()));
                                break;
                            case "PremiumHour":
                                Hawk.put("prefPremiumHour",item.getValue());
                                break;
                        }
                    }
                    if (!stop)
                        StartAutoLogin();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    GetPreferences(true);
                if (useBackup){
                    //maintenanceDialog.show(); TODO
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void StartAutoLogin(){
        if (Hawk.contains("loggedUser")){
            loggedUser = Hawk.get("loggedUser");
            if (loggedUser.getUser().getID() != 1)
                AutoLogin(false);
            else {
                Intent mainIntent = new Intent(SplashActivity.this,LoginActivity.class);
                SplashActivity.this.startActivity(mainIntent);
                SplashActivity.this.finish();
            }
        }
        else{
            new Handler().postDelayed(new Runnable(){
                @Override
                public void run() {

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            Intent myIntent = new Intent(SplashActivity.this, LoginActivity.class);
                            SplashActivity.this.startActivity(myIntent);
                            SplashActivity.this.finish();
                        }
                    }, 500);

                    //Intent mainIntent = new Intent(SplashActivity.this,LoginActivity.class);
                    //SplashActivity.this.startActivity(mainIntent);
                    //SplashActivity.this.finish();
                }
            }, 0);
        }
    }

    private void AutoLogin(final Boolean useBackup)
    {
        RequestQueue mRequestQueue = Volley.newRequestQueue(SplashActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        String login_service = "";
        switch (loggedUser.getUser().getAccountType()){
            case "Anonymous":
                login_service = getResources().getString(R.string.ws_LoginAnonymous);
                break;
            case "Email":
                login_service = getResources().getString(R.string.ws_AutoLogin);
                break;
            case "Facebook":
                login_service = getResources().getString(R.string.ws_AutoLogin);
                break;
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL+login_service, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<LoginResponse>() {
                }.getType();
                loginResponse = gson.fromJson(response, myType);
                if (loginResponse.isIsSuccess()){
                    FirebaseLogin(loginResponse.getUser().getEmail(),loginResponse.getToken());
                    Hawk.put("loggedUser",loginResponse);
                    Intent myIntent = new Intent(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(myIntent);
                    SplashActivity.this.finish();
                }
                else if(loginResponse.getMessage().equals("user_banned")){
                    Toasty.warning(SplashActivity.this, getResources().getString(R.string.user_banned), Toast.LENGTH_LONG, true).show();
                }
                else {
                    Intent mainIntent = new Intent(SplashActivity.this,LoginActivity.class);
                    SplashActivity.this.startActivity(mainIntent);
                    SplashActivity.this.finish();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    AutoLogin(true);
                if (useBackup){
                    //maintenanceDialog.show(); TODO
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                Log.d("tarja",String.valueOf(loggedUser.getUser().getID()));
                Log.d("tarja",loggedUser.getToken());
                switch (loggedUser.getUser().getAccountType()){
                    case "Anonymous":
                        params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                        params.put("token", loggedUser.getToken());
                        break;
                    case "Email":
                        params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                        params.put("accountKey", loggedUser.getUser().getAccountKey());
                        params.put("token", loggedUser.getToken());
                        break;
                    case "Facebook":
                        params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                        params.put("accountKey", loggedUser.getUser().getAccountKey());
                        params.put("token", loggedUser.getToken());
                        break;
                }
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    private void FirebaseLogin(String email, String pass){
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //FirebaseUser user = mAuth.getCurrentUser();
                            Log.d("tarja","firebase login success");
                        } else {
                            Log.w("tarja", "login:failure", task.getException());
                        }
                    }
                });
    }
}
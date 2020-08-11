package com.bcmobileappdevelopment.chatoon.Activities;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.transition.ChangeBounds;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;
import com.bcmobileappdevelopment.chatoon.GsonResponse.FacebookLoginResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.LoginResponse;
import com.bcmobileappdevelopment.chatoon.R;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class LoginActivity extends AppCompatActivity {

    ImageView backgroundPic, blur, ivLoading;
    EditText etEmail, etPassword;
    //Button btLogin;
    Gson gson;
    LoginResponse loginResponse;
    CallbackManager callbackManager;
    Button btFacebookLogin;
    String facebookAccessToken;
    FacebookLoginResponse facebookLoginResponse;
    TextView btAnonymous, btRegisterEmail, btLoginEmail;
    BroadcastReceiver finishActivityReceiver;
    Dialog loadingDialog;

    private ConstraintLayout constraint;
    private ConstraintSet constraintSet = new ConstraintSet();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Initialize();
        InitializeListeners();
    }

    private void Initialize() {
        backgroundPic = findViewById(R.id.background_pic);
        blur = findViewById(R.id.blur);
        Glide.with(this).load(getResources().getDrawable(R.drawable.splash_pic_blur))
                .into(blur);
        blur.setVisibility(View.VISIBLE);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Animation();
            }
        }, 1000);
        btAnonymous = findViewById(R.id.btAnonymous);
        btFacebookLogin = findViewById(R.id.btFacebookLogin);
        btRegisterEmail = findViewById(R.id.btRegisterEmail);
        btLoginEmail = findViewById(R.id.btLoginEmail);
        //LoginManager.getInstance().logOut();
        finishActivityReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LoginActivity.this.finish();
            }
        };
        registerReceiver(finishActivityReceiver,new IntentFilter("finish_activity"));
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ivLoading = loadingDialog.findViewById(R.id.ivLoading);
        Glide.with(LoginActivity.this).load(R.drawable.loading).into(ivLoading);
        Hawk.put("showcase",true);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(finishActivityReceiver);
        super.onDestroy();
    }

    private void InitializeListeners() {

        btFacebookLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show();
                StartLoginFacebook();
            }
        });

        btAnonymous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                myIntent.putExtra("register_type","anonymous");
                LoginActivity.this.startActivity(myIntent);
            }
        });
        btLoginEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, LoginEmailActivity.class);
                LoginActivity.this.startActivity(myIntent);
            }
        });
        btRegisterEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                myIntent.putExtra("register_type","email");
                LoginActivity.this.startActivity(myIntent);
            }
        });
        btAnonymous.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                btAnonymous.setBackgroundColor(getResources().getColor(R.color.transparent_red_pressed));
                if (event.getAction() == MotionEvent.ACTION_UP)
                    btAnonymous.setBackgroundColor(getResources().getColor(R.color.transparent_red));
                return false;
            }
        });
        btLoginEmail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                btLoginEmail.setBackgroundColor(getResources().getColor(R.color.transparent_green_pressed));
                if (event.getAction() == MotionEvent.ACTION_UP)
                    btLoginEmail.setBackgroundColor(getResources().getColor(R.color.transparent_green));
                return false;
            }
        });
        btRegisterEmail.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                btRegisterEmail.setBackgroundColor(getResources().getColor(R.color.transparent_blue_pressed));
                if (event.getAction() == MotionEvent.ACTION_UP)
                    btRegisterEmail.setBackgroundColor(getResources().getColor(R.color.transparent_blue));
                return false;
            }
        });
    }

    private void StartLoginFacebook(){
        callbackManager = CallbackManager.Factory.create();
        ArrayList<String> permissionList = new ArrayList<String>();
        permissionList.add("email");
        permissionList.add("public_profile");
        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, permissionList);

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("tarja","LoginFacebookSuccess");
                RequestFacebookData();
            }

            @Override
            public void onCancel() {
                loadingDialog.dismiss();
                Log.d("tarja", "On cancel");
            }

            @Override
            public void onError(FacebookException error) {
                loadingDialog.dismiss();
                Log.d("tarja", error.toString());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void RequestFacebookData() {
        facebookAccessToken = AccessToken.getCurrentAccessToken().getToken();
        Log.d("tarja","facebooktoken = " + facebookAccessToken);
        GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
            @Override
            public void onCompleted(JSONObject object, GraphResponse response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<FacebookLoginResponse>() {
                }.getType();
                facebookLoginResponse = gson.fromJson(object.toString(), myType);
                facebookLoginResponse.setProfile_pic("https://graph.facebook.com/"+facebookLoginResponse.getId()+"/picture?type=large");
                //Log.d("tarja",AccessToken.getCurrentAccessToken().getToken());
                //Log.d("tarja", "userID-> "+facebookLoginResponse.getId());
                LoginFacebook(false);
            }
        });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,first_name,last_name");
        request.setParameters(parameters);
        request.executeAsync();
    }

    private void LoginFacebook(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_LoginFacebook);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL+webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<LoginResponse>() {
                }.getType();
                loginResponse = gson.fromJson(response, myType);
                if (loginResponse.isIsSuccess()){
                    if(loginResponse.getMessage().equals("facebook_login")){
                        Hawk.put("loggedUser",loginResponse);
                        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                        LoginActivity.this.startActivity(myIntent);
                        LoginActivity.this.finish();
                    }
                    else if(loginResponse.getMessage().equals("register_facebook")){
                        Intent myIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                        Hawk.put("facebook_register_data",facebookLoginResponse);
                        myIntent.putExtra("register_type","facebook");
                        myIntent.putExtra("access_token",facebookAccessToken);
                        LoginActivity.this.startActivity(myIntent);
                    }
                }
                else{
                    if(loginResponse.getMessage().equals("user_banned")){
                        Toasty.warning(LoginActivity.this, getResources().getString(R.string.user_banned), Toast.LENGTH_LONG, true).show();
                    }
                    else if(loginResponse.getMessage().equals("email_used")){
                        //Toasty.error(LoginActivity.this, getResources().getString(R.string.basic_error), Toast.LENGTH_LONG, true).show(); //TODO
                    }
                }
                loadingDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    LoginFacebook(true);
                else
                    loadingDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("accessToken", facebookAccessToken);
                params.put("email", facebookLoginResponse.getEmail());
                params.put("facebookID", facebookLoginResponse.getId());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void LoginEmail(final boolean useBackup) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(LoginActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_LoginEmail);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<LoginResponse>() {
                }.getType();
                loginResponse = gson.fromJson(response, myType);
                if (loginResponse.isIsSuccess()){
                    Hawk.put("loggedUser",loginResponse);
                    Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(myIntent);
                    LoginActivity.this.finish();
                }
                else if(loginResponse.getMessage().equals("invalid_login")) {
                    //TODO
                }
                else if(loginResponse.getMessage().equals("user_banned")) {
                    //TODO
                }
                else if(loginResponse.getMessage().equals("user_not_active")) {
                    //TODO
                }
                else if(loginResponse.getMessage().equals("not_email_account")) {
                    //TODO
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tarja",error.getMessage());
                if (!useBackup){
                    LoginEmail(true);
                }
                if (useBackup){
                    //maintenanceDialog.show(); TODO
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("email", etEmail.getText().toString());
                params.put("accountKey", etPassword.getText().toString());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void Animation() {
        constraint = findViewById(R.id.login_layout_1);
        constraintSet.clone(this, R.layout.activity_login_2);
        Transition transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateOvershootInterpolator(.5f));
        transition.setDuration(2000);

        TransitionManager.beginDelayedTransition(constraint, transition);
        constraintSet.applyTo(constraint);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Animation2();
            }
        }, 2000);

    }

    private void Animation2() {
        constraint = findViewById(R.id.login_layout_1);
        constraintSet.clone(this, R.layout.activity_login_3);
        Transition transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateOvershootInterpolator(.5f));
        transition.setDuration(2000);

        TransitionManager.beginDelayedTransition(constraint, transition);
        constraintSet.applyTo(constraint);
    }
}

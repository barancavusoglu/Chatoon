package com.bcmobileappdevelopment.chatoon.Activities;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.bcmobileappdevelopment.chatoon.GsonResponse.CheckNewMessagesResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.LoginResponse;
import com.bcmobileappdevelopment.chatoon.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class LoginEmailActivity extends AppCompatActivity {

    ImageView btClose;
    Gson gson;
    LoginResponse loginResponse;
    MaterialEditText etEmail, etPassword;
    TextView btLogin;
    FirebaseAuth mAuth;
    Dialog loadingDialog;
    ImageView ivLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_email);
        Initialize();
        InitializeListeners();
    }

    private void Initialize() {
        mAuth = FirebaseAuth.getInstance();
        btClose = findViewById(R.id.btClose);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btLogin = findViewById(R.id.btLogin);

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ivLoading = loadingDialog.findViewById(R.id.ivLoading);
        Glide.with(LoginEmailActivity.this).load(R.drawable.loading).into(ivLoading);
    }

    private void InitializeListeners() {
        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginEmailActivity.this.finish();
            }
        });
        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!IsUserDonkey()){
                    loadingDialog.show();
                    LoginEmail(false);
                }
            }
        });
        btClose.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                btClose.setBackground(getResources().getDrawable(R.drawable.circle_pressed));
                if (event.getAction() == MotionEvent.ACTION_UP)
                    btClose.setBackground(getResources().getDrawable(R.drawable.circle));
                return false;
            }
        });
        btLogin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                btLogin.setBackgroundColor(getResources().getColor(R.color.button_green_pressed));
                if (event.getAction() == MotionEvent.ACTION_UP)
                    btLogin.setBackgroundColor(getResources().getColor(R.color.button_green));
                return false;
            }
        });
    }

    private boolean IsUserDonkey(){
        if (etEmail.getText().length() < getResources().getInteger(R.integer.email_length_min) || etEmail.getText().length() > getResources().getInteger(R.integer.email_length_max)){
            Toasty.warning(LoginEmailActivity.this, getResources().getString(R.string.invalid_length,getResources().getString(R.string.email)), Toast.LENGTH_LONG, true).show();
            return true;
        }
        if (etPassword.getText().length() < getResources().getInteger(R.integer.password_length_min) || etPassword.getText().length() > getResources().getInteger(R.integer.password_length_max)){
            Toasty.warning(LoginEmailActivity.this, getResources().getString(R.string.invalid_length,getResources().getString(R.string.password)), Toast.LENGTH_LONG, true).show();
            return true;
        }
        if (!isValidEmail(etEmail.getText().toString())){
            Toasty.warning(LoginEmailActivity.this, getResources().getString(R.string.invalid_input,getResources().getString(R.string.email)), Toast.LENGTH_LONG, true).show();
            return true;
        }
        if (!isValidPassword(etPassword.getText().toString())){
            Toasty.warning(LoginEmailActivity.this, getResources().getString(R.string.invalid_input,getResources().getString(R.string.password)), Toast.LENGTH_LONG, true).show();
            return true;
        }
        return false;
    }

    public boolean isValidEmail(String name) {
        return name.matches("[a-zA-Z0-9\\-@_.]+");
    }

    public boolean isValidPassword(String name) {
        return name.matches("[a-zA-z0-9-!'^+%&/()=?_*]+");
    }

    private void LoginEmail(final boolean useBackup) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(LoginEmailActivity.this);
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
                    FirebaseLogin(loginResponse.getUser().getEmail(), loginResponse.getToken());
                    Hawk.put("loggedUser",loginResponse);
                    Intent myIntent = new Intent(LoginEmailActivity.this, MainActivity.class);
                    LoginEmailActivity.this.startActivity(myIntent);
                    Intent intent = new Intent("finish_activity");
                    sendBroadcast(intent);
                    LoginEmailActivity.this.finish();
                }
                else if(loginResponse.getMessage().equals("invalid_login")) {
                    Toasty.warning(LoginEmailActivity.this, getResources().getString(R.string.invalid_login), Toast.LENGTH_LONG, true).show();
                }
                else if(loginResponse.getMessage().equals("user_banned")) {
                    Toasty.warning(LoginEmailActivity.this, getResources().getString(R.string.user_banned), Toast.LENGTH_LONG, true).show();
                }
                else if(loginResponse.getMessage().equals("user_not_active")) {
                    Toasty.warning(LoginEmailActivity.this, getResources().getString(R.string.invalid_login), Toast.LENGTH_LONG, true).show();
                }
                else if(loginResponse.getMessage().equals("not_email_account")) {
                    Toasty.warning(LoginEmailActivity.this, getResources().getString(R.string.invalid_login), Toast.LENGTH_LONG, true).show();
                }
                loadingDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup){
                    LoginEmail(true);
                }
                else
                    loadingDialog.dismiss();
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

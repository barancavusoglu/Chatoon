package com.bcmobileappdevelopment.chatoon.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bcmobileappdevelopment.chatoon.GsonResponse.BasicResponse;
import com.bumptech.glide.Glide;
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import com.bcmobileappdevelopment.chatoon.Fragments.VerificateEmailFragment;
import com.bcmobileappdevelopment.chatoon.GsonResponse.LoginResponse;
import com.bcmobileappdevelopment.chatoon.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import spencerstudios.com.ezdialoglib.EZDialog;
import spencerstudios.com.ezdialoglib.EZDialogListener;

public class SettingsActivity extends AppCompatActivity {

    ConstraintLayout settingsContainer;
    ImageView btBack, ivLoading;
    TextView btVerificateEmail, btLogOut, btOpenSource, btPrivacyPolicy, btTermsAndConditions, textView;
    LoginResponse loggedUser;
    BroadcastReceiver emailVerificatedReceiver;
    String logout_title, logout_message;
    BasicResponse anonymousLogoutResponse;
    Gson gson;
    Dialog loadingDialog;
    AlertDialog.Builder alert;
    WebView wv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        Initialize();
        InitializeListeners();
    }

    private void Initialize() {
        loggedUser = Hawk.get("loggedUser");
        btLogOut = findViewById(R.id.btLogOut);
        btOpenSource = findViewById(R.id.btOpenSource);
        settingsContainer = findViewById(R.id.settingsContainer);
        btBack = findViewById(R.id.btBack);
        btPrivacyPolicy = findViewById(R.id.btPrivacyPolicy);
        btTermsAndConditions = findViewById(R.id.btTermsAndConditions);
        if (!loggedUser.getUser().isIsEmailApproved() && loggedUser.getUser().getAccountType().equals("Email")){
            btVerificateEmail = findViewById(R.id.btVerificateEmail);
            btVerificateEmail.setVisibility(View.VISIBLE);
        }

        emailVerificatedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                btVerificateEmail.setVisibility(View.GONE);
            }
        };
        registerReceiver(emailVerificatedReceiver,new IntentFilter("email_verificated"));

        if (loggedUser.getUser().getAccountType().equals("Anonymous"))
        {
            logout_message = getResources().getString(R.string.logout_message_anonymous);
            logout_title = getResources().getString(R.string.logout_title_anonymous);
        }
        else {
            logout_message = getResources().getString(R.string.logout_message);
            logout_title = getResources().getString(R.string.logout_title);
        }
        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ivLoading = loadingDialog.findViewById(R.id.ivLoading);
        Glide.with(SettingsActivity.this).load(R.drawable.loading).into(ivLoading);

        alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.app_name));
        wv = new WebView(this);
        //wv.loadUrl("http://185.141.33.87/policy/terms_and_conditions_chatoon.html");
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        alert.setView(wv);
        alert.setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(emailVerificatedReceiver);
        super.onDestroy();
    }

    private void InitializeListeners() {
        btPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert = new AlertDialog.Builder(SettingsActivity.this);
                alert.setTitle(getResources().getString(R.string.app_name));
                wv = new WebView(SettingsActivity.this);
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        wv.loadUrl("www.google.com");
                    }
                });
                alert.setView(wv);
                alert.setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                wv.loadUrl("http://185.141.33.87/policy/privacy_policy_chatoon.html");
                alert.show();
            }
        });
        btTermsAndConditions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert = new AlertDialog.Builder(SettingsActivity.this);
                alert.setTitle(getResources().getString(R.string.app_name));
                wv = new WebView(SettingsActivity.this);
                wv.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        view.loadUrl(url);
                        return true;
                    }
                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        wv.loadUrl("www.google.com");
                    }
                });
                alert.setView(wv);
                alert.setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                wv.loadUrl("http://185.141.33.87/policy/terms_and_conditions_chatoon.html");
                alert.show();
            }
        });
        btOpenSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OssLicensesMenuActivity.setActivityTitle("Open Source Libraries");
                startActivity(new Intent(SettingsActivity.this, OssLicensesMenuActivity.class));
            }
        });
        if (!loggedUser.getUser().isIsEmailApproved() && loggedUser.getUser().getAccountType().equals("Email")){
            btVerificateEmail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    VerificateEmailFragment fragment = new VerificateEmailFragment(loggedUser);
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.settingsContainer,fragment,"settings_container")
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingsActivity.this.finish();
                onBackPressed();
            }
        });
        btLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new EZDialog.Builder(SettingsActivity.this)
                        .setTitle(logout_title)
                        .setMessage(logout_message)
                        .setPositiveBtnText(getResources().getString(R.string.logout))
                        .setNegativeBtnText(getResources().getString(R.string.cancel))
                        .setCancelableOnTouchOutside(false)
                        .OnPositiveClicked(new EZDialogListener() {
                            @Override
                            public void OnClick() {
                                if (loggedUser.getUser().getAccountType().equals("Anonymous")){
                                    loadingDialog.show();
                                    AnonymousLogout(false);
                                }
                                else
                                    Logout();
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
        });
    }

    @Override
    public void onBackPressed() {
        if (settingsContainer != null && settingsContainer.getChildCount() > 0)
            getSupportFragmentManager().popBackStack();
        else {
            SettingsActivity.this.finish();
            super.onBackPressed();
        }
    }

    public void Logout() {
        Log.d("tarja","first"+FirebaseInstanceId.getInstance().getId());
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

        Intent myIntent = new Intent(SettingsActivity.this, SplashActivity.class);
        SettingsActivity.this.startActivity(myIntent);
        SettingsActivity.this.finish();
    }

    private void AnonymousLogout(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(SettingsActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_AnonymousLogout);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<BasicResponse>() {
                }.getType();
                anonymousLogoutResponse = gson.fromJson(response, myType);
                if (anonymousLogoutResponse.isIsSuccess()){
                    Logout();
                }
                loadingDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tarja",error.getMessage());
                if (!useBackup){
                    AnonymousLogout(true);
                }
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
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }
}

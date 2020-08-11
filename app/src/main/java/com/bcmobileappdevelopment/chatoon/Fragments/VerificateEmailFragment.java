package com.bcmobileappdevelopment.chatoon.Fragments;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bcmobileappdevelopment.chatoon.GsonResponse.BasicResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.LoginResponse;
import com.bcmobileappdevelopment.chatoon.R;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerificateEmailFragment extends Fragment {

    public VerificateEmailFragment(LoginResponse loggedUserParam) {
        loggedUser = loggedUserParam;
    }

    View rootView;
    TextView btBack, btVerificate, btSendVerificationCode, tvSentMail;
    EditText etVerificationCode;
    Dialog loadingDialog;
    ImageView ivLoading;
    Gson gson;
    BasicResponse approveVerificationCodeResponse, sendVerificationCodeResponse;
    LoginResponse loggedUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_verificate_email, container, false);
        Initialize();
        InitializeListeners();
        return rootView;
    }

    private void Initialize() {
        btBack = rootView.findViewById(R.id.btBack);
        btVerificate = rootView.findViewById(R.id.btVerificate);
        etVerificationCode = rootView.findViewById(R.id.etVerificationCode);
        btSendVerificationCode = rootView.findViewById(R.id.btSendVerificationCode);
        tvSentMail = rootView.findViewById(R.id.tvSentMail);
        tvSentMail.setText(getResources().getString(R.string.sent_to_mail,loggedUser.getUser().getEmail()));

        loadingDialog = new Dialog(rootView.getContext());
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ivLoading = loadingDialog.findViewById(R.id.ivLoading);
        Glide.with(VerificateEmailFragment.this).load(R.drawable.loading).into(ivLoading);
    }

    private void InitializeListeners() {
        btSendVerificationCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendVerificationCode(false);
            }
        });
        btSendVerificationCode.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                btSendVerificationCode.setBackgroundColor(getResources().getColor(R.color.orange_pressed));
                if (event.getAction() == MotionEvent.ACTION_UP)
                    btSendVerificationCode.setBackgroundColor(getResources().getColor(R.color.orange));
                return false;
            }
        });
        btVerificate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etVerificationCode.length() == 6){
                    loadingDialog.show();
                    ApproveVerificationCode(false);
                }
            }
        });
        btVerificate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (etVerificationCode.length() == 6){
                    btVerificate.setBackgroundColor(getResources().getColor(R.color.transparent_green_pressed));
                    if (event.getAction() == MotionEvent.ACTION_UP)
                        btVerificate.setBackgroundColor(getResources().getColor(R.color.transparent_green));
                }
                return false;
            }
        });
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        btBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                btBack.setBackgroundColor(getResources().getColor(R.color.transparent_red_pressed));
                if (event.getAction() == MotionEvent.ACTION_UP)
                    btBack.setBackgroundColor(getResources().getColor(R.color.transparent_red));
                return false;
            }
        });
        etVerificationCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 6)
                    btVerificate.setBackgroundColor(getResources().getColor(R.color.transparent_green));
                else
                    btVerificate.setBackgroundColor(getResources().getColor(R.color.transparent_circle));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void ApproveVerificationCode(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(rootView.getContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_ApproveVerificationCode);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<BasicResponse>() {
                }.getType();
                approveVerificationCodeResponse = gson.fromJson(response, myType);
                Log.d("tarja","onResponse");
                if (approveVerificationCodeResponse.isIsSuccess()){
                    loggedUser.getUser().setIsEmailApproved(true);
                    Hawk.put("loggedUser",loggedUser);
                    Intent intent = new Intent("email_verificated");
                    getActivity().sendBroadcast(intent);
                    getActivity().getSupportFragmentManager().popBackStack();
                    Toasty.success(getContext(), getResources().getString(R.string.email_verificated), Toast.LENGTH_LONG, true).show();
                }
                else if(approveVerificationCodeResponse.getMessage().equals("wait")){
                    Toasty.warning(getContext(), getResources().getString(R.string.wait_1_minue_to_approve_verification_code), Toast.LENGTH_LONG, true).show();
                }
                else if(approveVerificationCodeResponse.getMessage().equals("invalid_code")){
                    Toasty.error(getContext(), getResources().getString(R.string.invalid_verification_code), Toast.LENGTH_LONG, true).show();
                }
                loadingDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tarja",error.getMessage());
                if (!useBackup){
                    ApproveVerificationCode(true);
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
                params.put("code", etVerificationCode.getText().toString());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void SendVerificationCode(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(rootView.getContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_SendVerificationCode);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<BasicResponse>() {
                }.getType();
                sendVerificationCodeResponse = gson.fromJson(response, myType);
                if (sendVerificationCodeResponse.isIsSuccess()){
                    Toasty.success(getContext(), getResources().getString(R.string.verification_code_sent_again), Toast.LENGTH_LONG, true).show();
                }
                else if(sendVerificationCodeResponse.getMessage().equals("wait"))
                {
                    Toasty.warning(getContext(), getResources().getString(R.string.wait_30_seconds_to_send_again), Toast.LENGTH_LONG, true).show();
                }
                loadingDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup){
                    SendVerificationCode(true);
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
                params.put("language", Locale.getDefault().getLanguage());
                params.put("mailTo", loggedUser.getUser().getEmail());
                params.put("sendJson", String.valueOf(true));
                params.put("nameSurname", loggedUser.getUser().getUsername());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }
}

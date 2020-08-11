package com.bcmobileappdevelopment.chatoon.Fragments;


import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
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
import java.util.Map;

import es.dmoral.toasty.Toasty;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchCredentialsFragment extends Fragment {

    public SearchCredentialsFragment(LoginResponse loggedUserParam) {
        loggedUser = loggedUserParam;
    }

    View rootView;
    NumberPicker npAgeSmall, npAgeBig;
    boolean m_clicked = false, f_clicked = false, searchCredentialsDialogNeedToUpdate = false, userFClicked = false, userMClicked = false;
    String updateLookingGender = "", updateLookingAgeRange = "";
    int updatingLookingAgeSmall, updatingLookingAgeBig, userLookingAgeSmall, userLookingAgeBig;
    ImageView ivMaleBack, ivFemaleBack, ivLoading;
    TextView btMale, btFemale, btUpdate, btBack;
    Gson gson;
    BasicResponse updateSearchCredentialsResponse;
    LoginResponse loggedUser;
    Dialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_search_credentials, container, false);
        Initialize();
        InitializeListeners();
        SetSearchCredentials();
        return rootView;
    }

    private void Initialize() {
        npAgeSmall = rootView.findViewById(R.id.npAgeSmall);
        npAgeBig = rootView.findViewById(R.id.npAgeBig);
        btMale = rootView.findViewById(R.id.btMale);
        btFemale = rootView.findViewById(R.id.btFemale);
        ivMaleBack = rootView.findViewById(R.id.ivMaleBack);
        ivFemaleBack = rootView.findViewById(R.id.ivFemaleBack);
        btUpdate = rootView.findViewById(R.id.btUpdate);
        btBack = rootView.findViewById(R.id.btBack);
        loadingDialog = new Dialog(rootView.getContext());
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ivLoading = loadingDialog.findViewById(R.id.ivLoading);
        Glide.with(SearchCredentialsFragment.this).load(R.drawable.loading).into(ivLoading);
    }

    private void InitializeListeners() {
        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchCredentialsDialogNeedToUpdate){
                    updateLookingAgeRange = npAgeSmall.getValue()+"-"+npAgeBig.getValue();
                    if (m_clicked && f_clicked)
                        updateLookingGender = "MF";
                    else if (m_clicked){
                        updateLookingGender = "M";
                    }
                    else
                        updateLookingGender = "F";
                    loadingDialog.show();
                    UpdateSearchCredentials(false);
                }
            }
        });
        btUpdate.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (searchCredentialsDialogNeedToUpdate){
                    btUpdate.setBackgroundColor(getResources().getColor(R.color.transparent_green_pressed));
                    if (event.getAction() == MotionEvent.ACTION_UP)
                        btUpdate.setBackgroundColor(getResources().getColor(R.color.transparent_green));
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

        npAgeBig.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updatingLookingAgeBig = newVal;
                CheckEditSearchCredentialsDialogNeedToUpdate();
            }
        });
        npAgeSmall.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                npAgeBig.setMinValue(newVal);
                if (npAgeBig.getValue() < newVal)
                    npAgeBig.setValue(newVal);
                updatingLookingAgeSmall = newVal;
                CheckEditSearchCredentialsDialogNeedToUpdate();
            }
        });

        ivMaleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_clicked){
                    ivMaleBack.setBackground(getResources().getDrawable(R.drawable.circle));
                    m_clicked = false;
                }
                else{
                    ivMaleBack.setBackground(getResources().getDrawable(R.drawable.blue_circle));
                    m_clicked = true;
                }
                CheckEditSearchCredentialsDialogNeedToUpdate();
            }
        });

        ivFemaleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (f_clicked){
                    ivFemaleBack.setBackground(getResources().getDrawable(R.drawable.circle));
                    f_clicked = false;
                }
                else{
                    ivFemaleBack.setBackground(getResources().getDrawable(R.drawable.pink_circle));
                    f_clicked = true;
                }
                CheckEditSearchCredentialsDialogNeedToUpdate();
            }
        });

        btMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivMaleBack.callOnClick();
            }
        });
        btFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivFemaleBack.callOnClick();
            }
        });
    }

    private void CheckEditSearchCredentialsDialogNeedToUpdate(){
        if (!m_clicked && !f_clicked){
            btUpdate.setBackgroundColor(getResources().getColor(R.color.transparent_circle));
            searchCredentialsDialogNeedToUpdate = false;
        }
        else if (userLookingAgeSmall != updatingLookingAgeSmall || userLookingAgeBig != updatingLookingAgeBig || (userMClicked != m_clicked || userFClicked != f_clicked)){
            searchCredentialsDialogNeedToUpdate = true;
            btUpdate.setBackgroundColor(getResources().getColor(R.color.transparent_green));
        }
        else{
            btUpdate.setBackgroundColor(getResources().getColor(R.color.transparent_circle));
            searchCredentialsDialogNeedToUpdate = false;
        }
    }

    private void SetSearchCredentials(){
        userLookingAgeSmall = Integer.valueOf(loggedUser.getUser().getLookingAgeRange().substring(0,2));
        userLookingAgeBig = Integer.valueOf(loggedUser.getUser().getLookingAgeRange().substring(3,5));

        switch (loggedUser.getUser().getLookingGender()){
            case "M":
                ivMaleBack.setBackground(getResources().getDrawable(R.drawable.blue_circle));
                m_clicked = true;
                break;
            case "F":
                ivFemaleBack.setBackground(getResources().getDrawable(R.drawable.pink_circle));
                f_clicked = true;
                break;
            case "MF":
                ivMaleBack.setBackground(getResources().getDrawable(R.drawable.blue_circle));
                ivFemaleBack.setBackground(getResources().getDrawable(R.drawable.pink_circle));
                m_clicked = true;
                f_clicked = true;
                break;
        }
        npAgeSmall.setValue(userLookingAgeSmall);
        npAgeBig.setValue(userLookingAgeBig);
        npAgeBig.setMinValue(npAgeSmall.getValue());
        updatingLookingAgeSmall = userLookingAgeSmall;
        updatingLookingAgeBig = userLookingAgeBig;
        userFClicked = f_clicked;
        userMClicked = m_clicked;
    }

    private void UpdateSearchCredentials(final boolean useBackup) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_UpdateSearchCredentials);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<BasicResponse>() {
                }.getType();
                updateSearchCredentialsResponse = gson.fromJson(response, myType);
                if (updateSearchCredentialsResponse.isIsSuccess()) {
                    loggedUser.getUser().setLookingAgeRange(updateLookingAgeRange);
                    loggedUser.getUser().setLookingGender(updateLookingGender);
                    Hawk.put("loggedUser",loggedUser);
                    Intent intent = new Intent("search_credentials_updated");
                    getActivity().sendBroadcast(intent);
                    getActivity().getSupportFragmentManager().popBackStack();
                    Toasty.success(getContext(), getResources().getString(R.string.your_search_credentials_updated), Toast.LENGTH_LONG, true).show();
                }
                loadingDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    UpdateSearchCredentials(true);
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
                params.put("lookingAgeRange", updateLookingAgeRange);
                params.put("lookingGender", updateLookingGender);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 10, 1.0f));
        mRequestQueue.add(stringRequest);
    }
}
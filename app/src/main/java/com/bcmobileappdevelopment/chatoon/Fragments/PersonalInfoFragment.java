package com.bcmobileappdevelopment.chatoon.Fragments;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.RawRes;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.bcmobileappdevelopment.chatoon.Adapters.CountryAdapter;
import com.bcmobileappdevelopment.chatoon.GsonResponse.BasicResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.LoginResponse;
import com.bcmobileappdevelopment.chatoon.HelperClass.Countries;
import com.bcmobileappdevelopment.chatoon.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.hawk.Hawk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import es.dmoral.toasty.Toasty;

/**
 * A simple {@link Fragment} subclass.
 */
public class PersonalInfoFragment extends Fragment {

    public PersonalInfoFragment(LoginResponse loggedUserParam) {
        loggedUser = loggedUserParam;
    }

    View rootView;
    LoginResponse loggedUser;
    EditText etUsername;
    NumberPicker npAge;
    ImageView ivCountry,ivMaleBack, ivFemaleBack, ivLoading;
    TextView tvCountryName, btMale, btFemale, btUpdate, btBack;
    boolean m_clicked = false, f_clicked = false, searchCredentialsDialogNeedToUpdate = false, isFirstTime = true;
    Locale enUsLocale = new Locale("en_US");
    RequestOptions requestOptions;
    String updateGender, userGender, updateUsername, userUsername, userFlagCode, updateFlagCode, userCountry, updateCountry, json;
    int updateAge, userAge, countryPosition;
    RecyclerView countryRecyclerView;
    Dialog countryListDialog;
    Countries countriesObject;
    Gson gson;
    ArrayList<String> countryNames;
    HashMap<String,String> countryMap,alpha3Map;
    BasicResponse updatePersonalInfoResponse;
    BaseQuickAdapter countryAdapter;
    Countries.CountriesBean clickedCountry;
    ConstraintLayout clUsername;
    Dialog loadingDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_personal_info, container, false);
        Initialize();
        InitializeCountryDialog();
        InitializeListeners();
        SetUserInfo();
        return rootView;
    }

    private void InitializeCountryDialog() {
        countryListDialog = new Dialog(getContext());
        countryListDialog.setContentView(R.layout.dialog_country_list);
        countryListDialog.setCancelable(true);
        countryListDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        countryListDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        countryRecyclerView = countryListDialog.findViewById(R.id.rvCountryList);
    }

    private void Initialize() {
        etUsername = rootView.findViewById(R.id.etUsername);
        npAge = rootView.findViewById(R.id.npAge);
        btUpdate = rootView.findViewById(R.id.btUpdate);
        btBack = rootView.findViewById(R.id.btBack);
        btMale = rootView.findViewById(R.id.btMale);
        btFemale = rootView.findViewById(R.id.btFemale);
        ivMaleBack = rootView.findViewById(R.id.ivMaleBack);
        ivFemaleBack = rootView.findViewById(R.id.ivFemaleBack);
        tvCountryName = rootView.findViewById(R.id.tvCountryName);
        ivCountry = rootView.findViewById(R.id.ivCountry);
        requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL);
        clUsername = rootView.findViewById(R.id.clUsername);

        loadingDialog = new Dialog(rootView.getContext());
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ivLoading = loadingDialog.findViewById(R.id.ivLoading);
        Glide.with(PersonalInfoFragment.this).load(R.drawable.loading).into(ivLoading);
    }

    public boolean isAlphaUsername(String name) {
        return name.matches("[a-zA-Z0-9_.\\-]+");
    }

    private void InitializeListeners() {
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateUsername = s.toString();
                CheckNeedToUpdate();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        btUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchCredentialsDialogNeedToUpdate){
                    if (!isAlphaUsername(updateUsername))
                    {
                        Toasty.error(getContext(), getResources().getString(R.string.enter_only_english_characters), Toast.LENGTH_LONG, true).show();
                        etUsername.requestFocus();
                    }
                    else if(updateUsername.length() < 3 || updateUsername.length()> 40){
                        Toasty.error(getContext(), getResources().getString(R.string.username_between_3_and_40), Toast.LENGTH_LONG, true).show();
                        etUsername.requestFocus();
                    }
                    else {
                        updateAge = npAge.getValue();
                        if (m_clicked)
                            updateGender = "M";
                        else if (f_clicked)
                            updateGender = "F";
                        if (loggedUser.getUser().getAccountType().equals("Anonymous"))
                            updateUsername = "";
                        loadingDialog.show();
                        UpdatePersonalInfo(false);
                    }
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
        ivCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowCountryDialog();
            }
        });
        npAge.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                updateAge = newVal;
                CheckNeedToUpdate();
            }
        });
        btMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(f_clicked) {
                    ivMaleBack.setBackground(getResources().getDrawable(R.drawable.blue_circle));
                    ivFemaleBack.setBackground(getResources().getDrawable(R.drawable.circle));
                    m_clicked = true;
                    f_clicked = false;
                    updateGender = "M";
                    CheckNeedToUpdate();
                }
            }
        });
        ivMaleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btMale.callOnClick();
            }
        });
        btFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(m_clicked) {
                    ivMaleBack.setBackground(getResources().getDrawable(R.drawable.circle));
                    ivFemaleBack.setBackground(getResources().getDrawable(R.drawable.pink_circle));
                    m_clicked = false;
                    f_clicked = true;
                    updateGender = "F";
                    CheckNeedToUpdate();
                }
            }
        });
        ivFemaleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btFemale.callOnClick();
            }
        });
    }

    private void SetUserInfo() {
        userUsername = loggedUser.getUser().getUsername();
        userAge = loggedUser.getUser().getAge();
        userGender = loggedUser.getUser().getGender();
        userFlagCode = loggedUser.getUser().getFlagCode();
        userCountry = loggedUser.getUser().getCountry();

        if (loggedUser.getUser().getAccountType().equals("Anonymous"))
        {
            clUsername.setVisibility(View.GONE);
            etUsername.setText("Anonymous");
            updateUsername = "Anonymous";
        }
        else{
            etUsername.setText(userUsername);
            updateUsername = userUsername;
        }

        updateAge = userAge;
        updateGender = userGender;
        updateFlagCode = userFlagCode;
        updateCountry = userCountry;


        npAge.setValue(userAge);
        if (userGender.equals("M")){
            ivMaleBack.setBackground(getResources().getDrawable(R.drawable.blue_circle));
            m_clicked = true;
        }
        else {
            ivFemaleBack.setBackground(getResources().getDrawable(R.drawable.pink_circle));
            f_clicked = true;
        }
        tvCountryName.setText(userCountry);
        Glide.with(getContext()).load(getResources().getIdentifier(userFlagCode.toLowerCase(enUsLocale), "drawable", getContext().getPackageName())).apply(requestOptions).into(ivCountry);
    }

    private void CheckNeedToUpdate(){
        if ((!m_clicked && !f_clicked) || updateUsername.length() < 3){
            btUpdate.setBackgroundColor(getResources().getColor(R.color.transparent_circle));
            searchCredentialsDialogNeedToUpdate = false;
        }
        else if((!userUsername.equals(updateUsername) && !loggedUser.getUser().getAccountType().equals("Anonymous")) || !userGender.equals(updateGender) || userAge != updateAge || !userFlagCode.equals(updateFlagCode)){
            searchCredentialsDialogNeedToUpdate = true;
            btUpdate.setBackgroundColor(getResources().getColor(R.color.transparent_green));
        }
        else{
            btUpdate.setBackgroundColor(getResources().getColor(R.color.transparent_circle));
            searchCredentialsDialogNeedToUpdate = false;
        }
    }

    private void ShowCountryDialog() {
        if (isFirstTime){
            ConsumeCountriesJson();
            Log.d("tarja","countryPos->"+countryPosition );
            InitializecountryRecyclerView();
            isFirstTime = false;
            countryRecyclerView.scrollToPosition(countryPosition-1);
        }
        countryListDialog.show();
    }

    private void InitializecountryRecyclerView(){
        countryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        countryAdapter = new CountryAdapter(R.layout.list_item_country, countriesObject.getCountries());
        countryAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        countryAdapter.setEnableLoadMore(true);
        countryAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                clickedCountry = countriesObject.getCountries().get(position);
                updateFlagCode = clickedCountry.getCountryCode().toLowerCase(enUsLocale);
                updateCountry = clickedCountry.getName();
                Glide.with(getContext()).load(getResources().getIdentifier(updateFlagCode.toLowerCase(enUsLocale), "drawable", getContext().getPackageName())).into(ivCountry);
                tvCountryName.setText(updateCountry);
                countryListDialog.dismiss();
                CheckNeedToUpdate();
            }
        });
        countryRecyclerView.setAdapter(countryAdapter);
    }

    private void ConsumeCountriesJsonOld() {
        try {
            InputStream is = getResources().getAssets().open("countries.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
        Type myType = new TypeToken<Countries>() {
        }.getType();
        countriesObject = gson.fromJson(json, myType);

        countryNames = new ArrayList<>();
        countryMap = new HashMap<>();
        alpha3Map = new HashMap<>();

        for(Countries.CountriesBean country : countriesObject.getCountries()){
            countryMap.put(country.getName(),country.getCountryCode());
            alpha3Map.put(country.getCountryCode(),country.getName());
            countryNames.add(country.getName());
            if (country.getCountryCode().toLowerCase(enUsLocale).equals(userFlagCode))
                countryPosition = country.getID();
        }
    }

    public String readRawResource(@RawRes int res) {
        return readStream(PersonalInfoFragment.this.getResources().openRawResource(res));
    }

    private String readStream(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void ConsumeCountriesJson() {
        json = readRawResource(R.raw.countries);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.create();
        Type myType = new TypeToken<Countries>() {
        }.getType();
        countriesObject = gson.fromJson(json, myType);

        countryNames = new ArrayList<>();
        countryMap = new HashMap<>();
        alpha3Map = new HashMap<>();

        for(Countries.CountriesBean country : countriesObject.getCountries()){
            countryMap.put(country.getName(),country.getCountryCode());
            alpha3Map.put(country.getCountryCode(),country.getName());
            countryNames.add(country.getName());
            if (country.getCountryCode().toLowerCase(enUsLocale).equals(userFlagCode))
                countryPosition = country.getID();
        }
    }

    private void UpdatePersonalInfo(final boolean useBackup) {
        RequestQueue mRequestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_UpdatePersonalInfo);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<BasicResponse>() {
                }.getType();
                updatePersonalInfoResponse = gson.fromJson(response, myType);
                if (updatePersonalInfoResponse.isIsSuccess()) {
                    if (!loggedUser.getUser().getAccountType().equals("Anonymous"))
                        loggedUser.getUser().setUsername(updateUsername);
                    loggedUser.getUser().setAge(updateAge);
                    loggedUser.getUser().setGender(updateGender);
                    loggedUser.getUser().setFlagCode(updateFlagCode);
                    loggedUser.getUser().setCountry(updateCountry);
                    Hawk.put("loggedUser",loggedUser);
                    Intent intent = new Intent("personal_info_updated");
                    getActivity().sendBroadcast(intent);
                    getActivity().getSupportFragmentManager().popBackStack();
                    Toasty.success(getContext(), getResources().getString(R.string.your_personal_info_updated), Toast.LENGTH_LONG, true).show();
                }
                loadingDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    UpdatePersonalInfo(true);
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
                params.put("username", updateUsername);
                params.put("gender", updateGender);
                params.put("age", String.valueOf(updateAge));
                params.put("country", updateCountry);
                params.put("flagCode", updateFlagCode);

                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000, 10, 1.0f));
        mRequestQueue.add(stringRequest);
    }
}

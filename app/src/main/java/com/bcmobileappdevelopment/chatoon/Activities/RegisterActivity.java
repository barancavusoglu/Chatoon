package com.bcmobileappdevelopment.chatoon.Activities;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.bcmobileappdevelopment.chatoon.Fragments.PersonalInfoFragment;
import com.bcmobileappdevelopment.chatoon.GsonResponse.BasicResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.FacebookLoginResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.LoginResponse;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.bcmobileappdevelopment.chatoon.Adapters.CountryAdapter;
import com.bcmobileappdevelopment.chatoon.HelperClass.Countries;
import com.bcmobileappdevelopment.chatoon.R;
import com.orhanobut.hawk.Hawk;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;

import es.dmoral.toasty.Toasty;

public class RegisterActivity extends AppCompatActivity {

    ImageView btClose, ivMaleBack, ivFemaleBack, ivFlag, ivFacebookPic, ivLoading;
    TextView btMale, btFemale,btSave, tvWelcome, tvPleaseEnterInfo, tvAge, tvCountryName, tvFacebookPic, tvAcceptPolicy;
    private ConstraintLayout constraint;
    private ConstraintSet constraintSet = new ConstraintSet();
    int currentPage = 1, age, countryPosition;
    String gender = "",lookingGender, lookingAge, alpha3Code, countryName, json = "", registerType, facebookAccessToken, profilePic = "";
    NumberPicker npAge, npAge2;
    boolean m_clicked = false, f_clicked = false, isFirstTime = true;
    Gson gson;
    Countries countriesObject;
    HashMap<String,String> countryMap,alpha3Map;
    Dialog countryListDialog, loadingDialog;
    ArrayList<String> countryNames;
    RecyclerView recyclerView;
    BaseQuickAdapter mQuickAdapter;
    Countries.CountriesBean clickedCountry;
    Locale enUsLocale = new Locale("en_US");
    LoginResponse loginResponse;
    MaterialEditText etUsername, etEmail, etPassword, etPassword2;
    ConstraintLayout clUsername, clEmail;
    FacebookLoginResponse facebookRegisterData;
    Switch swFacebookSwitch;
    RequestOptions requestOptions;
    FirebaseAuth mAuth;
    BasicResponse checkEmailUsernameDistinctResponse;
    AlertDialog.Builder alert;
    WebView wv;
    CheckBox cbAcceptPolicy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Initialize();
        InitializeListeners();
        InitializePolicy();
    }

    private void InitializePolicy() {
        tvAcceptPolicy = findViewById(R.id.tvAcceptPolicy);
        cbAcceptPolicy = findViewById(R.id.cbAcceptPolicy);
        SpannableString ss = new SpannableString(getResources().getString(R.string.accept_policy));
        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                ShowTermsAndConditions();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.RED);
            }
        };
        ClickableSpan clickableSpan2 = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                ShowPrivacyPolicy();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(Color.RED);
            }
        };
        if (Locale.getDefault().getLanguage().equals("tr")){
            ss.setSpan(clickableSpan1, 0, 19, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(clickableSpan2, 25, 44, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        else {
            ss.setSpan(clickableSpan1, 26, 44, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            ss.setSpan(clickableSpan2, 49, 63, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        //I want THIS and THIS to be clickable
        //I accept Chatoon Terms &amp; Conditions and read the Privacy Policy.
        //Şartlar ve Koşullar\'ı kabul ediyorum ve Gizlilik Politikası\'nı okudum.
        tvAcceptPolicy.setText(ss);
        tvAcceptPolicy.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void Initialize() {
        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        registerType = intent.getStringExtra("register_type");
        if (registerType.equals("facebook")){
            facebookAccessToken = intent.getStringExtra("access_token");
            facebookRegisterData = Hawk.get("facebook_register_data");
        }
        btClose = findViewById(R.id.btClose);
        ivMaleBack = findViewById(R.id.ivMaleBack);
        btMale = findViewById(R.id.btMale);
        ivFemaleBack = findViewById(R.id.ivFemaleBack);
        btFemale = findViewById(R.id.btFemale);
        btSave = findViewById(R.id.btSave);
        tvWelcome = findViewById(R.id.tvWelcome);
        tvPleaseEnterInfo = findViewById(R.id.tvPleaseEnterInfo);
        tvAge = findViewById(R.id.tvAge);
        npAge = findViewById(R.id.npAge);
        npAge2 = findViewById(R.id.npAge2);
        ivFlag = findViewById(R.id.ivFlag);
        tvCountryName = findViewById(R.id.tvCountryName);
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        clEmail = findViewById(R.id.clEmail);
        clUsername = findViewById(R.id.clUsername);
        etPassword = findViewById(R.id.etPassword);
        etPassword2 = findViewById(R.id.etPassword2);
        swFacebookSwitch = findViewById(R.id.swFacebookPic);
        ivFacebookPic = findViewById(R.id.ivFacebookPic);
        tvFacebookPic = findViewById(R.id.tvFacebookPic);

        countryListDialog = new Dialog(this);
        countryListDialog.setContentView(R.layout.dialog_country_list);
        countryListDialog.setCancelable(true);
        countryListDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        countryListDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        recyclerView = countryListDialog.findViewById(R.id.rvCountryList);

        requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new CircleCrop());

        alpha3Code = Locale.getDefault().getISO3Country();
        if (alpha3Code.equals("TUR"))
            alpha3Code = "turk";
        ConsumeJson();
        countryName = alpha3Map.get(alpha3Code.toUpperCase());
        UpdateCountry();

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ivLoading = loadingDialog.findViewById(R.id.ivLoading);
        Glide.with(RegisterActivity.this).load(R.drawable.loading).into(ivLoading);
    }

    private void InitializeListeners() {
        btClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegisterActivity.this.finish();
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
        ivMaleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage == 1){
                    gender = "M";
                    ivFemaleBack.setBackground(getResources().getDrawable(R.drawable.circle));
                    ivMaleBack.setBackground(getResources().getDrawable(R.drawable.blue_circle));
                }
                else
                {
                    lookingGender = "M";
                    if (m_clicked){
                        ivMaleBack.setBackground(getResources().getDrawable(R.drawable.circle));
                        m_clicked = false;
                    }
                    else{
                        ivMaleBack.setBackground(getResources().getDrawable(R.drawable.blue_circle));
                        m_clicked = true;
                    }
                }
            }
        });
        btMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivMaleBack.callOnClick();
            }
        });
        ivFemaleBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage == 1){
                    gender = "F";
                    ivMaleBack.setBackground(getResources().getDrawable(R.drawable.circle));
                    ivFemaleBack.setBackground(getResources().getDrawable(R.drawable.pink_circle));
                }
                else
                {
                    lookingGender = "F";
                    if (f_clicked)
                    {
                        ivFemaleBack.setBackground(getResources().getDrawable(R.drawable.circle));
                        f_clicked = false;
                    }
                    else
                    {
                        ivFemaleBack.setBackground(getResources().getDrawable(R.drawable.pink_circle));
                        f_clicked = true;
                    }
                }
            }
        });
        btFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivFemaleBack.callOnClick();
            }
        });
        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage == 1){
                    if (gender.equals("")){
                        Toasty.warning(RegisterActivity.this, getResources().getString(R.string.choose_gender), Toast.LENGTH_LONG, true).show();
                    }
                    else {
                        PrepareSecondPage();
                        Animation();
                    }
                }
                else if(currentPage == 2){
                    lookingGender = "";
                    if (m_clicked)
                        lookingGender = "M";
                    if (f_clicked)
                        lookingGender += "F";
                    if (!m_clicked && !f_clicked)
                        lookingGender = "MF";
                    lookingAge = npAge.getValue() +"-"+ npAge2.getValue();
                    if (registerType.equals("anonymous")){
                        if (!cbAcceptPolicy.isChecked())
                        {
                            Toasty.warning(RegisterActivity.this, getResources().getString(R.string.please_accept_policy), Toast.LENGTH_LONG, true).show();
                        }
                        else {
                            loadingDialog.show();
                            RegisterAnonymous(false);
                        }
                    }
                    else if(registerType.equals("email") || registerType.equals("facebook")){
                        Animation2();
                        PrepareThirdPage();
                    }
                }
                else if(currentPage == 3){
                    Log.d("tarja","evet şu an 3 tesin");
                    if (registerType.equals("facebook")){
                        if (!isFacebookUserDonkey()){
                            if (!cbAcceptPolicy.isChecked()){
                                Toasty.warning(RegisterActivity.this, getResources().getString(R.string.please_accept_policy), Toast.LENGTH_LONG, true).show();
                            }
                            else {
                                Log.d("tarja","kontrol ediyor distinct mi diye");
                                CheckEmailUsernameDistinct(false);
                            }
                        }
                    }
                    else if(registerType.equals("email")){
                        if (!isEmailUserDonkey())
                            CheckEmailUsernameDistinct(false);
                    }
                }
                else if(currentPage == 4){
                    if (!isEmailUserPasswordDonkey()){
                        if (!cbAcceptPolicy.isChecked()){
                            Toasty.warning(RegisterActivity.this, getResources().getString(R.string.please_accept_policy), Toast.LENGTH_LONG, true).show();
                        }
                        else {
                            loadingDialog.show();
                            RegisterEmail(false);
                        }
                    }
                }
            }
        });
        btSave.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (currentPage == 1){
                    btSave.setBackgroundColor(getResources().getColor(R.color.transparent_blue_pressed));
                    if (event.getAction() == MotionEvent.ACTION_UP)
                        btSave.setBackgroundColor(getResources().getColor(R.color.transparent_blue));
                }
                else if(currentPage == 2) {
                    if (registerType.equals("anonymous")){
                        btSave.setBackgroundColor(getResources().getColor(R.color.button_green_pressed));
                        if (event.getAction() == MotionEvent.ACTION_UP)
                            btSave.setBackgroundColor(getResources().getColor(R.color.button_green));
                    }
                    else if(registerType.equals("email") || registerType.equals("facebook")){
                        btSave.setBackgroundColor(getResources().getColor(R.color.transparent_blue_pressed));
                        if (event.getAction() == MotionEvent.ACTION_UP)
                            btSave.setBackgroundColor(getResources().getColor(R.color.transparent_blue));
                    }
                }
                else if(currentPage == 3){
                    if (registerType.equals("facebook")){
                        btSave.setBackgroundColor(getResources().getColor(R.color.button_green_pressed));
                        if (event.getAction() == MotionEvent.ACTION_UP)
                            btSave.setBackgroundColor(getResources().getColor(R.color.button_green));
                    }
                    else if(registerType.equals("email")){
                        btSave.setBackgroundColor(getResources().getColor(R.color.transparent_blue_pressed));
                        if (event.getAction() == MotionEvent.ACTION_UP)
                            btSave.setBackgroundColor(getResources().getColor(R.color.transparent_blue));
                    }
                }
                else if(currentPage == 4){
                    btSave.setBackgroundColor(getResources().getColor(R.color.button_green_pressed));
                    if (event.getAction() == MotionEvent.ACTION_UP)
                        btSave.setBackgroundColor(getResources().getColor(R.color.button_green));
                }
                return false;
            }
        });
        npAge.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                npAge2.setMinValue(newVal);
                if (npAge2.getValue() < newVal)
                    npAge2.setValue(newVal);
            }
        });
        ivFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowCountryDialog();
            }
        });
        tvCountryName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivFlag.callOnClick();
            }
        });

        swFacebookSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    ivFacebookPic.setImageAlpha(255);
                    profilePic = facebookRegisterData.getProfile_pic();
                }
                else {
                    ivFacebookPic.setImageAlpha(128);
                    profilePic = "";
                }
            }
        });
    }

    private boolean isInvalidUsername() {
        String name = etUsername.getText().toString();
        if (name.length() < getResources().getInteger(R.integer.username_length_min) || name.length() > getResources().getInteger(R.integer.username_length_max)){
            Toasty.warning(RegisterActivity.this, getResources().getString(R.string.invalid_length,getResources().getString(R.string.username)), Toast.LENGTH_LONG, true).show();
            return true;
        }
        if (!name.matches("[a-zA-Z0-9_.\\-]+")){
            Toasty.warning(RegisterActivity.this, getResources().getString(R.string.invalid_input,getResources().getString(R.string.username)), Toast.LENGTH_LONG, true).show();
            return true;
        }
        return false;
    }

    private boolean isInvalidEmail() {
        String mail = etEmail.getText().toString();
        if (mail.length() < getResources().getInteger(R.integer.email_length_min) || mail.length() > getResources().getInteger(R.integer.email_length_max)){
            Toasty.warning(RegisterActivity.this, getResources().getString(R.string.invalid_length,getResources().getString(R.string.email)), Toast.LENGTH_LONG, true).show();
            return true;
        }
        if (!mail.matches("[a-zA-Z0-9\\-@_.]+")){
            Toasty.warning(RegisterActivity.this, getResources().getString(R.string.invalid_input,getResources().getString(R.string.email)), Toast.LENGTH_LONG, true).show();
            return true;
        }
        return false;
    }

    private boolean isInvalidPassword() {
        String password = etPassword.getText().toString();
        String passwordAgain = etPassword2.getText().toString();

        if (password.length() < getResources().getInteger(R.integer.password_length_min) || password.length() > getResources().getInteger(R.integer.password_length_max)){
            Toasty.warning(RegisterActivity.this, getResources().getString(R.string.invalid_length,getResources().getString(R.string.password)), Toast.LENGTH_LONG, true).show();
            return true;
        }
        if (!password.matches("[a-zA-z0-9-!'^+%&/()=?_*]+")){
            Toasty.warning(RegisterActivity.this, getResources().getString(R.string.invalid_input,getResources().getString(R.string.password)), Toast.LENGTH_LONG, true).show();
            return true;
        }
        if (passwordAgain.length() < getResources().getInteger(R.integer.password_length_min) || passwordAgain.length() > getResources().getInteger(R.integer.password_length_max)){
            Toasty.warning(RegisterActivity.this, getResources().getString(R.string.invalid_length,getResources().getString(R.string.password_again)), Toast.LENGTH_LONG, true).show();
            return true;
        }
        if (!password.equals(passwordAgain)){
            Toasty.warning(RegisterActivity.this, getResources().getString(R.string.passwords_doesnt_match), Toast.LENGTH_LONG, true).show();
            return true;
        }
        return false;
    }

    private boolean isFacebookUserDonkey(){
        return isInvalidUsername();
    }

    private boolean isEmailUserDonkey(){
        return (isInvalidUsername() || isInvalidEmail());
    }

    private boolean isEmailUserPasswordDonkey(){
        return isInvalidPassword();
    }

    private void InitializeRecyclerView(){
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mQuickAdapter = new CountryAdapter(R.layout.list_item_country, countriesObject.getCountries());
        mQuickAdapter.openLoadAnimation(BaseQuickAdapter.ALPHAIN);
        mQuickAdapter.setEnableLoadMore(true);
        mQuickAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                clickedCountry = countriesObject.getCountries().get(position);
                alpha3Code = clickedCountry.getCountryCode();
                countryName = clickedCountry.getName();
                UpdateCountry();
                countryListDialog.dismiss();
            }
        });
        recyclerView.setAdapter(mQuickAdapter);
    }

    private void UpdateCountry(){
        Glide.with(RegisterActivity.this).load(getResources().getIdentifier(alpha3Code.toLowerCase(enUsLocale), "drawable", getPackageName())).into(ivFlag);
        tvCountryName.setText(countryName);
    }

    private void ConsumeJsonOld() {
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
            if (country.getCountryCode().compareToIgnoreCase(alpha3Code) == 0)
                countryPosition = country.getID();
        }
    }

    public String readRawResource(@RawRes int res) {
        return readStream(RegisterActivity.this.getResources().openRawResource(res));
    }

    private String readStream(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    private void ConsumeJson() {
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
            if (country.getCountryCode().compareToIgnoreCase(alpha3Code) == 0)
                countryPosition = country.getID();
        }
    }

    private void ShowCountryDialog() {
        if (isFirstTime){
            InitializeRecyclerView();
            isFirstTime = false;
            recyclerView.scrollToPosition(countryPosition-1);
        }
        countryListDialog.show();
    }

    private void PrepareSecondPage(){
        currentPage = 2;
        age = npAge.getValue();
        tvWelcome.setText(getResources().getString(R.string.people_you));
        tvPleaseEnterInfo.setText(getResources().getString(R.string.want_to_chat_with));
        tvAge.setText(getResources().getString(R.string.age_range));
        if (registerType.equals("anonymous")){
            btSave.setText(getResources().getString(R.string.start_chatting));
            btSave.setBackgroundColor(getResources().getColor(R.color.button_green));
            tvAcceptPolicy.setVisibility(View.VISIBLE);
        }
        if (gender.equals("M"))
            ivMaleBack.setBackground(getResources().getDrawable(R.drawable.circle));
        else
            ivFemaleBack.setBackground(getResources().getDrawable(R.drawable.circle));
        npAge.setValue(25);
    }

    private void PrepareThirdPage(){
        currentPage = 3;
        tvWelcome.setText("");
        tvPleaseEnterInfo.setText(getResources().getString(R.string.enter_your_info));
        if (registerType.equals("email")){
            clEmail.setVisibility(View.VISIBLE);
        }
        else if(registerType.equals("facebook")){
            //clEmail.setVisibility(View.GONE);
            //clEmail.setBackground(null);
            clEmail.setVisibility(View.GONE);
            btSave.setText(getResources().getString(R.string.start_chatting));
            btSave.setBackgroundColor(getResources().getColor(R.color.button_green));
            swFacebookSwitch.setVisibility(View.VISIBLE);
            ivFacebookPic.setVisibility(View.VISIBLE);
            tvFacebookPic.setVisibility(View.VISIBLE);
            Glide.with(getApplicationContext()).load(facebookRegisterData.getProfile_pic()).apply(requestOptions).into(ivFacebookPic);
        }
    }

    private void PrepareFourthPage(){
        currentPage = 4;
        btSave.setText(getResources().getString(R.string.start_chatting));
        btSave.setBackgroundColor(getResources().getColor(R.color.button_green));
    }

    private void Animation(){
        constraint = findViewById(R.id.register_anonymous_layout_1);
        constraintSet.clone(this, R.layout.activity_register_2);
        Transition transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateOvershootInterpolator(0f));
        transition.setDuration(500);
        TransitionManager.beginDelayedTransition(constraint, transition);
        constraintSet.applyTo(constraint);
        if(registerType.equals("anonymous")){
            tvAcceptPolicy.setVisibility(View.VISIBLE);
            cbAcceptPolicy.setVisibility(View.VISIBLE);
        }
    }

    private void Animation2(){
        constraint = findViewById(R.id.register_anonymous_layout_1);
        constraintSet.clone(this, R.layout.activity_register_3);
        Transition transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateOvershootInterpolator(0f));
        transition.setDuration(500);
        TransitionManager.beginDelayedTransition(constraint, transition);
        constraintSet.applyTo(constraint);
        if(registerType.equals("facebook")){
            tvAcceptPolicy.setVisibility(View.VISIBLE);
            cbAcceptPolicy.setVisibility(View.VISIBLE);
        }
    }

    private void Animation3(){
        constraint = findViewById(R.id.register_anonymous_layout_1);
        constraintSet.clone(this, R.layout.activity_register_4);
        Transition transition = new ChangeBounds();
        transition.setInterpolator(new AnticipateOvershootInterpolator(0f));
        transition.setDuration(500);
        TransitionManager.beginDelayedTransition(constraint, transition);
        constraintSet.applyTo(constraint);
        if(registerType.equals("email")){
            tvAcceptPolicy.setVisibility(View.VISIBLE);
            cbAcceptPolicy.setVisibility(View.VISIBLE);
        }
    }

    private void RegisterAnonymous(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(RegisterActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_RegisterAnonymous);
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
                    FirebaseRegister(loginResponse.getUser().getEmail(), loginResponse.getToken());
                    Hawk.put("loggedUser",loginResponse);
                    Intent myIntent = new Intent(RegisterActivity.this, MainActivity.class);
                    RegisterActivity.this.startActivity(myIntent);
                    Intent intent = new Intent("finish_activity");
                    sendBroadcast(intent);
                    RegisterActivity.this.finish();
                }
                else if(loginResponse.getMessage().equals("auth_error")) {
                    //TODO
                }
                loadingDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tarja",error.getMessage());
                if (!useBackup){
                    RegisterAnonymous(true);
                }
                else
                    loadingDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("country", countryName);
                params.put("flagCode", alpha3Code.toLowerCase(enUsLocale));
                params.put("userAge", String.valueOf(age));
                params.put("lookingAgeRange", lookingAge);
                params.put("userGender", gender);
                params.put("lookingGender", lookingGender);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void RegisterFacebook(final boolean useBackup){
        Log.d("tarja","facebookID "+facebookRegisterData.getId());
        Log.d("tarja","facebookAccessToken "+facebookAccessToken);

        RequestQueue mRequestQueue = Volley.newRequestQueue(RegisterActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_RegisterFacebook);
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
                    FirebaseRegister(loginResponse.getUser().getEmail(), loginResponse.getToken());
                    Hawk.put("loggedUser",loginResponse);
                    Intent myIntent = new Intent(RegisterActivity.this, MainActivity.class);
                    RegisterActivity.this.startActivity(myIntent);
                    Intent intent = new Intent("finish_activity");
                    sendBroadcast(intent);
                    RegisterActivity.this.finish();
                }
                else if(loginResponse.getMessage().equals("auth_error")) {
                    //TODO
                }
                loadingDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tarja",error.getMessage());
                if (!useBackup){
                    RegisterFacebook(true);
                }
                else
                    loadingDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("username", etUsername.getText().toString());//TODO
                params.put("country", countryName);
                params.put("accessToken", facebookAccessToken);
                params.put("flagCode", alpha3Code.toLowerCase(enUsLocale));
                params.put("email", facebookRegisterData.getEmail());
                params.put("facebookID", facebookRegisterData.getId());
                params.put("userAge", String.valueOf(age));
                params.put("lookingAgeRange", lookingAge);
                params.put("userGender", gender);
                params.put("lookingGender", lookingGender);
                params.put("profilePic", profilePic);
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void RegisterEmail(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(RegisterActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_RegisterEmail);
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
                    FirebaseRegister(loginResponse.getUser().getEmail(), loginResponse.getToken());
                    Hawk.put("loggedUser",loginResponse);
                    Intent myIntent = new Intent(RegisterActivity.this, MainActivity.class);
                    RegisterActivity.this.startActivity(myIntent);
                    Intent intent = new Intent("finish_activity");
                    sendBroadcast(intent);
                    RegisterActivity.this.finish();
                }
                else if(loginResponse.getMessage().equals("auth_error")) {
                    //TODO
                }
                loadingDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tarja",error.getMessage());
                if (!useBackup){
                    RegisterEmail(true);
                }
                else
                    loadingDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("username", etUsername.getText().toString());//TODO
                params.put("country", countryName);
                params.put("flagCode", alpha3Code.toLowerCase(enUsLocale));
                params.put("email", etEmail.getText().toString());
                params.put("password", etPassword.getText().toString());
                params.put("userAge", String.valueOf(age));
                params.put("lookingAgeRange", lookingAge);
                params.put("userGender", gender);
                params.put("lookingGender", lookingGender);
                params.put("language", Locale.getDefault().getLanguage());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void CheckEmailUsernameDistinct(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(RegisterActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_CheckEmailUsernameDistinct);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL + webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<BasicResponse>() {
                }.getType();
                checkEmailUsernameDistinctResponse = gson.fromJson(response, myType);
                if (checkEmailUsernameDistinctResponse.isIsSuccess()){
                    Log.d("tarja","lan ne bu lan registertype ->" +registerType);
                    switch (registerType){
                        case "email":
                            PrepareFourthPage();
                            Animation3();
                            break;
                        case "facebook":
                            Log.d("tarja","lan tamam registera da giriyor işte");
                            loadingDialog.show();
                            RegisterFacebook(false);
                            break;
                    }
                }
                else if(checkEmailUsernameDistinctResponse.getMessage().equals("wait")) {
                    Toasty.error(RegisterActivity.this, getResources().getString(R.string.blocked_1_day), Toast.LENGTH_LONG, true).show();
                }
                else if(checkEmailUsernameDistinctResponse.getMessage().equals("username_used")) {
                    Toasty.warning(RegisterActivity.this, getResources().getString(R.string.username_used), Toast.LENGTH_LONG, true).show();
                }
                else if(checkEmailUsernameDistinctResponse.getMessage().equals("email_used")) {
                    Toasty.warning(RegisterActivity.this, getResources().getString(R.string.email_used), Toast.LENGTH_LONG, true).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("tarja",error.getMessage());
                if (!useBackup){
                    CheckEmailUsernameDistinct(true);
                }
                else
                    loadingDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("username", etUsername.getText().toString());
                if (registerType.equals("email"))
                    params.put("email", etEmail.getText().toString());
                else
                    params.put("email", "");
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void FirebaseRegister(String email, String pass){
        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("tarja", "createUserWithEmail:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                        } else {
                            Log.d("tarja", "createUserWithEmail:failure", task.getException());
                        }
                    }
                });
    }

    private void ShowTermsAndConditions(){
        alert = new AlertDialog.Builder(RegisterActivity.this);
        alert.setTitle(getResources().getString(R.string.app_name));
        wv = new WebView(RegisterActivity.this);
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

    private void ShowPrivacyPolicy(){
        alert = new AlertDialog.Builder(RegisterActivity.this);
        alert.setTitle(getResources().getString(R.string.app_name));
        wv = new WebView(RegisterActivity.this);
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
}

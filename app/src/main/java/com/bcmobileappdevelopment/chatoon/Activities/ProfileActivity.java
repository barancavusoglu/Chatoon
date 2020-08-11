package com.bcmobileappdevelopment.chatoon.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
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
import com.bcmobileappdevelopment.chatoon.Fragments.PersonalInfoFragment;
import com.bcmobileappdevelopment.chatoon.Fragments.SearchCredentialsFragment;
import com.bcmobileappdevelopment.chatoon.GsonResponse.BasicResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.GetUserProfileImagesResponse;
import com.bcmobileappdevelopment.chatoon.GsonResponse.LoginResponse;
import com.bcmobileappdevelopment.chatoon.HelperClass.ProfileImage;
import com.bcmobileappdevelopment.chatoon.HelperClass.ProfileImageItem;
import com.bcmobileappdevelopment.chatoon.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity {

    ImageView iv0, iv1 ,iv2 ,iv3 ,iv4 ,iv5 , btBack, btRemove0, btRemove1, btRemove2, btRemove3, btRemove4, btRemove5, ivCountry, ivLoading;
    boolean needToUpdateImages;
    LoginResponse loggedUser;
    GetUserProfileImagesResponse getUserProfileImagesResponse;
    Gson gson;
    RequestOptions requestOptions;
    ArrayList<String> pixReturn;
    UCrop.Options uCropOptions;
    String destinationPath, fileName, updatingImageView;
    ProfileImage profileImages = new ProfileImage();
    BasicResponse updateUserProfileImagesResponse;
    TextView btUpdateProfileImages, tvAgeValue, tvGenderValue, tvCountryName, tvAgeRangeValue, tvSearchGenderValue, tvUsernameValue;
    StorageReference firebaseStorage;
    int uploadedImageCount, filledImageCount;
    Locale enUsLocale = new Locale("en_US");
    CardView cvSearchCredentials, cvUserCredentials;
    ConstraintLayout profile_container;
    BroadcastReceiver searchCredentialsUpdatedReceiver, personalInfoUpdatedReceiver;
    Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Initialize();
        InitializeListeners();
        GetUserProfileImages(false);
        FillUserDetails();
    }

    private void Initialize() {
        loggedUser = Hawk.get("loggedUser");
        btBack = findViewById(R.id.btBack);
        iv0 = findViewById(R.id.iv0);
        iv1 = findViewById(R.id.iv1);
        iv2 = findViewById(R.id.iv2);
        iv3 = findViewById(R.id.iv3);
        iv4 = findViewById(R.id.iv4);
        iv5 = findViewById(R.id.iv5);
        
        btRemove0 = findViewById(R.id.btRemove0);
        btRemove1 = findViewById(R.id.btRemove1);
        btRemove2 = findViewById(R.id.btRemove2);
        btRemove3 = findViewById(R.id.btRemove3);
        btRemove4 = findViewById(R.id.btRemove4);
        btRemove5 = findViewById(R.id.btRemove5);

        btUpdateProfileImages = findViewById(R.id.btUpdateProfileImages);

        requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL);

        uCropOptions = new UCrop.Options();
        uCropOptions.setCompressionQuality(50);
        uCropOptions.setCompressionFormat(Bitmap.CompressFormat.JPEG);
        uCropOptions.setToolbarTitle(getResources().getString(R.string.edit_photo));
        uCropOptions.setToolbarColor(getResources().getColor(R.color.colorPrimary));
        uCropOptions.setStatusBarColor(getResources().getColor(R.color.colorPrimary));
        uCropOptions.setActiveWidgetColor(getResources().getColor(R.color.chatoon_orange_medium));

        tvAgeValue = findViewById(R.id.tvAgeValue);
        tvGenderValue = findViewById(R.id.tvGenderValue);
        tvCountryName = findViewById(R.id.tvCountryName);
        ivCountry = findViewById(R.id.ivCountry);
        tvAgeRangeValue = findViewById(R.id.tvAgeRangeValue);
        tvSearchGenderValue = findViewById(R.id.tvSearchGenderValue);
        tvUsernameValue = findViewById(R.id.tvUsernameValue);

        cvUserCredentials = findViewById(R.id.cvUserCredentials);
        cvSearchCredentials = findViewById(R.id.cvSearchCredentials);
        profile_container = findViewById(R.id.profileContainer);

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

        loadingDialog = new Dialog(this);
        loadingDialog.setContentView(R.layout.dialog_loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        ivLoading = loadingDialog.findViewById(R.id.ivLoading);
        Glide.with(ProfileActivity.this).load(R.drawable.loading).into(ivLoading);
    }

    private void UpdateSearchCredentials(){
        loggedUser = Hawk.get("loggedUser");
        if (loggedUser.getUser().getLookingGender().equals("MF"))
            tvSearchGenderValue.setText(getResources().getString(R.string.male_or_female));
        else if(loggedUser.getUser().getLookingGender().equals("M"))
            tvSearchGenderValue.setText(getResources().getString(R.string.male));
        else
            tvSearchGenderValue.setText(getResources().getString(R.string.female));
        tvAgeRangeValue.setText(loggedUser.getUser().getLookingAgeRange());
    }

    private void UpdatePersonalInfo(){
        loggedUser = Hawk.get("loggedUser");
        tvUsernameValue.setText(loggedUser.getUser().getUsername());
        tvAgeValue.setText(String.valueOf(loggedUser.getUser().getAge()));
        if (loggedUser.getUser().getGender().equals("M"))
            tvGenderValue.setText(getResources().getString(R.string.male));
        else
            tvGenderValue.setText(getResources().getString(R.string.female));
        tvCountryName.setText(loggedUser.getUser().getCountry());
        Glide.with(ProfileActivity.this).load(getResources().getIdentifier(loggedUser.getUser().getFlagCode().toLowerCase(enUsLocale), "drawable", getPackageName())).apply(requestOptions).into(ivCountry);
    }

    private void FillUserDetails(){
        tvUsernameValue.setText(loggedUser.getUser().getUsername());
        tvAgeValue.setText(String.valueOf(loggedUser.getUser().getAge()));
        if (loggedUser.getUser().getGender().equals("M"))
            tvGenderValue.setText(getResources().getString(R.string.male));
        else
            tvGenderValue.setText(getResources().getString(R.string.female));
        tvCountryName.setText(loggedUser.getUser().getCountry());
        Glide.with(ProfileActivity.this).load(getResources().getIdentifier(loggedUser.getUser().getFlagCode().toLowerCase(enUsLocale), "drawable", getPackageName())).apply(requestOptions).into(ivCountry);
        if (loggedUser.getUser().getLookingGender().equals("MF"))
            tvSearchGenderValue.setText(getResources().getString(R.string.male_or_female));
        else if(loggedUser.getUser().getLookingGender().equals("M"))
            tvSearchGenderValue.setText(getResources().getString(R.string.male));
        else
            tvSearchGenderValue.setText(getResources().getString(R.string.female));
        tvAgeRangeValue.setText(loggedUser.getUser().getLookingAgeRange());
    }

    private void InitializeListeners() {
        btBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileActivity.this.finish();
                onBackPressed();
            }
        });
        cvUserCredentials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersonalInfoFragment fragment = new PersonalInfoFragment(loggedUser);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.profileContainer,fragment,"profile_container")
                        .addToBackStack(null)
                        .commit();
            }
        });
        cvSearchCredentials.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchCredentialsFragment fragment = new SearchCredentialsFragment(loggedUser);
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.profileContainer,fragment,"profile_container")
                        .addToBackStack(null)
                        .commit();
            }
        });
        btUpdateProfileImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.show();
                Log.d("tarja","upload başladı");
                StartUpload();
            }
        });
        btUpdateProfileImages.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                btUpdateProfileImages.setBackgroundColor(getResources().getColor(R.color.transparent_green_pressed));
                if (event.getAction() == MotionEvent.ACTION_UP)
                    btUpdateProfileImages.setBackgroundColor(getResources().getColor(R.color.transparent_green));
                return false;
            }
        });
        iv0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!profileImages.profilePic.getFilled())
                {
                    updatingImageView = "iv0";
                    Pix.start(ProfileActivity.this,1);
                }
            }
        });
        iv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!profileImages.iv1.getFilled()){
                    updatingImageView = "iv1";
                    Pix.start(ProfileActivity.this,1);
                }
            }
        });
        iv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!profileImages.iv2.getFilled()){
                    updatingImageView = "iv2";
                    Pix.start(ProfileActivity.this,1);
                }
            }
        });
        iv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!profileImages.iv3.getFilled()){
                    updatingImageView = "iv3";
                    Pix.start(ProfileActivity.this,1);
                }
            }
        });
        iv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!profileImages.iv4.getFilled()){
                    updatingImageView = "iv4";
                    Pix.start(ProfileActivity.this,1);
                }
            }
        });
        iv5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!profileImages.iv5.getFilled()){
                    updatingImageView = "iv5";
                    Pix.start(ProfileActivity.this,1);
                }
            }
        });
        btRemove0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btRemove0.setVisibility(View.GONE);
                Glide.with(ProfileActivity.this).load(getResources().getDrawable(R.drawable.user)).apply(requestOptions).into(iv0);
                profileImages.profilePic.setFilled(false);
                profileImages.profilePic.setFileURL("");
                profileImages.profilePic.setFileName("");
                profileImages.profilePic.setFilePath("");
                if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        btUpdateProfileImages.setVisibility(View.VISIBLE);
            }
        });
        btRemove1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btRemove1.setVisibility(View.GONE);
                Glide.with(ProfileActivity.this).load(getResources().getDrawable(R.drawable.user)).apply(requestOptions).into(iv1);
                profileImages.iv1.setFilled(false);
                profileImages.iv1.setFileURL("");
                profileImages.iv1.setFileName("");
                profileImages.iv1.setFilePath("");
                if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        btUpdateProfileImages.setVisibility(View.VISIBLE);
            }
        });
        btRemove2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btRemove2.setVisibility(View.GONE);
                Glide.with(ProfileActivity.this).load(getResources().getDrawable(R.drawable.user)).apply(requestOptions).into(iv2);
                profileImages.iv2.setFilled(false);
                profileImages.iv2.setFileURL("");
                profileImages.iv2.setFileName("");
                profileImages.iv2.setFilePath("");
                if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        btUpdateProfileImages.setVisibility(View.VISIBLE);
            }
        });
        btRemove3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btRemove3.setVisibility(View.GONE);
                Glide.with(ProfileActivity.this).load(getResources().getDrawable(R.drawable.user)).apply(requestOptions).into(iv3);
                profileImages.iv3.setFilled(false);
                profileImages.iv3.setFileURL("");
                profileImages.iv3.setFileName("");
                profileImages.iv3.setFilePath("");
                if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        btUpdateProfileImages.setVisibility(View.VISIBLE);
            }
        });
        btRemove4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btRemove4.setVisibility(View.GONE);
                Glide.with(ProfileActivity.this).load(getResources().getDrawable(R.drawable.user)).apply(requestOptions).into(iv4);
                profileImages.iv4.setFilled(false);
                profileImages.iv4.setFileURL("");
                profileImages.iv4.setFileName("");
                profileImages.iv4.setFilePath("");
                if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        btUpdateProfileImages.setVisibility(View.VISIBLE);
            }
        });
        btRemove5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btRemove5.setVisibility(View.GONE);
                Glide.with(ProfileActivity.this).load(getResources().getDrawable(R.drawable.user)).apply(requestOptions).into(iv5);
                profileImages.iv5.setFilled(false);
                profileImages.iv5.setFileURL("");
                profileImages.iv5.setFileName("");
                profileImages.iv5.setFilePath("");
                if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        btUpdateProfileImages.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 1 && resultCode == RESULT_OK ){
            pixReturn = data.getStringArrayListExtra(Pix.IMAGE_RESULTS);
            Uri source = Uri.fromFile(new File(pixReturn.get(0)));
            destinationPath = ProfileActivity.this.getFilesDir().toString()+CreateFileName();
            Uri destination = Uri.fromFile(new File(destinationPath));
            UCrop.of(source, destination)
                    .withAspectRatio(1, 1)
                    .withMaxResultSize(1200,1200)
                    .withOptions(uCropOptions)
                    .start(ProfileActivity.this,2);

        }
        else if(requestCode == 2 && resultCode == RESULT_OK){
            Uri resultUri = UCrop.getOutput(data);
            if (!needToUpdateImages)
                needToUpdateImages = true;
            switch (updatingImageView){
                case "iv0":
                    Glide.with(ProfileActivity.this).load(resultUri.getPath()).apply(requestOptions).into(iv0);
                    profileImages.profilePic.setFilePath(resultUri.getPath());
                    profileImages.profilePic.setFileName(fileName);
                    profileImages.profilePic.setFilled(true);
                    btRemove0.setVisibility(View.VISIBLE);
                    if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        btUpdateProfileImages.setVisibility(View.VISIBLE);
                    break;
                case "iv1":
                    Glide.with(ProfileActivity.this).load(resultUri.getPath()).apply(requestOptions).into(iv1);
                    profileImages.iv1.setFilePath(resultUri.getPath());
                    profileImages.iv1.setFileName(fileName);
                    profileImages.iv1.setFilled(true);
                    btRemove1.setVisibility(View.VISIBLE);
                    if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        btUpdateProfileImages.setVisibility(View.VISIBLE);
                    break;
                case "iv2":
                    Glide.with(ProfileActivity.this).load(resultUri.getPath()).apply(requestOptions).into(iv2);
                    profileImages.iv2.setFilePath(resultUri.getPath());
                    profileImages.iv2.setFileName(fileName);
                    profileImages.iv2.setFilled(true);
                    btRemove2.setVisibility(View.VISIBLE);
                    if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        btUpdateProfileImages.setVisibility(View.VISIBLE);
                    break;
                case "iv3":
                    Glide.with(ProfileActivity.this).load(resultUri.getPath()).apply(requestOptions).into(iv3);
                    profileImages.iv3.setFilePath(resultUri.getPath());
                    profileImages.iv3.setFileName(fileName);
                    profileImages.iv3.setFilled(true);
                    btRemove3.setVisibility(View.VISIBLE);
                    if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        btUpdateProfileImages.setVisibility(View.VISIBLE);
                    break;
                case "iv4":
                    Glide.with(ProfileActivity.this).load(resultUri.getPath()).apply(requestOptions).into(iv4);
                    profileImages.iv4.setFilePath(resultUri.getPath());
                    profileImages.iv4.setFileName(fileName);
                    profileImages.iv4.setFilled(true);
                    btRemove4.setVisibility(View.VISIBLE);
                    if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        btUpdateProfileImages.setVisibility(View.VISIBLE);
                    break;
                case "iv5":
                    Glide.with(ProfileActivity.this).load(resultUri.getPath()).apply(requestOptions).into(iv5);
                    profileImages.iv5.setFilePath(resultUri.getPath());
                    profileImages.iv5.setFileName(fileName);
                    profileImages.iv5.setFilled(true);
                    btRemove5.setVisibility(View.VISIBLE);
                    if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        if (btUpdateProfileImages.getVisibility() != View.VISIBLE)
                        btUpdateProfileImages.setVisibility(View.VISIBLE);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (profile_container != null && profile_container.getChildCount() > 0)
            getSupportFragmentManager().popBackStack();
        else {
            ProfileActivity.this.finish();
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(searchCredentialsUpdatedReceiver);
        unregisterReceiver(personalInfoUpdatedReceiver);
        super.onDestroy();
    }

    private String CreateFileName(){
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss:SSS");
        fileName = "/u"+String.valueOf(loggedUser.getUser().getID())+"_"+df.format(calendar.getTime())+".jpg";
        return  fileName;
    }

    private void StartUpload(){
        uploadedImageCount = 0;
        filledImageCount = 0;
        ArrayList<ProfileImageItem> profileImageItems = new ArrayList<>();
        profileImageItems.add(profileImages.profilePic);
        profileImageItems.add(profileImages.iv1);
        profileImageItems.add(profileImages.iv2);
        profileImageItems.add(profileImages.iv3);
        profileImageItems.add(profileImages.iv4);
        profileImageItems.add(profileImages.iv5);

        for (ProfileImageItem imageItem:profileImageItems){
            if (imageItem.getFilled() && imageItem.getFileURL().equals(""))
                filledImageCount++;
        }
        Log.d("tarja","filledImageCount"+filledImageCount);
        firebaseStorage = FirebaseStorage.getInstance().getReference();
        for (ProfileImageItem imageItem:profileImageItems){
            if (imageItem.getFilled() && imageItem.getFileURL().equals(""))
                UploadImage(imageItem);
        }
        if (filledImageCount == 0){
            UpdateUserProfileImages(false);
        }

    }

    private void UploadImage(final ProfileImageItem image){
        Uri file = Uri.fromFile(new File(image.getFilePath()));
        final StorageReference riversRef = firebaseStorage.child("images/"+loggedUser.getUser().getFlagCode()+image.getFileName());
        riversRef.putFile(file).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                //double progress = 100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount();
                //progressDialog.setMessage(uploadedImageCount+"/"+loadedImageCount+" Yüklendi");
            }
        })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Uri downloadUrl = uri;
                                switch (image.getImageName())
                                {
                                    case "iv0":
                                        profileImages.profilePic.setFileURL(downloadUrl.toString());
                                        break;
                                    case "iv1":
                                        profileImages.iv1.setFileURL(downloadUrl.toString());
                                        break;
                                    case "iv2":
                                        profileImages.iv2.setFileURL(downloadUrl.toString());
                                        break;
                                    case "iv3":
                                        profileImages.iv3.setFileURL(downloadUrl.toString());
                                        break;
                                    case "iv4":
                                        profileImages.iv4.setFileURL(downloadUrl.toString());
                                        break;
                                    case "iv5":
                                        profileImages.iv5.setFileURL(downloadUrl.toString());
                                        break;
                                }
                                uploadedImageCount++;
                                Log.d("Tarja","uploadedImageCount"+uploadedImageCount);

                                if (uploadedImageCount == filledImageCount){
                                    UpdateUserProfileImages(false);
                                }
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        //progressDialog.dismiss();TODO
                        Log.d("Tarja","UPLOAD FAILURE");
                    }
                });
    }

    private void GetUserProfileImages(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(ProfileActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_GetUserProfileImages);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL+webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<GetUserProfileImagesResponse>() {
                }.getType();
                getUserProfileImagesResponse = gson.fromJson(response, myType);
                if (getUserProfileImagesResponse.isIsSuccess()) {

                    if (getUserProfileImagesResponse.getProfilePic() != null){
                        Glide.with(ProfileActivity.this).load(getUserProfileImagesResponse.getProfilePic()).into(iv0);
                        profileImages.profilePic.setFileURL(getUserProfileImagesResponse.getProfilePic());
                        profileImages.profilePic.setFilled(true);
                        btRemove0.setVisibility(View.VISIBLE);
                    }

                    for (String item: getUserProfileImagesResponse.getUserImages()){
                        if (!profileImages.iv1.getFilled()){
                            Glide.with(ProfileActivity.this).load(item).into(iv1);
                            profileImages.iv1.setFileURL(item);
                            profileImages.iv1.setFilled(true);
                            btRemove1.setVisibility(View.VISIBLE);
                        }
                        else if (!profileImages.iv2.getFilled()){
                            Glide.with(ProfileActivity.this).load(item).into(iv2);
                            profileImages.iv2.setFileURL(item);
                            profileImages.iv2.setFilled(true);
                            btRemove2.setVisibility(View.VISIBLE);
                        }
                        else if (!profileImages.iv3.getFilled()){
                            Glide.with(ProfileActivity.this).load(item).into(iv3);
                            profileImages.iv3.setFileURL(item);
                            profileImages.iv3.setFilled(true);
                            btRemove3.setVisibility(View.VISIBLE);
                        }
                        else if (!profileImages.iv4.getFilled()){
                            Glide.with(ProfileActivity.this).load(item).into(iv4);
                            profileImages.iv4.setFileURL(item);
                            profileImages.iv4.setFilled(true);
                            btRemove4.setVisibility(View.VISIBLE);
                        }
                        else if (!profileImages.iv5.getFilled()){
                            Glide.with(ProfileActivity.this).load(item).into(iv5);
                            profileImages.iv5.setFileURL(item);
                            profileImages.iv5.setFilled(true);
                            btRemove5.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    GetUserProfileImages(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                params.put("token", loggedUser.getToken());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
    }

    private void UpdateUserProfileImages(final boolean useBackup){
        RequestQueue mRequestQueue = Volley.newRequestQueue(ProfileActivity.this);
        String webserviceURL = getResources().getString(R.string.webservice_main);
        String webserviceMethod = getResources().getString(R.string.ws_UpdateUserProfileImages);
        if (useBackup)
            webserviceURL = getResources().getString(R.string.webservice_backup);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, webserviceURL+webserviceMethod, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                GsonBuilder gsonBuilder = new GsonBuilder();
                gson = gsonBuilder.create();
                Type myType = new TypeToken<BasicResponse>() {
                }.getType();
                updateUserProfileImagesResponse = gson.fromJson(response, myType);
                if (updateUserProfileImagesResponse.isIsSuccess()) {
                    if (profileImages.profilePic.getFileURL().equals(""))
                        loggedUser.getUser().setProfilePicURL(getResources().getString(R.string.default_profile_pic));
                    else
                        loggedUser.getUser().setProfilePicURL(profileImages.profilePic.getFileURL());
                    Hawk.put("loggedUser",loggedUser);
                    Intent intent = new Intent("update_profile_pic");
                    sendBroadcast(intent);
                    ProfileActivity.this.finish();
                    Toasty.success(ProfileActivity.this, getResources().getString(R.string.your_photos_updated), Toast.LENGTH_LONG, true).show();
                }
                else if(updateUserProfileImagesResponse.getMessage().equals("user_banned"))
                {
                    Toasty.warning(ProfileActivity.this, getResources().getString(R.string.user_banned), Toast.LENGTH_LONG, true).show();
                    Logout();
                }
                loadingDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (!useBackup)
                    UpdateUserProfileImages(true);
                else
                    loadingDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("masterPass", getResources().getString(R.string.masterpass));
                params.put("userID", String.valueOf(loggedUser.getUser().getID()));
                params.put("token", loggedUser.getToken());
                params.put("pic0", profileImages.profilePic.getFileURL());
                params.put("pic1", profileImages.iv1.getFileURL());
                params.put("pic2", profileImages.iv2.getFileURL());
                params.put("pic3", profileImages.iv3.getFileURL());
                params.put("pic4", profileImages.iv4.getFileURL());
                params.put("pic5", profileImages.iv5.getFileURL());
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(3000,10,1.0f));
        mRequestQueue.add(stringRequest);
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

        Intent intent = new Intent(getApplicationContext(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        Intent myIntent = new Intent(ProfileActivity.this, SplashActivity.class);
        ProfileActivity.this.startActivity(myIntent);
        ProfileActivity.this.finish();
    }
}

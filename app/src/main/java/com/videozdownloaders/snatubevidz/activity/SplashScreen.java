package com.videozdownloaders.snatubevidz.activity;

import static com.google.android.play.core.install.model.AppUpdateType.IMMEDIATE;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.videozdownloaders.snatubevidz.R;
import com.videozdownloaders.snatubevidz.util.AppLangSessionManager;

import java.util.Locale;

public class SplashScreen extends AppCompatActivity {
    SplashScreen activity;
    Context context;
    AppUpdateManager appUpdateManager;
    AppLangSessionManager appLangSessionManager;
    DatabaseReference myRef;
    FirebaseDatabase database;
    private SharedPreferences adsunit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        context = activity = this;

       // FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        adsunit=getSharedPreferences("Adsunit",MODE_PRIVATE);


        Pickadsfromfirebase();

        appUpdateManager = AppUpdateManagerFactory.create(context);
        appLangSessionManager = new AppLangSessionManager(activity);
        UpdateApp();

        setLocale(appLangSessionManager.getLanguage());
    }

    private void Pickadsfromfirebase() {
        myRef.child("com_videozdownloaders_snatubevidz").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot datasnapshot: snapshot.getChildren()){


                    adsunit.edit().putString("banner",snapshot.child("banner").getValue().toString()).apply();
                    adsunit.edit().putString("interstitial",snapshot.child("interstitial").getValue().toString()).apply();
                    adsunit.edit().putString("native",snapshot.child("native").getValue().toString()).apply();

                    adsunit = getSharedPreferences("Adsunit", MODE_PRIVATE);
                    Toast.makeText(SplashScreen.this, ""+adsunit.getString("banner",""), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

                Toast.makeText(getApplicationContext(), "Error in read", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        activity = this;
        appLangSessionManager = new AppLangSessionManager(activity);

        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo, IMMEDIATE, activity, 101);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void HomeScreen() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isConnected(SplashScreen.this)){
                    Intent i = new Intent(SplashScreen.this, MainActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Intent n = new Intent(SplashScreen.this, ActivityNoInternet.class);
                    startActivity(n);
                    finish();
                }
                
            }
        }, 1000);

    }

    public void UpdateApp() {
        try {
            Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
            appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                        && appUpdateInfo.isUpdateTypeAllowed(IMMEDIATE)) {
                    try {
                        appUpdateManager.startUpdateFlowForResult(
                                appUpdateInfo, IMMEDIATE, activity, 101);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                } else {
                    HomeScreen();
                }
            }).addOnFailureListener(e -> {
                e.printStackTrace();
                HomeScreen();
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode != RESULT_OK) {
                HomeScreen();
            } else {
                HomeScreen();
            }
        }
    }


    public void setLocale(String lang) {
        if (lang.equals("")){
            lang="en";
        }
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);

    }

    public boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiInfo != null && wifiInfo.isConnected()) || (mobileInfo != null && mobileInfo.isConnected())) {
            return true;
        } else {

            return false;
        }

    }

}

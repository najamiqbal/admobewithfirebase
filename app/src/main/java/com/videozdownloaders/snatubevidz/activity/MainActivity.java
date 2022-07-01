package com.videozdownloaders.snatubevidz.activity;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.videozdownloaders.snatubevidz.R;
import com.videozdownloaders.snatubevidz.databinding.ActivityMainBinding;
import com.videozdownloaders.snatubevidz.util.AppLangSessionManager;
import com.videozdownloaders.snatubevidz.util.ClipboardListener;
import com.videozdownloaders.snatubevidz.util.Custom_exist_Dialog;
import com.videozdownloaders.snatubevidz.util.Utils;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.videozdownloaders.snatubevidz.util.Utils.createFileFolder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Custom_exist_Dialog.OnClickListener {
    MainActivity activity;
    ActivityMainBinding binding;
    boolean doubleBackToExitPressedOnce = false;
    private ClipboardManager clipBoard;
    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    String CopyKey = "";
    String CopyValue = "";

    AppLangSessionManager appLangSessionManager;
    InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private SharedPreferences adsunit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        activity = this;
        appLangSessionManager = new AppLangSessionManager(activity);
        adsunit=getSharedPreferences("Adsunit",MODE_PRIVATE);
        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        initBannerAdd();
        InterstitialAdsINIT();
        initViews();

    }


    public void initBannerAdd() {
        View adContainer_admob = MainActivity.this.findViewById(R.id.banner_container);
        mAdView = new com.google.android.gms.ads.AdView(this);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId(adsunit.getString("banner", ""));
        ((LinearLayout) adContainer_admob).addView(mAdView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activity = this;
        assert activity != null;
        clipBoard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
        InterstitialAdsINIT();

    }

    public static List<String> extractUrls(String text) {
        List<String> containedUrls = new ArrayList<String>();
        String urlRegex = "((https?|ftp|gopher|telnet|file):((//)|(\\\\))+[\\w\\d:#@%/;$()~_?\\+-=\\\\\\.&]*)";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        Matcher urlMatcher = pattern.matcher(text);

        while (urlMatcher.find()) {
            containedUrls.add(text.substring(urlMatcher.start(0),
                    urlMatcher.end(0)));
        }

        return containedUrls;
    }

    public void initViews() {
        clipBoard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
        if (activity.getIntent().getExtras() != null) {
            for (String key : activity.getIntent().getExtras().keySet()) {
                CopyKey = key;
                String value = activity.getIntent().getExtras().getString(CopyKey);
                if (CopyKey.equals("android.intent.extra.TEXT")) {
                    CopyValue = activity.getIntent().getExtras().getString(CopyKey);
                    callText(value);
                } else {
                    CopyValue = "";
                    callText(value);
                }
            }
        }
        if (clipBoard != null) {
            clipBoard.addPrimaryClipChangedListener(new ClipboardListener() {
                @Override
                public void onPrimaryClipChanged() {
                    try {
                        showNotification(Objects.requireNonNull(clipBoard.getPrimaryClip().getItemAt(0).getText()).toString());
                    } catch (
                            Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions(0);
        }

        binding.rvLikee.setOnClickListener(this);
        binding.rvInsta.setOnClickListener(this);
        binding.rvFB.setOnClickListener(this);
        binding.rvTwitter.setOnClickListener(this);
        binding.rvGallery.setOnClickListener(this);
        binding.rvAbout.setOnClickListener(this);
        binding.rvShareApp.setOnClickListener(this);
        binding.rvRateApp.setOnClickListener(this);
        binding.rvMoreApp.setOnClickListener(this);


        //TODO :  Change Language Dialog Open
        binding.rvChangeLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                final BottomSheetDialog dialogSortBy = new BottomSheetDialog(MainActivity.this, R.style.SheetDialog);
                dialogSortBy.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialogSortBy.setContentView(R.layout.dialog_language);
                final TextView tv_english = dialogSortBy.findViewById(R.id.tv_english);
                final TextView tv_hindi = dialogSortBy.findViewById(R.id.tv_hindi);
                final TextView tv_cancel = dialogSortBy.findViewById(R.id.tv_cancel);
                final TextView tvArabic = dialogSortBy.findViewById(R.id.tvArabic);

                dialogSortBy.show();


                tv_english.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setLocale("en");
                        appLangSessionManager.setLanguage("en");
                    }
                });
                tv_hindi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setLocale("hi");
                        appLangSessionManager.setLanguage("hi");
                    }
                });
                tvArabic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setLocale("ar");
                        appLangSessionManager.setLanguage("ar");
                    }
                });
                tv_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogSortBy.dismiss();
                    }
                });

            }
        });


        createFileFolder();

    }

    private void callText(String CopiedText) {
        try {
            if (CopiedText.contains("likee")) {

                try {
                    List<String> extractedUrls = extractUrls(CopiedText);
                    CopyValue = extractedUrls.get(0);

                    Log.d("LIKEEEEE MAIN", CopyValue);
                } catch (Exception ex) {

                }


                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(100);
                } else {
/*                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();
                    } else {
                        callLikeeActivity();
                    }
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            callLikeeActivity();
                        }
                    });*/
                }
            } else if (CopiedText.contains("instagram.com")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(101);
                } else {
                    callInstaActivity();
                }
            } else if (CopiedText.contains("facebook.com")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(104);
                } else {
                    callFacebookActivity();
                }
            } else if (CopiedText.contains("tiktok.com")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(103);
                } else {
                    callTikTokActivity();
                }
            } else if (CopiedText.contains("twitter.com")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(106);
                } else {
                    callTwitterActivity();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        Intent i = null;

        switch (v.getId()) {
            case R.id.rvLikee:
                if (Build.VERSION.SDK_INT >= 23) {
                    Toast.makeText(activity, "HERE IS IT", Toast.LENGTH_SHORT).show();
                    checkPermissions(100);
                } else {

                    if (mInterstitialAd!=null) {
                        mInterstitialAd.show(activity);
                    } else {
                        callLikeeActivity();

                    }
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            callLikeeActivity();
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            callLikeeActivity();

                        }
                    });


                }
                break;
            case R.id.rvInsta:
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(101);
                } else {

                    if (mInterstitialAd!=null) {
                        mInterstitialAd.show(activity);
                    } else {
                        callInstaActivity();

                    }
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            callInstaActivity();
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            callInstaActivity();

                        }
                    });


                }
                break;

/*            case R.id.rvWhatsApp:
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(102);
                } else {
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();
                    } else {
                        callWhatsappActivity();
                    }
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            callWhatsappActivity();
                        }
                    });

                }
                break;
            case R.id.rvTikTok:
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(103);
                } else {
                    if (interstitialAd.isLoaded()) {
                        interstitialAd.show();
                    } else {
                        callTikTokActivity();
                    }
                    interstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {
                            super.onAdClosed();
                            callTikTokActivity();
                        }
                    });

                }
                break;*/
            case R.id.rvFB:
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(104);
                } else {

                    if (mInterstitialAd!=null) {
                        mInterstitialAd.show(activity);
                    } else {
                        callFacebookActivity();

                    }
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            callFacebookActivity();
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            callFacebookActivity();

                        }
                    });

                }
                break;
            case R.id.rvGallery:
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(105);
                } else {

                    if (mInterstitialAd!=null) {
                        mInterstitialAd.show(activity);
                    } else {
                        callGalleryActivity();

                    }
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            callGalleryActivity();
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            callGalleryActivity();

                        }
                    });

                }
                break;
            case R.id.rvTwitter:
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions(106);
                } else {

                    if (mInterstitialAd!=null) {
                        mInterstitialAd.show(activity);
                    } else {
                        callTwitterActivity();

                    }
                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            super.onAdFailedToShowFullScreenContent(adError);
                            callTwitterActivity();
                        }

                        @Override
                        public void onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent();
                            callTwitterActivity();

                        }
                    });


                }
                break;
            case R.id.rvAbout:
                i = new Intent(activity, AboutUsActivity.class);
                startActivity(i);
                break;
            case R.id.rvShareApp:
                Utils.ShareApp(activity);
                break;

            case R.id.rvRateApp:
                Utils.RateApp(activity);
                break;
            case R.id.rvMoreApp:
                Utils.MoreApp(activity);
                break;

        }
    }


    public void callLikeeActivity() {
        Intent i = new Intent(activity, LikeeActivity.class);
        i.putExtra("CopyIntent", CopyValue);
        startActivity(i);
    }

    public void callInstaActivity() {
        Intent i = new Intent(activity, InstagramActivity.class);
        i.putExtra("CopyIntent", CopyValue);
        startActivity(i);
    }


    public void callWhatsappActivity() {
        Intent i = new Intent(activity, WhatsappActivity.class);
        startActivity(i);
    }

    public void callTikTokActivity() {
        Intent i = new Intent(activity, TikTokActivity.class);
        i.putExtra("CopyIntent", CopyValue);
        startActivity(i);
    }

    public void callFacebookActivity() {
        Intent i = new Intent(activity, FacebookActivity.class);
        i.putExtra("CopyIntent", CopyValue);
        startActivity(i);

    }

    public void callTwitterActivity() {
        Intent i = new Intent(activity, TwitterActivity.class);
        i.putExtra("CopyIntent", CopyValue);
        startActivity(i);
    }


    public void callGalleryActivity() {
        Intent i = new Intent(activity, GalleryActivity.class);
        startActivity(i);
    }

    public void showNotification(String Text) {
        if (Text.contains("instagram.com") || Text.contains("facebook.com") || Text.contains("tiktok.com")
                || Text.contains("twitter.com") || Text.contains("likee")) {
            Intent intent = new Intent(activity, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("Notification", Text);
            PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_ONE_SHOT);
            NotificationManager notificationManager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(getResources().getString(R.string.app_name),
                        getResources().getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH);
                mChannel.enableLights(true);
                mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                notificationManager.createNotificationChannel(mChannel);
            }
            NotificationCompat.Builder notificationBuilder;
            notificationBuilder = new NotificationCompat.Builder(activity, getResources().getString(R.string.app_name))
                    .setAutoCancel(true)
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setColor(getResources().getColor(R.color.black))
                    .setLargeIcon(BitmapFactory.decodeResource(activity.getResources(),
                            R.mipmap.ic_launcher_round))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setContentTitle("Copied text")
                    .setContentText(Text)
                    .setChannelId(getResources().getString(R.string.app_name))
                    .setFullScreenIntent(pendingIntent, true);
            notificationManager.notify(1, notificationBuilder.build());
        }
    }

    private boolean checkPermissions(int type) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(activity, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) (activity),
                    listPermissionsNeeded.toArray(new
                            String[listPermissionsNeeded.size()]), type);
            return false;
        } else {
            if (type == 100) {

                if (mInterstitialAd!=null) {
                    mInterstitialAd.show(activity);
                } else {
                    callLikeeActivity();

                }
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        callLikeeActivity();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        callLikeeActivity();

                    }
                });


            } else if (type == 101) {

                if (mInterstitialAd!=null) {
                    mInterstitialAd.show(activity);
                } else {
                    callInstaActivity();

                }
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        callInstaActivity();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        callInstaActivity();

                    }
                });


            } else if (type == 102) {

                if (mInterstitialAd!=null) {
                    mInterstitialAd.show(activity);
                } else {
                    callWhatsappActivity();

                }
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        callWhatsappActivity();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        callWhatsappActivity();

                    }
                });


            } else if (type == 103) {

                if (mInterstitialAd!=null) {
                    mInterstitialAd.show(activity);
                } else {
                    callTikTokActivity();

                }
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        callTikTokActivity();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        callTikTokActivity();

                    }
                });


            } else if (type == 104) {

                if (mInterstitialAd!=null) {
                    mInterstitialAd.show(activity);
                } else {
                    callFacebookActivity();

                }
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        callFacebookActivity();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        callFacebookActivity();

                    }
                });


            } else if (type == 105) {

                if (mInterstitialAd!=null) {
                    mInterstitialAd.show(activity);
                } else {
                    callGalleryActivity();

                }
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        callGalleryActivity();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        callGalleryActivity();

                    }
                });

            } else if (type == 106) {

                if (mInterstitialAd!=null) {
                    mInterstitialAd.show(activity);
                } else {
                    callTwitterActivity();

                }
                mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        super.onAdFailedToShowFullScreenContent(adError);
                        callTwitterActivity();
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        super.onAdDismissedFullScreenContent();
                        callTwitterActivity();

                    }
                });


            }

        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callLikeeActivity();
            } else {
            }
            return;
        } else if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callInstaActivity();
            } else {
            }
            return;
        } else if (requestCode == 102) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callWhatsappActivity();
            } else {
            }
            return;
        } else if (requestCode == 103) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callTikTokActivity();
            } else {
            }
            return;
        } else if (requestCode == 104) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callFacebookActivity();
            } else {
            }
            return;
        } else if (requestCode == 105) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callGalleryActivity();
            } else {
            }
            return;
        } else if (requestCode == 106) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callTwitterActivity();
            } else {
            }
            return;
        }

    }

    @Override
    public void onBackPressed() {
        Custom_exist_Dialog cdd = new Custom_exist_Dialog(MainActivity.this, this);
        cdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        cdd.show();
       /* if (doubleBackToExitPressedOnce) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        this.doubleBackToExitPressedOnce = true;
        Utils.setToast(activity, getResources().getString(R.string.pls_bck_again));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);*/

    }


    //TODO :  Using for Set Locale
    public void setLocale(String lang) {

        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);


        Intent refresh = new Intent(MainActivity.this, MainActivity.class);
        startActivity(refresh);
        finish();
    }


    @Override
    public void onRateClick() {
        rateUsOnPlayStore();
    }


    public void rateUsOnPlayStore() {
        Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
        }
    }

    @Override
    public void onExitClick() {
        finish();
    }


    //InterstitialAd : Start

    public void InterstitialAdsINIT(){
        AdRequest adRequest = new AdRequest.Builder().build();

        com.google.android.gms.ads.interstitial.InterstitialAd.load(this, adsunit.getString("interstitial", ""), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull com.google.android.gms.ads.interstitial.InterstitialAd interstitialAd) {

                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.i(TAG, loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }


    private void showInterstitial() {
        if (mInterstitialAd != null ) {
            mInterstitialAd.show(MainActivity.this);
        }
    }

    //InterstitialAd : End


}

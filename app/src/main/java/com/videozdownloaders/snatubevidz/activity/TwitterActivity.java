package com.videozdownloaders.snatubevidz.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.videozdownloaders.snatubevidz.R;
import com.videozdownloaders.snatubevidz.api.CommonClassForAPI;
import com.videozdownloaders.snatubevidz.databinding.ActivityTwitterBinding;
import com.videozdownloaders.snatubevidz.model.TwitterResponse;
import com.videozdownloaders.snatubevidz.util.AppLangSessionManager;
import com.videozdownloaders.snatubevidz.util.SharePrefs;
import com.videozdownloaders.snatubevidz.util.Utils;


import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import io.reactivex.observers.DisposableObserver;

import static android.content.ClipDescription.MIMETYPE_TEXT_PLAIN;
import static android.content.ContentValues.TAG;
import static com.videozdownloaders.snatubevidz.util.Utils.RootDirectoryTwitter;
import static com.videozdownloaders.snatubevidz.util.Utils.createFileFolder;
import static com.videozdownloaders.snatubevidz.util.Utils.startDownload;

public class TwitterActivity extends AppCompatActivity {
    private ActivityTwitterBinding binding;
    TwitterActivity activity;
    CommonClassForAPI commonClassForAPI;
    private String VideoUrl;
    private ClipboardManager clipBoard;



    AppLangSessionManager appLangSessionManager;
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;
    private SharedPreferences adsunit;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_twitter);
        activity = this;
        commonClassForAPI = CommonClassForAPI.getInstance(activity);

        adsunit=getSharedPreferences("Adsunit",MODE_PRIVATE);

        createFileFolder();

        initViews();

        // Initialize the Mobile Ads SDK.
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {}
        });

        appLangSessionManager = new AppLangSessionManager(activity);
        setLocale(appLangSessionManager.getLanguage());

        //AdsUtils.showFBBannerAdRect(activity,binding.bannerContainer);
        InterstitialAdsINIT();

        initBannerAdd();



    }


    public void initBannerAdd() {
        View adContainer_admob = TwitterActivity.this.findViewById(R.id.banner_container);
        mAdView = new com.google.android.gms.ads.AdView(this);
        mAdView.setAdSize(AdSize.BANNER);
        //mAdView.setAdUnitId("");
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
        PasteText();
    }

    private void initViews() {
        clipBoard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);

        binding.imBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        binding.imInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                binding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
            }
        });



        Glide.with(activity)
                .load(R.drawable.tw1)
                .into(binding.layoutHowTo.imHowto1);

        Glide.with(activity)
                .load(R.drawable.tw2)
                .into(binding.layoutHowTo.imHowto2);

        Glide.with(activity)
                .load(R.drawable.tw3)
                .into(binding.layoutHowTo.imHowto3);

        Glide.with(activity)
                .load(R.drawable.tw4)
                .into(binding.layoutHowTo.imHowto4);



        binding.layoutHowTo.tvHowTo1.setText(getResources().getString(R.string.open_twitter));
        binding.layoutHowTo.tvHowTo3.setText(getResources().getString(R.string.open_twitter));
        if (!SharePrefs.getInstance(activity).getBoolean(SharePrefs.ISSHOWHOWTOTWITTER)) {
            SharePrefs.getInstance(activity).putBoolean(SharePrefs.ISSHOWHOWTOTWITTER,true);
            binding.layoutHowTo.LLHowToLayout.setVisibility(View.VISIBLE);
        }else {
            binding.layoutHowTo.LLHowToLayout.setVisibility(View.GONE);
        }


        binding.loginBtn1.setOnClickListener(v -> {
            String LL = binding.etText.getText().toString();
            if (LL.equals("")) {
                Utils.setToast(activity, getResources().getString(R.string.enter_url));
            } else if (!Patterns.WEB_URL.matcher(LL).matches()) {
                Utils.setToast(activity, getResources().getString(R.string.enter_valid_url));
            } else {
                Utils.showProgressDialog(activity);
                GetTwitterData();
                //showInterstitial();
            }
        });

        binding.tvPaste.setOnClickListener(v -> {
            PasteText();
        });

        binding.LLOpenTwitter.setOnClickListener(v -> {
            Utils.OpenApp(activity,"com.twitter.android");
        });
    }

    private void GetTwitterData() {
        try {
            createFileFolder();
            URL url = new URL(binding.etText.getText().toString());
            String host = url.getHost();
            if (host.contains("twitter.com")) {
                Long id = getTweetId(binding.etText.getText().toString());
                if (id != null) {
                    callGetTwitterData(String.valueOf(id));
                }
            } else {
                Utils.setToast(activity, getResources().getString(R.string.enter_url));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Long getTweetId(String s) {
        try {
            String[] split = s.split("\\/");
            String id = split[5].split("\\?")[0];
            return Long.parseLong(id);
        } catch (Exception e) {
            Log.d("TAG", "getTweetId: " + e.getLocalizedMessage());
            return null;
        }
    }

    private void PasteText() {
        try {
            binding.etText.setText("");
            String CopyIntent = getIntent().getStringExtra("CopyIntent");
            if (CopyIntent.equals("")) {

                if (!(clipBoard.hasPrimaryClip())) {

                } else if (!(clipBoard.getPrimaryClipDescription().hasMimeType(MIMETYPE_TEXT_PLAIN))) {
                    if (clipBoard.getPrimaryClip().getItemAt(0).getText().toString().contains("twitter.com")) {
                        binding.etText.setText(clipBoard.getPrimaryClip().getItemAt(0).getText().toString());
                    }

                } else {
                    ClipData.Item item = clipBoard.getPrimaryClip().getItemAt(0);
                    if (item.getText().toString().contains("twitter.com")) {
                        binding.etText.setText(item.getText().toString());
                    }

                }
            } else {
                if (CopyIntent.contains("twitter.com")) {
                    binding.etText.setText(CopyIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callGetTwitterData(String id) {
        String URL = "https://twittervideodownloaderpro.com/twittervideodownloadv2/index.php";
        try {
            Utils utils = new Utils(activity);
            if (utils.isNetworkAvailable()) {
                if (commonClassForAPI != null) {
                    Utils.showProgressDialog(activity);
                    commonClassForAPI.callTwitterApi(observer,URL,id);
                }
            } else {
                Utils.setToast(activity, getResources().getString(R.string.no_net_conn));
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


    private DisposableObserver<TwitterResponse> observer = new DisposableObserver<TwitterResponse>() {
        @Override
        public void onNext(TwitterResponse twitterResponse) {
            Utils.hideProgressDialog(activity);
            try {
                VideoUrl = twitterResponse.getVideos().get(0).getUrl();
                if (twitterResponse.getVideos().get(0).getType().equals("image")){
                    startDownload(VideoUrl, RootDirectoryTwitter, activity, getFilenameFromURL(VideoUrl,"image"));
                    binding.etText.setText("");
                }else {
                    VideoUrl = twitterResponse.getVideos().get(twitterResponse.getVideos().size()-1).getUrl();
                    startDownload(VideoUrl, RootDirectoryTwitter, activity, getFilenameFromURL(VideoUrl,"mp4"));
                    binding.etText.setText("");
                }

            } catch (Exception e) {
                e.printStackTrace();
                Utils.setToast(activity,getResources().getString(R.string.no_media_on_tweet));
            }
        }

        @Override
        public void onError(Throwable e) {
            Utils.hideProgressDialog(activity);
            e.printStackTrace();

        }

        @Override
        public void onComplete() {
            Utils.hideProgressDialog(activity);
        }
    };


    public String getFilenameFromURL(String url, String type) {
        if (type.equals("image")){
            try {
                return new File(new URL(url).getPath()).getName() + "";
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return System.currentTimeMillis() + ".jpg";
            }
        }else {
            try {
                return new File(new URL(url).getPath()).getName() + "";
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return System.currentTimeMillis() + ".mp4";
            }
        }
    }


    public void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);



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
            mInterstitialAd.show(TwitterActivity.this);
        }
    }

    //InterstitialAd : End


}
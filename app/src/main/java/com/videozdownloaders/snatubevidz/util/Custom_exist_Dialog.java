package com.videozdownloaders.snatubevidz.util;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.VideoOptions;

import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;
import com.videozdownloaders.snatubevidz.R;

public class Custom_exist_Dialog extends Dialog implements android.view.View.OnClickListener {

    public Activity c;
    public Dialog d;
    public RelativeLayout yes, no;
    ImageView btn_close;
    OnClickListener listener;
    private NativeAd nativeAd;
    private SharedPreferences adsunit;

    public Custom_exist_Dialog(Activity a, OnClickListener listener) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialogs);
        yes = (RelativeLayout) findViewById(R.id.btnRate);
        no = (RelativeLayout) findViewById(R.id.btnExit);
        btn_close = (ImageView) findViewById(R.id.btnClose);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);
        btn_close.setOnClickListener(this);
        adsunit=c.getSharedPreferences("Adsunit",MODE_PRIVATE);
        refreshAd();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnRate:
                listener.onRateClick();
                break;
            case R.id.btnExit:
                listener.onExitClick();
                break;
            case R.id.btnClose:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }


    private void refreshAd() {

        AdLoader.Builder builder = new AdLoader.Builder(getContext(),adsunit.getString("native", ""));

        builder.forNativeAd(new com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener() {
            @Override
            public void onNativeAdLoaded(com.google.android.gms.ads.nativead.NativeAd unifiedNativeAd) {

                if (nativeAd != null) {
                    nativeAd.destroy();
                }
                nativeAd = unifiedNativeAd;
                showNativeAd();
            }
        });

        VideoOptions videoOptions = new VideoOptions.Builder()
                .setStartMuted(true)
                .build();

        com.google.android.gms.ads.nativead.NativeAdOptions adOptions = new NativeAdOptions.Builder()
                .setVideoOptions(videoOptions)
                .build();

        builder.withNativeAdOptions(adOptions);

        AdLoader adLoader = builder.withAdListener(new AdListener() {


            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
            }
        }).build();

        adLoader.loadAd(new AdRequest.Builder().build());

    }


    private void showNativeAd() {

        NativeAdView adView = (NativeAdView) getLayoutInflater()
                .inflate(R.layout.ad_unified, null);
        populateUnifiedNativeAdView(nativeAd, adView);

        FrameLayout frameLayout = findViewById(R.id.fl_adplaceholder);
        frameLayout.removeAllViews();
        frameLayout.addView(adView);
    }

    private void populateUnifiedNativeAdView(com.google.android.gms.ads.nativead.NativeAd nativeAd, NativeAdView adView) {
        MediaView mediaView = adView.findViewById(R.id.ad_media);

        //mediaView.setImageScaleType(ImageView.ScaleType.CENTER_CROP);
        adView.setMediaView(mediaView);

        // Set other ad assets.
        adView.setHeadlineView(adView.findViewById(R.id.ad_headline));
        adView.setBodyView(adView.findViewById(R.id.ad_body));
        adView.setCallToActionView(adView.findViewById(R.id.ad_call_to_action));
        adView.setIconView(adView.findViewById(R.id.ad_app_icon));
        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        if (nativeAd.getBody() == null) {
            adView.getBodyView().setVisibility(View.INVISIBLE);
        } else {
            adView.getBodyView().setVisibility(View.VISIBLE);
            ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        }

        if (nativeAd.getCallToAction() == null) {
            adView.getCallToActionView().setVisibility(View.INVISIBLE);
        } else {
            adView.getCallToActionView().setVisibility(View.VISIBLE);
            ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
        }

        if (nativeAd.getIcon() == null) {
            adView.getIconView().setVisibility(View.GONE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(
                    nativeAd.getIcon().getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        adView.setNativeAd(nativeAd);
    }

    @Override
    public void onDetachedFromWindow() {
        if (nativeAd != null) {
            nativeAd.destroy();
        }
        super.onDetachedFromWindow();
    }

    public interface OnClickListener {
        public void onRateClick();

        public void onExitClick();
    }
}
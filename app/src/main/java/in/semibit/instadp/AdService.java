package in.semibit.instadp;


import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.List;

public class AdService {

    private static  String INTERSTETIAL_AD_ID = "ca-app-pub-1192734194936989/4622372722";
    private static  String REWARDED_AD_ID = "ca-app-pub-1192734194936989/7715439928";
    private static  String NATIVE_AD_ID = "ca-app-pub-1192734194936989/6515707194";
    private static  String BANNER_AD_ID = "ca-app-pub-1192734194936989/5063024720";


    private final String TAG = AdService.class.getName();
    MainActivity act;

    public static long interstetialCounter = 0;
    public static long adFreqInters = 3;

    public static long nativeCounter = 0;
    public static long adFreqNative = 3;


    List<GenricCallback> onInit = new ArrayList<>();
    public GenricCallback onRewardLoaded = new GenricCallback() {
        @Override
        public void onStart() {

        }
    };
    public GenricCallback onInterstetialLoaded = new GenricCallback() {
        @Override
        public void onStart() {

        }
    };

    public AdService(MainActivity baseActivity) {
        this.act = baseActivity;
        FirebaseApp.initializeApp(baseActivity);

        adFreqNative =FirebaseRemoteConfig.getInstance().getLong("ad_freq_native");
        adFreqInters =FirebaseRemoteConfig.getInstance().getLong("ad_freq_inters");

        INTERSTETIAL_AD_ID = FirebaseRemoteConfig.getInstance().getString("INTERSTETIAL_AD_ID");
        REWARDED_AD_ID = FirebaseRemoteConfig.getInstance().getString("REWARDED_AD_ID");
        NATIVE_AD_ID = FirebaseRemoteConfig.getInstance().getString("NATIVE_AD_ID");
        BANNER_AD_ID = FirebaseRemoteConfig.getInstance().getString("BANNER_AD_ID");
    }

    public boolean init = false;

    public void initAds() {
        MobileAds.initialize(act, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                init = true;
                initializeRewardedAd();
//                initIntertitial();
                if (!onInit.isEmpty()) {
                    for (int i = 0; i < onInit.size(); i++) {
                        try {
                            onInit.get(i).onStart();
                        } catch (Exception e) {
                            FirebaseCrashlytics.getInstance().recordException(e);
                        }
                    }
                }

            }
        });
    }

    public void addWaitForInit(GenricCallback callback) {

        onInit.add(callback);
    }
    ///////////////////////////////// INTERTITIAL

    InterstitialAd mInterstitialAd;

    public void initIntertitial() {
        if (!init)
            return;
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(act, INTERSTETIAL_AD_ID, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i(TAG, "INTERSTETIAL_AD_ID onAdLoaded");
                        onInterstetialLoaded.onStart();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.i(TAG, "INTERSTETIAL_AD_ID " + loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }

    public void showIntertitial(GenricCallback cb) {
        if (mInterstitialAd != null) {
            interstetialCounter = 0;
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {


                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when fullscreen content is dismissed.
                    Log.d("TAG", "The ad was dismissed.");
                    cb.onStart();
                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when fullscreen content failed to show.
                    Log.d("TAG", "The ad failed to show.");
                    cb.onStart();
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when fullscreen content is shown.
                    // Make sure to set your reference to null so you don't
                    // show it a second time.
                    mInterstitialAd = null;
                    initIntertitial();
                    Log.d("TAG", "The ad was shown.");
//                    cb.onStart();
                }
            });
            mInterstitialAd.show(act);
        } else {
            Log.d("TAG", "The interstitial ad wasn't ready yet.");
        }
    }

    ////////////////////////////////REWARDED
    RewardedAd mRewardedAd;

    public void initializeRewardedAd() {
        if (!init)
            return;
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(act, REWARDED_AD_ID,
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        Log.e(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.e(TAG, "REWARDED_AD Ad was loaded.");
                        setUpFullScreenRewardedAd();
                        onRewardLoaded.onStart();
                    }
                });
    }


    public void setUpFullScreenRewardedAd() {
        if (mRewardedAd == null)
            return;
        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdShowedFullScreenContent() {
                // Called when ad is shown.
                Log.d(TAG, "mRewardedAd Ad was shown.");
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
                // Called when ad fails to show.
                Log.d(TAG, "mRewardedAd Ad failed to show.");
            }

            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when ad is dismissed.
                // Set the ad reference to null so you don't show the ad a second time.
                Log.d(TAG, "mRewardedAd Ad was dismissed.");
                mRewardedAd = null;
                initializeRewardedAd();
            }
        });
    }

    public boolean isRewardedAdReady() {
        return mRewardedAd != null;
    }

    public void showRewardedAd(GenricCallback onReward) {
        if (mRewardedAd != null) {
            mRewardedAd.show(act, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d(TAG, "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();
                    mRewardedAd = null;
                    onReward.onStart();
                }
            });
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.");
        }
    }

    /////////////////////
    public void showBannerAd(final ConstraintLayout root) {

        root.removeAllViews();

        AdView adView = new AdView(act);
        try {
            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.WRAP_CONTENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
            adView.setLayoutParams(layoutParams);
            adView.setAdUnitId(BANNER_AD_ID);
            root.addView(adView);
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
            Log.e(TAG, "Native Ad UI error" + e.getMessage());
            return;
        }

        try {
            Display display = act.getWindowManager().getDefaultDisplay();
            DisplayMetrics outMetrics = new DisplayMetrics();
            display.getMetrics(outMetrics);

            float widthPixels = outMetrics.widthPixels;
            float density = outMetrics.density;

            int adWidth = (int) (widthPixels / density);

            AdSize adSize = AdSize.getPortraitAnchoredAdaptiveBannerAdSize(act, adWidth);

            adView.setAdSize(adSize);


        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

        GenricCallback cb = new GenricCallback() {
            @Override
            public void onStart() {
                Log.e(TAG, "Native Ad loading");


                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);

                adView.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        // Code to be executed when an ad finishes loading.
                        Log.e(TAG, "Native Ad Loaded");
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError adError) {
                        // Code to be executed when an ad request fails.
                        Log.e(TAG, "Native Ad Error " + adError.getMessage());

                    }

                    @Override
                    public void onAdOpened() {
                        // Code to be executed when an ad opens an overlay that
                        // covers the screen.
                        Log.e(TAG, "Native Ad Opened");

                    }

                    @Override
                    public void onAdClicked() {

                        AdRequest adRequest = new AdRequest.Builder().build();
                        adView.loadAd(adRequest);
                        Log.e(TAG, "Native Ad Clicked");

                    }

                    @Override
                    public void onAdClosed() {
                        Log.e(TAG, "Native Ad Closed");
                        // Code to be executed when the user is about to return
                        // to the app after tapping on an ad.
                    }
                });


            }
        };
        if (init) {
            cb.onStart();
        } else {
            onInit.add(cb);
        }
    }

}
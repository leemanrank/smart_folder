package com.smartfolder.app.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * AdMob 광고 관리 클래스
 */
object AdManager {

    private const val TAG = "AdManager"

    // 테스트 광고 단위 ID (실제 배포 시 변경 필요)
    object TestAdUnits {
        const val BANNER = "ca-app-pub-3940256099942544/6300978111"
        const val INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
        const val REWARDED = "ca-app-pub-3940256099942544/5224354917"
    }

    // 전면 광고 인스턴스
    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false

    // 보상형 광고 인스턴스
    private var rewardedAd: RewardedAd? = null
    private var isLoadingRewarded = false

    /**
     * AdMob 초기화
     */
    fun initialize(context: Context) {
        MobileAds.initialize(context) { initializationStatus ->
            android.util.Log.d(TAG, "AdMob initialized: ${initializationStatus.adapterStatusMap}")
        }
    }

    /**
     * 배너 광고 AdView 생성
     */
    fun createBannerAdView(context: Context): AdView {
        return AdView(context).apply {
            adUnitId = TestAdUnits.BANNER
            setAdSize(AdSize.BANNER)
        }
    }

    /**
     * 배너 광고 로드
     */
    fun loadBannerAd(adView: AdView) {
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        android.util.Log.d(TAG, "Banner ad loading...")
    }

    /**
     * 전면 광고 로드
     */
    fun loadInterstitialAd(context: Context, onAdLoaded: (() -> Unit)? = null) {
        if (isLoadingInterstitial || interstitialAd != null) {
            android.util.Log.d(TAG, "Interstitial ad already loading or loaded")
            return
        }

        isLoadingInterstitial = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            TestAdUnits.INTERSTITIAL,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    android.util.Log.d(TAG, "Interstitial ad loaded")
                    interstitialAd = ad
                    isLoadingInterstitial = false
                    onAdLoaded?.invoke()

                    // 광고가 닫힐 때 콜백
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            android.util.Log.d(TAG, "Interstitial ad dismissed")
                            interstitialAd = null
                            // 다음 광고 미리 로드
                            loadInterstitialAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            android.util.Log.e(TAG, "Interstitial ad failed to show: ${adError.message}")
                            interstitialAd = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            android.util.Log.d(TAG, "Interstitial ad showed")
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    android.util.Log.e(TAG, "Interstitial ad failed to load: ${loadAdError.message}")
                    interstitialAd = null
                    isLoadingInterstitial = false
                }
            }
        )
    }

    /**
     * 전면 광고 표시
     */
    fun showInterstitialAd(activity: Activity, onAdClosed: (() -> Unit)? = null) {
        if (interstitialAd != null) {
            interstitialAd?.show(activity)
            onAdClosed?.invoke()
        } else {
            android.util.Log.d(TAG, "Interstitial ad not ready yet")
            onAdClosed?.invoke()
            // 광고 로드 시도
            loadInterstitialAd(activity)
        }
    }

    /**
     * 보상형 광고 로드
     */
    fun loadRewardedAd(context: Context, onAdLoaded: (() -> Unit)? = null) {
        if (isLoadingRewarded || rewardedAd != null) {
            android.util.Log.d(TAG, "Rewarded ad already loading or loaded")
            return
        }

        isLoadingRewarded = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            TestAdUnits.REWARDED,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    android.util.Log.d(TAG, "Rewarded ad loaded")
                    rewardedAd = ad
                    isLoadingRewarded = false
                    onAdLoaded?.invoke()

                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            android.util.Log.d(TAG, "Rewarded ad dismissed")
                            rewardedAd = null
                            loadRewardedAd(context)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            android.util.Log.e(TAG, "Rewarded ad failed to show: ${adError.message}")
                            rewardedAd = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            android.util.Log.d(TAG, "Rewarded ad showed")
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    android.util.Log.e(TAG, "Rewarded ad failed to load: ${loadAdError.message}")
                    rewardedAd = null
                    isLoadingRewarded = false
                }
            }
        )
    }

    /**
     * 보상형 광고 표시
     */
    fun showRewardedAd(
        activity: Activity,
        onRewarded: (Int) -> Unit,
        onAdClosed: (() -> Unit)? = null
    ) {
        if (rewardedAd != null) {
            rewardedAd?.show(activity) { rewardItem ->
                val rewardAmount = rewardItem.amount
                android.util.Log.d(TAG, "User earned reward: $rewardAmount")
                onRewarded(rewardAmount)
            }
            onAdClosed?.invoke()
        } else {
            android.util.Log.d(TAG, "Rewarded ad not ready yet")
            onAdClosed?.invoke()
            loadRewardedAd(activity)
        }
    }

    /**
     * 전면 광고 준비 여부
     */
    fun isInterstitialAdReady(): Boolean = interstitialAd != null

    /**
     * 보상형 광고 준비 여부
     */
    fun isRewardedAdReady(): Boolean = rewardedAd != null
}

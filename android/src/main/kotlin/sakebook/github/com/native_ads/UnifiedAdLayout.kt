package sakebook.github.com.native_ads

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.formats.*
import com.google.android.gms.ads.formats.NativeAdOptions.*
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

class UnifiedAdLayout(
    context: Context,
    messenger: BinaryMessenger,
    id: Int,
    arguments: HashMap<String, String>
) : PlatformView {

    private val hostPackageName = arguments["package_name"]
    private val layoutRes = context.resources.getIdentifier(
        arguments["layout_name"],
        "layout",
        hostPackageName
    )
    private val unifiedNativeAdView: UnifiedNativeAdView = View.inflate(context, layoutRes, null) as UnifiedNativeAdView
    private val headlineView: TextView = unifiedNativeAdView.findViewById(
        context.resources.getIdentifier(
            "flutter_native_ad_headline",
            "id",
            hostPackageName
        )
    )
    private val bodyView: TextView = unifiedNativeAdView.findViewById(
        context.resources.getIdentifier(
            "flutter_native_ad_body",
            "id",
            hostPackageName
        )
    )
    private val callToActionView: TextView = unifiedNativeAdView.findViewById(
        context.resources.getIdentifier(
            "flutter_native_ad_call_to_action",
            "id",
            hostPackageName
        )
    )

    private val mediaView: MediaView? = unifiedNativeAdView.findViewById(
        context.resources.getIdentifier(
            "flutter_native_ad_media",
            "id",
            hostPackageName
        )
    )
    private val iconView: ImageView? = unifiedNativeAdView.findViewById(
        context.resources.getIdentifier(
            "flutter_native_ad_icon",
            "id",
            hostPackageName
        )
    )
    private val starRatingView: TextView? = unifiedNativeAdView.findViewById(
        context.resources.getIdentifier(
            "flutter_native_ad_star",
            "id",
            hostPackageName
        )
    )
    private val storeView: TextView? = unifiedNativeAdView.findViewById(
        context.resources.getIdentifier(
            "flutter_native_ad_store",
            "id",
            hostPackageName
        )
    )
    private val priceView: TextView? = unifiedNativeAdView.findViewById(
        context.resources.getIdentifier(
            "flutter_native_ad_price",
            "id",
            hostPackageName
        )
    )
    private val advertiserView: TextView? = unifiedNativeAdView.findViewById(
        context.resources.getIdentifier(
            "flutter_native_ad_advertiser",
            "id",
            hostPackageName
        )
    )

    private val choicesView: AdChoicesView? = unifiedNativeAdView.findViewById(
        context.resources.getIdentifier(
            "flutter_native_choices_view",
            "id",
            hostPackageName
        )
    )

    private val methodChannel: MethodChannel = MethodChannel(
        messenger,
        "com.github.sakebook.android/unified_ad_layout_$id"
    )
    private var ad: UnifiedNativeAd? = null

    init {
        unifiedNativeAdView.findViewById<TextView>(
            context.resources.getIdentifier(
                "flutter_native_ad_attribution",
                "id",
                hostPackageName
            )
        ).apply {
            this.text = arguments["text_attribution"]
        }

        mediaView!!.setImageScaleType(ImageView.ScaleType.CENTER_CROP);

      /*  mediaView!!.setOnHierarchyChangeListener(object : ViewGroup.OnHierarchyChangeListener {
            override fun onChildViewAdded(parent: View?, child: View?) {
                if (child is ImageView) {
                    child.adjustViewBounds = true
                }
            }

            override fun onChildViewRemoved(parent: View?, child: View?) {}
        })*/


        AdLoader.Builder(context, arguments["placement_id"])
                .forUnifiedNativeAd {
                    ad = it
                    ensureUnifiedAd(it)
                }
                .withAdListener(object : AdListener() {
                    override fun onAdImpression() {
                        super.onAdImpression()
                        methodChannel.invokeMethod("onAdImpression", null)
                    }

                    override fun onAdLeftApplication() {
                        super.onAdLeftApplication()
                        methodChannel.invokeMethod("onAdLeftApplication", null)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        methodChannel.invokeMethod("onAdClicked", null)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        super.onAdFailedToLoad(error)
                        methodChannel.invokeMethod(
                            "onAdFailedToLoad",
                            hashMapOf("errorCode" to error.code)
                        )
                    }

                    override fun onAdFailedToLoad(errorCode: Int) {
                        super.onAdFailedToLoad(errorCode)
                        // TODO: Migrate deprecated method.
                        methodChannel.invokeMethod(
                            "onAdFailedToLoad",
                            hashMapOf("errorCode" to errorCode)
                        )
                    }

                    override fun onAdLoaded() {
                        super.onAdLoaded()

                        //fit image cover
//                        for (i in 0 until mediaView!!.childCount) {
//                            val view = mediaView!!.getChildAt(i)
//                            if (view is ImageView) {
//                                (view as ImageView).adjustViewBounds = true
//                            }
//                        }

                        methodChannel.invokeMethod("onAdLoaded", null)
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setMediaAspectRatio(NATIVE_MEDIA_ASPECT_RATIO_PORTRAIT)
                        .setAdChoicesPlacement(ADCHOICES_TOP_RIGHT)
                        .build()
                )
                .build()
                .loadAd(
                    AdRequest.Builder()
                        .build()
                )
    }

    override fun getView(): View {
        return unifiedNativeAdView
    }

    override fun dispose() {
        ad?.destroy()
        unifiedNativeAdView.removeAllViews()
        methodChannel.setMethodCallHandler(null)
    }

    private fun ensureUnifiedAd(ad: UnifiedNativeAd?) {
        headlineView.text = ad?.headline
        bodyView.text = ad?.body
        callToActionView.text = ad?.callToAction

        mediaView?.setMediaContent(ad?.mediaContent)
        iconView?.setImageDrawable(ad?.icon?.drawable)
        starRatingView?.text = "${ad?.starRating}"
        storeView?.text = ad?.store
        priceView?.text = ad?.price
        advertiserView?.text = ad?.advertiser

        unifiedNativeAdView.bodyView = bodyView
        unifiedNativeAdView.headlineView = headlineView
        unifiedNativeAdView.callToActionView = callToActionView

        unifiedNativeAdView.mediaView = mediaView
        unifiedNativeAdView.iconView = iconView
        unifiedNativeAdView.starRatingView = starRatingView
        unifiedNativeAdView.storeView = storeView
        unifiedNativeAdView.priceView = priceView
        unifiedNativeAdView.advertiserView = advertiserView

        unifiedNativeAdView.adChoicesView = choicesView;

        unifiedNativeAdView.setNativeAd(ad)
    }
}

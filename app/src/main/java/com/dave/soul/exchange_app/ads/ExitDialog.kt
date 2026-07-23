package com.dave.soul.exchange_app.ads

import android.content.Intent
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import com.dave.soul.exchange_app.R
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/**
 * 뒤로가기 종료 팝업 — 네이티브 광고(크게) + 하단 [종료 · 리뷰 · 취소] (왼쪽부터 고정).
 * 광고 미로드 시 광고 영역 없이 동일 다이얼로그.
 */
@Composable
fun ExitDialog(
    nativeAd: NativeAd?,
    onExit: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = stringResource(R.string.exit_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                if (nativeAd != null) {
                    Spacer(Modifier.height(14.dp))
                    NativeAdCard(nativeAd, ctaFallback = stringResource(R.string.exit_ad_cta))
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    TextButton(onClick = onExit) { Text(stringResource(R.string.exit_confirm)) }
                    TextButton(onClick = {
                        val pkg = context.packageName
                        val market = Intent(Intent.ACTION_VIEW, "market://details?id=$pkg".toUri())
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { context.startActivity(market) }.onFailure {
                            val web = Intent(
                                Intent.ACTION_VIEW,
                                "https://play.google.com/store/apps/details?id=$pkg".toUri(),
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            runCatching { context.startActivity(web) }
                        }
                        onDismiss()
                    }) { Text(stringResource(R.string.exit_review)) }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.exit_cancel)) }
                }
            }
        }
    }
}

/** 프로그래매틱 NativeAdView — 미디어(대형) + 아이콘/헤드라인 + 본문 + CTA. */
@Composable
private fun NativeAdCard(ad: NativeAd, ctaFallback: String) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { ctx ->
            val density = ctx.resources.displayMetrics.density
            fun dp(v: Int) = (v * density).toInt()

            val adView = NativeAdView(ctx)
            val root = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                )
            }

            val media = MediaView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(170),
                )
            }
            root.addView(media)

            val headRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(10), 0, 0)
            }
            val icon = ImageView(ctx).apply {
                layoutParams = LinearLayout.LayoutParams(dp(36), dp(36))
            }
            val headline = TextView(ctx).apply {
                textSize = 15f
                setTypeface(typeface, Typeface.BOLD)
                maxLines = 1
                setPadding(dp(8), 0, 0, 0)
            }
            headRow.addView(icon)
            headRow.addView(headline)
            root.addView(headRow)

            val body = TextView(ctx).apply {
                textSize = 13f
                maxLines = 2
                setPadding(0, dp(6), 0, 0)
            }
            root.addView(body)

            val cta = Button(ctx).apply {
                isAllCaps = false
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { topMargin = dp(8) }
            }
            root.addView(cta)

            adView.addView(root)
            adView.mediaView = media
            adView.iconView = icon
            adView.headlineView = headline
            adView.bodyView = body
            adView.callToActionView = cta
            adView
        },
        update = { adView ->
            (adView.headlineView as TextView).text = ad.headline.orEmpty()
            (adView.bodyView as TextView).text = ad.body.orEmpty()
            (adView.callToActionView as Button).text = ad.callToAction ?: ctaFallback
            val iconView = adView.iconView as ImageView
            val icon = ad.icon
            if (icon != null) {
                iconView.setImageDrawable(icon.drawable)
                iconView.visibility = View.VISIBLE
            } else {
                iconView.visibility = View.GONE
            }
            ad.mediaContent?.let { adView.mediaView?.mediaContent = it }
            adView.setNativeAd(ad)
        },
    )
}

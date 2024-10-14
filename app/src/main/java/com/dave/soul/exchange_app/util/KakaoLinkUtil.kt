package com.dave.soul.exchange_app.util

import android.content.Context
import com.kakao.sdk.share.ShareClient
import com.kakao.sdk.template.model.Button
import com.kakao.sdk.template.model.Content
import com.kakao.sdk.template.model.FeedTemplate
import com.kakao.sdk.template.model.Link
import org.jetbrains.anko.toast

/**
 * Created by soul on 2017. 8. 6..
 */
object KakaoLinkUtil {
    val PLAY_STORE_URL =
        "https://play.google.com/store/apps/details?id=com.dave.soul.exchange_app"
    private val MAIN_IMAGE_URL =
        "https://lh3.googleusercontent.com/Km9yKP5Jtd0oeGgKCVHkYE_OPJgtcoOgyGJrBWCiXtWdbE32-PB90bFjocpdHjQw-g=w300-rw"

    fun sendLink(context: Context, title: String, description: String?) {
        if (!isKakaoTalkSharingAvailable(context)) {
            return
        }
        val params: FeedTemplate = getMessageFeed(title, description)
        ShareClient.instance.shareDefault(context, params) { sharingResult, error ->
            when {
                error != null -> context.toast("카카오톡 공유 실패")
                (sharingResult != null) -> context.startActivity(sharingResult.intent)
                else -> {} // Do nothing
            }
        }
    }

    fun isKakaoTalkSharingAvailable(context: Context) =
        ShareClient.instance.isKakaoTalkSharingAvailable(context)

    private fun getMessageFeed(
        title: String,
        description: String?,
    ) = FeedTemplate(
        content = Content(
            title = "환율 알리미 - [$title]",
            description = description,
            imageUrl = MAIN_IMAGE_URL,
            link = Link(
                webUrl = PLAY_STORE_URL,
                mobileWebUrl = PLAY_STORE_URL,
            ),
        ),
        buttons = listOf(
            Button(
                "앱에서 보기",
                Link(
                    webUrl = PLAY_STORE_URL,
                    mobileWebUrl = PLAY_STORE_URL,
                ),
            ),
        ),
    )
}

package com.dave.soul.exchange_app.util;

import android.content.Context;
import android.util.Log;

import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ButtonObject;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.FeedTemplate;
import com.kakao.message.template.LinkObject;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;

/**
 * Created by soul on 2017. 8. 6..
 */

public class KakaoLinkUtil {

    private static final String TAG = KakaoLinkUtil.class.getSimpleName();
    public final String PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.dave.soul.exchange_app";
    public final String MAIN_IMAGE_URL = "https://lh3.googleusercontent.com/Km9yKP5Jtd0oeGgKCVHkYE_OPJgtcoOgyGJrBWCiXtWdbE32-PB90bFjocpdHjQw-g=w300-rw";

    private KakaoLinkUtil(){
    }

    private static class SingletonHolder {
        public static final KakaoLinkUtil INSTANCE = new KakaoLinkUtil();
    }

    public static KakaoLinkUtil getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public boolean isKakaoLinkAvailable(Context context){
        return KakaoLinkService.getInstance().isKakaoLinkV2Available(context);
    }

    public void sendLink(Context context, String title, String descrption){
        FeedTemplate params = FeedTemplate
                .newBuilder(ContentObject.newBuilder("환율 알리미 - ["+title+"]",
                        MAIN_IMAGE_URL,
                        LinkObject.newBuilder().setWebUrl(PLAY_STORE_URL)
                                .setMobileWebUrl(PLAY_STORE_URL).build())
                        .setDescrption(descrption)
                        .setImageHeight(120)
                        .setImageWidth(120)
                        .build())
                .addButton(new ButtonObject("앱에서 보기", LinkObject.newBuilder()
                        .setWebUrl(PLAY_STORE_URL)
                        .setMobileWebUrl(PLAY_STORE_URL)
                        .build()))
                .build();


        KakaoLinkService.getInstance().sendDefault(context, params, new ResponseCallback<KakaoLinkResponse>() {

            @Override
            public void onFailure(ErrorResult errorResult) {
                Log.d(TAG, errorResult.toString());
            }

            @Override
            public void onSuccess(KakaoLinkResponse result) {
                Log.d(TAG, result.toString());
            }
        });

    }
}

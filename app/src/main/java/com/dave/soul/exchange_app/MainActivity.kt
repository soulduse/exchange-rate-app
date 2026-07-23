package com.dave.soul.exchange_app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.dave.soul.exchange_app.ads.AdsManager
import com.dave.soul.exchange_app.core.prefs.UserPrefs
import com.dave.soul.exchange_app.push.DeviceRegistrar
import com.dave.soul.exchange_app.ui.common.SplashGate
import com.dave.soul.exchange_app.ui.nav.ExchangeNavRoot
import com.dave.soul.exchange_app.ui.theme.ExchangeTheme
import com.dave.soul.exchange_app.ui.theme.resolveDarkTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var deviceRegistrar: DeviceRegistrar
    @Inject lateinit var prefs: UserPrefs
    @Inject lateinit var adsManager: AdsManager

    /** 콜드 스타트 세션 번호 — 1이면 런치 전면광고 면제. 로드 전(null)엔 면제로 취급. */
    private var launchCount by mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by prefs.themeMode.collectAsState(initial = "SYSTEM")
            val darkTheme = resolveDarkTheme(themeMode)
            // 인앱 테마가 시스템 설정과 다를 수 있어 상태바/내비바 아이콘 대비를 테마에 맞춰 갱신
            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle =
                        SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                    navigationBarStyle =
                        SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { darkTheme },
                )
                onDispose {}
            }
            var gatePassed by remember { mutableStateOf(false) }
            ExchangeTheme(darkTheme = darkTheme) {
                if (!gatePassed) {
                    SplashGate(
                        adsManager = adsManager,
                        adExempt = launchCount?.let { it <= 1 },
                        onFinished = { gatePassed = true },
                    )
                } else {
                    ExchangeNavRoot(adsManager = adsManager)
                }
            }
        }
        adsManager.initialize(this)
        lifecycleScope.launch {
            launchCount = prefs.incrementLaunchCount()
            deviceRegistrar.ensureRegistered()
        }
    }
}

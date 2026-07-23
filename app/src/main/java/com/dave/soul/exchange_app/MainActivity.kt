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
import androidx.lifecycle.lifecycleScope
import com.dave.soul.exchange_app.ads.AdsManager
import com.dave.soul.exchange_app.core.prefs.UserPrefs
import com.dave.soul.exchange_app.push.DeviceRegistrar
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

    override fun onCreate(savedInstanceState: Bundle?) {
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
            ExchangeTheme(darkTheme = darkTheme) {
                ExchangeNavRoot(adsManager = adsManager)
            }
        }
        adsManager.initialize(this)
        lifecycleScope.launch { deviceRegistrar.ensureRegistered() }
    }
}

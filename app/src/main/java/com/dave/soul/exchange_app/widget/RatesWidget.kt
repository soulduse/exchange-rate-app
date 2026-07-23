package com.dave.soul.exchange_app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.cornerRadius
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.LocalSize
import com.dave.soul.exchange_app.MainActivity
import com.dave.soul.exchange_app.core.db.RateDao
import com.dave.soul.exchange_app.core.prefs.UserPrefs
import com.dave.soul.exchange_app.core.rates.CrossRates
import com.dave.soul.exchange_app.core.util.flagEmoji
import com.dave.soul.exchange_app.core.util.formatRate
import com.dave.soul.exchange_app.core.util.formatSigned
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import androidx.compose.ui.unit.DpSize
import com.dave.soul.exchange_app.R
import kotlinx.coroutines.flow.first

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun rateDao(): RateDao
    fun userPrefs(): UserPrefs
}

/** 위젯 표시용 행 스냅샷 — 크로스 모드는 change 없이 changeRatio(%)만. */
data class WidgetRate(
    val code: String,
    val countryCode: String,
    val price: Double,
    val change: Double?,
    val changeRatio: Double?,
    val isVirtualKrw: Boolean,
)

class RatesWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, LARGE))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entry = EntryPointAccessors.fromApplication(context, WidgetEntryPoint::class.java)
        val rates = entry.rateDao().observeAll().first()
        val selected = entry.userPrefs().selectedCodes.first()
        val base = entry.userPrefs().baseCurrency.first()
        val rows = CrossRates.displayRates(
            rates = rates,
            selected = selected,
            base = base,
            krwName = context.getString(R.string.calc_krw_name),
        ).map {
            WidgetRate(it.code, it.countryCode, it.value, it.change, it.changeRatio, it.isVirtualKrw)
        }
        // Glance @Composable 에는 LocalContext 가 없어 여기서 문자열을 확보해 전달
        val emptyText = context.getString(R.string.widget_empty)
        provideContent {
            GlanceTheme {
                WidgetContent(rows, emptyText)
            }
        }
    }

    companion object {
        val SMALL = DpSize(140.dp, 40.dp)
        val LARGE = DpSize(250.dp, 140.dp)
    }
}

class RatesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RatesWidget()
}

// 다크/라이트 대응 색 (day, night)
private val BgColor = ColorProvider(Color(0xF2FFFFFF), Color(0xF2181B22))
private val TextMain = ColorProvider(Color(0xFF171B24), Color(0xFFE7EAF1))
private val TextSub = ColorProvider(Color(0xFF6B7385), Color(0xFF98A0B3))
private val Rise = ColorProvider(Color(0xFFE5484D), Color(0xFFFF7A7E))
private val Fall = ColorProvider(Color(0xFF3B6EF6), Color(0xFF8AA6FF))

@Composable
private fun WidgetContent(rows: List<WidgetRate>, emptyText: String) {
    val size = LocalSize.current
    val isLarge = size.height >= 100.dp
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(BgColor)
            .cornerRadius(16.dp)
            .padding(12.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (rows.isEmpty()) {
            Text(emptyText, style = TextStyle(color = TextSub, fontSize = 12.sp))
        } else if (!isLarge) {
            // 소형 1행 — 크로스 모드의 KRW 가상 행 대신 첫 실통화를 보여준다
            WidgetRow(rows.firstOrNull { !it.isVirtualKrw } ?: rows.first(), large = true)
        } else {
            rows.take(4).forEachIndexed { index, row ->
                if (index > 0) Spacer(GlanceModifier.height(6.dp))
                WidgetRow(row, large = false)
            }
        }
    }
}

@Composable
private fun WidgetRow(rate: WidgetRate, large: Boolean) {
    // KRW 기준=절대 등락, 크로스=합성 등락률(%) — 홈 ChangeBadge 와 동일 규칙
    val signal = rate.change ?: rate.changeRatio ?: 0.0
    val changeColor = if (signal >= 0) Rise else Fall
    val changeText =
        if (rate.change != null) formatSigned(rate.change)
        else "${formatSigned(rate.changeRatio ?: 0.0)}%"
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "${flagEmoji(rate.countryCode)} ${rate.code}",
            style = TextStyle(
                color = TextMain,
                fontSize = if (large) 14.sp else 13.sp,
                fontWeight = FontWeight.Medium,
            ),
        )
        Spacer(GlanceModifier.defaultWeight())
        Text(
            formatRate(rate.price),
            style = TextStyle(
                color = TextMain,
                fontSize = if (large) 18.sp else 14.sp,
                fontWeight = FontWeight.Bold,
            ),
        )
        Spacer(GlanceModifier.width(6.dp))
        Text(
            changeText,
            style = TextStyle(color = changeColor, fontSize = if (large) 13.sp else 11.sp),
        )
    }
}

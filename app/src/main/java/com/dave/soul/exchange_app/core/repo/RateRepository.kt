package com.dave.soul.exchange_app.core.repo

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.dave.soul.exchange_app.core.db.RateDao
import com.dave.soul.exchange_app.core.db.RateEntity
import com.dave.soul.exchange_app.core.network.ExchangeApi
import com.dave.soul.exchange_app.core.network.HistoryResponse
import com.dave.soul.exchange_app.widget.RatesWidget
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/** 시세 저장소 — Room 이 단일 진실 원천, 네트워크는 refresh 로만 유입(오프라인 우선). */
@Singleton
class RateRepository @Inject constructor(
    private val api: ExchangeApi,
    private val dao: RateDao,
    @ApplicationContext private val context: Context,
) {

    val rates: Flow<List<RateEntity>> = dao.observeAll()

    fun rate(code: String): Flow<RateEntity?> = dao.observeByCode(code)

    /** 서버 보드 갱신 — 실패해도 기존 캐시 유지(마지막 시세 서빙). 성공 시 위젯 동기화. */
    suspend fun refresh(): Result<Unit> = runCatching {
        val board = api.getRates()
        if (board.rates.isEmpty()) return@runCatching
        val now = System.currentTimeMillis()
        dao.upsertAll(board.rates.map { RateEntity.from(it, now) })
        runCatching { RatesWidget().updateAll(context) }
        Unit
    }.onFailure { Log.w(TAG, "refresh failed", it) }

    private companion object {
        const val TAG = "RateRepository"
    }

    suspend fun history(code: String, range: String): Result<HistoryResponse> =
        runCatching { api.getHistory(code, range) }
}

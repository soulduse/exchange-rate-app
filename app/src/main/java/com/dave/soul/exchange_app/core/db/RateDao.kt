package com.dave.soul.exchange_app.core.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RateDao {

    @Query("SELECT * FROM rates ORDER BY name")
    fun observeAll(): Flow<List<RateEntity>>

    @Query("SELECT * FROM rates WHERE currencyCode = :code")
    fun observeByCode(code: String): Flow<RateEntity?>

    @Upsert
    suspend fun upsertAll(rates: List<RateEntity>)
}

package com.dave.soul.exchange_app

import android.app.Application
import com.dave.soul.exchange_app.di.appModule
import com.google.android.gms.ads.MobileAds
import io.realm.Realm
import io.realm.RealmConfiguration
import org.koin.android.ext.android.startKoin
import timber.log.Timber

/**
 * Created by soul on 2017. 3. 21..
 */

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initTimberLog()
        Realm.init(this)
        val config = RealmConfiguration.Builder()
            .name(Realm.DEFAULT_REALM_NAME)
            .schemaVersion(0)
            .deleteRealmIfMigrationNeeded()
            .build()
        Realm.setDefaultConfiguration(config)

        startKoin(this, listOf(appModule))

        MobileAds.initialize(this)
        /*
        RealmConfiguration myConfig = new RealmConfiguration.Builder(context)
                .name("myrealm.realm")
                .schemaVersion(2)
                .setModules(new MyCustomSchema())
                .build();

        RealmConfiguration otherConfig = new RealmConfiguration.Builder(context)
                .name("otherrealm.realm")
                .schemaVersion(5)
                .setModules(new MyOtherSchema())
                .build();

        Realm myRealm = Realm.getInstance(myConfig);
        Realm otherRealm = Realm.getInstance(otherConfig);
        */
    }

    private fun initTimberLog() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}

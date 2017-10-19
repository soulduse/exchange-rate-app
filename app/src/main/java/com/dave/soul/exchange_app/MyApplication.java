package com.dave.soul.exchange_app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by soul on 2017. 3. 21..
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {

        super.onCreate();
        Fabric.with(this, new Crashlytics());

        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name(Realm.DEFAULT_REALM_NAME)
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);

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
}

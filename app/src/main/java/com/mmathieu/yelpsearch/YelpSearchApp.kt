package com.mmathieu.yelpsearch

import android.app.Application
import com.mmathieu.yelpsearch.di.AppComponent
import com.mmathieu.yelpsearch.di.AppModule
import com.mmathieu.yelpsearch.di.DaggerAppComponent

class YelpSearchApp : Application() {

    internal lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()

        yelpSearchApp = this

        appComponent = DaggerAppComponent.builder()
            .appModule(AppModule(this))
            .build()
    }

    companion object {
        lateinit var yelpSearchApp: YelpSearchApp
    }

}
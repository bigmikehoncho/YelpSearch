package com.mmathieu.yelpsearch.di

import com.mmathieu.yelpsearch.view.main.MainActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class])
internal interface AppComponent {

    fun inject(activity: MainActivity)
}
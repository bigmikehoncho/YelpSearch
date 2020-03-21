package com.mmathieu.yelpsearch.di

import android.app.Application
import com.mmathieu.yelpsearch.api.YelpService
import com.mmathieu.yelpsearch.repository.YelpRepository
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module(includes = [ViewModelModule::class])
class AppModule(private val app: Application) {

    @Provides
    @Singleton
    fun providesApplication(): Application = app

    @Singleton
    @Provides
    fun provideYelpService(): YelpService {
        return Retrofit.Builder()
            .baseUrl(YelpService.BASE_URL)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(MoshiConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request()
                            .newBuilder()
                            .addHeader("Authorization", "Bearer ${YelpService.API_KEY}")
                            .build()
                        chain.proceed(request)
                    }.build()
            )
            .build()
            .create(YelpService::class.java)
    }
}
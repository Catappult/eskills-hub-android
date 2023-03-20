package com.asfoundation.wallet.di.api

import com.appcoins.wallet.core.analytics.analytics.AnalyticsAPI
import com.asfoundation.wallet.di.annotations.DefaultHttpClient
import com.fasterxml.jackson.databind.ObjectMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.jackson.JacksonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AnalyticsApiModule {

  private val analyticsUrl = "https://ws75.aptoide.com/api/7/"

  @Singleton
  @Provides
  @Named("analytics-default")
  fun provideAnalyticsDefaultRetrofit(
    @DefaultHttpClient client: OkHttpClient,
    objectMapper: ObjectMapper
  ): Retrofit {
    return Retrofit.Builder()
      .baseUrl(analyticsUrl)
      .client(client)
      .addConverterFactory(JacksonConverterFactory.create(objectMapper))
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  fun provideAnalyticsAPI(
    @Named("analytics-default") retrofit: Retrofit
  ): AnalyticsAPI {
    return retrofit.create(AnalyticsAPI::class.java)
  }
}
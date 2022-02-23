package com.asfoundation.wallet.di.api.microservices

import com.appcoins.wallet.bdsbilling.subscriptions.SubscriptionBillingApi
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.di.annotations.BlockchainHttpClient
import com.asfoundation.wallet.di.annotations.DefaultHttpClient
import com.asfoundation.wallet.subscriptions.UserSubscriptionApi
import com.asfoundation.wallet.topup.TopUpValuesService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class ProductApiModule {

  private val productV1Url = "${BuildConfig.BASE_HOST}/product/"
  private val productV2Url = "${BuildConfig.SUBS_BASE_HOST}/productv2/"

  @Singleton
  @Provides
  @Named("product-v1-default")
  fun provideProductV1DefaultRetrofit(@DefaultHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(productV1Url)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  @Named("product-v2-blockchain")
  fun provideSubscriptionsBlockchainRetrofit(@BlockchainHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(productV2Url)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  @Named("product-v2-default")
  fun provideSubscriptionsDefaultRetrofit(@DefaultHttpClient client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
      .baseUrl(productV2Url)
      .client(client)
      .addConverterFactory(GsonConverterFactory.create())
      .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
      .build()
  }

  @Singleton
  @Provides
  fun providesTopUpValuesApi(
    @Named("product-v1-default") retrofit: Retrofit
  ): TopUpValuesService.TopUpValuesApi {
    return retrofit.create(TopUpValuesService.TopUpValuesApi::class.java)
  }

  @Provides
  fun providesSubscriptionBillingApi(
    @Named("product-v2-blockchain") retrofit: Retrofit
  ): SubscriptionBillingApi {
    return retrofit.create(SubscriptionBillingApi::class.java)
  }

  @Provides
  fun providesUserSubscriptionApi(
    @Named("product-v2-default") retrofit: Retrofit
  ): UserSubscriptionApi {
    return retrofit.create(UserSubscriptionApi::class.java)
  }
}
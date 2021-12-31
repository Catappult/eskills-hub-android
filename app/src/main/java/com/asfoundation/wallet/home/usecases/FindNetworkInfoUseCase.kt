package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.entity.NetworkInfo
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class FindNetworkInfoUseCase @Inject constructor(private val networkInfo: NetworkInfo) {

  operator fun invoke(): Single<NetworkInfo> {
    return Single.just(networkInfo)
        .observeOn(Schedulers.io())
  }
}
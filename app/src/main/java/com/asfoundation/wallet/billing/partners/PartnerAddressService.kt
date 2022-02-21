package com.asfoundation.wallet.billing.partners

import com.asf.wallet.BuildConfig
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = AddressService::class)
class PartnerAddressService @Inject constructor(private val installerService: InstallerService,
                                                private val oemIdExtractorService: OemIdExtractorService) :
    AddressService {

  private val defaultStoreAddress: String = BuildConfig.DEFAULT_STORE_ADDRESS
  private val defaultOemAddress: String = BuildConfig.DEFAULT_OEM_ADDRESS

  override fun getStoreAddress(suggestedStoreAddress: String?): String {
    return suggestedStoreAddress?.let { suggestedStoreAddress } ?: defaultStoreAddress
  }

  override fun getOemAddress(suggestedOemAddress: String?): String {
    return suggestedOemAddress?.let { suggestedOemAddress } ?: defaultOemAddress
  }

  override fun getAttributionEntity(packageName: String): Single<AttributionEntity> {
    return Single.zip(installerService.getInstallerPackageName(packageName),
        oemIdExtractorService.extractOemId(packageName),
        { installerPackage, oemId ->
          AttributionEntity(oemId.ifEmpty { null }, installerPackage.ifEmpty { null })
        })
  }
}
package com.asfoundation.wallet.permissions

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appcoins.wallet.permissions.PermissionName
import com.asf.wallet.R
import com.jakewharton.rxbinding2.view.RxView
import dagger.android.support.DaggerFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_permissions_layout.*
import javax.inject.Inject

class PermissionFragment : DaggerFragment(), PermissionFragmentView {
  companion object {
    private const val CALLING_PACKAGE = "calling_package_key"
    private const val PERMISSION_KEY = "permission_key"
    private const val APK_SIGNATURE_KEY = "apk_signature_key"

    fun newInstance(callingPackage: String, apkSignature: String,
                    permission: PermissionName): PermissionFragment {

      return PermissionFragment().apply {
        arguments = Bundle().apply {
          putString(CALLING_PACKAGE, callingPackage)
          putString(APK_SIGNATURE_KEY, apkSignature)
          putSerializable(PERMISSION_KEY, permission)
        }
      }
    }
  }

  @Inject
  lateinit var permissionsInteractor: PermissionsInteractor
  lateinit var navigator: PermissionFragmentNavigator
  private lateinit var presenter: PermissionsFragmentPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val permission: PermissionName = arguments?.getSerializable(PERMISSION_KEY) as PermissionName
    presenter = PermissionsFragmentPresenter(this, permissionsInteractor,
        arguments?.getString(CALLING_PACKAGE)!!, permission,
        arguments?.getString(APK_SIGNATURE_KEY)!!, CompositeDisposable(),
        AndroidSchedulers.mainThread())
  }

  override fun getAllowButtonClick(): Observable<Any> {
    return RxView.clicks(provide_wallet_always_allow_button)
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    when (context) {
      is PermissionFragmentNavigator -> navigator = context
      else -> throw IllegalArgumentException(
          "${PermissionFragment::class} has to be attached to an activity that implements ${PermissionFragmentNavigator::class}")
    }
  }

  override fun closeSuccess(walletAddress: String) {
    navigator.closeSuccess(walletAddress)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                            savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.fragment_permissions_layout, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    presenter.present()
  }

  override fun onDestroyView() {
    presenter.stop()
    super.onDestroyView()
  }
}

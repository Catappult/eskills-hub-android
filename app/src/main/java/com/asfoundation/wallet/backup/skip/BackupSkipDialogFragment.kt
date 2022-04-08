package com.asfoundation.wallet.backup.skip

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import by.kirich1409.viewbindingdelegate.viewBinding
import com.asf.wallet.R
import com.asf.wallet.databinding.BackupSkipDialogFragmentBinding
import com.asfoundation.wallet.backup.triggers.BackupTriggerDialogFragment
import com.asfoundation.wallet.base.SideEffect
import com.asfoundation.wallet.base.SingleStateFragment
import com.asfoundation.wallet.base.ViewState
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BackupSkipDialogFragment : BottomSheetDialogFragment(),
  SingleStateFragment<ViewState, SideEffect> {


  @Inject
  lateinit var navigator: BackupSkipDialogNavigator

  private val views by viewBinding(BackupSkipDialogFragmentBinding::bind)

  companion object {
    @JvmStatic
    fun newInstance(walletAddress: String, triggerSource: String): BackupSkipDialogFragment {
      return BackupSkipDialogFragment()
        .apply {
          arguments = Bundle().apply {
            putString(BackupTriggerDialogFragment.WALLET_ADDRESS_KEY, walletAddress)
            putString(BackupTriggerDialogFragment.TRIGGER_SOURCE, triggerSource)
          }
        }
    }
  }

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    return inflater.inflate(R.layout.backup_skip_dialog_fragment, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    views.confirm.setOnClickListener {
      navigator.finishBackup()
    }
    views.cancel.setOnClickListener {
      navigator.navigateBack(
        requireArguments().getString(BackupTriggerDialogFragment.WALLET_ADDRESS_KEY)!!,
        requireArguments().getString(BackupTriggerDialogFragment.TRIGGER_SOURCE)!!
      )
    }
  }

  override fun onStart() {
    val behavior = BottomSheetBehavior.from(requireView().parent as View)
    behavior.state = BottomSheetBehavior.STATE_EXPANDED
    super.onStart()
  }

  override fun getTheme(): Int {
    return R.style.AppBottomSheetDialogThemeNoFloating
  }

  override fun onStateChanged(state: ViewState) = Unit

  override fun onSideEffect(sideEffect: SideEffect) = Unit
}
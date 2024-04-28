package com.asfoundation.wallet.manage_cards

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.Nullable
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import by.kirich1409.viewbindingdelegate.viewBinding
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.redirect.RedirectConfiguration
import com.appcoins.wallet.billing.adyen.PaymentInfoModel
import com.appcoins.wallet.core.arch.SingleStateFragment
import com.appcoins.wallet.core.arch.data.Async
import com.appcoins.wallet.core.utils.android_common.KeyboardUtils
import com.appcoins.wallet.ui.widgets.TopBar
import com.asf.wallet.BuildConfig
import com.asf.wallet.R
import com.asf.wallet.databinding.ManageAdyenPaymentFragmentBinding
import com.asfoundation.wallet.billing.adyen.AdyenCardWrapper
import com.asfoundation.wallet.util.AdyenCardView
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ManageAdyenPaymentFragment : BasePageViewFragment(),
  SingleStateFragment<ManageAdyenPaymentState, ManageAdyenPaymentSideEffect> {

  private val viewModel: ManageAdyenPaymentViewModel by viewModels()
  private val views by viewBinding(ManageAdyenPaymentFragmentBinding::bind)

  //configurations
  private lateinit var cardConfiguration: CardConfiguration
  private lateinit var redirectConfiguration: RedirectConfiguration
  private lateinit var adyen3DS2Configuration: Adyen3DS2Configuration

  //components
  private lateinit var adyenCardComponent: CardComponent
  private lateinit var redirectComponent: RedirectComponent
  private lateinit var adyen3DS2Component: Adyen3DS2Component
  private lateinit var adyenCardView: AdyenCardView
  private lateinit var adyenCardWrapper: AdyenCardWrapper

  private lateinit var webViewLauncher: ActivityResultLauncher<Intent>
  private lateinit var outerNavController: NavController

  @Inject
  lateinit var navigator: ManageAdyenPaymentNavigator

  @Inject
  lateinit var adyenEnvironment: Environment

  private val manageCardSharedViewModel: ManageCardSharedViewModel by activityViewModels()

  override fun onCreateView(
    inflater: LayoutInflater, @Nullable container: ViewGroup?,
    @Nullable savedInstanceState: Bundle?
  ): View {
    return ManageAdyenPaymentFragmentBinding.inflate(inflater).root
  }

  override fun onViewCreated(view: View, @Nullable savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setupUi()
    clickListeners()
    viewModel.collectStateAndEvents(lifecycle, viewLifecycleOwner.lifecycleScope)
    view.findViewById<ComposeView>(R.id.app_bar).apply {
      setContent {
        TopBar(isMainBar = false, onClickSupport = { viewModel.displayChat() })
      }
    }
  }

  private fun clickListeners() {
    requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
      navigator.navigateBack()
    }
    views.manageWalletAddCardSubmitButton.setOnClickListener {
      viewModel.handleBuyClick(
        adyenCardWrapper,
        RedirectComponent.getReturnUrl(requireContext())
      )
    }
  }

  override fun onStateChanged(state: ManageAdyenPaymentState) {
    when (state.paymentInfoModel) {
      Async.Uninitialized,
      is Async.Loading -> {
        views.loadingAnimation.playAnimation()
      }

      is Async.Success -> {
        state.paymentInfoModel()?.let {
          prepareCardComponent(it)
        }
      }

      is Async.Fail -> Unit
    }
  }

  override fun onSideEffect(sideEffect: ManageAdyenPaymentSideEffect) {
    when (sideEffect) {
      is ManageAdyenPaymentSideEffect.NavigateToPaymentResult -> {
        manageCardSharedViewModel.onCardSaved()
        navigator.navigateBack()
      }

      ManageAdyenPaymentSideEffect.NavigateBackToPaymentMethods -> navigator.navigateBack()
      ManageAdyenPaymentSideEffect.ShowLoading -> showLoading(shouldShow = true)
      is ManageAdyenPaymentSideEffect.Handle3DS -> handle3DSAction(sideEffect.action)
      ManageAdyenPaymentSideEffect.ShowCvvError -> handleCVCError()
    }
  }

  private fun setupUi() {
    adyenCardView = AdyenCardView(views.adyenCardForm)
    setupConfiguration()
    setup3DSComponent()
    manageCardSharedViewModel.resetCardSavedValue()
  }

  private fun prepareCardComponent(paymentInfoModel: PaymentInfoModel) {
    showLoading(false)
    adyenCardComponent = paymentInfoModel.cardComponent!!(this, cardConfiguration)
    views.adyenCardForm.attach(adyenCardComponent, this)
    adyenCardComponent.observe(this) {
      if (it != null && it.isValid) {
        views.manageWalletAddCardSubmitButton.isEnabled = true
        view?.let { view -> KeyboardUtils.hideKeyboard(view) }
        it.data.paymentMethod?.let { paymentMethod ->
          val hasCvc = !paymentMethod.encryptedSecurityCode.isNullOrEmpty()
          adyenCardWrapper = AdyenCardWrapper(
            paymentMethod,
            adyenCardView.cardSave,
            hasCvc,
            paymentInfoModel.supportedShopperInteractions
          )
        }
      } else {
        views.manageWalletAddCardSubmitButton.isEnabled = false
      }
    }
  }

  private fun showLoading(shouldShow: Boolean) {
    views.manageAdyenPaymentTitle.visibility = if (shouldShow) View.GONE else View.VISIBLE
    views.adyenCardForm.visibility = if (shouldShow) View.GONE else View.VISIBLE
    adyenCardView.adyenSaveDetailsSwitch?.visibility = View.GONE
    views.manageWalletAddCardSubmitButton.visibility =
      if (shouldShow) View.GONE else View.VISIBLE
    views.loadingAnimation.visibility = if (shouldShow) View.VISIBLE else View.GONE
  }

  private fun setupConfiguration() {
    setupCardConfiguration()
    setupAdyen3DS2Configuration()
  }

  private fun setupCardConfiguration() {
    cardConfiguration = CardConfiguration.Builder(requireContext(), BuildConfig.ADYEN_PUBLIC_KEY)
      .setEnvironment(adyenEnvironment).build()
  }

  private fun setupAdyen3DS2Configuration() {
    adyen3DS2Configuration =
      Adyen3DS2Configuration.Builder(requireContext(), BuildConfig.ADYEN_PUBLIC_KEY)
        .setEnvironment(adyenEnvironment).build()
  }

  private fun setup3DSComponent() {
    adyen3DS2Component =
      Adyen3DS2Component.PROVIDER.get(this, requireActivity().application, adyen3DS2Configuration)
    adyen3DS2Component.observe(this) { actionComponentData ->
      viewModel.handleRedirectComponentResponse(actionComponentData)
    }
    adyen3DS2Component.observeErrors(this) { componentError ->
      viewModel.handle3DSErrors(componentError)
    }
  }

  private fun handle3DSAction(action: Action?) {
    action?.let {
      adyen3DS2Component.handleAction(requireActivity(), it)
    }
  }

  private fun handleCVCError() {
    showLoading(shouldShow = false)
    views.manageWalletAddCardSubmitButton.isEnabled = false
    adyenCardView.setError(getString(R.string.purchase_card_error_CVV))
  }
}
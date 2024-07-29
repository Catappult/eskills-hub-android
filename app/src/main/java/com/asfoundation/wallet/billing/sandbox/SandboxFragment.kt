package com.asfoundation.wallet.billing.sandbox

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.StringRes
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.FontAssetDelegate
import com.airbnb.lottie.TextDelegate
import com.asf.eskills.R
import com.asf.eskills.databinding.FragmentSandboxBinding
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.navigator.UriNavigator
import com.asfoundation.wallet.ui.iab.IabNavigator
import com.asfoundation.wallet.ui.iab.IabView
import com.asfoundation.wallet.ui.iab.Navigator
import com.wallet.appcoins.core.legacy_base.BasePageViewFragment
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@AndroidEntryPoint
class SandboxFragment : BasePageViewFragment() {

  @Inject
  lateinit var navigator: SandboxNavigator

  private val viewModel: SandboxViewModel by viewModels()

  private var binding: FragmentSandboxBinding? = null
  private val views get() = binding!!
  private lateinit var compositeDisposable: CompositeDisposable

  private lateinit var resultAuthLauncher: ActivityResultLauncher<Intent>
  private var successBundle: Bundle? = null

  private lateinit var iabView: IabView
  var navigatorIAB: Navigator? = null

  override fun onCreateView(
    inflater: LayoutInflater, container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    binding = FragmentSandboxBinding.inflate(inflater, container, false)
    compositeDisposable = CompositeDisposable()
    navigatorIAB = IabNavigator(parentFragmentManager, activity as UriNavigator?, iabView)
    return views.root
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    check(context is IabView) { "Sandbox payment fragment must be attached to IAB activity" }
    iabView = context
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    setListeners()
    handleBonusAnimation()
    showLoadingAnimation()
    setObserver()
    startPayment()
  }

  private fun setObserver() {
    viewModel.state.observe(viewLifecycleOwner) { state ->
      when (state) {
        SandboxViewModel.State.Start -> {
          showLoadingAnimation()
        }

        is SandboxViewModel.State.Error -> {
          showSpecificError(state.stringRes)
        }

        is SandboxViewModel.State.SuccessPurchase -> {
          handleSuccess(state.bundle)
        }
      }
    }
  }

  private fun startPayment() {
    viewModel.startPayment(
      amount = amount,
      currency = currency,
      transactionBuilder = transactionBuilder,
      origin = origin
    )
  }

  private fun setListeners() {
    views.sandboxErrorButtons.errorBack.setOnClickListener {
      close()
    }
    views.sandboxErrorButtons.errorCancel.setOnClickListener {
      close()
    }
    views.sandboxErrorButtons.errorTryAgain.setOnClickListener {
      close()
    }
    views.successContainer.lottieTransactionSuccess
      .addAnimatorListener(object : Animator.AnimatorListener {
        override fun onAnimationRepeat(animation: Animator) = Unit
        override fun onAnimationEnd(animation: Animator) = concludeWithSuccess()
        override fun onAnimationCancel(animation: Animator) = Unit
        override fun onAnimationStart(animation: Animator) = Unit
      })
    views.sandboxErrorLayout.layoutSupportIcn.setOnClickListener {
      viewModel.showSupport(gamificationLevel)
    }
  }

  private fun concludeWithSuccess() {
    viewLifecycleOwner.lifecycleScope.launch {
      delay(1500L)
      navigatorIAB?.popView(successBundle)
    }
  }

  private fun handleSuccess(bundle: Bundle) {
    showSuccessAnimation(bundle)
  }

  private fun close() {
    iabView.close(Bundle())
  }

  private fun showSuccessAnimation(bundle: Bundle) {
    successBundle = bundle
    views.successContainer.iabActivityTransactionCompleted.visibility = View.VISIBLE
    views.loadingAuthorizationAnimation.visibility = View.GONE
  }

  private fun showLoadingAnimation() {
    views.successContainer.iabActivityTransactionCompleted.visibility = View.GONE
    views.loadingAuthorizationAnimation.visibility = View.VISIBLE

  }

  private fun showSpecificError(@StringRes stringRes: Int) {
    views.successContainer.iabActivityTransactionCompleted.visibility = View.GONE
    views.loadingAuthorizationAnimation.visibility = View.GONE
    val message = getString(stringRes)
    views.sandboxErrorLayout.errorMessage.text = message
    views.sandboxErrorLayout.root.visibility = View.VISIBLE
    views.sandboxErrorButtons.root.visibility = View.VISIBLE
  }

  private fun getAnimationDuration() = views.successContainer.lottieTransactionSuccess.duration

  private fun handleBonusAnimation() {
    views.successContainer.lottieTransactionSuccess.setAnimation(R.raw.success_animation)
    views.successContainer.bonusSuccessLayout.visibility = View.GONE
  }

  private fun setupTransactionCompleteAnimation() {
    val textDelegate = TextDelegate(views.successContainer.lottieTransactionSuccess)
    textDelegate.setText("bonus_value", bonus)
    textDelegate.setText(
      "bonus_received",
      resources.getString(R.string.gamification_purchase_completed_bonus_received)
    )
    views.successContainer.lottieTransactionSuccess.setTextDelegate(textDelegate)
    views.successContainer.lottieTransactionSuccess.setFontAssetDelegate(object :
      FontAssetDelegate() {
      override fun fetchFont(fontFamily: String): Typeface {
        return Typeface.create("sans-serif-medium", Typeface.BOLD)
      }
    })
  }

  private val amount: BigDecimal by lazy {
    if (requireArguments().containsKey(AMOUNT_KEY)) {
      requireArguments().getSerializable(AMOUNT_KEY) as BigDecimal
    } else {
      throw IllegalArgumentException("amount data not found")
    }
  }

  private val currency: String by lazy {
    if (requireArguments().containsKey(CURRENCY_KEY)) {
      requireArguments().getString(CURRENCY_KEY, "")
    } else {
      throw IllegalArgumentException("currency data not found")
    }
  }

  private val origin: String? by lazy {
    if (requireArguments().containsKey(ORIGIN_KEY)) {
      requireArguments().getString(ORIGIN_KEY)
    } else {
      throw IllegalArgumentException("origin not found")
    }
  }

  private val transactionBuilder: TransactionBuilder by lazy {
    if (requireArguments().containsKey(TRANSACTION_DATA_KEY)) {
      requireArguments().getParcelable<TransactionBuilder>(TRANSACTION_DATA_KEY)!!
    } else {
      throw IllegalArgumentException("transaction data not found")
    }
  }

  private val bonus: String by lazy {
    if (requireArguments().containsKey(BONUS_KEY)) {
      requireArguments().getString(BONUS_KEY, "")
    } else {
      throw IllegalArgumentException("bonus data not found")
    }
  }

  private val gamificationLevel: Int by lazy {
    if (requireArguments().containsKey(GAMIFICATION_LEVEL)) {
      requireArguments().getInt(GAMIFICATION_LEVEL, 0)
    } else {
      throw IllegalArgumentException("gamification level data not found")
    }
  }


  companion object {

    private const val PAYMENT_TYPE_KEY = "payment_type"
    private const val ORIGIN_KEY = "origin"
    private const val TRANSACTION_DATA_KEY = "transaction_data"
    private const val AMOUNT_KEY = "amount"
    private const val CURRENCY_KEY = "currency"
    private const val BONUS_KEY = "bonus"
    private const val PRE_SELECTED_KEY = "pre_selected"
    private const val IS_SUBSCRIPTION = "is_subscription"
    private const val IS_SKILLS = "is_skills"
    private const val FREQUENCY = "frequency"
    private const val GAMIFICATION_LEVEL = "gamification_level"
    private const val SKU_DESCRIPTION = "sku_description"
    private const val NAVIGATOR = "navigator"

    @JvmStatic
    fun newInstance(
      paymentType: PaymentType,
      origin: String?,
      transactionBuilder: TransactionBuilder,
      amount: BigDecimal,
      currency: String?,
      bonus: String?,
      isPreSelected: Boolean,
      gamificationLevel: Int,
      skuDescription: String,
      isSubscription: Boolean,
      isSkills: Boolean,
      frequency: String?,
    ): SandboxFragment = SandboxFragment().apply {
      arguments = Bundle().apply {
        putString(PAYMENT_TYPE_KEY, paymentType.name)
        putString(ORIGIN_KEY, origin)
        putParcelable(TRANSACTION_DATA_KEY, transactionBuilder)
        putSerializable(AMOUNT_KEY, amount)
        putString(CURRENCY_KEY, currency)
        putString(BONUS_KEY, bonus)
        putBoolean(PRE_SELECTED_KEY, isPreSelected)
        putInt(GAMIFICATION_LEVEL, gamificationLevel)
        putString(SKU_DESCRIPTION, skuDescription)
        putBoolean(IS_SUBSCRIPTION, isSubscription)
        putBoolean(IS_SKILLS, isSkills)
        putString(FREQUENCY, frequency)
      }
    }

  }

}

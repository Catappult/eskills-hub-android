package com.asfoundation.wallet.ui.iab

import com.asfoundation.wallet.ui.iab.PaymentMethodsView.SelectedPaymentMethod
import io.reactivex.exceptions.OnErrorNotImplementedException

class PaymentMethodsMapper {

  fun map(paymentId: String): SelectedPaymentMethod {
    when (paymentId) {
      "ask_friend" -> return SelectedPaymentMethod.SHARE_LINK
      "paypal" -> return SelectedPaymentMethod.PAYPAL
      "credit_card" -> return SelectedPaymentMethod.CREDIT_CARD
      "alfamart" -> return SelectedPaymentMethod.LOCAL_PAYMENTS
      "bank_transfer" -> return SelectedPaymentMethod.LOCAL_PAYMENTS
      "gopay" -> return SelectedPaymentMethod.LOCAL_PAYMENTS
      "appcoins" -> return SelectedPaymentMethod.APPC
      "appcoins_credits" -> return SelectedPaymentMethod.APPC_CREDITS
      else -> throw OnErrorNotImplementedException(Throwable("Method not implemented"))
    }
  }

}
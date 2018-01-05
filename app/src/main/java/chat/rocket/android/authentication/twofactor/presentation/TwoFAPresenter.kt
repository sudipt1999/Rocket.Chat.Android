package chat.rocket.android.authentication.twofactor.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.SharedPreferencesRepository
import chat.rocket.android.util.launchUI
import chat.rocket.common.RocketChatAuthException
import chat.rocket.common.RocketChatException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.registerPushToken
import javax.inject.Inject

class TwoFAPresenter @Inject constructor(private val view: TwoFAView,
                                         private val strategy: CancelStrategy,
                                         private val navigator: AuthenticationNavigator) {
    @Inject lateinit var client: RocketChatClient

    fun authenticate(twoFactorAuthenticationCode: String) {
        if (twoFactorAuthenticationCode.isBlank()) {
            view.alertBlankTwoFactorAuthenticationCode()
        } else {
            launchUI(strategy) {
                if (NetworkHelper.hasInternetAccess()) {
                    view.showLoading()

                    try {
                        client.login(navigator.usernameOrEmail, navigator.password, twoFactorAuthenticationCode) // TODO This function returns a user token so should we save it?
                        registerPushToken()
                        navigator.toChatList()
                    } catch (exception: RocketChatException) {
                        if (exception is RocketChatAuthException) {
                            view.alertInvalidTwoFactorAuthenticationCode()
                        } else {
                            val message = exception.message
                            if (message != null) {
                                view.showMessage(message)
                            } else {
                                view.showGenericErrorMessage()
                            }
                        }
                    }

                    view.hideLoading()
                } else {
                    view.showNoInternetConnection()
                }
            }
        }
    }

    fun signup() {
        navigator.toSignUp()
    }

    private suspend fun registerPushToken() {
        // TODO: put it on constructor
        val localRepository: LocalRepository = SharedPreferencesRepository(navigator.activity)

        localRepository.get(LocalRepository.KEY_PUSH_TOKEN)?.let {
            client.registerPushToken(it)
        }

        // TODO: Schedule push token registering when it comes up null
    }
}
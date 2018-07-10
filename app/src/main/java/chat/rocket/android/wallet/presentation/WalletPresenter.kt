package chat.rocket.android.wallet.presentation

import android.content.Context
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.android.wallet.BlockchainInterface
import chat.rocket.android.wallet.WalletDBInterface
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.Token
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.me
import kotlinx.coroutines.experimental.async
import okhttp3.*
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class WalletPresenter @Inject constructor (private val view: WalletView,
                                           private val strategy: CancelStrategy,
                                           private val navigator: MainNavigator,
                                           private val localRepository: LocalRepository,
                                           private val getChatRoomsInteractor: GetChatRoomsInteractor,
                                           private val tokenRepository: TokenRepository,
                                           serverInteractor: GetCurrentServerInteractor,
                                           factory: RocketChatClientFactory) {

    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)
    private val restUrl: HttpUrl? = HttpUrl.parse(serverUrl)
    private val bcInterface = BlockchainInterface()
    private val dbInterface = WalletDBInterface()

    /**
     * Get transaction history associated with the user's wallet
     */
    fun loadTransactions() {
        launchUI(strategy) {
            try {
                loadWalletAddress {addr ->
                    // Query the DB for transaction hashes
                    if (bcInterface.isValidAddress(addr)) {
                        dbInterface.getTransactionList(addr, { hashList ->
                            // Update transaction history
                            if (hashList != null) {
                                async {
                                    view.updateTransactions(bcInterface.getTransactions(addr, hashList))
                                }
                            }
                        })
                    }
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    /**
     * Check if the user has a wallet
     *  both tied to their rocket.chat account and stored on their device
     *  and display either their wallet or the create wallet button.
     * Only display the wallet if it is stored with the rocket.chat account and on
     *  the user's device. TODO add more options for checking for wallets (e.g. what if there's a private key file on the device, but no address in the rocket.chat account)
     */
    fun loadWallet(c: Context) {
        launchUI(strategy) {
            try {
                loadWalletAddress {
                    if (bcInterface.isValidAddress(it) && bcInterface.walletFileExists(it, c)) {
                        view.showWallet(true, bcInterface.getBalance(it).toDouble())
                    } else {
                        view.showWallet(false)
                    }
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    /**
     * Retrieve the walletAddress field in a user's customFields object
     *
     * If the user does not have a wallet address stored, then an empty string
     *  is given to the callback
     *
     * @param username the user name of the user to get the walletAddress of
     *                  if none is given, then the current user is used
     *
     * NOTE: this function directly calls the REST API, which normally should be
     *          done in the Kotlin SDK
     */
    fun loadWalletAddress(username: String? = null, callback: (String) -> Unit) {
        launchUI(strategy) {
            try {
                val me = retryIO("me") { client.me() }
                val httpUrl = restUrl?.newBuilder()
                        ?.addPathSegment("api")
                        ?.addPathSegment("v1")
                        ?.addPathSegment("users.info")
                        ?.addQueryParameter("username", username ?: me.username)
                        ?.build()

                val token: Token? = tokenRepository.get(serverUrl)
                val builder = Request.Builder().url(httpUrl)
                token?.let {
                    builder.addHeader("X-Auth-Token", token.authToken)
                            .addHeader("X-User-Id", token.userId)
                }

                val request = builder.get().build()
                val httpClient = OkHttpClient()
                httpClient.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) { Timber.d("ERROR: request call failed!")}
                    override fun onResponse(call: Call, response: Response) {
                        var jsonObject = JSONObject(response.body()?.string())
                        var walletAddress = ""
                        if (jsonObject.isNull("error")) {

                            if (!jsonObject.isNull("user")) {
                                jsonObject = jsonObject.getJSONObject("user")

                                if (!jsonObject.isNull("customFields")) {
                                    jsonObject = jsonObject.getJSONObject("customFields")
                                    walletAddress = jsonObject.getString("walletAddress")
                                }
                            }
                        } else {
                            Timber.d("ERROR: %s", jsonObject.getString("error"))
                        }
                        launchUI(strategy) {
                            callback(walletAddress)
                        }
                    }
                })
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    fun getUserName(): String {
        return localRepository.get(LocalRepository.CURRENT_USERNAME_KEY) ?: ""
    }

    /**
     * Get all room names the user has open that are Direct Message rooms
     */
    fun loadDMRooms() {
        launchUI(strategy) {
            try {
                val roomList = getChatRoomsInteractor.getByName(serverUrl, "")
                val directMessageRoomList = roomList.filter( {it.type.javaClass == RoomType.DIRECT_MESSAGE.javaClass} )
                view.setupSendToDialog(directMessageRoomList.map({room -> room.name}))
            } catch (ex: RocketChatException) {
                Timber.e(ex)
            }
        }
    }

    /**
     * Find an open direct message room that matches a given username
     *  and redirect the user to the ChatRoom Activity, then immediately to a Transaction Activity
     */
    fun loadDMRoomByName(name: String) {
        launchUI(strategy) {
            try {
                val roomList = getChatRoomsInteractor.getByName(serverUrl, name)
                val directMessageRoomList = roomList.filter( {it.type.javaClass == RoomType.DIRECT_MESSAGE.javaClass} )

                if (directMessageRoomList.isEmpty()) {
                    view.showRoomFailedToLoadMessage(name)
                } else {
                    val room = directMessageRoomList.first()
                    navigator.toChatRoom(room.id, room.name, room.type.toString(),
                            room.readonly ?: false,
                            room.lastSeen ?: -1,
                            room.open, true)
                }
            } catch (ex: RocketChatException) {
                Timber.e(ex)
            }
        }
    }
}
package chat.rocket.android.helper

object Constants {
    const val CHATROOM_SORT_TYPE_KEY: String = "chatroom_sort_type"
    const val CHATROOM_GROUP_BY_TYPE_KEY: String = "chatroom_group_by_type"
    const val CHATROOM_GROUP_FAVOURITES_KEY: String = "chatroom_group_favourites"

    //Used to sort chat rooms
    const val CHATROOM_CHANNEL = 0
    const val CHATROOM_PRIVATE_GROUP = 1
    const val CHATROOM_DM = 2
    const val CHATROOM_LIVE_CHAT = 3

    // Enables/disables WIDECHAT specific features, functionality and views
    // Use both WIDECHAT and WIDECHAT_DEV switches == true to allow for normal RC login sequence including login to any server
    const val WIDECHAT = true
    const val WIDECHAT_DEV = false

    const val AVATAR_SHAPE_CIRCLE = true
    const val DEEP_LINK_INFO = "deep_link_info"

    const val CONTACTS_ACCESS_PERMISSION_REQUESTED = "contacts_access_permission_requested"
    const val WIDECHAT_REAL_USER_NAME = "real_user_name"
}

object ChatRoomsSortOrder {
    const val ALPHABETICAL: Int = 0
    const val ACTIVITY: Int = 1
}
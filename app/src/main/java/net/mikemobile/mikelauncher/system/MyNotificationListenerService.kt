package net.mikemobile.mikelauncher.system

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.SpannableString
import android.util.Log
import net.mikemobile.mikelauncher.constant.Global
import net.mikemobile.mikelauncher.constant.NotificationFieldData

class MyNotificationListenerService: NotificationListenerService() {

    override fun onCreate() {
        super.onCreate()
        Log.d("NotificationListener", "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("NotificationListener", "onDestroy")
    }


    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d("NotificationListener", "onListenerConnected")
        activeNotificationList()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d("NotificationListener", "onListenerDisconnected")
        // サービスが切断されたときの処理
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d("NotificationListener_onNotificationPosted", "onNotificationPosted")
        sbn?.let {
            // 通知の詳細を取得

            val notificationData = getNotificationFieldData(sbn)

            val intent = Intent("com.example.notificationlistener.ACTIVE_NOTIFICATIONS")
            intent.putExtra("notificationType", "one")
            intent.putExtra("notification", notificationData)
            intent.putExtra("notification_flg", true)
            sendBroadcast(intent)

        }

        if (Global.notificationCountList.size == 0) {
            activeNotificationList()
        }

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Log.d("NotificationListener_onNotificationPosted", "onNotificationRemoved")
        // 通知が削除されたときの処理をここに書きます（オプション）

        sbn?.let {
            val notificationData = getNotificationFieldData(sbn)

            val intent = Intent("com.example.notificationlistener.ACTIVE_NOTIFICATIONS")
            intent.putExtra("notificationType", "one")
            intent.putExtra("notification", notificationData)
            intent.putExtra("notification_flg", false)
            sendBroadcast(intent)

        }
        //activeNotificationList()
    }

    private fun activeNotificationList() {
        Log.d("NotificationListener", "activeNotificationList")

        try {
            val list = ArrayList<NotificationData>()

            this.activeNotifications.map { sbn ->
                val data = getNotificationFieldData(sbn)
                if (data != null) {
                    list.add(data)
                }
            }


            val intent = Intent("com.example.notificationlistener.ACTIVE_NOTIFICATIONS")
            intent.putExtra("notificationType", "ALL")
            intent.putParcelableArrayListExtra("notifications", list)

            sendBroadcast(intent)
        }catch(e: Exception) {
            Log.e("NotificationListener", "error:" + e.toString())
        }
    }

    private fun getNotificationFieldData(sbn: StatusBarNotification): NotificationData? {
        Log.d("NotificationListener", "getNotificationFieldData")
        val extras = sbn.notification.extras
        val packageName = sbn.packageName?: ""
        val category = sbn.notification.category?: ""
        val id = sbn.id
        val key = sbn.key

//        if (packageName == "com.google.android.gm") {
//            for (key in extras.keySet()) {
//                val value = extras.get(key)
//                Log.d("NotificationListener", "Key: $key Value: $value")
//            }
//        }

        val title = getStringText(extras, "android.title")
        val text = getStringText(extras, "android.text")
        val bigText = getStringText(extras, "android.bigText")
        val subText = getStringText(extras, "android.subText")
        val summaryText = getStringText(extras, "android.summaryText")
        val infoText = getStringText(extras, "android.infoText")

        if (title == "" && text == "" && bigText == "" && subText == "" && summaryText == "" && infoText == "") {
            return null
        }

        Log.d("NotificationListener", "${packageName} / ${category} / ${title} " +
                "/ ${text} / ${bigText} / ${subText} / ${summaryText} / ${infoText}")


        return NotificationData(id, key, packageName, category, title, text, bigText)
    }

    private fun getStringText(extras: Bundle, key: String): String {
        if (extras.containsKey(key)) {
            val txt = extras.get(key)
            if (txt is SpannableString) {
                return txt.toString()
            } else if (txt is String) {
                return txt
            } else {
                return ""
            }
        } else {
            return ""
        }
    }

    /**
     * 通知に関して注意点
     * 「onNotificationPosted」でIntentで取得したデータは「activeNotificationList」には入らないので注意が必要
     * ※単純にリストに追加するでいいのかも？
     *
     * データはIntentで渡すとタイミングによってはズレそうなので、データの保存はPreferenceに実行して、Intentでは読み込み処理を実施する方がいいかも？
     *
     * 確認できていないから断言はできないけど「onNotificationRemoved」も同じ理屈が通りそう
     * 逆に「activeNotificationList」でも取れてしまうから、「activeNotificationList」から取得した内容から消さないといけないかも。
     *
     *
     */




    data class NotificationData(
        val id: Int,
        val key: String?,
        val packageName: String?,
        val category: String?,
        val title: String?,
        val text: String?,
        val bigText: String?
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeInt(id)
            parcel.writeString(key)
            parcel.writeString(packageName)
            parcel.writeString(category)
            parcel.writeString(title)
            parcel.writeString(text)
            parcel.writeString(bigText)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<NotificationData> {
            override fun createFromParcel(parcel: Parcel): NotificationData {
                return NotificationData(parcel)
            }

            override fun newArray(size: Int): Array<NotificationData?> {
                return arrayOfNulls(size)
            }
        }
    }
}
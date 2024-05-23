package net.mikemobile.mikelauncher.system

import android.content.Intent
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

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
            val packageName = it.packageName
            val extras = it.notification.extras
            val category = sbn.notification.category
            val title = if (extras.containsKey("android.title")) {
                extras.get("android.title") as? String ?: ""
            } else {
                ""
            }
            val text = extras.getCharSequence("android.text").toString()

            Log.d("NotificationListener_onNotificationPosted", "Package: $packageName, Title: $title, Text: $text")

            // 通知の内容を処理する
            val data = NotificationData(packageName, category, title, text)

            val intent = Intent("com.example.notificationlistener.ACTIVE_NOTIFICATIONS")
            intent.putExtra("notificationType", "one")
            intent.putExtra("notification", data)
            intent.putExtra("notification_flg", true)
            //sendBroadcast(intent)

        }
        activeNotificationList()

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        Log.d("NotificationListener_onNotificationPosted", "onNotificationRemoved")
        // 通知が削除されたときの処理をここに書きます（オプション）

        var count = 1
        sbn?.let {
            // 通知の詳細を取得
            val packageName = it.packageName
            val extras = it.notification.extras
            val category = sbn.notification.category
            val title = if (extras.containsKey("android.title")) {
                extras.get("android.title") as? String ?: ""
            } else {
                ""
            }
            val text = extras.getCharSequence("android.text").toString()

            Log.d("NotificationListener_onNotificationPosted", "count: $count, Package: $packageName, Title: $title, Text: $text")
            count++
            // 通知の内容を処理する
            val data = NotificationData(packageName, category, title, text)

            val intent = Intent("com.example.notificationlistener.ACTIVE_NOTIFICATIONS")
            intent.putExtra("notificationType", "one")
            intent.putExtra("notification", data)
            intent.putExtra("notification_flg", false)
            //sendBroadcast(intent)

        }
        activeNotificationList()
    }

    private fun activeNotificationList() {
        Log.d("NotificationListener", "activeNotificationList")

        try {


            val activeNotifications = this.activeNotifications
            val notificationsList = activeNotifications.map { sbn ->

                val extras = sbn.notification.extras
                val packageName = sbn.packageName
                val category = sbn.notification.category
                val title = if (extras.containsKey("android.title")) {
                    extras.get("android.title") as? String ?: ""
                } else {
                    ""
                }
                val text = extras.getCharSequence("android.text").toString()
                NotificationData(packageName, category, title, text)
            }

            val intent = Intent("com.example.notificationlistener.ACTIVE_NOTIFICATIONS")
            intent.putExtra("notificationType", "ALL")
            intent.putParcelableArrayListExtra("notifications", ArrayList(notificationsList))
            sendBroadcast(intent)
        }catch(e: Exception) {
            Log.e("NotificationListener", "error:" + e.toString())
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
        val packageName: String?,
        val category: String?,
        val title: String?,
        val text: String?
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(packageName)
            parcel.writeString(category)
            parcel.writeString(title)
            parcel.writeString(text)
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
package net.mikemobile.mikelauncher


import android.appwidget.AppWidgetHost
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.mikemobile.android.view.DragLayer
import net.mikemobile.android.view.Folder
import net.mikemobile.mikelauncher.constant.Global
import net.mikemobile.mikelauncher.data.AppPreference
import net.mikemobile.mikelauncher.ui.applist.AppListFragment
import net.mikemobile.mikelauncher.ui.home.HomeFragment
import net.mikemobile.mikelauncher.ui.test.TestWidgetFragment


class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {

    var dragLayer: DragLayer? = null
    var mWorkspace: FrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            //setFragment(MainFragment.newInstance())
            //setFragment(AppListFragment.newInstance())
        }
//
//        for (id in mAppWidgetHost!!.appWidgetIds) {
//            Log.i("TESTTEST", "onCreate  id:" + id)
//            mAppWidgetHost!!.deleteAppWidgetId(id) //…(8)
//        }

        // 保存データ取得
        val pref = AppPreference(this)
        pref.getAppsList()

        // 保存データから、Widgetの配置に関するデータ保管をする
        Global.homeItemData.setWidgetField()

        // 取得したデータを保存しておく
        pref.setAppsList()


        if (false) {// widget設置テスト用
            setFragment(TestWidgetFragment.newInstance())
        }

        if (true) {// ホーム
            setFragment2(HomeFragment.newInstance())
        }

//        dragLayer = findViewById(R.id.dragLayer_main) as DragLayer
//        mWorkspace = findViewById(R.id.workspace_main) as FrameLayout


        // 事前にアプリリストFragmentを生成しておく
        CoroutineScope(Dispatchers.IO).launch {
            AppListFragment.newInstance()
        }

        // 通知の許可がなければ許可を取れるようにするための呼び出し
//        if (!isNotificationServiceEnabled()) {
//            intentMoveNotificationListener()
//        }

        if (!isNotificationServiceEnabled(this)) {
            requestNotificationListenerPermission(this)
        }
    }

    var mAppWidgetHost: AppWidgetHost? = null
    override fun onResume(){
        super.onResume()
    }
    override fun onDestroy(){
        super.onDestroy()
        try {
            mAppWidgetHost?.stopListening()
        } catch (ex: NullPointerException) {
            Log.w(
                LOG_TAG,
                "problem while stopping AppWidgetHost during Launcher destruction",
                ex
            )
        }
    }

    private fun setFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container_main,
            fragment
        ).commitNow()
    }

    private fun setFragment2(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container_main2,
            fragment
        ).commitNow()
    }

    private fun setFragmentOverlay(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.container_overlay,
            fragment
        ).commitNow()
    }

    private fun removeFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().remove(fragment).commitNow()
    }

    fun openApplicationList() {
        setFragmentOverlay(AppListFragment.newInstance())
    }

    fun closeApplicationList() {
        removeFragment(AppListFragment.newInstance())
    }

    override fun finish() {

    }

    override fun onBackPressed() {
        // 何もしない
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 何もしない
            true // 戻る動作をキャンセル
        } else super.onKeyDown(keyCode, event)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

    }

    companion object {
        const val APPWIDGET_HOST_ID = 1024
        const val LOG_TAG = "Launcher"
        const val DEFAULT_SCREN = 1
        private val sLock = Any()
        private var sScreen: Int = DEFAULT_SCREN

        fun setScreen(screen: Int) {
            synchronized(net.mikemobile.mikelauncher.MainActivity.sLock) {
                net.mikemobile.mikelauncher.MainActivity.sScreen = screen
            }
        }

        fun getScreen(): Int {
            synchronized(net.mikemobile.mikelauncher.MainActivity.sLock) {
                return net.mikemobile.mikelauncher.MainActivity.sScreen }
        }
    }

    private val mDesktopLocked = true
    fun isWorkspaceLocked(): Boolean {
        return mDesktopLocked
    }


    override fun onClick(p0: View?) {

    }

    fun startActivitySafely(intent: Intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show()
            Log.e(
                LOG_TAG,
                "Launcher does not have the permission to launch " + intent +
                        ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                        "or use the exported attribute for this activity.",
                e
            )
        }
    }

    private fun closeFolder() {
        /**
        val folder: Folder = mWorkspace!!.getOpenFolder()
        if (folder != null) {
            closeFolder(folder)
        }
        }*/
    }

    fun closeFolder(folder: Folder) {
        //folder.getInfo().opened = false
        //val parent = folder.getParent() as ViewGroup
        //parent?.removeView(folder)
        //folder.onClose()
    }

    override fun onLongClick(p0: View?): Boolean {

        return false
    }

    fun intentMoveNotificationListener() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        startActivity(intent)

    }

    private fun isNotificationServiceEnabled(): Boolean {
        val packageName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val componentName = android.content.ComponentName.unflattenFromString(name)
                if (componentName != null && componentName.packageName == packageName) {
                    return true
                }
            }
        }
        return false
    }

    fun requestNotificationListenerPermission(context: Context) {
        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        context.startActivity(intent)
    }
    fun isNotificationServiceEnabled(context: Context): Boolean {
        val packageName = context.packageName
        val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
        return flat != null && flat.contains(packageName)
    }
}
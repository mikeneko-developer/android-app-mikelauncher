package net.mikemobile.mikelauncher

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import net.mikemobile.mikelauncher.ui.applist.AppListFragment
import net.mikemobile.mikelauncher.ui.main.MainFragment
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView

import android.appwidget.AppWidgetManager

import android.graphics.drawable.Drawable

import android.appwidget.AppWidgetProviderInfo
import android.content.ActivityNotFoundException

import androidx.core.app.ActivityCompat.startActivityForResult

import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import net.mikemobile.android.view.CellLayout
import net.mikemobile.android.view.DragLayer
import net.mikemobile.android.view.Folder
import net.mikemobile.android.view.Workspace
import java.lang.NullPointerException


class MainActivity : AppCompatActivity(), View.OnClickListener, View.OnLongClickListener {

    val APPWIDGET_HOST_ID = 1024
    var dragLayer: DragLayer? = null
    var mWorkspace: FrameLayout? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            //setFragment(MainFragment.newInstance())
            //setFragment(AppListFragment.newInstance())
        }

        dragLayer = findViewById(R.id.dragLayer) as DragLayer


        mWorkspace = findViewById(R.id.workspace) as FrameLayout


        val button = findViewById(R.id.button) as Button
        button.setOnClickListener {
            openWidget()
        }
        val button2 = findViewById(R.id.button2) as Button
        button2.setOnClickListener {
            removeWidget()
        }
        val button3 = findViewById(R.id.button3) as Button
        button3.setOnClickListener {
            updateWidget()
        }


        mAppWidgetHost = AppWidgetHost(applicationContext, XXXX)    //…(1)
        mAppWidgetHost!!.startListening()

        for (id in mAppWidgetHost!!.appWidgetIds) {
            Log.i("TESTTEST", "onCreate  id:" + id)
            mAppWidgetHost!!.deleteAppWidgetId(id) //…(8)
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
        supportFragmentManager.beginTransaction().replace(R.id.container,
            fragment
        ).commitNow()
    }

    /**
    override fun finish() {
    }
    */

    val XXXX = 111111

    val REQUEST_CODE_ADD_APPWIDGET = 1
    val REQUEST_CODE_ADD_APPWIDGET_2 = 2

    var widgetWidth = 0
    var widgetHeight = 0

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            RESULT_OK -> when (requestCode) {
                REQUEST_CODE_ADD_APPWIDGET -> {
                    val appWidgetId = data!!.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    val appWidgetProviderInfo =
                        AppWidgetManager.getInstance(applicationContext).getAppWidgetInfo(appWidgetId) //…(2)

                    //…(3)
                    if (appWidgetProviderInfo.configure != null) {
                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                            .setComponent(appWidgetProviderInfo.configure)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        startActivityForResult(intent, REQUEST_CODE_ADD_APPWIDGET_2)

                        //…(4)
                    } else {
                        onActivityResult(REQUEST_CODE_ADD_APPWIDGET_2, RESULT_OK, data)
                    }
                }
                REQUEST_CODE_ADD_APPWIDGET_2 -> {

                    //…(6)
                    val appWidgetId2 = data!!.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    val appWidgetProviderInfo2 =
                        AppWidgetManager.getInstance(applicationContext).getAppWidgetInfo(appWidgetId2)
                    val widgetLabel = appWidgetProviderInfo2.label
                    val widgetIcon = packageManager.getDrawable(
                        appWidgetProviderInfo2.provider.packageName,
                        appWidgetProviderInfo2.icon,
                        null
                    )
                    widgetWidth = appWidgetProviderInfo2.minWidth
                    widgetHeight = appWidgetProviderInfo2.minHeight


                    appWidgetId = appWidgetId2

                    var hostView = mAppWidgetHost!!.createView(applicationContext, appWidgetId2, appWidgetProviderInfo2)

                    //hostView.setMinimumHeight(appWidgetProviderInfo2.minHeight)
                    if (Build.VERSION.SDK_INT > 15) {
                        //hostView.updateAppWidgetSize(null,
                        //    widgetWidth, appWidgetProviderInfo2.minHeight,
                        //    widgetHeight, appWidgetProviderInfo2.minHeight)
                    }
                    hostView.setAppWidget(appWidgetId2, appWidgetProviderInfo2)
                    addWodiget(hostView, true, 0, 0, 1, 1, widgetWidth, widgetHeight)

                }
            }
            RESULT_CANCELED -> when (requestCode) {
                REQUEST_CODE_ADD_APPWIDGET, REQUEST_CODE_ADD_APPWIDGET_2 -> if (data != null) {
                    val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    val appWidgetHost = AppWidgetHost(this, XXXX)
                    if (appWidgetId != -1) appWidgetHost.deleteAppWidgetId(appWidgetId) //…(8)
                }
            }
        }
    }

    fun openWidget() {

        var appWidgetId = mAppWidgetHost!!.allocateAppWidgetId()    //…(2)
        var appWidgetProviderInfoList = ArrayList<AppWidgetProviderInfo>()

        var bundleList = ArrayList<Bundle>()
        var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            .putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, appWidgetProviderInfoList)
            .putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, bundleList);

        startActivityForResult(intent, REQUEST_CODE_ADD_APPWIDGET)    //…(3)
    }

    var appWidgetId: Int = -1
    var widgetView: AppWidgetHostView? = null
    fun addWodiget(child: AppWidgetHostView, insert: Boolean, x: Int, y: Int, spanX: Int, spanY: Int, width: Int, height: Int) {
        Log.i("TESTTEST", "addWodiget  appWidgetId:" + appWidgetId)
        widgetView = child
        var lp: CellLayout.LayoutParams
        if (child.layoutParams == null) {
            lp = CellLayout.LayoutParams(x, y, spanX, spanY)
        } else {
            lp = child.layoutParams as CellLayout.LayoutParams
            lp.cellX = x
            lp.cellY = y
            lp.cellHSpan = spanX
            lp.cellVSpan = spanY
        }
        child.setLayoutParams(lp)

        mWorkspace!!.addView(child, lp)

        mWorkspace!!.layoutParams.width = width * 2
        mWorkspace!!.layoutParams.height = height * 2

        //mWorkspace!!.addView(child, if (insert) 0 else -1, lp)
        child.setOnLongClickListener(this)
    }

    fun updateWidget() {

    }

    fun removeWidget() {
        Log.i("TESTTEST", "removeWidget  appWidgetId:" + appWidgetId)

        if (appWidgetId != -1) mAppWidgetHost!!.deleteAppWidgetId(appWidgetId) //…(8)

        mWorkspace!!.removeAllViews()
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
}
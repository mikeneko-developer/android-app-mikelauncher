package net.mikemobile.mikelauncher.ui.test

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import net.mikemobile.android.view.CellLayout
import net.mikemobile.android.view.DragLayer
import net.mikemobile.mikelauncher.MainActivity
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.ui.main.MainFragment
import net.mikemobile.mikelauncher.ui.main.MainViewModel
import java.lang.NullPointerException

class WidgetFragment : Fragment(), View.OnLongClickListener {


    //private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_widget, container, false)

        dragLayer = view.findViewById(R.id.dragLayer) as DragLayer
        mWorkspace = view.findViewById(R.id.workspace) as FrameLayout

        val button = view.findViewById(R.id.button) as Button
        button.setOnClickListener {
            openWidget()
        }
        val button2 = view.findViewById(R.id.button2) as Button
        button2.setOnClickListener {
            removeWidget()
        }
        val button3 = view.findViewById(R.id.button3) as Button
        button3.setOnClickListener {
            updateWidget()
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        //viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        mAppWidgetHost = AppWidgetHost(this.requireActivity().applicationContext, XXXX)    //…(1)
        mAppWidgetHost!!.startListening()

        for (id in mAppWidgetHost!!.appWidgetIds) {
            Log.i("TESTTEST", "onCreate  id:" + id)
            mAppWidgetHost!!.deleteAppWidgetId(id) //…(8)
        }
    }

    override fun onDestroy(){
        super.onDestroy()
        try {
            mAppWidgetHost?.stopListening()
        } catch (ex: NullPointerException) {
            Log.w(
                MainActivity.LOG_TAG,
                "problem while stopping AppWidgetHost during Launcher destruction",
                ex
            )
        }
    }


    var dragLayer: DragLayer? = null
    var mWorkspace: FrameLayout? = null
    var mAppWidgetHost: AppWidgetHost? = null

    val XXXX = 111111

    val REQUEST_CODE_ADD_APPWIDGET = 1
    val REQUEST_CODE_ADD_APPWIDGET_2 = 2

    var widgetWidth = 0
    var widgetHeight = 0

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            AppCompatActivity.RESULT_OK -> when (requestCode) {
                REQUEST_CODE_ADD_APPWIDGET -> {
                    val appWidgetId = data!!.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    val appWidgetProviderInfo =
                        AppWidgetManager.getInstance(this.requireActivity().applicationContext).getAppWidgetInfo(appWidgetId) //…(2)

                    //…(3)
                    if (appWidgetProviderInfo.configure != null) {
                        val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                            .setComponent(appWidgetProviderInfo.configure)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                        startActivityForResult(intent, REQUEST_CODE_ADD_APPWIDGET_2)

                        //…(4)
                    } else {
                        onActivityResult(REQUEST_CODE_ADD_APPWIDGET_2,
                            AppCompatActivity.RESULT_OK, data)
                    }
                }
                REQUEST_CODE_ADD_APPWIDGET_2 -> {

                    //…(6)
                    val appWidgetId2 = data!!.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    val appWidgetProviderInfo2 =
                        AppWidgetManager.getInstance(this.requireActivity().applicationContext).getAppWidgetInfo(appWidgetId2)
                    val widgetLabel = appWidgetProviderInfo2.label
                    val widgetIcon = this.requireActivity().packageManager.getDrawable(
                        appWidgetProviderInfo2.provider.packageName,
                        appWidgetProviderInfo2.icon,
                        null
                    )
                    widgetWidth = appWidgetProviderInfo2.minWidth
                    widgetHeight = appWidgetProviderInfo2.minHeight


                    appWidgetId = appWidgetId2

                    var hostView = mAppWidgetHost!!.createView(this.requireActivity().applicationContext, appWidgetId2, appWidgetProviderInfo2)

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
            AppCompatActivity.RESULT_CANCELED -> when (requestCode) {
                REQUEST_CODE_ADD_APPWIDGET, REQUEST_CODE_ADD_APPWIDGET_2 -> if (data != null) {
                    val appWidgetId = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    val appWidgetHost = AppWidgetHost(this.requireContext(), XXXX)
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

    private val mDesktopLocked = true
    fun isWorkspaceLocked(): Boolean {
        return mDesktopLocked
    }

    override fun onLongClick(v: View?): Boolean {
        return false
    }



    companion object {
        fun newInstance() = WidgetFragment()

        const val APPWIDGET_HOST_ID = 1024
        const val LOG_TAG = "Launcher"
        const val DEFAULT_SCREN = 1
        private val sLock = Any()
        private var sScreen: Int = DEFAULT_SCREN

//        fun setScreen(screen: Int) {
//            synchronized(net.mikemobile.mikelauncher.MainActivity.sLock) {
//                net.mikemobile.mikelauncher.MainActivity.sScreen = screen
//            }
//        }
//
//        fun getScreen(): Int {
//            synchronized(net.mikemobile.mikelauncher.MainActivity.sLock) {
//                return net.mikemobile.mikelauncher.MainActivity.sScreen }
//        }
    }
}
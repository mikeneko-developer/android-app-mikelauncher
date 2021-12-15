package net.mikemobile.mikelauncher

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import net.mikemobile.mikelauncher.ui.applist.AppListFragment
import net.mikemobile.mikelauncher.ui.main.MainFragment
import android.appwidget.AppWidgetHost

import android.appwidget.AppWidgetManager

import android.graphics.drawable.Drawable

import android.appwidget.AppWidgetProviderInfo

import androidx.core.app.ActivityCompat.startActivityForResult

import android.content.Intent




class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        if (savedInstanceState == null) {
            //setFragment(MainFragment.newInstance())
            setFragment(AppListFragment.newInstance())
        }

        var appWidgetHost = AppWidgetHost(this, XXXX)    //…(1)
        var appWidgetId = appWidgetHost.allocateAppWidgetId()    //…(2)

        var appWidgetProviderInfoList = ArrayList<AppWidgetProviderInfo>()


        var bundleList = ArrayList<Bundle>()
        var intent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            .putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, appWidgetProviderInfoList)
            .putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, bundleList);

        startActivityForResult(intent, REQUEST_CODE_ADD_APPWIDGET)    //…(3)
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
                        AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId) //…(2)

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
                        AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId2)
                    val widgetLabel = appWidgetProviderInfo2.label
                    val widgetIcon = packageManager.getDrawable(
                        appWidgetProviderInfo2.provider.packageName,
                        appWidgetProviderInfo2.icon,
                        null
                    )
                    widgetWidth = appWidgetProviderInfo2.minWidth
                    widgetHeight = appWidgetProviderInfo2.minHeight

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
}
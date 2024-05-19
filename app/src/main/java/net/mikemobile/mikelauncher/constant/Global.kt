package net.mikemobile.mikelauncher.constant

import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View
import androidx.lifecycle.MutableLiveData
import net.mikemobile.mikelauncher.data.HomeItem
import net.mikemobile.mikelauncher.ui.applist.AppInfo

class Global {

    companion object {

        const val COLUMN_COUNT = 5
        const val ROW_COUNT = 7

        val homeItemList = DataManagement()
        val dockItemList = DataManagement()

        var selectItem: MutableLiveData<HomeItem> = MutableLiveData<HomeItem>(null)


        /**
         * アイコン取得
         */
        fun getAppIcon(context: Context, packageName: String): Drawable? {
            val pm = context.packageManager

            try {
                val icon = pm.getApplicationIcon(packageName)
                return icon
            } catch(e: Exception) {
                return null
            }
        }

        /**
         * アプリ起動
         */
        fun launch(context: Context, item: HomeItem, view: View? = null) {

            if (item.widgetId != -1) return
            if (item.toolId != -1) return

            val info = AppInfo(
                item.icon!!,
                item.label,
                ComponentName(item.packageName, item.name),
                item.packageName,
                item.name
            )


            try {
                val intent = Intent(Intent.ACTION_MAIN).also {
                    it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                    it.addCategory(Intent.CATEGORY_LAUNCHER)
                    it.component = info.componentName
                }
                val options = view?.let {
                    ActivityOptions.makeScaleUpAnimation(it, 0, 0, it.width, it.height)
                        .toBundle()
                }
                context.startActivity(intent, options)
            } catch (e: ActivityNotFoundException) {
            }
        }
    }
}
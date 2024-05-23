package net.mikemobile.mikelauncher.constant

import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.MutableLiveData
import net.mikemobile.mikelauncher.data.HomeItem
import net.mikemobile.mikelauncher.ui.applist.AppInfo
import net.mikemobile.mikelauncher.ui.home.HomeFragment
import java.util.UUID


class Global {

    companion object {

        const val COLUMN_COUNT = 5
        const val ROW_COUNT = 8

        val homeItemData = DataManagement(CELL_POINT_NAME.DESKTOP)
        val dockItemData = DataManagement(CELL_POINT_NAME.DOCK)
        val folderManager = FolderManagement()

        val gridSize: GridSize = GridSize(-1f, -1f)

        var selectItem: MutableLiveData<HomeItem> = MutableLiveData<HomeItem>(null)

        /**
         * 座標から位置をGridの位置を取得する
         */
        fun calcDimenToGridPoint(width: Int, height: Int): GridPoint {
            return calcDimenToGridPoint(DimenPoint(width.toFloat(), height.toFloat()))
        }
        fun calcDimenToGridPoint(width: Float, height: Float): GridPoint {
            return calcDimenToGridPoint(DimenPoint(width, height))
        }
        fun calcDimenToGridPoint(point: DimenPoint): GridPoint {

            val oneWidth = gridSize.width
            val oneHeight = gridSize.height

            val column = (point.x / oneWidth).toInt()
            val row = (point.y / oneHeight).toInt()

            return GridPoint(row, column)
        }

        fun calcDimenPointFieldToOriginal(touchPoint: DimenPoint, fieldItem: HomeItem): DimenPoint? {
            var originalItem = homeItemData.getItem(fieldItem.fieldId)

            if (homeItemData.checkNotWidgetData(fieldItem.fieldId)) {
                // オリジナルのデータがない
            } else if (originalItem != null){

                if (originalItem.fieldRow > 1 || originalItem.fieldColumn > 1) {
                    val gridSize = Global.gridSize

                    android.util.Log.i(HomeFragment.TAG + HomeFragment.TAG_DRAG,"original position x:" + touchPoint.x + " / y:" + touchPoint.y)

                    var differenceSize = calcDimenPointFieldToDifference(fieldItem)

                    android.util.Log.i(HomeFragment.TAG + HomeFragment.TAG_DRAG,"minus position horizontal:" + differenceSize.x + " / vetical:" + differenceSize.y)

                    var point = DimenPoint(touchPoint.x + (differenceSize.x / 2), touchPoint.y + differenceSize.y)

                    android.util.Log.i(HomeFragment.TAG + HomeFragment.TAG_DRAG,"change position x:" + point.x + " / y:" + point.y)

                    return point
                }

            }
            return null
        }

        fun calcStartDimenPoint(homeItem: HomeItem): DimenPoint {

            val gridSize = Global.gridSize

            var pointY = homeItem.row * gridSize.height
            var pointX = homeItem.column * gridSize.width

            var point = DimenPoint(pointX, pointY)

            return point
        }

        fun calcDimenPointFieldToDifference(fieldItem: HomeItem): DimenPoint {
            var originalItem = homeItemData.getItem(fieldItem.fieldId)

            val gridSize = Global.gridSize

            var minusRow = (fieldItem.row - originalItem!!.row) * gridSize.height
            var minusColumn = (fieldItem.column - originalItem!!.column) * gridSize.width

            var point = DimenPoint(-minusColumn, - minusRow)

            return point
        }

        fun calcSizeToGridCount(width: Int, height: Int): GridCount {
            var rowCount = 1
            var columnCount = 1

            android.util.Log.i(HomeFragment.TAG,
                "Global.calcSizeToGridPoint >> widgetData width:" + width + " height:" + height)


            for(row in 0 until Global.ROW_COUNT) {
                val gridHeight = gridSize.height * (row + 1)
                android.util.Log.i(HomeFragment.TAG,"Global.calcSizeToGridPoint >> widgetData gridHeight:" + gridHeight)

                if (height <= gridHeight) {
                    rowCount = row + 1
                    break
                }
            }
            for(column in 0 until Global.COLUMN_COUNT) {
                val gridwidth = gridSize.width * (column + 1)
                android.util.Log.i(HomeFragment.TAG,"Global.calcSizeToGridPoint >> widgetData gridwidth:" + gridwidth)

                if (width <= gridwidth) {
                    columnCount = column + 1
                    break
                }
            }
            android.util.Log.i(HomeFragment.TAG,
                "Global.calcSizeToGridPoint >> GridCount rowCount:" + rowCount + " columnCount:" + columnCount)

            return GridCount(rowCount, columnCount)
        }



        fun updateItem(folder: HomeItem) {

            if (homeItemData.updateItem(folder)) {
                // 更新あり
            } else if (dockItemData.updateItem(folder)) {
                // 更新あり
            }
        }


        /**
         * アイコン取得
         */
        fun getAppIcon(context: Context, packageName: String): Drawable? {

            return try {
                val pm = context.packageManager
                pm.getApplicationIcon(packageName)
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                null
            }
        }

        /**
         * アプリ起動
         */
        fun launch(context: Context, item: HomeItem, view: View? = null) {

            if (item.widgetId != -1) return
            if (item.toolId != -1) return

            val info = AppInfo(
//                item.icon!!,
                item.label,
                ComponentName(item.packageName, item.name),
                item.packageName,
                item.name
            )

            launch(context, info, view)
        }

        fun launch(context: Context, info: AppInfo, view: View? = null) {

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

        fun getToolIcon(context: Context, toolId: Int): Drawable? {
            val iconResource = if (toolId == 1) {
                net.mikemobile.mikelauncher.R.drawable.icon_drawer_menu
            } else if (toolId == 2) {
                net.mikemobile.mikelauncher.R.drawable.folder
            } else {
                return null
            }

            return ResourcesCompat.getDrawable(
                context.resources,
                iconResource,
                null)
        }

        fun generateId(): Int {
            val uuid = UUID.randomUUID()
            return uuid.hashCode() // ハッシュコードをIDとして使用

        }

        fun checkWidget(widgetId: Int): Boolean {
            if (homeItemData.checkItemToWidget(widgetId)) {
                return true
            } else if (dockItemData.checkItemToWidget(widgetId)) {
                return true
            }

            return false
        }
    }
}
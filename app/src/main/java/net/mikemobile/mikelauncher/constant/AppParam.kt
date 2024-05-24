package net.mikemobile.mikelauncher.constant

import android.view.View
import java.io.Serializable


enum class HomeItemType(val value: Int) {
    ALL(999),
    APP(0),
    WIDGET(1),
    TOOL(2),
}

enum class CELL_POINT_NAME(val value: Int) {
    NONE(0),
    DESKTOP(1),
    DOT(2),
    DOCK(3),

    FOLDER(4),
}

enum class ToolType(val value: Int) {
    DRAWER(1),
    FOLDER(2),
}


enum class ITEM_MOVE {
    MOVING_ITEM_ENABLED, // 他に移動するアイテムあり
    MOVING_ITEM_NONE, // 他に移動するアイテムなし
    MOVE_NG,// 指定場所にアイテムが移動できない

}

data class ViewSize(var width: Float, var height: Float)
data class GridSize(var width: Float, var height: Float)
data class DimenPoint(var x: Float, var y: Float)
data class GridPoint(var row: Int, var column: Int)
data class GridCount(var rowCount: Int, var columnCount: Int)
data class WidgetData(
    var view: View,
    var width: Int,
    var height: Int,
    val label: String,
)

data class NotificationCountData(var count: Int)

data class NotificationFieldData(
    val id: Int,
    val key: String,
    val packageName: String,
    val category: String,
    val title: String,
    val text: String,
    val bigText: String
): Serializable {

}
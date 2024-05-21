package net.mikemobile.mikelauncher.constant

import android.view.View


enum class HomeItemType(val value: Int) {
    APP(1),
    WIDGET(2),
    TOOL(3),
}

enum class CELL_POINT_NAME(val value: Int) {
    NONE(0),
    DESKTOP(1),
    DOT(2),
    DOCK(3)
}

enum class ITEM_MOVE {
    MOVING_ITEM_ENABLED, // 他に移動するアイテムあり
    MOVING_ITEM_NONE, // 他に移動するアイテムなし
    MOVE_NG,// 指定場所にアイテムが移動できない

}

data class CellSize(var width: Float, var height: Float)
data class DimenPoint(val x: Float, val y: Float)
data class GridPoint(var row: Int, var column: Int)
data class WidgetData(
    var view: View,
    var width: Int,
    var height: Int,
    val label: String,
)
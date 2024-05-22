package net.mikemobile.mikelauncher.data

import android.content.Context
import android.graphics.drawable.Drawable
import net.mikemobile.mikelauncher.constant.Global
import net.mikemobile.mikelauncher.constant.GridCount
import net.mikemobile.mikelauncher.constant.GridPoint
import net.mikemobile.mikelauncher.constant.WidgetData
import net.mikemobile.mikelauncher.ui.applist.AppInfo

data class HomeItem(
    var id: Int,
    var homeName: String,
    val image: String?,

    val type: Int, // 0 = アプリ , 1 = widget , 2 = ツール

    // アプリ情報
    var icon: Drawable?,
    val label: String,
    val packageName: String,
    val name: String,

    // アイテムに関しての説明
    var detail: String = "",

    // アプリ自作ツール管理用ID
    var toolId: Int = -1,

    // フォルダーで一意管理用のID
    var folderId: Int = -1,

    // WidgetID
    var widgetId: Int = -1,
    ){

    // Gridの座標情報
    var page: Int = -1
    var row: Int = -1
    var column: Int = -1

    // Widgetのサイズ情報
    var width: Int = -1
    var height: Int = -1

    // Widgtの範囲を埋めるための変数
    var fieldId: Int = -1
    var widgetField = false // widgetのフィールド判定用

    // Widgetのフィールド範囲を指定する（縦1,横2マス分なら　fieldRow:2 fieldColumn:1
    var fieldRow = 1
    var fieldColumn = 1


    fun convertHash(key: String): HashMap<String,String> {
        val map = HashMap<String,String>()
        map["id"] = "" + id
        map["homeName"] = "" + homeName
        map["image"] = "" + image
        map["type"] = "" + type
        map["label"] = "" + label
        map["packageName"] = "" + packageName
        map["name"] = "" + name
        map["key"] = key
        map["page"] = "" + page
        map["row"] = "" + row
        map["column"] = "" + column
        map["widgetId"] = "" + widgetId
        map["toolId"] = "" + toolId
        map["detail"] = "" + detail
        map["folderId"] = "" + folderId
        map["width"] = "" + width
        map["height"] = "" + height

        if (widgetField) {
            map["widgetField"] = "1"
        } else {
            map["widgetField"] = "0"
        }
        map["fieldRow"] = "" + fieldRow
        map["fieldColumn"] = "" + fieldColumn
        map["fieldId"] = "" + fieldId

        return map
    }

    fun convertHash(): HashMap<String,String> {
        val map = HashMap<String,String>()
        map["id"] = "" + id
        map["homeName"] = "" + homeName
        map["image"] = "" + image
        map["type"] = "" + type
        map["label"] = "" + label
        map["packageName"] = "" + packageName
        map["name"] = "" + name
        map["key"] = ""
        map["page"] = "" + page
        map["row"] = "" + row
        map["column"] = "" + column
        map["widgetId"] = "" + widgetId
        map["toolId"] = "" + toolId
        map["detail"] = "" + detail
        map["folderId"] = "" + folderId
        map["width"] = "" + width
        map["height"] = "" + height

        if (widgetField) {
            map["widgetField"] = "1"
        } else {
            map["widgetField"] = "0"
        }
        map["fieldRow"] = "" + fieldRow
        map["fieldColumn"] = "" + fieldColumn
        map["fieldId"] = "" + fieldId

        return map
    }

    constructor(map: HashMap<String,String>): this(
        map["id"]!!.toInt(),
        map["homeName"]!!,
        map["image"]!!,
        map["type"]!!.toInt(),
        null,
        map["label"]!!,
        map["packageName"]!!,
        map["name"]!!,
    ) {
        if (map.containsKey("toolId")) {
            toolId = map["toolId"]!!.toInt()
        }
        if (map.containsKey("folderId")) {
            folderId = map["folderId"]!!.toInt()
        }
        if (map.containsKey("detail")) {
            detail = map["detail"]!!
        }
        if (map.containsKey("widgetId")) {
            widgetId = map["widgetId"]!!.toInt()
        }
        if (map.containsKey("page")) {
            page = map["page"]!!.toInt()
        }
        if (map.containsKey("row")) {
            row = map["row"]!!.toInt()
        }
        if (map.containsKey("column")) {
            column = map["column"]!!.toInt()
        }

        if (map.containsKey("width")) {
            width = map["width"]!!.toInt()
        }
        if (map.containsKey("height")) {
            height = map["height"]!!.toInt()
        }
        if (map.containsKey("widgetField")) {
            widgetField = (map["widgetField"]!!.toInt() == 1)
        }
        if (map.containsKey("fieldRow")) {
            fieldRow = map["fieldRow"]!!.toInt()
        }
        if (map.containsKey("fieldColumn")) {
            fieldColumn = map["fieldColumn"]!!.toInt()
        }
        if (map.containsKey("fieldId")) {
            fieldId = map["fieldId"]!!.toInt()
        }

        if (map.containsKey("key")) {
            if (!map.containsKey("row") || !map.containsKey("column")) {
                val parse = map["key"]!!.split("-")
                row = parse[0].toInt()
                column = parse[1].toInt()
            }
        }
    }


    fun copyField(row: Int, column: Int): HomeItem{
        val item = this.copy()
        item.fieldId = id
        item.id = Global.generateId()
        item.row = row
        item.column = column
        item.widgetField = true

        return item
    }

    companion object {
        fun createWidget(widgetId: Int, widgetData: WidgetData, gridCount: GridCount): HomeItem {
            val homeItem = HomeItem(
                Global.generateId(),
                "",
                "",
                1,
                null,
                label = widgetData.label,
                "",
                "",
                widgetId = widgetId
            )

            homeItem.widgetId = widgetId
            homeItem.width = widgetData.width
            homeItem.height = widgetData.height
            homeItem.fieldRow = gridCount.rowCount
            homeItem.fieldColumn = gridCount.columnCount

            return homeItem
        }

        fun createTool(context: Context, label: String, detail: String, toolId: Int): HomeItem {
            var type = 2

            var folderId = -1
            if (toolId == 2) {
                folderId = Global.generateId()
            }

            return HomeItem(
                Global.generateId(),
                "", null, type,
                icon = Global.getToolIcon(context, toolId),
                label = label,
                "", "",
                detail = detail,
                toolId = toolId,
                folderId = folderId
            )
        }

        fun crateItem(context: Context, info: AppInfo): HomeItem {
            val homeItem = HomeItem(
                id = Global.generateId(),
                homeName = info.label,
                image = null,
                type = 0,
                icon = Global.getAppIcon(context, info.packageName),
                label = info.label,
                packageName = info.packageName,
                name = info.name,
            )

            return homeItem
        }
    }

}
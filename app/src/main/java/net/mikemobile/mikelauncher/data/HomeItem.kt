package net.mikemobile.mikelauncher.data

import android.content.ComponentName
import android.graphics.drawable.Drawable
import android.view.View
import net.mikemobile.mikelauncher.constant.Global

data class HomeItem(
    var id: Int,
    var homeName: String,
    val image: String?,

    val type: Int, // 0 = アプリ , 1 = widget

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
    var folderId: Int = -1
    ){

    var row: Int = -1
    var column: Int = -1

    // Widget情報
    var widgetId: Int = -1
    var width: Int = -1
    var height: Int = -1

    // Widgetのフィールド範囲を指定する（縦1,横2マス分なら　fieldRow:2 fieldColumn:1
    var fieldRow = 1
    var fieldColumn = 1

    var widgetField = false // widgetのフィールド判定用
    var ownerId: Int = -1

    // Tool




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
        map["ownerId"] = "" + ownerId

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
        map["ownerId"] = "" + ownerId

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
        if (map.containsKey("ownerId")) {
            ownerId = map["ownerId"]!!.toInt()
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
        item.ownerId = id
        item.id = Global.generateId()
        item.row = row
        item.column = column
        item.widgetField = true

        return item
    }
}
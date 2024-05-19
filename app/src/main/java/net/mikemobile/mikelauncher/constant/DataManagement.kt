package net.mikemobile.mikelauncher.constant

import net.mikemobile.mikelauncher.data.HomeItem

class DataManagement(private val cellPointName: CELL_POINT_NAME) {

    val itemList = HashMap<String, HashMap<String, HomeItem>>()

    fun addItem(page: Int, item: HomeItem): Boolean {
        val key = "" + page

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        var row = -1
        var column = -1

        for(rowId in 0 until 10) {
            for(columnId in 0 until 5) {
                val itemKey = "$rowId-$columnId"
                if (!list.containsKey(itemKey)) {
                    row = rowId
                    column = columnId

                    break
                }
            }

            if (row != -1 && column != -1) {
                break
            }
        }

        if (row != -1 && column != -1) {
            item.row = row
            item.column = column

            val itemKey = "$row-$column"
            list[itemKey] = item
            itemList[key] = list

            return true
        } else {
            return false
        }

    }


    fun addItem(
        position: Int,
        row: Int,
        column: Int,
        item: HomeItem,
        update: ITEM_MOVE = ITEM_MOVE.MOVING_ITEM_NONE
    ): ITEM_MOVE {
        val key = "" + position

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        val addItemKey = "$row-$column"

        if (!list.containsKey(addItemKey)) {
            // データがないので入れて終わり

            item.row = row
            item.column = column

            list[addItemKey] = item
            itemList[key] = list

        } else {
            // データある場合

            // 指定場所以降にスペースがあるかチェックする
            var blancCellEnable = checkBlank(list, row, column)

            if (!blancCellEnable) return ITEM_MOVE.MOVE_NG

            // もともと入っているデータを取り出す
            val outputItem = list[addItemKey]

            list[addItemKey] = item
            itemList[key] = list

            outputItem?.let {
                // 隣の場所を計算する
                var newRow = row
                var newColumn = column + 1
                if (newColumn > 4) {
                    newColumn = 0
                    newRow = row + 1
                }

                // メソッドの際入れ子呼び出しを実施する
                return addItem(position, newRow, newColumn, outputItem, ITEM_MOVE.MOVING_ITEM_ENABLED)
            }
        }

        return update
    }

    private fun checkBlank(list: HashMap<String, HomeItem>, row: Int, column: Int): Boolean {
        val rowMax = if (cellPointName == CELL_POINT_NAME.DOCK) {
            1
        } else {
            Global.ROW_COUNT
        }


        for(rowId in row until rowMax) {
            for(columnId in 0 until Global.COLUMN_COUNT ) {
                if (rowId == row) {
                    if (columnId >= column) {
                        val addItemKey = "$rowId-$columnId"

                        list[addItemKey] ?: return true
                    }
                } else {
                    val addItemKey = "$rowId-$columnId"

                    list[addItemKey] ?: return true
                }
            }
        }

        return false
    }


    fun checkWidget(position: Int, widgetId: Int): Boolean {
        val key = "" + position

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        for (key in list.keys) {
            val item = list[key]

            if (item!!.widgetId == widgetId) {
                return true
            }
        }

        return false
    }

    /**
     * フォルダー判定
     */
    fun checkToolToFolder(position: Int, row: Int, column: Int): Boolean {
        val key = "" + position

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        val addItemKey = "$row-$column"

        if (list.containsKey(addItemKey)) {
            val item = list[addItemKey]
            if (item!!.toolId == 2) {
                return true
            }
        }

        return false
    }

    /**
     * フォルダーアイテム取得
     */
    fun getFolderItem(position: Int, row: Int, column: Int): HomeItem? {
        val key = "" + position

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        val addItemKey = "$row-$column"

        if (list.containsKey(addItemKey)) {
            val item = list[addItemKey]
            if (item!!.toolId == 2) {
                return item
            }
        }

        return null
    }





    fun getWidgetHomeItem(position: Int, widgetId: Int): HomeItem? {
        val key = "" + position

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        for (key in list.keys) {
            val item = list[key]

            if (item!!.widgetId == widgetId) {
                return item
            }
        }

        return null
    }

    fun setItem(position: Int, row: Int, column: Int, item: HomeItem) {
        val key = "" + position

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }
        val itemKey = "$row-$column"

        list[itemKey] = item

        itemList[key] = list
    }

    fun removeHomeItem(position: Int, row: Int, column: Int) {
        val key = "" + position

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        if (list.size == 0) return

        val itemKey = "$row-$column"

        if (list.containsKey(itemKey)) {
            list.remove(itemKey)

            itemList[key] = list
        } else {
            android.util.Log.i("TESTEST","")
        }
    }

    fun getItem(position: Int, row: Int, column: Int): HomeItem? {
        val key = "" + position

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        if (list.size == 0) return null

        val itemKey = "$row-$column"

        if (list.containsKey(itemKey)) {
            val item = list[itemKey]
            item?.row = row
            item?.column = column
            return item
        }

        return null
    }

    fun checkHomeInApps(packageName: String, name: String): Boolean {

        val keys = itemList.keys

        for(key in keys) {
            val pageList = itemList[key]

            pageList?.let {
                for(key in pageList.keys) {
                    val item = pageList[key]!!
                    if (item.packageName == packageName && item.name == name) {
                        return true
                    }
                }
            }
        }

        return false
    }
}
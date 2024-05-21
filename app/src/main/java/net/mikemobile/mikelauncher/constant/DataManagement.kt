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

        for(rowId in 0 until Global.ROW_COUNT) {
            for(columnId in 0 until Global.COLUMN_COUNT) {
                val itemKey = "$rowId-$columnId"
                if (!list.containsKey(itemKey)) {

                    if (
                        (item.fieldRow > 1 || item.fieldColumn > 1)
                        && checkFieldSpace(item, page, rowId, columnId)
                    ) {
                        // widgetだったら、配置範囲が空白かチェックする
                        row = rowId
                        column = columnId
                    } else {
                        // widgetじゃないので追加とする
                        row = rowId
                        column = columnId
                    }

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

            if (item.fieldRow > 1 || item.fieldColumn > 1) {
                for(rowId in row until (row + item.fieldRow)) {
                    for(columnId in column until (column + item.fieldColumn)) {
                        if (rowId != row || columnId != column) {
                            val fieldItemKey = "$rowId-$columnId"

                            val fieldItem = item.copyField(rowId, columnId)
                            list[fieldItemKey] = fieldItem
                            itemList[key] = list
                        }
                    }
                }
            }

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
            if (item.widgetId != -1 && (item.fieldRow > 0 || item.fieldColumn > 0)) {
                // widgetでかつサイズが１＊１より大きい場合に空きがないかチェックする
                //if (!checkWidgetOnCell(row, column)) return ITEM_MOVE.MOVE_NG
            }


            // データがないので入れて終わり

            item.row = row
            item.column = column

            list[addItemKey] = item
            itemList[key] = list

            if (item.widgetId != -1) {
                removeWidgetField(item.id)
                addWidgetField(position, item)
            }


        } else {
            // データある場合

            // 指定箇所以降にWidgetがある場合は一旦移動はなしとする
            if (!checkWidgetOnCell(row, column)) return ITEM_MOVE.MOVE_NG



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


    fun updateItem(newItem: HomeItem): Boolean {
        var update = false

        for (key in itemList.keys) {
            val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
                HashMap<String, HomeItem>()
            } else {
                itemList[key]!!
            }


            for (itemKey in list.keys) {
                val item = list[itemKey]

                if (item!!.id == newItem.id && item!!.row == newItem.row && item?.column == newItem.column) {
                    list[itemKey] = newItem
                    update = true
                    break
                }
            }

            if (update) {
                itemList[key] = list
                break
            }

        }

        return update
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

        if (item.widgetId != -1) {
            removeWidgetField(item.id)
            addWidgetField(position, item)
        }

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

    fun getItem(ownerId: Int): HomeItem? {
        val rowMax = if (cellPointName == CELL_POINT_NAME.DOCK) {
            1
        } else {
            Global.ROW_COUNT
        }


        for(page in 0 until 5) {
            val key = "" + page
            val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
                HashMap<String, HomeItem>()
            } else {
                itemList[key]!!
            }

            for (rowId in 0 until rowMax) {
                for (columnId in 0 until Global.COLUMN_COUNT) {
                    val itemKey = "$rowId-$columnId"
                    if (list.containsKey(itemKey)) {
                        val item = list[itemKey]

                        if (item != null && item.id == ownerId) {
                            return item
                        }
                    }
                }
            }
        }

        return null
    }

    fun checkEnableApp(packageName: String, name: String): Boolean {

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


    fun addWidgetField(position: Int, item: HomeItem) {
        val row = item.row
        val column = item.column

        val key = "" + position

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        if (item.fieldRow > 0 || item.fieldColumn > 0) {
            for(rowId in row until (row + (item.fieldRow + 1))) {
                for(columnId in column until (column + (item.fieldColumn + 1))) {
                    if (rowId != row || columnId != column) {
                        val fieldItemKey = "$rowId-$columnId"

                        val fieldItem = item.copyField(rowId, columnId)

                        list[fieldItemKey] = fieldItem

                    }
                }
            }
        }
        itemList[key] = list
    }

    fun removeWidgetField(ownerId: Int) {
        val rowMax = if (cellPointName == CELL_POINT_NAME.DOCK) {
            1
        } else {
            Global.ROW_COUNT
        }


        for(page in 0 until 5) {
            val key = "" + page
            val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
                HashMap<String, HomeItem>()
            } else {
                itemList[key]!!
            }

            for (rowId in 0 until rowMax) {
                for (columnId in 0 until Global.COLUMN_COUNT) {
                    val itemKey = "$rowId-$columnId"
                    if (list.containsKey(itemKey)) {
                        val item = list[itemKey]

                        if (item != null && item.ownerId == ownerId) {
                            //removeHomeItem(page, rowId, columnId)
                        }
                    }
                }
            }
        }


    }

    fun getWidgetFieldList(ownerId: Int): ArrayList<HomeItem> {
        val homeItemList = ArrayList<HomeItem>()

        val rowMax = if (cellPointName == CELL_POINT_NAME.DOCK) {
            1
        } else {
            Global.ROW_COUNT
        }


        for(page in 0 until 5) {
            val key = "" + page
            val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
                HashMap<String, HomeItem>()
            } else {
                itemList[key]!!
            }

            for (rowId in 0 until rowMax) {

                for (columnId in 0 until Global.COLUMN_COUNT) {
                    val itemKey = "$rowId-$columnId"
                    if (list.containsKey(itemKey)) {
                        val item = list[itemKey]

                        if (item != null && item.id != ownerId && item.ownerId == ownerId) {
                            homeItemList.add(item)
                        }
                    }
                }
            }
        }

        return homeItemList
    }

    fun checkNotWidgetData(ownerId: Int): Boolean {
        val rowMax = if (cellPointName == CELL_POINT_NAME.DOCK) {
            1
        } else {
            Global.ROW_COUNT
        }


        for(page in 0 until 5) {
            val key = "" + page
            val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
                HashMap<String, HomeItem>()
            } else {
                itemList[key]!!
            }

            for (rowId in 0 until rowMax) {

                for (columnId in 0 until Global.COLUMN_COUNT) {

                    val itemKey = "$rowId-$columnId"
                    if (list.containsKey(itemKey)) {
                        val item = list[itemKey]

                        if (item != null && item.id == ownerId) {
                            return false
                        }
                    }
                }
            }
        }

        return true
    }

    /**
     * 空白地帯があるかチェックする
     */
    private fun checkFieldSpace(item: HomeItem, page: Int, row: Int, column: Int):Boolean {
        val key = "" + page

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        for(rowId in row until (row + item.fieldRow + 1)) {
            for(columnId in column until (row + item.fieldColumn + 1)) {
                val itemKey = "$rowId-$columnId"
                if (list.containsKey(itemKey)) {
                    return false
                }
            }
        }

        return true
    }

    /**
     * 指定地点以降にWidgetがあるかチェックする
     */
    private fun checkWidgetOnCell(row: Int, column: Int): Boolean {
        val rowMax = if (cellPointName == CELL_POINT_NAME.DOCK) {
            1
        } else {
            Global.ROW_COUNT
        }


        for(page in 0 until 5) {
            val key = "" + page
            val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
                HashMap<String, HomeItem>()
            } else {
                itemList[key]!!
            }

            for (rowId in 0 until rowMax) {
                if (rowId < row) continue

                for (columnId in 0 until Global.COLUMN_COUNT) {
                    if (rowId == row && columnId < column) continue

                    val itemKey = "$rowId-$columnId"
                    if (list.containsKey(itemKey)) {
                        val item = list[itemKey]

                        if (item != null && item.widgetId != -1) {
                            return true
                        }
                    }
                }
            }
        }

        return false
    }


}
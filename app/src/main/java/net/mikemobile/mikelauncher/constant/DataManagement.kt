package net.mikemobile.mikelauncher.constant

import net.mikemobile.mikelauncher.data.HomeItem
import net.mikemobile.mikelauncher.ui.home.HomeFragment

class DataManagement(private val cellPointName: CELL_POINT_NAME) {

    val itemList = HashMap<String, HashMap<String, HomeItem>>()

    fun addItem(page: Int, item: HomeItem): HomeItem? {
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
                    if (item.type == HomeItemType.WIDGET.value) {
                        // widgetだったら、配置範囲が空白かチェックする
                        if (checkFieldSpace(item, page, rowId, columnId)) {
                            // widgetだったら、配置範囲が空白かチェックする
                            row = rowId
                            column = columnId
                            break
                        }
                    } else {
                        // widgetじゃないので追加とする
                        row = rowId
                        column = columnId
                        break
                    }

                }
            }

            if (row != -1 && column != -1) {
                break
            }
        }

        if (row != -1 && column != -1) {
            item.page = page
            item.row = row
            item.column = column

            val itemKey = "$row-$column"
            list[itemKey] = item
            itemList[key] = list


        } else {
            return null
        }
        return item
    }


    fun addItem(
        position: Int,
        newRow: Int,
        newColumn: Int,
        item: HomeItem,
        update: ITEM_MOVE = ITEM_MOVE.MOVING_ITEM_NONE
    ): ITEM_MOVE {
        val key = "" + position
        var row = newRow
        var column = newColumn

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        val addItemKey = "$row-$column"

        if (!list.containsKey(addItemKey)) {
            // データがないので入れて終わり

            if (item.widgetField) {
                // Widget用ブランクデータなのでそのまま追加する
                item.row = row
                item.column = column

                list[addItemKey] = item
                itemList[key] = list

                return ITEM_MOVE.MOVING_ITEM_ENABLED
            } else if (item.type != HomeItemType.WIDGET.value) {
                // widgetじゃないのでそのまま追加
                item.row = row
                item.column = column

                list[addItemKey] = item
                itemList[key] = list

                return ITEM_MOVE.MOVING_ITEM_ENABLED
            }

            // widgetなのでさらに条件をつける

            // 配置位置がGrid外に出ていないかチェック
            if (row + item.fieldRow >= Global.ROW_COUNT) {
                row = Global.ROW_COUNT - item.fieldRow
            }

            if (column + item.fieldColumn > Global.COLUMN_COUNT) {
                column = Global.COLUMN_COUNT - item.fieldColumn
            }



            val fieldList = getWidgetFieldAddList(item, row, column)

            var notItem = true
            for(fieldItem in fieldList) {
                var fieldRow = fieldItem.row
                var fieldColumn = fieldItem.column
                val fieldItemKey = "$fieldRow-$fieldColumn"
                if (list.containsKey(fieldItemKey)) {
                    notItem = false
                    break
                }
            }

            if (notItem) {
                // Widgetの配置場所にアイテムがないのでそのまま追加する
                item.row = row
                item.column = column

                list[addItemKey] = item
                itemList[key] = list

                return ITEM_MOVE.MOVING_ITEM_ENABLED
            }


        }

        // データある場合


//        if (item.type == HomeItemType.WIDGET.value) {
//
//            // ウィジェットの範囲を取得
//            val fieldList = getWidgetFieldAddList(item, row, column)
//
//            // 移動先にアイテムがあるならそれも取得しておく
//            if (list.containsKey(addItemKey)) {
//                list[addItemKey]?.let { fieldList.add(0, it) }
//            }
//
//            // 使用されている（予定も含む）Gridのデータ
//            val activeGridList = list.clone() as HashMap<String, HomeItem>
//
//            // 移動対象のリストを生成する
//            var moveItemList = ArrayList<HomeItem>()
//            for(posiItem in fieldList) {
//                val moveItemKey = "" + posiItem.row + "-" + posiItem.column
//                if (list.containsKey(moveItemKey)) {
//                    list[moveItemKey]?.let {
//                        moveItemList.add(it)
//                    }
//
//
//
//                    // ウィジェットの範囲も対象にするため追加する
//                    activeGridList[moveItemKey] = posiItem
//                }
//            }
//
//            var reMoveItemList = ArrayList<HomeItem>()
//            for(moveItem in moveItemList) {
//                if (moveItem.type == HomeItemType.WIDGET.value) {
//                    // 移動対象にWidgetがあるので動かすのをやめる
//                    return ITEM_MOVE.MOVE_NG
//                }
//
//                val gridPoint = moveItems(moveItem, activeGridList)
//
//                if (gridPoint == null) {
//                    // 移動できないアイテムがあったので移動はなしとする
//                    return ITEM_MOVE.MOVE_NG
//                }
//
//                moveItem.row = gridPoint.row
//                moveItem.column = gridPoint.column
//
//                reMoveItemList.add(moveItem)
//
//                val moveItemKey = "" + moveItem.row + "-" + moveItem.column
//                activeGridList[moveItemKey] = moveItem
//            }
//
//
//            // Widgetを配置する
//            item.row = row
//            item.column = column
//
//            list[addItemKey] = item
//
//            // ここまで来れたので、移動するデータをそれぞれ移動する
//            for(moveItem in reMoveItemList) {
//                val moveItemKey = "" + moveItem.row + "-" + moveItem.column
//                list[moveItemKey] = item
//            }
//
//            itemList[key] = list
//
//            return ITEM_MOVE.MOVING_ITEM_ENABLED
//        }


        // 上記外の条件でWidgetがきたら強制NG
        if (item.type == HomeItemType.WIDGET.value) return ITEM_MOVE.MOVE_NG

        // 指定箇所以降にWidgetがある場合は一旦移動はなしとする
        //if (!checkWidgetOnCell(row, column)) return ITEM_MOVE.MOVE_NG


        // 指定場所以降にスペースがあるかチェックする
        var blancCellEnable = checkBlank(list, row, column)

        if (!blancCellEnable) return ITEM_MOVE.MOVE_NG

        // もともと入っているデータを取り出す
        val outputItem = list[addItemKey]

        list[addItemKey] = item
        itemList[key] = list


        if (item.type != HomeItemType.WIDGET.value) {
            addWidgetField(item)
        }

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

        return update
    }

    private fun moveItems(item: HomeItem, activeGridList: HashMap<String, HomeItem>): GridPoint? {
        val rowMax = if (cellPointName == CELL_POINT_NAME.DOCK) {
            1
        } else {
            Global.ROW_COUNT
        }

        val row = item.row
        val column = item.column

        for (rowId in 0 until rowMax) {
            if (rowId < row) continue

            for (columnId in 0 until Global.COLUMN_COUNT) {
                if (rowId == row && columnId < column) continue

                val itemKey = "$rowId-$columnId"
                if (!activeGridList.containsKey(itemKey)) {
                    // 移動先がない場所があるので確定
                    return GridPoint(rowId, columnId)
                }
            }
        }

        return null
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

    /**
     * 指定箇所が空白かチェックする
     */
    fun checkBlank(page:Int, row: Int, column: Int): Boolean {

        val key = "" + page

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        val addItemKey = "$row-$column"

        if (list.containsKey(addItemKey)) {
            return true
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
    fun checkToolToFolder(position: Int, row: Int, column: Int, homeItem: HomeItem): Boolean {
        val key = "" + position

        val list = if (itemList.size == 0 || !itemList.containsKey(key)) {
            HashMap<String, HomeItem>()
        } else {
            itemList[key]!!
        }

        val addItemKey = "$row-$column"

        if (list.containsKey(addItemKey)) {
            val item = list[addItemKey]

            android.util.Log.i(HomeFragment.TAG,"checkToolToFolder >> homeItem.id :" + homeItem!!.id)
            android.util.Log.i(HomeFragment.TAG,"checkToolToFolder >> homeItem.homeName :" + homeItem!!.homeName)
            android.util.Log.i(HomeFragment.TAG,"checkToolToFolder >> homeItem.label :" + homeItem!!.label)
            android.util.Log.i(HomeFragment.TAG,"checkToolToFolder >> homeItem.type :" + homeItem!!.type)
            android.util.Log.i(HomeFragment.TAG,"checkToolToFolder >> homeItem.toolId :" + homeItem!!.toolId)

            android.util.Log.i(HomeFragment.TAG,"checkToolToFolder >> id :" + item!!.id)
            android.util.Log.i(HomeFragment.TAG,"checkToolToFolder >> homeName :" + item!!.homeName)
            android.util.Log.i(HomeFragment.TAG,"checkToolToFolder >> label :" + item!!.label)
            android.util.Log.i(HomeFragment.TAG,"checkToolToFolder >> type :" + item!!.type)
            android.util.Log.i(HomeFragment.TAG,"checkToolToFolder >> toolId :" + item!!.toolId)

            if (item!!.type == 2 && item!!.toolId == 2) {
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
            //removeWidgetField(item.id)
            //addWidgetField(position, item)
        }

        itemList[key] = list
    }

    fun addHomeItem(item: HomeItem) {
        var page = item.page
        var row = item.row
        var column = item.column

        val key = "" + page

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
            val delItem = list[itemKey]

            list.remove(itemKey)
            itemList[key] = list

            if (delItem != null && delItem.type == HomeItemType.WIDGET.value) {
                removeWidgetField(delItem)
            }

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


    fun addWidgetField(item: HomeItem) {
        var page = item.page
        var row = item.row
        var column = item.column

        val fieldRow = item.fieldRow
        val fieldColumn = item.fieldColumn

        for(rowId in row until (row + fieldRow)) {
            for(columnId in column until (column + fieldColumn)) {
                if (rowId == row && columnId == column) continue

                val widgetField = item.copyField(rowId, columnId)
                addHomeItem(widgetField)
            }
        }
    }

    fun removeWidgetField(item: HomeItem) {
        var page = item.page
        var row = item.row
        var column = item.column

        val fieldRow = item.fieldRow
        val fieldColumn = item.fieldColumn

        for(rowId in row until (row + fieldRow)) {
            for(columnId in column until (column + fieldColumn)) {
                if (rowId == row && columnId == column) continue

                removeHomeItem(page, rowId, columnId)
            }
        }

    }

    fun getWidgetFieldList(item: HomeItem): ArrayList<HomeItem> {
        val homeItemList = ArrayList<HomeItem>()

        var page = item.page
        var row = item.row
        var column = item.column

        val fieldRow = item.fieldRow
        val fieldColumn = item.fieldColumn

        for(rowId in row until (row + fieldRow)) {
            for(columnId in column until (column + fieldColumn)) {
                if (rowId == row && columnId == column) continue

                val itemKey = "$rowId-$columnId"

                val fieldItem = getItem(page, rowId, columnId)

                fieldItem?.let { homeItemList.add(it) }
            }
        }

        return homeItemList
    }


    fun getWidgetFieldAddList(item: HomeItem): ArrayList<HomeItem> {
        var row = item.row
        var column = item.column
        return getWidgetFieldAddList(item, row, column)
    }


    fun getWidgetFieldAddList(item: HomeItem, row: Int, column: Int): ArrayList<HomeItem> {
        val homeItemList = ArrayList<HomeItem>()

        var page = item.page

        val fieldRow = item.fieldRow
        val fieldColumn = item.fieldColumn

        for(rowId in row until (row + fieldRow)) {
            for(columnId in column until (column + fieldColumn)) {
                if (rowId == row && columnId == column) continue

                val fieldItem = item.copyField(rowId, columnId)
                homeItemList.add(fieldItem)
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

        val fieldRow = item.fieldRow
        val fieldColumn = item.fieldColumn

        if (row + fieldRow >= Global.ROW_COUNT || column + fieldColumn >= Global.COLUMN_COUNT) {
            return false
        }

        for(rowId in row until (row + fieldRow)) {
            for(columnId in column until (column + fieldColumn)) {
                if (rowId == row && columnId == column) continue

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



    /**
     * フォルダーの存在判定
     */
    fun checkToolToFolder(homeItem: HomeItem): Boolean {
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

                        if (item != null && item.folderId == homeItem.folderId) {
                            return true
                        }
                    }
                }
            }
        }

        return false
    }





    //////
    fun getList(type: HomeItemType): ArrayList<HomeItem> {
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

            for(row in 0 until rowMax) {
                for(column in 0 until Global.COLUMN_COUNT) {
                    val itemKey = "$row-$column"
                    if (list.containsKey(itemKey)) {
                        val item = list[itemKey]

                        if (item != null && item.page == -1) {
                            item.page = page

                            list[itemKey] = item
                        }

                        if (item != null && !item.widgetField) {
                            if (type == HomeItemType.ALL || item.type == type.value) {
                                homeItemList.add(item)
                            }
                        }
                    }
                }
            }

            itemList[key] = list
        }

        return homeItemList
    }

    fun setWidgetField() {
        val list = getList(HomeItemType.WIDGET)

        for(widgetItem in list) {
            addWidgetFieldToItem(widgetItem)
        }
    }


    fun addWidgetFieldToItem(widgetItem: HomeItem) {

        var page = widgetItem.page
        var row = widgetItem.row
        var column = widgetItem.column

        val fieldRow = widgetItem.fieldRow
        val fieldColumn = widgetItem.fieldColumn

//            android.util.Log.i("DataManagement","name:" + widgetItem.label + "\n" +
//                    "     page:" + page + " / row:" + row + " / column:" + column + "\n" +
//                    "     fieldRow:" + fieldRow + " / fieldColumn:" + fieldColumn + "\n" +
//                    "     width:" + widgetItem.width + " / height:" + widgetItem.height)

        for(rowId in row until (row + fieldRow)) {
            for(columnId in column until (column + fieldColumn)) {
                if (rowId == row && columnId == column) continue

                val itemKey = "$rowId-$columnId"

                android.util.Log.i("DataManagement","name:" + widgetItem.label +
                        "     blank - itemKey:" + itemKey)

                if (!checkBlank(page, rowId, columnId)) {
                    // 空白が存在しないので追加する
                    android.util.Log.e("DataManagement","add widget field")

                    val fieldItem = widgetItem.copyField(rowId, columnId)
                    addItem(page, rowId, columnId, fieldItem)
                } else {
                    // 空白データあり
                    android.util.Log.i("DataManagement","is widget field")

                }
            }
        }
    }

}
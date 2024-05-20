package net.mikemobile.mikelauncher.constant

import net.mikemobile.mikelauncher.data.HomeItem

class FolderManagement() {


    val itemList = HashMap<String, ArrayList<HomeItem>>()

    fun addItem(folderId: Int, item: HomeItem): ArrayList<HomeItem> {
        val folderKey = "" + folderId


        val list = if (itemList.size == 0 || !itemList.containsKey(folderKey)) {
            ArrayList<HomeItem>()
        } else {
            itemList[folderKey]!!
        }

        list.add(item)
        itemList[folderKey] = list

        return list
    }

    fun getList(folderId: Int): ArrayList<HomeItem> {
        val folderKey = "" + folderId

        val list = if (itemList.size == 0 || !itemList.containsKey(folderKey)) {
            ArrayList<HomeItem>()
        } else {
            itemList[folderKey]!!
        }

        return list
    }



    fun updateItem(folderId: Int, item: HomeItem): ArrayList<HomeItem> {
        val folderKey = "" + folderId

        val list = if (itemList.size == 0 || !itemList.containsKey(folderKey)) {
            ArrayList<HomeItem>()
        } else {
            itemList[folderKey]!!
        }

        for(i in 0 until list.size) {
            if (list[i].id == item.id && list[i].packageName == item.packageName) {
                list.set(i, item)
                break
            }
        }
        itemList[folderKey] = list

        return list
    }

    fun removeItem(folderId: Int, item: HomeItem): Boolean {
        val folderKey = "" + folderId

        val list = if (itemList.size == 0 || !itemList.containsKey(folderKey)) {
            ArrayList<HomeItem>()
        } else {
            itemList[folderKey]!!
        }

        for(i in 0 until list.size) {
            if (list[i].id == item.id && list[i].packageName == item.packageName) {
                list.removeAt(i)
                break
            }
        }
        itemList[folderKey] = list

        return true
    }

    fun removeAllItem(folderId: Int) {
        val folderKey = "" + folderId

        val list = if (itemList.size == 0 || !itemList.containsKey(folderKey)) {
            ArrayList<HomeItem>()
        } else {
            itemList[folderKey]!!
        }

        list.clear()

        itemList[folderKey] = list
    }

    fun checkEnableApp(packageName: String, name: String): Boolean {

        val keys = itemList.keys

        for(key in keys) {
            val pageList = itemList[key]

            pageList?.let {

                for(item in pageList) {
                    if (item.packageName == packageName && item.name == name) {
                        return true
                    }
                }
            }
        }

        return false
    }


}
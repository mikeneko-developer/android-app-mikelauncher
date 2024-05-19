package net.mikemobile.mikelauncher.data

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.mikemobile.mikelauncher.constant.Global
import net.mikemobile.mikelauncher.constant.Global.Companion.generateId
import net.mikemobile.mikelauncher.constant.Global.Companion.getToolIcon

class AppPreference(private val context: Context) {

    private val sharePrefs = context.getSharedPreferences("NET.MIKEMOBILE.MIKE_LAUNCHER", Context.MODE_PRIVATE)
    private fun getEditor() = sharePrefs.edit()

    private fun setAppsData(jsonText: String?) = getEditor().putString("APPS_PAGE_DATA", jsonText).apply()
    private fun getAppsData() = sharePrefs.getString("APPS_PAGE_DATA", null)


    private fun setDockAppsData(jsonText: String?) = getEditor().putString("DOCK_APPS_PAGE_DATA", jsonText).apply()
    private fun getDockAppsData() = sharePrefs.getString("DOCK_APPS_PAGE_DATA", null)



    private fun setFolderAppsData(jsonText: String?) = getEditor().putString("FOLDER_APPS_DATA", jsonText).apply()
    private fun getFolderAppsData() = sharePrefs.getString("FOLDER_APPS_DATA", null)



    fun setAppsList() {
        saveDesktopAppsList()
        saveDockAppsList()
        saveFolderAppsList()
    }

    private fun saveDesktopAppsList() {
        val jsonList = HashMap<String, ArrayList<HashMap<String, String>>>()

        for(position in 0 until 5) {
            val key = "" + position

            val jsonHashList = ArrayList<HashMap<String, String>>()

            val list = Global.homeItemData.itemList[key]

            list?.let {

                for(itemKey in list.keys) {
                    val item = list[itemKey]!!
                    jsonHashList.add(item.convertHash(itemKey))
                }
            }

            jsonList[key] = jsonHashList

        }

        val jsonText = Json.encodeToString(jsonList)
        setAppsData(jsonText)
    }
    private fun saveDockAppsList() {
        val jsonList = HashMap<String, ArrayList<HashMap<String, String>>>()

        for(position in 0 until 5) {
            val key = "" + position

            val jsonHashList = ArrayList<HashMap<String, String>>()

            val list = Global.dockItemData.itemList[key]

            list?.let {

                for(itemKey in list.keys) {
                    val item = list[itemKey]!!
                    jsonHashList.add(item.convertHash(itemKey))
                }
            }

            jsonList[key] = jsonHashList

        }

        val jsonText = Json.encodeToString(jsonList)
        setDockAppsData(jsonText)
    }

    private fun saveFolderAppsList() {
        val jsonList = HashMap<String, ArrayList<HashMap<String, String>>>()

        for(key in Global.folderManager.itemList.keys) {
            val jsonHashList = ArrayList<HashMap<String, String>>()

            val list = Global.folderManager.itemList[key]

            list?.let {
                for(item in list) {
                    jsonHashList.add(item.convertHash())
                }
            }
            jsonList[key] = jsonHashList
        }

        val jsonText = Json.encodeToString(jsonList)
        setFolderAppsData(jsonText)
    }



    fun getAppsList() {
        loadDesktopAppsList()
        loadDockAppsList()
        loadFolderAppsList()
    }

    private fun loadDesktopAppsList() {
        Global.homeItemData.itemList.clear()

        val jsonText = getAppsData()
        jsonText?.let {
            val list = Json.decodeFromString(it) as HashMap<String, ArrayList<HashMap<String, String>>>

            for(position in 0 until 5) {
                val key = "" + position

                val itemList = HashMap<String, HomeItem>()

                val pageList = list[key]
                pageList?.let {
                    for(item in pageList) {
                        val itemKey = item["key"]!!
                        val homeItem = HomeItem(item)

                        if (homeItem.id == -1) {
                            homeItem.id = generateId()
                        }

                        if (homeItem.toolId != -1) {
                            homeItem.icon = getToolIcon(context, homeItem.toolId)

                        } else if (homeItem.widgetId == -1) {
                            homeItem.icon = Global.getAppIcon(context, homeItem.packageName)
                        }

                        itemList[itemKey] = homeItem
                    }
                }

                Global.homeItemData.itemList[key] = itemList
            }

        }
    }
    private fun loadDockAppsList() {
        Global.dockItemData.itemList.clear()

        val jsonText = getDockAppsData()
        jsonText?.let {
            val list = Json.decodeFromString(it) as HashMap<String, ArrayList<HashMap<String, String>>>

            for(position in 0 until 5) {
                val key = "" + position

                val itemList = HashMap<String, HomeItem>()

                val pageList = list[key]
                pageList?.let {
                    for(item in pageList) {
                        val itemKey = item["key"]!!
                        val homeItem = HomeItem(item)

                        if (homeItem.id == -1) {
                            homeItem.id = generateId()
                        }

                        if (homeItem.toolId != -1) {
                            homeItem.icon = getToolIcon(context, homeItem.toolId)

                        } else if (homeItem.widgetId == -1) {
                            homeItem.icon = Global.getAppIcon(context, homeItem.packageName)
                        }

                        itemList[itemKey] = homeItem
                    }
                }

                Global.dockItemData.itemList[key] = itemList
            }

        }
    }

    private fun loadFolderAppsList() {
        Global.folderManager.itemList.clear()

        val jsonText = getFolderAppsData()
        jsonText?.let {
            val list = Json.decodeFromString(it) as HashMap<String, ArrayList<HashMap<String, String>>>

            for(key in list.keys) {
                val itemList = ArrayList<HomeItem>()

                val folderInItemList = list[key]

                folderInItemList?.let {
                    for(item in folderInItemList) {
                        val homeItem = HomeItem(item)

                        if (homeItem.id == -1) {
                            homeItem.id = generateId()
                        }

                        if (homeItem.toolId != -1) {
                            homeItem.icon = getToolIcon(context, homeItem.toolId)

                        } else if (homeItem.widgetId == -1) {
                            homeItem.icon = Global.getAppIcon(context, homeItem.packageName)
                        }

                        itemList.add(homeItem)
                    }
                }

                Global.folderManager.itemList[key] = itemList

            }

        }
    }
}
package net.mikemobile.mikelauncher.data

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.mikemobile.mikelauncher.constant.Global

class AppPreference(private val context: Context) {

    private val sharePrefs = context.getSharedPreferences("NET.MIKEMOBILE.MIKE_LAUNCHER", Context.MODE_PRIVATE)
    private fun getEditor() = sharePrefs.edit()

    private fun setAppsData(jsonText: String?) = getEditor().putString("APPS_PAGE_DATA", jsonText).apply()
    private fun getAppsData() = sharePrefs.getString("APPS_PAGE_DATA", null)


    private fun setDockAppsData(jsonText: String?) = getEditor().putString("DOCK_APPS_PAGE_DATA", jsonText).apply()
    private fun getDockAppsData() = sharePrefs.getString("DOCK_APPS_PAGE_DATA", null)



    fun setAppsList() {
        saveDesktopAppsList()
        saveDockAppsList()
    }

    private fun saveDesktopAppsList() {
        val jsonList = HashMap<String, ArrayList<HashMap<String, String>>>()

        for(position in 0 until 5) {
            val key = "" + position

            val jsonHashList = ArrayList<HashMap<String, String>>()

            val list = Global.homeItemList.itemList[key]

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

            val list = Global.dockItemList.itemList[key]

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



    fun getAppsList() {
        loadDesktopAppsList()
        loadDockAppsList()
    }

    private fun loadDesktopAppsList() {
        Global.homeItemList.itemList.clear()

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

                        if (homeItem.toolId != -1) {
                            homeItem.icon = ResourcesCompat.getDrawable(
                                context.resources,
                                net.mikemobile.mikelauncher.R.drawable.icon_drawer_menu,
                                null)

                        } else if (homeItem.widgetId == -1) {
                            homeItem.icon = Global.getAppIcon(context, homeItem.packageName)
                        }

                        itemList[itemKey] = homeItem
                    }
                }

                Global.homeItemList.itemList[key] = itemList
            }

        }
    }
    private fun loadDockAppsList() {
        Global.dockItemList.itemList.clear()

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

                        if (homeItem.toolId != -1) {
                            homeItem.icon = ResourcesCompat.getDrawable(
                                context.resources,
                                net.mikemobile.mikelauncher.R.drawable.icon_drawer_menu,
                                null)

                        } else if (homeItem.widgetId == -1) {
                            homeItem.icon = Global.getAppIcon(context, homeItem.packageName)
                        }

                        itemList[itemKey] = homeItem
                    }
                }

                Global.dockItemList.itemList[key] = itemList
            }

        }
    }
}
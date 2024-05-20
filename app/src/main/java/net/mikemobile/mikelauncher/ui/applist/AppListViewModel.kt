package net.mikemobile.mikelauncher.ui.applist

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModel
import net.mikemobile.mikelauncher.R

class AppListViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    var list = listOf<AppInfo>()

    fun create(context: Context, update: Boolean): List<AppInfo> {
        if (!update && list.size > 0) return list


        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN)
            .also { it.addCategory(Intent.CATEGORY_LAUNCHER) }
        list = pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
            .asSequence()
            .mapNotNull { it.activityInfo }
            .filter { it.packageName != context.packageName }
            .map {
                AppInfo(
//                    it.loadIcon(pm) ?: getDefaultIcon(context),
                    it.loadLabel(pm).toString(),
                    ComponentName(it.packageName, it.name),
                    it.packageName,
                    it.name
                )
            }
            .sortedBy { it.label }
            .toList()

        return list
    }

    fun getDefaultIcon(context: Context): Drawable {
        return ResourcesCompat.getDrawable(context.resources, R.drawable.ic_launcher_foreground, null)!!
    }
}
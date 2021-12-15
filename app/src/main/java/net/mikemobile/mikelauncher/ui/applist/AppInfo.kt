package net.mikemobile.mikelauncher.ui.applist

import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.View

data class AppInfo(
    val icon: Drawable,
    val label: String,
    val componentName: ComponentName
) {
    fun launch(context: Context, view: View? = null) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).also {
                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                it.addCategory(Intent.CATEGORY_LAUNCHER)
                it.component = componentName
            }
            val options = view?.let {
                ActivityOptions.makeScaleUpAnimation(it, 0, 0, it.width, it.height)
                    .toBundle()
            }
            context.startActivity(intent, options)
        } catch (e: ActivityNotFoundException) {
        }
    }
}
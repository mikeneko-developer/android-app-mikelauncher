package net.mikemobile.mikelauncher.ui.applist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.constant.Global

class AppAdapter(
    private val context: Context,
    private val inflater: LayoutInflater,
    private val onClick: (view: View, info: AppInfo) -> Unit,
    private val onLongClick: (view: View, info: AppInfo) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder =
        AppViewHolder(inflater.inflate(R.layout.folder_item, parent, false))

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val info = list[position]
        holder.itemView.setOnClickListener { onClick(holder.icon, info) } // <- ココ
        holder.itemView.setOnLongClickListener {
            onLongClick(holder.icon, info)
            return@setOnLongClickListener true
        }

        downLoadImage(info, holder.icon)

        //holder.icon.setImageDrawable(info.icon)
        holder.label.text = info.label
        //holder.packageName.text = info.componentName.packageName

        val count = Global.getNotificationCount(info.packageName)
        if (count == 0) {
            holder.notification.text = ""
            holder.notification.visibility = View.GONE
        } else {
            holder.notification.text = "" + count
            holder.notification.visibility = View.VISIBLE
        }

        if (Global.homeItemData.checkEnableApp( info.componentName.packageName, info.componentName.className)) {
            holder.itemView.setBackgroundResource(R.drawable.select_app)
        } else if (Global.dockItemData.checkEnableApp( info.componentName.packageName, info.componentName.className)) {
            holder.itemView.setBackgroundResource(R.drawable.select_app)
        } else if (Global.folderManager.checkEnableApp( info.componentName.packageName, info.componentName.className)) {
            holder.itemView.setBackgroundResource(R.drawable.select_app)
        } else {
            holder.itemView.setBackgroundResource(R.drawable.un_select_app)
        }
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.imageView)
        val notification: TextView = itemView.findViewById(R.id.noti_count)
        val label: TextView = itemView.findViewById(R.id.textView)
        //val packageName: TextView = itemView.findViewById(R.id.packageName)
    }

    private var list: List<AppInfo> = emptyList()

    fun updateList(newList: List<AppInfo>) {
        val diff = DiffUtil.calculateDiff(DiffCallback(list, newList), true)
        list = newList
        diff.dispatchUpdatesTo(this)
    }

    fun downLoadImage(info: AppInfo, imageView: ImageView) {
        CoroutineScope(Dispatchers.IO).launch {

            val icon = Global.getAppIcon(context, info.packageName)
            // メインスレッドでUIを更新
            withContext(Dispatchers.Main) {
                imageView.setImageDrawable(icon)
            }
        }
    }

    private class DiffCallback(
        private val old: List<AppInfo>,
        private val new: List<AppInfo>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = old.size
        override fun getNewListSize(): Int = new.size
        override fun areItemsTheSame(op: Int, np: Int): Boolean =
            old[op].componentName == new[np].componentName
        override fun areContentsTheSame(op: Int, np: Int): Boolean =
//            old[op].label == new[np].label && old[op].icon == new[np].icon
            old[op].label == new[np].label
    }
}

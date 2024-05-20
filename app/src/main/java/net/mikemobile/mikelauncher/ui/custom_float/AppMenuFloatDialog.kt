package net.mikemobile.mikelauncher.ui.custom_float

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.ui.home.createMenuView

class AppMenuFloatDialog(private val context: Context,
                         private val callbackDelete: () -> Unit,
                         private val callbackInfo: () -> Unit,
                         private val callbackEdit: () -> Unit,
                         close:() -> Unit
) : BaseFloatingDialog(context, close) {
    override fun onCreate(context: Context): View? {

        val width = 600

        return createMenuView(context, width)
    }

    override fun onCreateView(context: Context, view: View) {
        val deleteButton = view.findViewById(R.id.button_delete) as LinearLayout
        deleteButton?.setOnClickListener {
            close()
            callbackDelete.invoke()
        }

        val infoButton = view.findViewById(R.id.button_info) as LinearLayout
        infoButton?.setOnClickListener {
            close()
            callbackInfo.invoke()
        }

        val editButton = view.findViewById(R.id.button_edit) as LinearLayout
        editButton?.setOnClickListener {
            close()
            callbackEdit.invoke()
        }
    }

    fun setDialogSize(startX: Float, startY: Float) {
        getDialogView()?.let {
            it.translationX = startX
            it.translationY = startY
        }
    }
}
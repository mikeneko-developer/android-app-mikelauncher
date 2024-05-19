package net.mikemobile.mikelauncher.ui.custom_float

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet


abstract class BaseFloatingDialog(context: Context) {

    private var dialogView: View? = null
    private var ownerView: ViewGroup? = null
    init {
        dialogView = onCreate(context)
        dialogView?.let {
            onCreateView(context, it)
        }
    }

    abstract fun onCreate(context: Context): View?
    abstract fun onCreateView(context: Context, view: View)

    fun open(ownerView: ConstraintLayout?, isCenter: Boolean = true) {

        if (dialogView == null) {
            android.util.Log.e("BaseFloatingDialog","open >> dialogView is null")
        }

        if (ownerView == null) {
            android.util.Log.e("BaseFloatingDialog","open >> ownerView is null")
        }
        ownerView!!.visibility = View.VISIBLE
        ownerView!!.addView(dialogView)

        this.ownerView = ownerView
        val viewId = View.generateViewId()
        dialogView!!.id = viewId

        if (!isCenter) return

        // ConstraintSetを使ってビューの制約を設定
        val constraintSet = ConstraintSet()
        constraintSet.clone(ownerView)


        // 中央に配置
        constraintSet.connect(
            dialogView!!.id,
            ConstraintSet.LEFT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.LEFT,
            0
        )
        constraintSet.connect(
            dialogView!!.id,
            ConstraintSet.RIGHT,
            ConstraintSet.PARENT_ID,
            ConstraintSet.RIGHT,
            0
        )
        constraintSet.connect(
            dialogView!!.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP,
            0
        )
        constraintSet.connect(
            dialogView!!.id,
            ConstraintSet.BOTTOM,
            ConstraintSet.PARENT_ID,
            ConstraintSet.BOTTOM,
            0
        )

        // ConstraintSetを適用
        constraintSet.applyTo(ownerView)
    }

    fun close() {
        ownerView?.let {
            it.removeAllViews()
            it.visibility = View.GONE
        }
    }

    fun getDialogView(): View? {
        return dialogView
    }
}
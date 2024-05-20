package net.mikemobile.mikelauncher.ui.dialog

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.constant.HomeItemType
import net.mikemobile.mikelauncher.data.HomeItem
import net.mikemobile.mikelauncher.ui.custom.DragAndDropView

/**
 * Created by mikeneko on 2016/09/10.
 */
class TitleEditDialog(
    val item: HomeItem,
    private val callback: (HomeItem) -> Unit) : DialogFragment(), View.OnTouchListener {
    private lateinit var dialog: Dialog

    private val TAG = "TitleEditDialog"

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val activity = requireActivity()
        val inflater = activity.layoutInflater

        //ダイアログの作成
        dialog = Dialog(activity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
        )

        val view = inflater.inflate(R.layout.dialog_title_edit, null, false)
        dialog.setContentView(view)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        view.setOnTouchListener(this)

        setView(view)
        return dialog
    }

    // ダイアログの横幅、高さ、表示位置を設定
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val lp = dialog.window!!.attributes
        val metrics = resources.displayMetrics
        //lp.width = (int) (metrics.widthPixels * 0.8);//横幅を80%
        //lp.height = (int) (metrics.heightPixels * 0.8);//高さを80%
        //lp.x = 100; //表示位置を指定した分、右へ移動
        //lp.y = 200; //表示位置を指定した分、下へ移動
        dialog.window!!.attributes = lp
    }
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        // 領域外をタップしたときの処理をここに書く
        close()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        // ダイアログが消えたときの処理をここに書く
    }

    fun close() {
        dismiss()
    }

    fun open(parentFragmentManager: FragmentManager) {
        activity?.let {
            show(parentFragmentManager,"")
        }
    }

    private var negative = "閉じる"


    private fun setView(view: View) {
        val textView = view.findViewById<EditText>(R.id.edit_title)
        val btn_negative = view.findViewById<View>(R.id.button) as Button


        btn_negative.setOnClickListener {
            item.homeName = textView.text.toString()
            callback.invoke(item)
            close()
        }

        val text = if (item.homeName != ""){
            item.homeName
        } else {
            item.label
        }

        textView.setText(text)
    }

    override fun onTouch(v: View?, motionEvent: MotionEvent?): Boolean {

        when (motionEvent?.action) {
            MotionEvent.ACTION_DOWN -> {
                android.util.Log.i(TAG,"ACTION_DOWN")
                close()
            }
            MotionEvent.ACTION_MOVE -> {
                android.util.Log.i(TAG,"ACTION_MOVE")


            }
            MotionEvent.ACTION_UP -> {
                android.util.Log.i(TAG,"ACTION_UP")

            }
            MotionEvent.ACTION_CANCEL -> {
                android.util.Log.i(TAG,"ACTION_CANCEL")

            }
        }
        return false
    }
}
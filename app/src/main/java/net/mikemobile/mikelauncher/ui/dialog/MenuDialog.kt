package net.mikemobile.mikelauncher.ui.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import net.mikemobile.mikelauncher.R
import net.mikemobile.mikelauncher.constant.HomeItemType
import net.mikemobile.mikelauncher.data.AppBackupPreference
import java.util.Date

/**
 * Created by mikeneko on 2016/09/10.
 */
class MenuDialog(
    val title: String,
    val restoreListener: () -> Unit,
    val positiveListener: (HomeItemType) -> Unit,
) : DialogFragment() {
    private lateinit var dialog: Dialog

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

        val view = inflater.inflate(R.layout.dialog_menu, null, false)
        dialog.setContentView(view)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

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
        val tv_title = view.findViewById<View>(R.id.dialog_title_text) as TextView
        val btn_negative = view.findViewById<View>(R.id.button7) as Button

        val btnApps = view.findViewById<View>(R.id.button4) as Button
        val btnWidget = view.findViewById<View>(R.id.button5) as Button
        val btnTools = view.findViewById<View>(R.id.button6) as Button

        val btnBackup = view.findViewById<View>(R.id.button2) as Button
        val btnRestore = view.findViewById<View>(R.id.button8) as Button
        val tv_backupDate = view.findViewById<View>(R.id.textView4) as TextView

        tv_title.text = title
        btn_negative.text = negative

        btn_negative.setOnClickListener {
            close()
        }

        btnApps.setOnClickListener {
            positiveListener.invoke(HomeItemType.APP)
            close()
        }
        btnWidget.setOnClickListener {
            positiveListener.invoke(HomeItemType.WIDGET)
            close()
        }
        btnTools.setOnClickListener {
            positiveListener.invoke(HomeItemType.TOOL)
            close()
        }
        context?.let {
            val backupPref = AppBackupPreference(it)

            val datetime = backupPref.getDate()

            if (datetime == 0L) {
                tv_backupDate.text = "Backupデータなし"
            } else {
                val df = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                val date = Date(datetime)

                tv_backupDate.text = "Backup日:" + df.format(date)
            }
        }

        btnBackup.setOnClickListener {
            context?.let {
                val backupPref = AppBackupPreference(it)
                backupPref.setAppsList()

                Toast.makeText(it, "バックアップが完了しました", Toast.LENGTH_SHORT).show()

                val datetime = backupPref.getDate()
                if (datetime == 0L) {
                    tv_backupDate.text = "Backupデータなし"
                } else {
                    val df = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    val date = Date(datetime)

                    tv_backupDate.text = "Backup日:" + df.format(date)
                }
            }
        }

        btnRestore.setOnClickListener {
            context?.let {
                val backupPref = AppBackupPreference(it)

                val datetime = backupPref.getDate()
                if (datetime == 0L) {
                    Toast.makeText(it, "復元するデータがありません", Toast.LENGTH_SHORT).show()
                } else {
                    backupPref.getAppsList()
                    restoreListener.invoke()
                    Toast.makeText(it, "データの復元が完了しました", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }
}
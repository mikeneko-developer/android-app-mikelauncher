package net.mikemobile.mikelauncher.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.view.View
import kotlin.math.sqrt

/**
 * 画像からアウトライン画像を生成する
 */
fun processImageToOutline(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    // 出力用のBitmapを作成
    val outlineBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outlineBitmap)
    val paint = Paint()
    paint.color = Color.DKGRAY
    paint.style = Paint.Style.STROKE

    // Sobelフィルタのカーネル
    val sobelX = arrayOf(
        intArrayOf(-1, 0, 1),
        intArrayOf(-2, 0, 2),
        intArrayOf(-1, 0, 1)
    )

    val sobelY = arrayOf(
        intArrayOf(-1, -2, -1),
        intArrayOf(0, 0, 0),
        intArrayOf(1, 2, 1)
    )

    // 画像のエッジを計算
    for (y in 1 until height - 1) {
        for (x in 1 until width - 1) {
            var gx = 0
            var gy = 0

            // 3x3カーネルで畳み込み
            for (i in -1..1) {
                for (j in -1..1) {
                    val pixel = bitmap.getPixel(x + i, y + j)
                    val intensity = Color.red(pixel)
                    gx += intensity * sobelX[i + 1][j + 1]
                    gy += intensity * sobelY[i + 1][j + 1]
                }
            }

            // エッジ強度の計算
            val edgeStrength = sqrt((gx * gx + gy * gy).toDouble()).toInt()
            if (edgeStrength > 128) {
                canvas.drawPoint(x.toFloat(), y.toFloat(), paint)
            }
        }
    }

    return outlineBitmap
}

fun processImageToFill(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    // 出力用のBitmapを作成
    val filledBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(filledBitmap)
    val paint = Paint()
    paint.color = Color.BLACK
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)

    // 塗りつぶし部分を描く
    canvas.drawRect(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat(), paint)


    return filledBitmap
}


fun getViewCapture(view: View): Bitmap? {
    view.isDrawingCacheEnabled = true

    // Viewのキャッシュを取得
    val cache = view.drawingCache
    val screenShot = Bitmap.createBitmap(cache)
    view.isDrawingCacheEnabled = false
    return screenShot
}

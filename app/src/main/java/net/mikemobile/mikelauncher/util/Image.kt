package net.mikemobile.mikelauncher.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.sqrt

fun processImageToOutline(bitmap: Bitmap): Bitmap {
    val width = bitmap.width
    val height = bitmap.height

    // 出力用のBitmapを作成
    val outlineBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(outlineBitmap)
    val paint = Paint()
    paint.color = Color.BLACK
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
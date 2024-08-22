package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.Html
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions


class BarCodeAnalyzer(private val context: Context) :
    ImageAnalysis.Analyzer {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(
            Barcode.FORMAT_EAN_13,
            Barcode.FORMAT_UPC_A,
            Barcode.FORMAT_ALL_FORMATS
        )
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    private var lastToast: Long = 0
    private val toastInterval: Long = 5000

    private val recordSearch = RecordSearch()
    private var scannable = true

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {

        imageProxy.image?.let { image ->
            scanner.process(
                InputImage.fromMediaImage(
                    image, imageProxy.imageInfo.rotationDegrees
                )
            ).addOnSuccessListener { barcode ->
                if (barcode.isNotEmpty()) {
                    val result = barcode.mapNotNull {
                        it.rawValue ?: it.displayValue ?: it.url?.url
                    }.joinToString(",")

                    if (result.isNotEmpty()) {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastToast > toastInterval) {
                            recordSearch.searchByBarcode(result) { record, year, country, format, label, genre, style, cover ->
                                val message = buildString {
                                    append("<b>$record</b></font><br><br>")
                                    append("Released $year - $country<br><br>")
                                    append("$format<br><br>")
                                    append("Label: $label<br><br>")
                                    append("Genre: $genre<br>")
                                    append("Style: $style<br>")
                                }

                                val dialogView = View.inflate(context, R.layout.dialog_layout, null)
                                val imageView =
                                    dialogView.findViewById<ImageView>(R.id.dialog_image)
                                val messageView =
                                    dialogView.findViewById<TextView>(R.id.dialog_message)

                                Glide.with(context)
                                    .load(cover)
                                    .apply(RequestOptions().centerCrop())
                                    .into(imageView)

                                messageView.text = Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
                                messageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)


                                val alertDialog =
                                    AlertDialog.Builder(context, R.style.CustomAlertDialog)
                                        .setView(dialogView)
                                        .setPositiveButton("close") { dialog: DialogInterface, _: Int ->
                                            dialog.dismiss()
                                        }
                                        .create()

                                alertDialog.setOnShowListener {
                                    alertDialog.window?.setBackgroundDrawable(
                                        ColorDrawable(
                                            Color.TRANSPARENT
                                        )
                                    )

                                    val positiveButton =
                                        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    positiveButton.setTextColor(
                                        ContextCompat.getColor(
                                            context,
                                            R.color.white
                                        )
                                    )

                                }
                                alertDialog.show()
                            }
                            lastToast = currentTime

                        }
                    }
                }
            }.addOnFailureListener {
                Log.e("BarCodeAnalyzer", "Barcode processing failed: ${it.localizedMessage}", it)
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }
}
package com.oldbag.statement

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var preview: PreviewView
    private lateinit var result: EditText
    private lateinit var mill: Spinner
    private lateinit var year: Spinner
    private var imageCapture: ImageCapture? = null
    private val recognizer by lazy { TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) startCamera()
        else ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 44)
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            setBackgroundColor(0xFFF5F7FA.toInt())
        }
        val title = TextView(this).apply {
            text = "OLD BAG STATEMENT MAKER\nOffline Bill Scanner"
            textSize = 22f; setTextColor(0xFF173B68.toInt()); gravity = Gravity.CENTER
        }
        root.addView(title, LinearLayout.LayoutParams(-1, 76))
        mill = Spinner(this)
        mill.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Mill 1", "Mill 2", "Mill 3", "Mill 4"))
        root.addView(label("Select Mill")); root.addView(mill)
        year = Spinner(this)
        year.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("2022-23", "2023-24", "2024-25", "2025-26"))
        root.addView(label("Financial Year")); root.addView(year)
        preview = PreviewView(this)
        root.addView(preview, LinearLayout.LayoutParams(-1, 280))
        val scan = Button(this).apply { text = "CAPTURE BILL & READ OFFLINE" }
        root.addView(scan)
        scan.setOnClickListener { captureAndRead() }
        root.addView(label("Recognized text (check and correct before saving)"))
        result = EditText(this).apply {
            minLines = 8; gravity = Gravity.TOP; hint = "Scanned bill text will appear here"
        }
        root.addView(result, LinearLayout.LayoutParams(-1, 0, 1f))
        val note = TextView(this).apply {
            text = "Version 0.1 • OCR text only. Verify every figure against the original bill."
            textSize = 12f; setTextColor(0xFF555555.toInt())
        }
        root.addView(note)
        setContentView(root)
    }

    private fun label(s: String) = TextView(this).apply {
        text = s; textSize = 14f; setTextColor(0xFF333333.toInt()); setPadding(0, 10, 0, 4)
    }

    private fun startCamera() {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider = future.get()
            val previewUseCase = Preview.Builder().build().also { it.setSurfaceProvider(preview.surfaceProvider) }
            imageCapture = ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY).build()
            try {
                provider.unbindAll()
                provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, previewUseCase, imageCapture)
            } catch (e: Exception) { Toast.makeText(this, "Camera error: ${e.message}", Toast.LENGTH_LONG).show() }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureAndRead() {
        val capture = imageCapture ?: run { Toast.makeText(this, "Camera is starting", Toast.LENGTH_SHORT).show(); return }
        val dir = File(getExternalFilesDir(null), "original_bills/${mill.selectedItem}/${year.selectedItem}")
        dir.mkdirs()
        val name = "bill_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date()) + ".jpg"
        val file = File(dir, name)
        val options = ImageCapture.OutputFileOptions.Builder(file).build()
        capture.takePicture(options, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Toast.makeText(this@MainActivity, "Photo saved. Reading offline…", Toast.LENGTH_SHORT).show()
                try {
                    val image = InputImage.fromFilePath(this@MainActivity, android.net.Uri.fromFile(file))
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            result.setText(visionText.text)
                            Toast.makeText(this@MainActivity, "OCR complete. Please verify the bill.", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { e -> Toast.makeText(this@MainActivity, "OCR failed: ${e.message}", Toast.LENGTH_LONG).show() }
                } catch (e: Exception) { Toast.makeText(this@MainActivity, "Image error: ${e.message}", Toast.LENGTH_LONG).show() }
            }
            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(this@MainActivity, "Capture failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 44 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) startCamera()
        else Toast.makeText(this, "Camera permission is required to scan bills.", Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() { recognizer.close(); super.onDestroy() }
}

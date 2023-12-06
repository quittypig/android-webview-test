package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    var cameraPath = ""
    var mWebViewImageUpload: ValueCallback<Array<Uri>>? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val webView = findViewById<WebView>(R.id.webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            setSupportMultipleWindows(true)
        }

        webView.apply {
            webViewClient = WebViewClient()
            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?
                ): Boolean {
                    try {
                        mWebViewImageUpload = filePathCallback!!

                        var takePictureIntent: Intent?

                        takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

                        if (takePictureIntent.resolveActivity(packageManager) != null) {

                            var photoFile: File? = createImageFile()
                            takePictureIntent.putExtra("PhotoPath", cameraPath)

                            if (photoFile != null) {
                                cameraPath = "file:${photoFile.absolutePath}"
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                            } else takePictureIntent = null
                        }
                        val contentSelectionIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        contentSelectionIntent.type = "image/*"

                        var intentArray: Array<Intent?>

                        if (takePictureIntent != null) intentArray = arrayOf(takePictureIntent)
                        else intentArray = takePictureIntent?.get(0)!!

                        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                        chooserIntent.putExtra(Intent.EXTRA_TITLE, "사용할 앱을 선택해주세요.")
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                        launcher.launch(chooserIntent)
                    } catch (e: Exception) {
                    }
                    return true
                }
            }

            loadUrl("https://dev-crbt.lvup.gg")
        }
    }

    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data

            if (intent == null) { //바로 사진을 찍어서 올리는 경우
                val results = arrayOf(Uri.parse(cameraPath))
                mWebViewImageUpload!!.onReceiveValue(results!!)
            } else { //사진 앱을 통해 사진을 가져온 경우
                val results = intent!!.data!!
                mWebViewImageUpload!!.onReceiveValue(arrayOf(results!!))
            }
        } else { //취소 한 경우 초기화
            mWebViewImageUpload!!.onReceiveValue(null)
            mWebViewImageUpload = null
        }
    }

    private fun createImageFile(): File? {
        @SuppressLint("SimpleDateFormat")
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

}
package com.camera2x.android

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var imageCapture : ImageCapture? = null
    private var videoCapture : VideoCapture? = null
    private var switcher = "front"

    private lateinit var outputDirectory: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
            }else{
                requestPermissions(arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE), 12)
            }
        }

        outputDirectory = getOutputDirectory()

        switch_photo.background = ContextCompat.getDrawable(this, R.drawable.shape_btn)
        switch_photo.setPadding(10,8,10,8)
        switch_photo.setTextColor(ContextCompat.getColor(this, R.color.black))


        switch_video.setOnClickListener {

            timer.visibility = View.VISIBLE
            btn_takePicture.visibility = View.INVISIBLE
            btn_takePicture.isEnabled = false

            take_video.visibility = View.VISIBLE
            take_video.isEnabled = true

            switch_video.background = ContextCompat.getDrawable(this, R.drawable.shape_btn)
            switch_video.setPadding(10,8,10,8)
            switch_video.setTextColor(ContextCompat.getColor(this, R.color.black))

            switch_photo.background = null
            switch_photo.setPadding(0,0,0,0)
            switch_photo.setTextColor(ContextCompat.getColor(this, R.color.white))

        }

        switch_photo.setOnClickListener {

            timer.visibility = View.GONE

            take_video.visibility = View.INVISIBLE
            take_video.isEnabled = false

            btn_takePicture.visibility = View.VISIBLE
            btn_takePicture.isEnabled = true

            switch_photo.background = ContextCompat.getDrawable(this, R.drawable.shape_btn)
            switch_photo.setPadding(10,8,10,8)
            switch_photo.setTextColor(ContextCompat.getColor(this, R.color.black))

            switch_video.background = null
            switch_video.setPadding(0,0,0,0)
            switch_video.setTextColor(ContextCompat.getColor(this, R.color.white))

        }


        btn_takePicture.setOnClickListener {

            takePicture()
        }

        take_video.setOnTouchListener { view, motionEvent ->
            val action = motionEvent.action
            if (action == MotionEvent.ACTION_DOWN){
                takeVideo()

                timer.start()
                detect_take_video.visibility = View.VISIBLE
                return@setOnTouchListener true
            }else{
                if (action == MotionEvent.ACTION_UP){
                    videoCapture!!.stopRecording()

                    timer.stop()

                    detect_take_video.visibility = View.INVISIBLE
                    return@setOnTouchListener true
                }

            }
            return@setOnTouchListener true
        }


        btn_switchCamera.setOnClickListener {

            val ob = ObjectAnimator.ofFloat(btn_switchCamera, View.ROTATION, 0f, 360f)
            ob.duration = 1000
            val an = AnimatorSet()
            an.play(ob)
            an.start()

            when(switcher){

                "back"->{
                    startCamera(CameraSelector.DEFAULT_BACK_CAMERA)
                    switcher = "front"
                }

                "front"->{
                    startCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                    switcher = "back"
                }
            }

        }

    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }

    private fun takePicture() {

        val imageCapture = imageCapture ?: return

        val mediaPlayer = MediaPlayer.create(this, R.raw.capturesound)
        mediaPlayer.start()

        val photoFile = File(outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".jpg")

        val output = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(output, ContextCompat.getMainExecutor(this), object :ImageCapture.OnImageSavedCallback{
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {

                val intent = Intent(this@MainActivity, ShowContent::class.java)
                intent.putExtra("Uri", photoFile.path)
                startActivity(intent)

            }

            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_LONG).show()

            }

        })



    }

    private fun takeVideo() {

        val videoCapture = videoCapture ?: return

        val photoFile = File(outputDirectory,
            SimpleDateFormat(FILENAME_FORMAT, Locale.US
            ).format(System.currentTimeMillis()) + ".mp4")

        val output = VideoCapture.OutputFileOptions.Builder(photoFile).build()



        videoCapture.startRecording(output, ContextCompat.getMainExecutor(this), object :VideoCapture.OnVideoSavedCallback{
            override fun onVideoSaved(outputFileResults: VideoCapture.OutputFileResults) {

                val intent = Intent(this@MainActivity, ShowContent::class.java)
                intent.putExtra("Uri", photoFile.path)
                startActivity(intent)

            }

            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {

            }

        })


    }

    private fun startCamera(defaultBackCamera: CameraSelector) {


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M){
                requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), 13)
            }
        }


        imageCapture = ImageCapture.Builder()
            .build()

        videoCapture = VideoCapture.Builder()
            .build()



        val cameraProviderFuture  = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder_.surfaceProvider)
                }
            //val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try{


                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this,defaultBackCamera, preview, imageCapture,videoCapture)

            }catch (e:Exception){

            }



        }, ContextCompat.getMainExecutor(this))
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 12 &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED &&
            grantResults[1] == PackageManager.PERMISSION_GRANTED &&
            grantResults[2] == PackageManager.PERMISSION_GRANTED &&
            grantResults[3] == PackageManager.PERMISSION_GRANTED ){
            startCamera(CameraSelector.DEFAULT_BACK_CAMERA)

        }else{

            Toast.makeText(this, "Camera access has denied", Toast.LENGTH_SHORT).show()

        }



    }

}
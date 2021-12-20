package com.camera2x.android

import android.content.pm.ActivityInfo
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_show_content.*

class ShowContent : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_content)


        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)

        val path = intent.getStringExtra("Uri")!!

        if (path.contains(".jpg")){

            show_photo.visibility = View.VISIBLE
            val Uri_ = Uri.parse(path)
            show_photo.setImageURI(Uri_)

        }else{

            show_video.visibility = View.VISIBLE
            val uri = Uri.parse(path)
            show_video.setVideoURI(uri)
            val media = MediaController(this)
            show_video.setMediaController(media)
            show_video.start()

        }


    }
}
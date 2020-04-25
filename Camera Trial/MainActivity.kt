package com.example.cameracode

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FilePermission
import java.io.IOException

class MainActivity : AppCompatActivity() {
    lateinit var photoPath: String
    val REQUEST_TAKE_PICTURE=1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        take_picture.setOnClickListener{
            takePicture()
        }
    }

    private fun takePicture() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (intent.resolveActivity(packageManager)!= null){
            var photoFile: File? = null

            try {
                photoFile = createImageFile()
            }catch (e: IOException){}
            if(photoFile!=null){
                val photoUrl = FileProvider.getUriForFile(
                    this,"com.example.android.fileProvider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT,photoUrl)
                startActivityForResult(intent,REQUEST_TAKE_PICTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_TAKE_PICTURE && resultCode == Activity.RESULT_OK)
        {
           picture.rotation=90f
            picture.setImageURI(Uri.parse(photoPath))
       }
    }

    private fun createImageFile(): File? {
      val fileName = "MyPicture"
      val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
      val image = File.createTempFile(
          fileName, ".jpg",storageDir
      )
        photoPath = image.absolutePath
        return image
    }
}

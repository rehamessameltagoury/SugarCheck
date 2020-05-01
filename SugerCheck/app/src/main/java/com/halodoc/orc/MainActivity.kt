package com.halodoc.orc

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.bumptech.glide.Glide
import com.googlecode.tesseract.android.TessBaseAPI
import com.halodoc.orc.Constants.INITIAL_PERMS
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_main.*
import pl.aprilapps.easyphotopicker.DefaultCallback
import pl.aprilapps.easyphotopicker.EasyImage
import java.io.File
import android.graphics.ColorMatrixColorFilter
import android.graphics.ColorMatrix
import android.graphics.Bitmap
import android.graphics.Canvas;
import android.graphics.Paint;

class MainActivity : AppCompatActivity() {

    private var imageFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(this, INITIAL_PERMS, Constants.INITIAL_REQUEST)

        btnTakePhoto.setOnClickListener {
            EasyImage.openCameraForImage(this, 0)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        EasyImage.handleActivityResult(requestCode, resultCode, data, this, object : DefaultCallback() {
            override fun onImagesPicked(imageFiles: MutableList<File>, source: EasyImage.ImageSource?, type: Int) {
                CropImage.activity(Uri.fromFile(imageFiles[0])).start(this@MainActivity)
            }
        })
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val result = CropImage.getActivityResult(data)
                imageFile = File(result.uri.path)
                loadImage(imageFile)
                ConvertTask().execute(imageFile)
            }
        }
    }

    private fun loadImage(imageFile: File?) {
        Glide.with(this)
            .load(imageFile)
            .into(ivOCR)
    }

    private inner class ConvertTask : AsyncTask<File, Void, String>() {
        internal var tesseract = TessBaseAPI()
        val  sugarCane= listOf("Blackstrap molasses","Brown sugar","Cane juice",  "Cane sugar"
            , "Cane sugar extract","Caster sugar","Coffee crystals","Demerara sugar"," Golden syrup"
            ," Icing sugar"," Invert sugar","Molasses"," Panela","Rapadura","Raw sugar"
            ,"Treacle","Turbinado sugar","White sugar","Sugar")
        val  fruitSugar= listOf("Date sugar", "Fruit juice concentrate", "Fruit sugar",
            "Grape juice concentrate", "Grape sugar", "Pear juice concentrate")
        val chemicalSugar = listOf("Glucose", "Dextrose ", "Fructose ", "Lactose ", "Maltose ", "Sucrose")
        val syrup_sweeteners = listOf("Agave", "Barley malt syrup", "Birch syrup", "Brown rice syrup",
            "Coconut sugar", "Date sugar", "Honey", "Invert sugar", "Malt extract",
            "Maple syrup", "Palm sugar", "Rice malt syrup")
        var corn_Suger = listOf("Corn sugar", "Gluco-malt", "Glucose syrup", "High fructose corn syrup", "Karo")
        var safe_sugarCane:Boolean = false
        var safe_fruitSugar:Boolean = false
        var safe_chemicalSugar:Boolean = false
        var safe_corn_Suger:Boolean = false
        var safe_syrup_sweeteners:Boolean = false

        override fun onPreExecute() {
            super.onPreExecute()
            val datapath = "$filesDir/tesseract/";
            FileUtil.checkFile(
                this@MainActivity,
                datapath.toString(),
                File(datapath + "tessdata/")
            )
            tesseract.init(datapath, "eng")
            tvResult.visibility = View.GONE
            progressBar.visibility = View.VISIBLE

        }

        override fun doInBackground(vararg files: File): String {
            val options = BitmapFactory.Options()
            options.inSampleSize =
                4 // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
            var bitmap = BitmapFactory.decodeFile(imageFile?.path, options)
            //bitmap = toGrayscale(bitmap)
            tesseract.setImage(bitmap)
            var result = tesseract.utF8Text
            result = result.replace("(?m)^[ \t]*\r?\n", "")
            result = result.replace("(?m)^[ \t]*\r?'-'", "")
            tesseract.end()
            //val regex = "Suger".toRegex()
            //val safe  = result.contains("Suger", ignoreCase = true)
            var sugar_Flag:Int = 0
            for (item in sugarCane){
                 safe_sugarCane = result.contains(item, ignoreCase = true)
                if (safe_sugarCane == true) {
                    sugar_Flag = 1
                    break

                }
            }
            for (item in fruitSugar){
                safe_fruitSugar = result.contains(item, ignoreCase = true)
                if (safe_fruitSugar == true){
                    sugar_Flag = 1
                    break

                }
            }
            for (item in chemicalSugar){
                safe_chemicalSugar = result.contains(item, ignoreCase = true)
                if (safe_chemicalSugar == true){
                    sugar_Flag = 1
                    break

                }
            }
            for (item in corn_Suger){
                safe_corn_Suger = result.contains(item, ignoreCase = true)
                if (safe_corn_Suger == true){
                    sugar_Flag = 1
                    break

                }
            }
            for (item in syrup_sweeteners){
                 safe_syrup_sweeteners = result.contains(item, ignoreCase = true)
                if (safe_syrup_sweeteners == true){
                    break
                sugar_Flag = 1
                }
            }

            //val safe = result.contains("Sugar", ignoreCase = true)
            var final_result:String = ""
            //if (safe_sugarCane== false &&  safe_fruitSugar== false &&  !safe_chemicalSugar== false &&  !safe_corn_Suger== false &&  !safe_syrup_sweeteners== false )
             if(sugar_Flag == 0)
                final_result =  "SAFE , No any kind of sugar"
            else{
                if(safe_sugarCane){
                    final_result+= "-NOT SAFE , Has Sugar Cane "
                }
                if(safe_corn_Suger){
                    final_result+= "-NOT SAFE , Has Corn Sugar "
                }
                if(safe_fruitSugar){
                    final_result+= "-NOT SAFE , Has Fruit Sugar "
                }
                if(safe_chemicalSugar){
                    final_result+= "-NOT SAFE , Has chemical Sugar "
                }
                if(safe_syrup_sweeteners){
                    final_result+= "NOT SAFE , Has syrup sweeters "
                }
            }
            return ( final_result)
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            tvResult.text = result
            tvResult.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }
        fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
            val width: Int
            val height: Int
            height = bmpOriginal.height
            width = bmpOriginal.width

            val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val c = Canvas(bmpGrayscale)
            val paint = Paint()
            val cm = ColorMatrix()
            cm.setSaturation(0f)
            val f = ColorMatrixColorFilter(cm)
            paint.setColorFilter(f)
            //c.drawBitmap(bmpOriginal, 0.0, 0.0, paint)
            return bmpGrayscale
        }
    }
}

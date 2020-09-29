package com.example.kids_drawing

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.widget.Gallery
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_brush_size.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.jar.Manifest
import android.media.MediaScannerConnection.scanFile as scanFile

class MainActivity : AppCompatActivity() {
    private  var mImageButtonCurrentPaint : ImageButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mImageButtonCurrentPaint = ll_color_pellete[1] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this,R.drawable.pallet_normal)
        )
        drawing_view.setSizeforBrush(20.toFloat())
        id_brush.setOnClickListener{
            setBrushSizeChooser()
        }

        id_gallery.setOnClickListener{
            if(isReadStorageAllowed()){
                //run something code
                val pickPhotoInent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(pickPhotoInent,GALLERY)
            }else{
                requestStoragePermission()
            }
        }

        id_undo.setOnClickListener{
            drawing_view.onClickUndo()
        }

        id_save.setOnClickListener{
            if(isReadStorageAllowed()){
                BitmapAsyncTask(getBitmapFromView(fl_background_image)).execute()
            }else{
                requestStoragePermission()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode == GALLERY){
                try{
                    if(data!!.data !== null){
                        iv_background.visibility = View.VISIBLE
                        iv_background.setImageURI(data!!.data)
                    }else{
                        Toast.makeText(this,"Error in parsing image computed",Toast.LENGTH_LONG).show()
                    }
                }catch (e: Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun setBrushSizeChooser(){
        var brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)
        brushDialog.setTitle("Brush Chooser:")
        val smallBtn = brushDialog.ib_small_brush
        smallBtn.setOnClickListener{
            drawing_view.setSizeforBrush(10.toFloat())
            brushDialog.dismiss()
        }
        val mediumBtn = brushDialog.ib_medium_brush
        mediumBtn.setOnClickListener{
            drawing_view.setSizeforBrush(20.toFloat())
            brushDialog.dismiss()
        }
        val largeBtn = brushDialog.ib_large_brush
        largeBtn.setOnClickListener{
            drawing_view.setSizeforBrush(30.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()
    }


    fun paintClicked(view : View){
        if(view !== mImageButtonCurrentPaint){
            val imageButton = view  as ImageButton
            val colorTag = imageButton.tag.toString()
            drawing_view.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_pressed)
            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_normal)
            )
        }
        mImageButtonCurrentPaint = view as ImageButton
    }


    private fun requestStoragePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
            Toast.makeText(this,"Need permission to add background",Toast.LENGTH_LONG).show()
        }
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == STORAGE_PERMISSION_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission granted,now you can read the storage",Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this,"OOps your permission is not granted.You can change this in the setting",Toast.LENGTH_LONG).show()
            }
        }
    }


    private  fun isReadStorageAllowed() :Boolean{
        val result = ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)

        return result == PackageManager.PERMISSION_GRANTED
    }

    //to save image getiing bitmap

    private fun getBitmapFromView(view: View) :Bitmap{
        val returnBitmap = Bitmap.createBitmap(view.height,view.width,Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnBitmap)

        val bgDrawable = view.background
        if(bgDrawable !== null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)

        return  returnBitmap

    }







    private inner class BitmapAsyncTask(val mBitmap: Bitmap) : AsyncTask<Any,Void,String>()

    {
        private  lateinit var mProgressDialog: Dialog
        override fun onPreExecute() {
            super.onPreExecute()
            startProgressDialog()
        }

        override fun doInBackground(vararg p0: Any?): String {
            var result = ""

            if(mBitmap !== null){
                try {
                    val bytes = ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)

                    val f = File(externalCacheDir!!.absoluteFile.toString()+File.separator+"KidDrawingApp_"
                    +System.currentTimeMillis()/1000+".png")

                    val fos =FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    result = f.absolutePath

                }catch (e : Exception){
                    result =""
                    e.printStackTrace()
                }
            }
            return result

        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
            if(!result!!.isEmpty()){
                Toast.makeText(this@MainActivity,"File save successfully $result",Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this@MainActivity,"Something is wrong for saving file",Toast.LENGTH_LONG).show()
            }


            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null){
                path , uri -> val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
                shareIntent.type = "image/png"

                startActivity(
                    Intent.createChooser(
                        shareIntent,"share"
                    )
                )
            }


//            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null){
//                path, uri -> val shareIntent = Intent()
//                shareIntent.action = Intent.ACTION_SEND
//                shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
//                shareIntent.type = "image/png"
//
//                startActivity(
//                    Intent.createChooser(
//                        shareIntent,"share"
//                    )
//                )
//            }
        }

        private  fun startProgressDialog(){
            mProgressDialog = Dialog(this@MainActivity)
            mProgressDialog.setContentView(R.layout.dialog_custom_progress)
            mProgressDialog.show()
        }
        private fun cancelProgressDialog(){
            mProgressDialog.dismiss()
        }

    }

    companion object{
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }

}
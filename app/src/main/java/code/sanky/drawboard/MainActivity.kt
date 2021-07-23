package code.sanky.drawboard

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import code.sanky.drawboard.databinding.ActivityMainBinding
import java.io.File
import java.io.OutputStream
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.drawingView.setBrushSize(5.toFloat())

        mImageButtonCurrentPaint = binding.colorPicker[1] as ImageButton
        //adding background to selected color//
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
        )

        binding.ibBrush.setOnClickListener {
            showBrushDialog()
        }

        binding.saveBtn.setOnClickListener {
            if (isReadStorageAllowed()) {
                BitMapAsyncTask(getBitMapFromView(binding.drawingViewContainer)).execute()
            } else {
                requestStoragePermission()
            }
        }

        // better method for setting background image //
        val resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    if (data!!.data != null) {
                        binding.ivBackground.setImageURI(data.data)
                        binding.ivBackground.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            this@MainActivity, "Error While Fetching Image", Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }

        binding.gallery.setOnClickListener {
            if (isReadStorageAllowed()) {

                //this method is deprecated so i have to use getImage method
                //getImage.launch("image/8")
                val pickPicsFromGallery =
                    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                //better method than getImage //
                resultLauncher.launch(pickPicsFromGallery)
                //old way below//
//                startActivityForResult(pickPicsFromGallery , GALLERY)
            } else {
                requestStoragePermission()
            }
        }

        binding.ibUndo.setOnClickListener {
            binding.drawingView.undoDraw()
        }
        binding.ibRedo.setOnClickListener {
            binding.drawingView.redoDraw()
        }

    }

    override fun onRequestPermissionsResult(
        // handling cases according to the user selection if granted or not after requesting //
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    this@MainActivity,
                    "Now You Can Read Storage Files", Toast.LENGTH_SHORT
                ).show()

            } else {
                Toast.makeText(
                    this@MainActivity, "OOPSIE You Just Denied The Request", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun showBrushDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dailog_brush_size)
        brushDialog.setTitle("Brush Size: ")

        val smallBtn = brushDialog.findViewById<ImageButton>(R.id.small_brush)
        smallBtn.setOnClickListener {
            binding.drawingView.setBrushSize(5.toFloat())
            brushDialog.dismiss()
        }

        val mediumBtn = brushDialog.findViewById<ImageButton>(R.id.medium_brush)
        mediumBtn.setOnClickListener {
            binding.drawingView.setBrushSize(10.toFloat())
            brushDialog.dismiss()
        }

        val largeBtn = brushDialog.findViewById<ImageButton>(R.id.large_brush)
        largeBtn.setOnClickListener {
            binding.drawingView.setBrushSize(15.toFloat())
            brushDialog.dismiss()
        }

        brushDialog.show()

    }

    fun paintClicked(view: View) {
        if (view != mImageButtonCurrentPaint) {    // checking if the clicked id not previously selected //
            val imageButton = view as ImageButton
            val colorTag = imageButton.tag.toString()
            binding.drawingView.setColor(colorTag)
            imageButton.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.pallet_pressed)
            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.color_pallet)
            )
            mImageButtonCurrentPaint = view
        }
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).toString()
            )
        ) {
            Toast.makeText(this, "Need Permission For Adding Background", Toast.LENGTH_SHORT).show()
        }
        // if permission is not granted then ask for it.//
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            STORAGE_PERMISSION_CODE
        )
    }

    private fun isReadStorageAllowed(): Boolean {
        // function for checking if the result is granted or not //
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun getBitMapFromView(view: View): Bitmap {
        //this function is used for getting the drawing view and converted to bitmap//
        val returnedBitmap = Bitmap.createBitmap(
            view.width, view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(returnedBitmap)
        val background = view.background
        if (background != null) {
            background.draw(canvas)
        } else {
            canvas.drawColor(Color.WHITE)
        }

        view.draw(canvas)
        return returnedBitmap
    }

    private inner class BitMapAsyncTask(val mBitmap: Bitmap) :
        AsyncTask<Any, Void, String>() {
        override fun doInBackground(vararg params: Any?): String {
            var result = ""

            var resolver = this@MainActivity.contentResolver

            val foldername =
                packageManager.getApplicationLabel(applicationInfo).toString().replace(" ", "")
            val filename = createFilename(foldername)
            val saveLocation = Environment.DIRECTORY_PICTURES + File.separator + foldername

            if (android.os.Build.VERSION.SDK_INT >= 29) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000)

                // RELATIVE_PATH and IS_PENDING are introduced in API 29.
                values.put(MediaStore.Images.Media.RELATIVE_PATH, saveLocation)
                values.put(MediaStore.Images.Media.IS_PENDING, true)


                val uri: Uri? = resolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
                )

                if (uri != null) {
                    //val outstream = resolver.openOutputStream(uri)

                    if (mBitmap != null) {
                        saveImageToStream(mBitmap, resolver.openOutputStream(uri))
                    }

                    values.put(MediaStore.Images.Media.IS_PENDING, false)

                    this@MainActivity.contentResolver.update(uri, values, null, null)

                    result = uri.toString()
                }
            }

            return result
        }

        override fun onPostExecute(result: String) {
            super.onPostExecute(result)

            if (!result.isEmpty()) {
                Toast.makeText(
                    this@MainActivity,
                    "File saved successfully :$result",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Something went wrong while saving the file.",
                    Toast.LENGTH_SHORT
                ).show()
            }

            shareImage(Uri.parse(result))
        }

    }

    fun shareImage(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        startActivity(
            Intent.createChooser(
                intent, "Share"
            )
        )
    }

    fun createFilename(filename: String): String {
        val formatter = SimpleDateFormat("YYYYMMdd-HHmm.ssSSS")
        val dateString = formatter.format(Date()) + "_"

        return dateString + filename + ".jpg"
    }

    fun saveImageToStream(mBitmap: Bitmap, outputStream: OutputStream?) {
        if (outputStream != null) {
            try {
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.close()
            } catch (e: Exception) {
                Log.e("**Exception", "Could not write to stream")
                e.printStackTrace()
            }
        }
    }


    companion object {
        private const val STORAGE_PERMISSION_CODE = 1
        private const val GALLERY = 2
    }
}
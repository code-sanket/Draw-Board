package code.sanky.drawboard

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.view.get
import code.sanky.drawboard.databinding.ActivityMainBinding
import code.sanky.drawboard.databinding.DailogBrushSizeBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private  var mImageButtonCurrentPaint: ImageButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.drawingView.setBrushSize(5.toFloat())

        mImageButtonCurrentPaint = binding.colorPicker[1] as ImageButton
        //adding background to selected color//
        mImageButtonCurrentPaint!!.setImageDrawable(
            ContextCompat.getDrawable(this , R.drawable.pallet_pressed))

        binding.ibBrush.setOnClickListener {
            showBrushDialog()
        }

        //this is a dummy commit//

    }

    private fun showBrushDialog(){
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

    fun paintClicked(view : View){
        if(view != mImageButtonCurrentPaint) {    // checking if the clicked id not previously selected //
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
}
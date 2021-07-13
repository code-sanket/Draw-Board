package code.sanky.drawboard

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.nio.file.Path
import java.util.jar.Attributes

class DrawingView(context: Context , attributes : AttributeSet) : View(context , attributes){

    private var mDrawPath : CustomPath? = null
    private var mCanvasBitmap : Bitmap? = null
    private var mDrawPaint : Paint? =null
    private var mCanvasPaint : Paint? = null
    private var mBrushSize : Float = 0.toFloat()
    private var color = Color.BLACK
    private var canvas : Canvas? = null
    private val mPaths = ArrayList<CustomPath>()


    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color , mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND // end point of the lines //
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND // shape of the end points//
        mCanvasPaint = Paint(Paint.DITHER_FLAG)

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!! , 0f , 0f , mCanvasPaint)

        for(paths in mPaths){
            mDrawPaint!!.strokeWidth = paths.brushThickness
            mDrawPaint!!.color = paths.color
            canvas.drawPath(paths , mDrawPaint!!)
        }

        if (!mDrawPath!!.isEmpty){
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!! , mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){

            MotionEvent.ACTION_DOWN ->{    // when we put our finger on the screen
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!! , touchY!!)

            }

            MotionEvent.ACTION_MOVE ->{       // when we drag our finger on the screen
                mDrawPath!!.lineTo(touchX!! , touchY!!)
            }
            MotionEvent.ACTION_UP ->{     // when we release our finger off the screen
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color , mBrushSize)
            }
            else -> return false
        }

        invalidate()

        return true
    }

        fun setBrushSize(newSize : Float){
            mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP ,
            newSize , resources.displayMetrics)
            mDrawPaint!!.strokeWidth = mBrushSize
        }

        fun setColor(newColor: String ){
            color = Color.parseColor(newColor)
            mDrawPaint!!.color = color
        }

    internal inner class CustomPath(var color : Int , var brushThickness : Float) : android.graphics.Path(){

    }

}
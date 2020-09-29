package com.example.kids_drawing

import android.app.usage.UsageEvents
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import java.lang.reflect.Type

class DrawingView(context: Context,attrs: AttributeSet) : View(context,attrs){
    private var mCanvas:Canvas? = null
    private var mDrawPaint:Paint? = null
    private var mCanvasPaint:Paint? = null
    private var mCanvasBitmap:Bitmap? = null
    private var mDrawPath:CustomPath? = null
    private var mColor = Color.BLACK
    private var mBrushsize = 0.toFloat()
    private val mPaths = ArrayList<CustomPath>()
    private val mUndoPaths = ArrayList<CustomPath>()

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        mDrawPaint = Paint()
        mDrawPaint!!.color = mColor
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mDrawPath = CustomPath(mColor,mBrushsize)
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
//        mBrushsize = 20.toFloat()
    }


    internal inner class CustomPath(var color:Int,var brushThickness:Float) :Path(){

    }

    //Undo click
    fun onClickUndo(){
        if(mPaths.size > 0){
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate()
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)
        for(path in mPaths){
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas?.drawPath(path,mDrawPaint!!)
        }

        if(!mDrawPath!!.isEmpty){
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas?.drawPath(mDrawPath!!,mDrawPaint!!)
        }

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN ->{
                mDrawPath!!.color = mColor
                mDrawPath!!.brushThickness = mBrushsize
                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX,touchY)
                    }
                }

            }
            MotionEvent.ACTION_MOVE ->{
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.lineTo(touchX,touchY)
                    }
                }

            }

            MotionEvent.ACTION_UP->{
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(mColor,mBrushsize)
            }
            else ->false
        }
        invalidate()
        return true

    }
    fun setSizeforBrush(newSize:Float){
        mBrushsize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,newSize,resources.displayMetrics)
        mDrawPaint!!.strokeWidth = mBrushsize
    }

    fun setColor(newColor:String){
        mColor = Color.parseColor(newColor)
        mDrawPaint!!.color= mColor
    }


}

package com.example.latranslator.utils.customAmbilWarnaColorPicker

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.ImageView
import com.example.latranslator.R
import yuku.ambilwarna.AmbilWarnaDialog

class CustomAmbilWarnaDialog(context: Context,
                             color: Int,
                             listener: OnAmbilWarnaListener) : AmbilWarnaDialog(context, color, listener) {


    lateinit var alertDialog: AlertDialog
    var supportsAlpha = false
    lateinit var listener: OnAmbilWarnaListener
    lateinit var viewHue: View
    lateinit var viewSatVal: CustomAmbilWarnaSquare
    lateinit var viewCursor: ImageView
    lateinit var viewAlphaCursor: ImageView
    lateinit var viewOldColor: View
    lateinit var viewNewColor: View
    lateinit var viewAlphaOverlay: View
    lateinit var viewTarget: ImageView
    lateinit var viewAlphaCheckered: ImageView
    lateinit var viewContainer: ViewGroup
    val currentColorHsv = FloatArray(3)
    var alpha = 0

    constructor(
        context: Context,
        color: Int,
        supportsAlpha: Boolean,
        listener: OnAmbilWarnaListener
    ) : this(context, color, listener) {

        var color = color
        this.supportsAlpha = supportsAlpha
        this.listener = listener
        if (!supportsAlpha) { // remove alpha if not supported
            color = color or -0x1000000
        }
        Color.colorToHSV(color, currentColorHsv)
        alpha = Color.alpha(color)
        val view = LayoutInflater.from(context).inflate(R.layout.ambilwarna_dialog, null)
        viewHue = view.findViewById(R.id.ambilwarna_viewHue)
        viewSatVal = view.findViewById<View>(R.id.ambilwarna_viewSatBri) as CustomAmbilWarnaSquare
        viewCursor = view.findViewById<View>(R.id.ambilwarna_cursor) as ImageView
        viewOldColor = view.findViewById(R.id.ambilwarna_oldColor)
        viewNewColor = view.findViewById(R.id.ambilwarna_newColor)
        viewTarget = view.findViewById<View>(R.id.ambilwarna_target) as ImageView
        viewContainer = view.findViewById<View>(R.id.ambilwarna_viewContainer) as ViewGroup
        viewAlphaOverlay = view.findViewById(R.id.ambilwarna_overlay)
        viewAlphaCursor = view.findViewById<View>(R.id.ambilwarna_alphaCursor) as ImageView
        viewAlphaCheckered = view.findViewById<View>(R.id.ambilwarna_alphaCheckered) as ImageView
        run {
            // hide/show alpha
            viewAlphaOverlay.visibility = if (supportsAlpha) View.VISIBLE else View.GONE
            viewAlphaCursor.visibility = if (supportsAlpha) View.VISIBLE else View.GONE
            viewAlphaCheckered.visibility = if (supportsAlpha) View.VISIBLE else View.GONE
        }
        viewSatVal.setHue(currentColorHsv[0])
        viewOldColor.setBackgroundColor(color)
        viewNewColor.setBackgroundColor(color)
        viewHue.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                var y = event.y
                if (y < 0f) y = 0f
                if (y > viewHue.measuredHeight) {
                    y =
                        viewHue.measuredHeight - 0.001f // to avoid jumping the cursor from bottom to top.
                }
                var hue = 360f - 360f / viewHue.measuredHeight * y
                if (hue == 360f) hue = 0f
                currentColorHsv[0] = hue

                // update view
                viewSatVal.setHue(currentColorHsv[0])
                moveCursor()
                viewNewColor.setBackgroundColor(getColor())
                updateAlphaView()
                return@OnTouchListener true
            }
            false
        })
        if (supportsAlpha) viewAlphaCheckered.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                var y = event.y
                if (y < 0f) {
                    y = 0f
                }
                if (y > viewAlphaCheckered.measuredHeight) {
                    y =
                        viewAlphaCheckered.measuredHeight - 0.001f // to avoid jumping the cursor from bottom to top.
                }
                val a = Math.round(255f - 255f / viewAlphaCheckered.measuredHeight * y)
                this@CustomAmbilWarnaDialog.alpha = a

                // update view
                moveAlphaCursor()
                val col: Int = this@CustomAmbilWarnaDialog.getColor()
                val c = a shl 24 or (col and 0x00ffffff)
                viewNewColor.setBackgroundColor(c)
                return@OnTouchListener true
            }
            false
        })
        viewSatVal.setOnTouchListener(OnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_MOVE || event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_UP) {
                var x = event.x // touch event are in dp units.
                var y = event.y
                if (x < 0f) x = 0f
                if (x > viewSatVal.measuredWidth) x = viewSatVal.measuredWidth.toFloat()
                if (y < 0f) y = 0f
                if (y > viewSatVal.measuredHeight) y = viewSatVal.measuredHeight.toFloat()
                currentColorHsv[1]= 1f / viewSatVal.measuredWidth * x
                currentColorHsv[2] = 1f - 1f / viewSatVal.measuredHeight * y

                // update view
                moveTarget()
                viewNewColor.setBackgroundColor(getColor())
                return@OnTouchListener true
            }
            false
        })

        alertDialog = AlertDialog.Builder(context)
            .setPositiveButton(
                android.R.string.ok
            ) { dialog, which ->
                this@CustomAmbilWarnaDialog.listener.onOk(this@CustomAmbilWarnaDialog, getColor())
            }
            .setNegativeButton(
                android.R.string.cancel
            ) { dialog, which ->
                this@CustomAmbilWarnaDialog.listener.onCancel(this@CustomAmbilWarnaDialog)
            }
            .setOnCancelListener {
                this@CustomAmbilWarnaDialog.listener.onCancel(this@CustomAmbilWarnaDialog)
            }
            .create()
        // kill all padding from the dialog window
        alertDialog.setView(view, 0, 0, 0, 0)

        // move cursor & target on first draw
        val vto = view.viewTreeObserver
        vto.addOnGlobalLayoutListener(object : OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                moveCursor()
                if (this@CustomAmbilWarnaDialog.supportsAlpha) moveAlphaCursor()
                moveTarget()
                if (this@CustomAmbilWarnaDialog.supportsAlpha) updateAlphaView()
                view.viewTreeObserver.removeGlobalOnLayoutListener(this)
            }
        })
    }

    private fun getColor(): Int {
        val argb = Color.HSVToColor(currentColorHsv)
        return alpha shl 24 or (argb and 0x00ffffff)
    }

    private fun updateAlphaView() {
        val gd = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(
                Color.HSVToColor(currentColorHsv), 0x0
            )
        )
        viewAlphaOverlay.setBackgroundDrawable(gd)
    }
}
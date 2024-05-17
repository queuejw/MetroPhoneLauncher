package ru.dimon6018.metrolauncher.helpers.anim

import android.graphics.Camera
import android.view.animation.Animation
import android.view.animation.Transformation

class Flip3dAnimationHorizontal(
    private val mFromDegrees: Float, private val mToDegrees: Float,
    private val mCenterX: Float, private val mCenterY: Float
) : Animation() {
    private var mCamera: Camera? = null

    override fun initialize(width: Int, height: Int, parentWidth: Int, parentHeight: Int) {
        super.initialize(width, height, parentWidth, parentHeight)
        mCamera = Camera()
    }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        val fromDegrees = mFromDegrees
        val degrees = fromDegrees + ((mToDegrees - fromDegrees) * interpolatedTime)

        val centerX = mCenterX
        val centerY = mCenterY
        val camera = mCamera

        val matrix = t.matrix

        camera?.apply {
            save()
            rotateY(degrees)
            getMatrix(matrix)
            restore()
        }
        matrix.apply {
            preTranslate(-centerX, -centerY)
            postTranslate(centerX, centerY)
        }
    }
}
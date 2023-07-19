/*
 * Copyright © 2017 Jorge Martín Espinosa
 */

package com.arasthel.spannedgridlayoutmanager

import android.graphics.RectF

/**
 * Created by Jorge Martín on 4/6/17.
 */


fun RectF.isAdjacentTo(rect: RectF): Boolean {
    return (this.right == rect.left
            || this.top == rect.bottom
            || this.left == rect.right
            || this.bottom == rect.top)
}

fun RectF.intersects(rect: RectF): Boolean {
    return this.intersects(rect.left, rect.top, rect.right, rect.bottom)
}
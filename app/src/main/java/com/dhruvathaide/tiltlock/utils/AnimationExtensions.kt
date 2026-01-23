package com.dhruvathaide.tiltlock.utils

import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.dhruvathaide.tiltlock.R

fun RecyclerView.addItemAnimations() {
    val animation = AnimationUtils.loadAnimation(context, R.anim.item_animation)
    layoutAnimation = android.view.animation.LayoutAnimationController(animation).apply {
        delay = 0.1f
        order = android.view.animation.LayoutAnimationController.ORDER_NORMAL
    }
}

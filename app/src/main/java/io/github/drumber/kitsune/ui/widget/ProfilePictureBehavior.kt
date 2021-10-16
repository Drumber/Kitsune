package io.github.drumber.kitsune.ui.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.appbar.AppBarLayout
import de.hdodenhof.circleimageview.CircleImageView
import io.github.drumber.kitsune.R

/*
    Custom collapsing view behavior adapted from:
    https://github.com/hanscappelle/CoordinatorBehaviorExample
 */

class ProfilePictureBehavior(
    context: Context,
    attrs: AttributeSet
) : CoordinatorLayout.Behavior<CircleImageView>(context, attrs) {

    private var areValuesInitialized = false

    private var startXPositionImage = 0
    private var startYPositionImage = 0
    private var startHeight = 0
    private var startToolbarHeight = 0

    private var amountOfToolbarToMove = 0f
    private var amountOfImageToReduce = 0f
    private var amountToMoveXPosition = 0f
    private var amountToMoveYPosition = 0f

    private val finalXPosition: Float
    private val finalYPosition: Float
    private val finalHeight: Float
    private val finalToolbarHeight: Float

    var offsetX = 0f
    var offsetY = 0f

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.ProfilePictureBehavior)
        finalXPosition = a.getDimension(R.styleable.ProfilePictureBehavior_finalXPosition, 0f)
        finalYPosition = a.getDimension(R.styleable.ProfilePictureBehavior_finalYPosition, 0f)
        finalHeight = a.getDimension(R.styleable.ProfilePictureBehavior_finalHeight, 0f)
        finalToolbarHeight = a.getDimension(R.styleable.ProfilePictureBehavior_finalToolbarHeight, 0f)
        a.recycle()
    }

    override fun layoutDependsOn(
        parent: CoordinatorLayout,
        child: CircleImageView,
        dependency: View
    ): Boolean {
        return dependency is AppBarLayout
    }

    override fun onDependentViewChanged(
        parent: CoordinatorLayout,
        child: CircleImageView,
        dependency: View
    ): Boolean {
        initProperties(child, dependency)

        val currentToolbarHeight = (startToolbarHeight + dependency.y).coerceAtLeast(finalToolbarHeight)
        val amountAlreadyMoved = startToolbarHeight - currentToolbarHeight
        val progress = amountAlreadyMoved / amountOfToolbarToMove

        // update image size
        val heightToSubtract = progress * amountOfImageToReduce
        val layoutParams = child.layoutParams as CoordinatorLayout.LayoutParams
        (startHeight - heightToSubtract).toInt().let {
            layoutParams.width = it
            layoutParams.height = it
        }
        child.layoutParams = layoutParams

        // update image position
        val distanceXToSubtract = progress * amountToMoveXPosition
        val distanceYToSubtract = progress * amountToMoveYPosition
        child.x = offsetX + startXPositionImage - distanceXToSubtract
        child.y = offsetY + startYPositionImage - distanceYToSubtract

        return true
    }

    private fun initProperties(child: CircleImageView, dependency: View) {
        if(areValuesInitialized) return
        startHeight = child.height
        startXPositionImage = child.x.toInt()
        startYPositionImage = child.y.toInt()
        startToolbarHeight = dependency.height

        amountOfToolbarToMove = startToolbarHeight - finalToolbarHeight
        amountOfImageToReduce = startHeight - finalHeight
        amountToMoveXPosition = startXPositionImage - finalXPosition
        amountToMoveYPosition = startYPositionImage - finalYPosition

        areValuesInitialized = true
    }

}
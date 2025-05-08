package com.dung.madfamilytree.views.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.ScaleGestureDetector
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.dung.madfamilytree.R
import com.dung.madfamilytree.views.fragments.TreeFragment
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.min
import kotlin.math.max

class FamilyTreeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val TAG = "FamilyTreeView"
    private var onNodeClickListener: ((TreeFragment.TreeNode) -> Unit)? = null
    private var onAddPartnerClickListener: ((TreeFragment.TreeNode) -> Unit)? = null
    private var onAddChildClickListener: ((TreeFragment.TreeNode) -> Unit)? = null

    // Zoom related properties
    private var scaleFactor = 1.0f
    private val minScale = 0.5f
    private val maxScale = 2.0f
    private var focusX = 0f
    private var focusY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = INVALID_POINTER_ID
    private var mode = Mode.NONE

    private enum class Mode {
        NONE, DRAG, ZOOM
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }

    // Fixed dimensions for nodes and spacing
    private val nodeWidth = 76
    private val nodeHeight = 99
    private val nodeSpacing = 700
    private val levelSpacing = 600
    private val pairSpacing = 200 // Spacing between paired profiles
    private val addChildButtonSpacing = 325 // Spacing between node and add child button

    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.black)
        strokeWidth = 2f
        style = Paint.Style.STROKE
    }

    private val path = Path()
    private var rootNode: TreeFragment.TreeNode? = null
    private val nodeViews = mutableMapOf<String, View>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())

    init {
        // Set the background to be transparent
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
    }

    fun setTree(root: TreeFragment.TreeNode) {
        Log.d(TAG, "Setting tree with root: ${root.profile?.name}")
        rootNode = root
        // Clear existing views
        removeAllViews()
        nodeViews.clear()
        // Create all node views first
        createAllNodeViews(root)
        requestLayout()
        invalidate()
    }

    private fun createAllNodeViews(node: TreeFragment.TreeNode) {
        // Create view for current node
        val nodeView = createNodeView(node)
        nodeViews[node.profileId] = nodeView
        
        // Create view for partner if exists
        node.partner?.let { partner ->
            val partnerView = createNodeView(partner)
            nodeViews[partner.profileId] = partnerView
        } ?: run {
            // Add partner button if no partner exists
            val addPartnerButton = LayoutInflater.from(context).inflate(R.layout.add_partner_button, this, false) as Button
            addPartnerButton.setOnClickListener {
                onAddPartnerClickListener?.invoke(node)
            }
            nodeViews["add_partner_${node.profileId}"] = addPartnerButton
            addView(addPartnerButton)
        }

        // Add child button
        val addChildButton = LayoutInflater.from(context).inflate(R.layout.add_child_button, this, false) as Button
        addChildButton.setOnClickListener {
            onAddChildClickListener?.invoke(node)
        }
        nodeViews["add_child_${node.profileId}"] = addChildButton
        addView(addChildButton)
        
        // Create views for children
        node.children.forEach { child ->
            createAllNodeViews(child)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        
        // Calculate total height needed for the tree
        val treeHeight = calculateTreeHeight(rootNode) * levelSpacing
        val totalHeight = treeHeight + nodeHeight + addChildButtonSpacing // Add extra space for the last level and add child buttons
        
        // Use the maximum of measured height and calculated height
        val finalHeight = maxOf(measuredHeight, totalHeight)
        
        Log.d(TAG, "onMeasure: width=$measuredWidth, calculatedHeight=$totalHeight, finalHeight=$finalHeight")
        setMeasuredDimension(measuredWidth, finalHeight)
    }

    private fun calculateTreeHeight(node: TreeFragment.TreeNode?): Int {
        if (node == null) return 0
        val childrenHeight = node.children.maxOfOrNull { calculateTreeHeight(it) } ?: 0
        return 1 + childrenHeight
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d(TAG, "onLayout: left=$left, top=$top, right=$right, bottom=$bottom")
        layoutNodes()
    }

    private fun layoutNodes() {
        val root = rootNode ?: return
        val width = width.toFloat()
        Log.d(TAG, "layoutNodes: width=$width")
        
        // Calculate the total width needed for the tree
        val totalWidth = calculateTreeWidth(root)
        Log.d(TAG, "Total tree width: $totalWidth")
        
        // Center the root node
        val startX = (width - totalWidth) / 2
        layoutNode(root, 0, startX + totalWidth / 2, totalWidth)
    }

    private fun calculateTreeWidth(node: TreeFragment.TreeNode): Float {
        // Calculate width for current node and its partner
        var width = nodeWidth.toFloat()
        if (node.partner != null) {
            width += nodeSpacing + nodeWidth
        } else {
            // Add space for the add partner button
            width += nodeSpacing + 40 // 40 is the width of the add partner button
        }
        
        // Calculate width for children
        if (node.children.isNotEmpty()) {
            val childrenWidth = node.children.sumOf { calculateTreeWidth(it).toDouble() }.toFloat()
            width = maxOf(width, childrenWidth)
        }
        
        return width
    }

    private fun layoutNode(node: TreeFragment.TreeNode, level: Int, x: Float, availableWidth: Float) {
        Log.d(TAG, "Layout node: ${node.profile?.name}, level: $level, x: $x")
        val nodeView = nodeViews[node.profileId] ?: return
        
        // Position the main node with zoom factor
        val y = level * levelSpacing.toFloat()
        nodeView.x = (x - nodeWidth / 2) * scaleFactor + focusX
        nodeView.y = y * scaleFactor + focusY
        nodeView.scaleX = scaleFactor
        nodeView.scaleY = scaleFactor
        Log.d(TAG, "Node positioned: ${node.profile?.name} at (${nodeView.x}, ${nodeView.y})")
        
        // Position partner if exists, otherwise position add partner button
        if (node.partner != null) {
            val partnerView = nodeViews[node.partner.profileId] ?: return
            partnerView.x = (x + nodeWidth + pairSpacing) * scaleFactor + focusX
            partnerView.y = y * scaleFactor + focusY
            partnerView.scaleX = scaleFactor
            partnerView.scaleY = scaleFactor
            Log.d(TAG, "Partner positioned: ${node.partner.profile?.name} at (${partnerView.x}, ${partnerView.y})")
        } else {
            val addPartnerButton = nodeViews["add_partner_${node.profileId}"] ?: return
            addPartnerButton.x = (x + nodeWidth + pairSpacing) * scaleFactor + focusX
            addPartnerButton.y = (y + (nodeHeight - 40) / 2) * scaleFactor + focusY
            addPartnerButton.scaleX = scaleFactor
            addPartnerButton.scaleY = scaleFactor
        }

        // Position add child button
        val addChildButton = nodeViews["add_child_${node.profileId}"] ?: return
        addChildButton.x = (x - 15) * scaleFactor + focusX
        addChildButton.y = (y + nodeHeight + addChildButtonSpacing) * scaleFactor + focusY
        addChildButton.scaleX = scaleFactor
        addChildButton.scaleY = scaleFactor
        
        // Position children
        if (node.children.isNotEmpty()) {
            val childWidth = availableWidth / node.children.size
            node.children.forEachIndexed { index, child ->
                val childX = x - availableWidth / 2 + childWidth * (index + 0.5f)
                layoutNode(child, level + 1, childX, childWidth)
            }
        }
    }

    fun setOnNodeClickListener(listener: (TreeFragment.TreeNode) -> Unit) {
        onNodeClickListener = listener
    }

    fun setOnAddPartnerClickListener(listener: (TreeFragment.TreeNode) -> Unit) {
        onAddPartnerClickListener = listener
    }

    fun setOnAddChildClickListener(listener: (TreeFragment.TreeNode) -> Unit) {
        onAddChildClickListener = listener
    }

    private fun createNodeView(node: TreeFragment.TreeNode): View {
        Log.d(TAG, "Creating node view for: ${node.profile?.name} with gender: ${node.profile?.gender}")
        val layoutId = if (node.profile?.gender == "Nam") R.layout.family_tree_node_male else R.layout.family_tree_node_female
        val view = LayoutInflater.from(context).inflate(layoutId, this, false)
        
        // Set name
        view.findViewById<TextView>(R.id.node_name).text = node.profile?.name ?: "Unknown"
        
        // Set dates
        val dateText = node.profile?.date_of_birth?.let { formatTimestamp(it) } ?: "_/_"
        view.findViewById<TextView>(R.id.node_dates).text = dateText
        
        // Set image
        val imageView = view.findViewById<ImageView>(R.id.node_image)
        if (node.profile?.gender == "Nam" || node.profile?.gender == "male") {
            imageView.setImageResource(R.drawable.download_5)
        } else {
            imageView.setImageResource(R.drawable.download_5)
        }

        // Add click listener
        view.setOnClickListener {
            onNodeClickListener?.invoke(node)
        }
        
        // Add to view hierarchy
        addView(view)
        Log.d(TAG, "Node view created and added for: ${node.profile?.name} with layout: ${if (layoutId == R.layout.family_tree_node_male) "male" else "female"}")
        return view
    }

    private fun formatTimestamp(timestamp: Timestamp?): String {
        return timestamp?.toDate()?.let { dateFormat.format(it) } ?: "_/_"
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d(TAG, "onDraw called")
        
        // Save canvas state
        canvas.save()
        
        // Apply zoom and translation
        canvas.translate(focusX, focusY)
        canvas.scale(scaleFactor, scaleFactor)
        canvas.translate(-focusX, -focusY)
        
        drawConnections(canvas)
        
        // Restore canvas state
        canvas.restore()
    }

    private fun drawConnections(canvas: Canvas) {
        rootNode?.let { drawNodeConnections(it, canvas) }
    }

    private fun drawNodeConnections(node: TreeFragment.TreeNode, canvas: Canvas) {
        val nodeView = nodeViews[node.profileId] ?: return
        Log.d(TAG, "Drawing connections for ${node.profile?.name}")

        if (node.children.isEmpty()) return

        // Calculate parent couple center point
        val parentCenterX = if (node.partner != null) {
            val partnerView = nodeViews[node.partner.profileId]
            if (partnerView != null) {
                (nodeView.x + partnerView.x + nodeWidth) / 2
            } else {
                nodeView.x + nodeWidth / 2
            }
        } else {
            nodeView.x + nodeWidth / 2
        }
        val parentCenterY = nodeView.y + nodeHeight

        // Calculate all children center points
        val childCenters = node.children.map { child ->
            val childView = nodeViews[child.profileId]
            if (child.partner != null) {
                val childPartnerView = nodeViews[child.partner.profileId]
                if (childView != null && childPartnerView != null) {
                    ((childView.x + childPartnerView.x + nodeWidth) / 2) to (childView.y)
                } else if (childView != null) {
                    (childView.x + nodeWidth / 2) to (childView.y)
                } else null
            } else if (childView != null) {
                (childView.x + nodeWidth / 2) to (childView.y)
            } else null
        }.filterNotNull()

        if (childCenters.isEmpty()) return

        // Get leftmost and rightmost child center
        val leftX = childCenters.first().first
        val rightX = childCenters.last().first
        // The y for the horizontal line (just above the children)
        val horizontalY = childCenters.minOf { it.second } - 40 * scaleFactor
        // The center x of the horizontal line
        val horizontalCenterX = (leftX + rightX) / 2

        // Draw vertical line from parent center down to horizontal line center
        path.reset()
        path.moveTo(parentCenterX / scaleFactor, parentCenterY / scaleFactor)
        path.lineTo(parentCenterX / scaleFactor, horizontalY / scaleFactor)
        path.lineTo(horizontalCenterX / scaleFactor, horizontalY / scaleFactor)
        canvas.drawPath(path, paint)

        // Draw horizontal line connecting all children centers
        path.reset()
        path.moveTo(leftX / scaleFactor, horizontalY / scaleFactor)
        path.lineTo(rightX / scaleFactor, horizontalY / scaleFactor)
        canvas.drawPath(path, paint)

        // Draw vertical lines from horizontal line down to each child center
        childCenters.forEach { (centerX, centerY) ->
            path.reset()
            path.moveTo(centerX / scaleFactor, horizontalY / scaleFactor)
            path.lineTo(centerX / scaleFactor, centerY / scaleFactor)
            canvas.drawPath(path, paint)
        }

        // Draw connections for children recursively
        node.children.forEach { child ->
            drawNodeConnections(child, canvas)
        }
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(minScale, min(scaleFactor, maxScale))
            
            // Update focus point
            focusX = detector.focusX
            focusY = detector.focusY
            
            // Relayout all nodes with new scale
            rootNode?.let { layoutNode(it, 0, width / 2f, width.toFloat()) }
            
            invalidate()
            return true
        }
    }

    override fun onTouchEvent(event: android.view.MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.actionMasked) {
            android.view.MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                activePointerId = event.getPointerId(0)
                mode = Mode.DRAG
            }
            android.view.MotionEvent.ACTION_POINTER_DOWN -> {
                mode = Mode.ZOOM
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                if (mode == Mode.DRAG) {
                    val pointerIndex = event.findPointerIndex(activePointerId)
                    val x = event.getX(pointerIndex)
                    val y = event.getY(pointerIndex)
                    
                    val dx = x - lastTouchX
                    val dy = y - lastTouchY
                    
                    focusX += dx
                    focusY += dy
                    
                    lastTouchX = x
                    lastTouchY = y
                    
                    // Relayout all nodes with new position
                    rootNode?.let { layoutNode(it, 0, width / 2f, width.toFloat()) }
                    
                    invalidate()
                }
            }
            android.view.MotionEvent.ACTION_UP, android.view.MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
                mode = Mode.NONE
            }
            android.view.MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = event.actionIndex
                val pointerId = event.getPointerId(pointerIndex)
                if (pointerId == activePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    lastTouchX = event.getX(newPointerIndex)
                    lastTouchY = event.getY(newPointerIndex)
                    activePointerId = event.getPointerId(newPointerIndex)
                }
            }
        }
        return true
    }
} 
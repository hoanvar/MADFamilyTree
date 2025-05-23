package com.dung.madfamilytree.views.custom

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ScaleFactor
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.dung.madfamilytree.R
import com.dung.madfamilytree.dtos.TreeNode
import com.dung.madfamilytree.utility.TreeUtility
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class FamilyTreeView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val TAG = "FamilyTreeView"
    private var onNodeClickListener: ((TreeNode) -> Unit)? = null
    private var onAddPartnerClickListener: ((TreeNode) -> Unit)? = null
    private var onAddChildClickListener: ((TreeNode) -> Unit)? = null

    // Panning related properties
    private var focusX = 0f
    private var focusY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var activePointerId = INVALID_POINTER_ID
    private var mode = Mode.NONE
    private var scaleFactor = 1.0f

    private enum class Mode {
        NONE, DRAG, ZOOM
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
        private const val NODE_WIDTH = 680  // Chiều rộng của một pair node
        private const val NODE_HEIGHT = 370 // Chiều cao của một pair node
        private const val LEVEL_SPACING = 600 // Khoảng cách giữa các level
        private const val NODE_SPACING = 300  // Khoảng cách tối thiểu giữa các node
        private const val MIN_SPACING = 300  // Khoảng cách tối thiểu giữa các node con
        private const val PAIR_SPACING = 40  // Khoảng cách giữa node chính và partner
        private const val LINE_MOVE_FACTOR = 0.0025f  // Hệ số điều chỉnh tỷ lệ di chuyển giữa đường thẳng và pair node
        private const val ZOOM_STEP = 0.1f  // Bước nhảy khi zoom
        private const val MIN_ZOOM = 0.5f   // Mức zoom tối thiểu
        private const val MAX_ZOOM = 2.0f   // Mức zoom tối đa
    }

    // Drawing related properties
    private val paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.black)
        strokeWidth = 2f
        style = Paint.Style.STROKE
        isAntiAlias = true
        textSize = 40f
    }
    private val path = Path()
    private val nodeRect = Rect()

    private var rootNode: TreeNode? = null
    private val nodeViews = mutableMapOf<String, View>()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private lateinit var zoomContainer: LinearLayout
    private lateinit var zoomInButton: ImageButton
    private lateinit var zoomOutButton: ImageButton

    private var selectedAvatarUri: Uri? = null
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var editBiographyLauncher: ActivityResultLauncher<Intent>




    init {
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        setupZoomButtons()
    }

    private fun setupZoomButtons() {


        // Tạo container cho các nút zoom
        zoomContainer = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(ContextCompat.getColor(context, android.R.color.white))
            elevation = 8f
            alpha = 0.9f
            // Đặt ID cho container để có thể tìm lại sau này
            id = View.generateViewId()
        }

        // Tạo nút zoom in
        zoomInButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.arrow_up_float)
            setBackgroundResource(android.R.color.transparent)
            setOnClickListener {
                zoomIn()
            }
        }

        // Tạo nút zoom out
        zoomOutButton = ImageButton(context).apply {
            setImageResource(android.R.drawable.arrow_down_float)
            setBackgroundResource(android.R.color.transparent)
            setOnClickListener {
                zoomOut()
            }
        }

        // Thêm các nút vào container
        zoomContainer.addView(zoomInButton)
        zoomContainer.addView(zoomOutButton)

        // Thêm container vào view
        addView(zoomContainer)
    }

    private fun zoomIn() {
        if (scaleFactor < MAX_ZOOM) {
            scaleFactor += ZOOM_STEP
            requestLayout()
            invalidate()
        }
    }

    private fun zoomOut() {
        if (scaleFactor > MIN_ZOOM) {
            scaleFactor -= ZOOM_STEP
            requestLayout()
            invalidate()
        }
    }

    fun setTree(root: TreeNode) {
        Log.d(TAG, "Setting tree with root: ${root.profile?.name}")
        rootNode = root
        refreshTree()
    }

    private fun refreshTree() {
        // Lưu lại container zoom
        val zoomContainer = findViewById<LinearLayout>(zoomContainer.id)
        removeAllViews()
        nodeViews.clear()
        createAllNodeViews(rootNode!!)
        // Thêm lại container zoom
        addView(zoomContainer)
        requestLayout()
        invalidate()
    }

    private fun createNodeView(node: TreeNode): View {
        // Tạo view cho một pair node
        val pairView = LayoutInflater.from(context).inflate(R.layout.family_tree_node_pair, this, false)
        
        // Thiết lập thông tin cho node chính
        val maleNodeContainer = pairView.findViewById<LinearLayout>(R.id.male_node_container)
        maleNodeContainer.setBackgroundResource(
            if (node.profile?.gender == "Nam") R.drawable.family_node_background
            else R.drawable.family_node_background_female
        )
        pairView.findViewById<TextView>(R.id.male_node_name).text = node.profile?.name ?: "Unknown"
        pairView.findViewById<TextView>(R.id.male_node_dates).text = node.profile?.date_of_birth?.let { formatTimestamp(it) } ?: "_/_/_"
        pairView.findViewById<TextView>(R.id.male_node_die).text = node.profile?.date_of_death?.let { formatTimestamp(it) } ?: "_/_/_"

        // Load avatar for male node
        node.profile?.avatar_url?.let { avatarUrl ->
            if (avatarUrl.isNotEmpty()) {
                Glide.with(context)
                    .load(avatarUrl)
                    .placeholder(R.drawable.profile_icon)
                    .circleCrop()
                    .into(pairView.findViewById<ImageView>(R.id.male_node_image))
            } else {
                pairView.findViewById<ImageView>(R.id.male_node_image).setImageResource(R.drawable.profile_icon)
            }
        } ?: pairView.findViewById<ImageView>(R.id.male_node_image).setImageResource(R.drawable.profile_icon)

        // Thêm click listener cho node chính
        maleNodeContainer.setOnClickListener {
            onNodeClickListener?.invoke(node)
        }

        // Thiết lập thông tin cho partner
        if (node.partner != null) {
            val femaleNodeContainer = pairView.findViewById<LinearLayout>(R.id.female_node_container)
            femaleNodeContainer.setBackgroundResource(
                if (node.partner.profile?.gender == "Nam") R.drawable.family_node_background
                else R.drawable.family_node_background_female
            )
            pairView.findViewById<TextView>(R.id.female_node_name).text = node.partner.profile?.name ?: "Unknown"
            pairView.findViewById<TextView>(R.id.female_node_dates).text = node.partner.profile?.date_of_birth?.let { formatTimestamp(it) } ?: "_/_/_"
            pairView.findViewById<TextView>(R.id.female_node_die).text = node.partner?.profile?.date_of_death?.let { formatTimestamp(it) } ?: "_/_/_"

            // Load avatar for female node
            node.partner.profile?.avatar_url?.let { avatarUrl ->
                if (avatarUrl.isNotEmpty()) {
                    Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(R.drawable.profile_icon)
                        .circleCrop()
                        .into(pairView.findViewById<ImageView>(R.id.female_node_image))
                } else {
                    pairView.findViewById<ImageView>(R.id.female_node_image).setImageResource(R.drawable.profile_icon)
                }
            } ?: pairView.findViewById<ImageView>(R.id.female_node_image).setImageResource(R.drawable.profile_icon)

            // Thêm click listener cho partner node
            femaleNodeContainer.setOnClickListener {
                onNodeClickListener?.invoke(node.partner)
            }
        } else {
            // Nếu chưa có partner, hiển thị nút thêm partner ở giữa
            val femaleNodeContainer = pairView.findViewById<LinearLayout>(R.id.female_node_container)
            femaleNodeContainer.removeAllViews()
            femaleNodeContainer.setBackgroundResource(android.R.color.transparent)
            
            val addPartnerButton = LayoutInflater.from(context).inflate(R.layout.add_partner_button, this, false) as ImageButton
            addPartnerButton.setOnClickListener {
                onAddPartnerClickListener?.invoke(node)
                // Refresh tree sau khi thêm partner
                refreshTree()
            }
            
            // Tạo layout params để căn giữa button
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER
                topMargin = 60 // Thêm padding top
            }
            
            addPartnerButton.layoutParams = layoutParams
            // Đặt kích thước nhỏ hơn cho button
            addPartnerButton.scaleX = 1f
            addPartnerButton.scaleY = 1f
            femaleNodeContainer.addView(addPartnerButton)
        }

        // Thêm nút thêm con
        val addChildButton = LayoutInflater.from(context).inflate(R.layout.add_child_button, this, false) as ImageButton
        addChildButton.setOnClickListener {
            onAddChildClickListener?.invoke(node)
            // Refresh tree sau khi thêm child
            refreshTree()
        }
        
        // Tạo layout params để điều chỉnh vị trí button
        val childButtonLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            leftMargin = 80  // X + 50
            topMargin = -25  // Y - 30
        }
        
        addChildButton.layoutParams = childButtonLayoutParams
        // Thu nhỏ button
        addChildButton.scaleX = 0.5f
        addChildButton.scaleY = 0.5f
        
        pairView.findViewById<LinearLayout>(R.id.add_child_container).addView(addChildButton)

        return pairView
    }

    private fun createAllNodeViews(node: TreeNode) {
        val nodeView = createNodeView(node)
        nodeViews[node.profileId] = nodeView
        addView(nodeView)
        
        node.children.forEach { child ->
            createAllNodeViews(child)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        
        // Đặt vị trí cho container zoom ở góc dưới bên phải
        val margin = 16
        val buttonSize = 100
        val containerWidth = buttonSize
        val containerHeight = buttonSize * 2 + margin
        
        // Tìm lại container zoom
        val zoomContainer = findViewById<LinearLayout>(zoomContainer.id)
        zoomContainer?.let {
            it.layout(
                width - containerWidth - margin,
                height - containerHeight - margin,
                width - margin,
                height - margin
            )
            
            // Đặt kích thước cho các nút
            it.getChildAt(0)?.layout(0, 0, buttonSize, buttonSize)
            it.getChildAt(1)?.layout(0, buttonSize + margin, buttonSize, buttonSize * 2 + margin)
        }
        
        layoutNodes()
    }

    private fun layoutNodes() {
        val root = rootNode ?: return
        val width = width.toFloat()
        
        // Tính toán số lượng node và node con ở mỗi level
        val nodesByLevel = mutableMapOf<Int, MutableList<TreeNode>>()
        val childrenCountByLevel = mutableMapOf<Int, Int>()
        countNodesAndChildrenByLevel(root, 0, nodesByLevel, childrenCountByLevel)
        
        // Tính toán khoảng cách cho mỗi level
        val levelSpacings = calculateLevelSpacings(nodesByLevel, childrenCountByLevel, width)
        
        // Bố trí các node
        layoutNodeWithSpacing(root, 0, width / 2, width, levelSpacings)
    }

    private fun countNodesAndChildrenByLevel(
        node: TreeNode, 
        level: Int, 
        nodesByLevel: MutableMap<Int, MutableList<TreeNode>>,
        childrenCountByLevel: MutableMap<Int, Int>
    ) {
        // Thêm node vào level hiện tại
        nodesByLevel.getOrPut(level) { mutableListOf() }.add(node)
        
        // Cập nhật số lượng node con cho level hiện tại
        val currentChildrenCount = childrenCountByLevel.getOrDefault(level, 0)
        childrenCountByLevel[level] = currentChildrenCount + node.children.size
        
        // Đệ quy cho các node con
        node.children.forEach { child ->
            countNodesAndChildrenByLevel(child, level + 1, nodesByLevel, childrenCountByLevel)
        }
    }

    private fun calculateLevelSpacings(
        nodesByLevel: Map<Int, List<TreeNode>>,
        childrenCountByLevel: Map<Int, Int>,
        totalWidth: Float
    ): Map<Int, Float> {
        val spacings = mutableMapOf<Int, Float>()
        
        nodesByLevel.forEach { (level, nodes) ->
            val totalNodes = nodes.size
            if (totalNodes > 1) {
                // Tính toán tổng số node con ở level tiếp theo
                val nextLevelChildrenCount = childrenCountByLevel[level + 1] ?: 0
                
                // Tính toán khoảng cách cần thiết dựa trên số lượng node con
                val requiredSpacing = if (nextLevelChildrenCount > 0) {
                    // Nếu có node con, tính toán dựa trên số lượng node con
                    val isEven = nextLevelChildrenCount % 2 == 0
                    val centerOffset = if (isEven) {
                        // Nếu số lượng con chẵn, căn giữa giữa hai node con ở giữa
                        (NODE_WIDTH / 2).toFloat()
                    } else {
                        // Nếu số lượng con lẻ, căn giữa node con ở giữa
                        0f
                    }
                    
                    // Tính toán tổng chiều rộng cần thiết
                    val totalRequiredWidth = (nextLevelChildrenCount - 1) * NODE_SPACING + 
                                          nextLevelChildrenCount * NODE_WIDTH + 
                                          centerOffset * 2
                    
                    // Thêm padding để tránh chồng chéo
                    totalRequiredWidth + NODE_WIDTH
                } else {
                    // Nếu không có node con, sử dụng khoảng cách tối thiểu
                    (NODE_SPACING + NODE_WIDTH).toFloat()
                }
                
                // Tính toán khoảng cách thực tế
                val spacing = if (requiredSpacing > totalWidth) {
                    // Nếu không gian không đủ, sử dụng khoảng cách tối thiểu
                    (MIN_SPACING + NODE_WIDTH).toFloat()
                } else {
                    // Nếu có đủ không gian, sử dụng khoảng cách đã tính
                    requiredSpacing / (totalNodes - 1).toFloat()
                }
                
                spacings[level] = spacing
            }
        }
        
        return spacings
    }

    private fun layoutNodeWithSpacing(node: TreeNode, level: Int, x: Float, availableWidth: Float, levelSpacings: Map<Int, Float>) {
        val nodeView = nodeViews[node.profileId] ?: return
        
        // Tính toán vị trí y dựa trên level
        val y = level * LEVEL_SPACING.toFloat()
        
        // Đặt vị trí cho node, áp dụng scale factor
        nodeView.x = (x - NODE_WIDTH / 2 + focusX) * scaleFactor
        nodeView.y = (y + focusY) * scaleFactor
        nodeView.scaleX = scaleFactor
        nodeView.scaleY = scaleFactor

        // Bố trí các node con
        if (node.children.isNotEmpty()) {
            // Sử dụng khoảng cách của level con
            val spacing = levelSpacings[level + 1] ?: (NODE_SPACING + NODE_WIDTH).toFloat()
            
            // Tính toán tổng chiều rộng cần thiết cho các node con
            val totalWidth = (node.children.size - 1) * spacing + node.children.size * NODE_WIDTH
            
            // Tính toán điểm bắt đầu để căn giữa các node con
            val startX = x - totalWidth / 2 + NODE_WIDTH / 2

            // Bố trí từng node con
            node.children.forEachIndexed { index, child ->
                val childX = startX + index * (NODE_WIDTH + spacing)
                // Chia đều không gian cho các node con
                val childAvailableWidth = (availableWidth - NODE_WIDTH) / node.children.size
                layoutNodeWithSpacing(child, level + 1, childX, childAvailableWidth, levelSpacings)
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        // Áp dụng hệ số di chuyển cho các đường thẳng
        canvas.translate(focusX * LINE_MOVE_FACTOR , focusY * LINE_MOVE_FACTOR )
        canvas.scale(scaleFactor, scaleFactor, 0f, 0f)
        drawConnections(canvas)
        canvas.restore()
    }

    private fun drawConnections(canvas: Canvas) {
        // Vẽ kết nối cho từng node cha riêng biệt
        rootNode?.let { root ->
            drawNodeConnections(root, canvas)
        }
    }

    private fun drawNodeConnections(node: TreeNode, canvas: Canvas) {
        val nodeView = nodeViews[node.profileId] ?: return
        if (node.children.isEmpty()) return

        // Tính toán điểm kết nối từ node cha
        val parentCenterX = (nodeView.x + NODE_WIDTH / 2) / scaleFactor
        val parentCenterY = (nodeView.y + NODE_HEIGHT) / scaleFactor

        // Tính toán vị trí của các node con
        val childCenters = node.children.mapNotNull { child ->
            nodeViews[child.profileId]?.let { view ->
                ((view.x + NODE_WIDTH / 2) / scaleFactor) to (view.y / scaleFactor)
            }
        }

        if (childCenters.isEmpty()) return

        // Tính toán khoảng cách giữa node cha và node con đầu tiên
        val verticalGap = childCenters.first().second - parentCenterY
        val horizontalY = parentCenterY + verticalGap * 0.3f // Điểm giao giữa đường đứng và đường ngang

        if (childCenters.size == 1) {
            // Trường hợp chỉ có một node con - vẽ đường thẳng
            path.reset()
            path.moveTo(parentCenterX, parentCenterY)
            path.lineTo(childCenters.first().first, childCenters.first().second)
            canvas.drawPath(path, paint)
        } else {
            // Trường hợp có nhiều node con
            // Vẽ đường thẳng đứng từ node cha
            path.reset()
            path.moveTo(parentCenterX, parentCenterY)
            path.lineTo(parentCenterX, horizontalY)
            canvas.drawPath(path, paint)

            // Vẽ đường ngang kết nối các node con của node cha hiện tại
            val leftX = childCenters.first().first
            val rightX = childCenters.last().first

            path.reset()
            path.moveTo(leftX, horizontalY)
            path.lineTo(rightX, horizontalY)
            canvas.drawPath(path, paint)

            // Vẽ đường thẳng đứng đến từng node con
            childCenters.forEach { (centerX, centerY) ->
                path.reset()
                path.moveTo(centerX, horizontalY)
                path.lineTo(centerX, centerY)
                canvas.drawPath(path, paint)
            }
        }

        // Vẽ kết nối cho các node con
        node.children.forEach { child ->
            drawNodeConnections(child, canvas)
        }
    }

    private fun formatTimestamp(timestamp: Timestamp?): String {
        return timestamp?.toDate()?.let { dateFormat.format(it) } ?: "_/_"
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y
                activePointerId = event.getPointerId(0)
                mode = Mode.DRAG
            }
            MotionEvent.ACTION_MOVE -> {
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
                    
                    requestLayout()
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                activePointerId = INVALID_POINTER_ID
                mode = Mode.NONE
            }
            MotionEvent.ACTION_POINTER_UP -> {
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

    fun setOnNodeClickListener(listener: (TreeNode) -> Unit) {
        onNodeClickListener = listener
    }

    fun setOnAddPartnerClickListener(listener: (TreeNode) -> Unit) {
        onAddPartnerClickListener = listener
    }

    fun setOnAddChildClickListener(listener: (TreeNode) -> Unit) {
        onAddChildClickListener = listener
    }



} 
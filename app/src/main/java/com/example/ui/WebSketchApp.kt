package com.example.ui

import android.content.Context
import android.content.Intent
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// --- ViewModel ---

class WebSketchViewModel(private val repository: WebProjectRepository) : ViewModel() {
    private val geminiService = GeminiService()

    // Database Projects
    val savedProjects: StateFlow<List<WebProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Currently Editing Project
    private val _currentProject = MutableStateFlow<WebProject?>(null)
    val currentProject: StateFlow<WebProject?> = _currentProject.asStateFlow()

    // Active visual components loaded from HTML
    private val _components = MutableStateFlow<List<WebComponent>>(emptyList())
    val components: StateFlow<List<WebComponent>> = _components.asStateFlow()

    // Live synced HTML representation of the active components
    private val _liveHtml = MutableStateFlow("")
    val liveHtml: StateFlow<String> = _liveHtml.asStateFlow()

    // Active selected component for visual editing
    private val _selectedComponent = MutableStateFlow<WebComponent?>(null)
    val selectedComponent: StateFlow<WebComponent?> = _selectedComponent.asStateFlow()

    // Gemini API Loading state
    private val _aiLoading = MutableStateFlow(false)
    val aiLoading: StateFlow<Boolean> = _aiLoading.asStateFlow()

    // Active screen navigation
    private val _currentScreen = MutableStateFlow<Screen>(Screen.ProjectList)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    sealed class Screen {
        object ProjectList : Screen()
        object Editor : Screen()
    }

    init {
        // Automatically sync HTML whenever visual components change
        viewModelScope.launch {
            _components.collect { list ->
                if (list.isNotEmpty()) {
                    val html = list.toHtml()
                    _liveHtml.value = html
                }
            }
        }
    }

    fun navigateToEditor(project: WebProject) {
        _currentProject.value = project
        _liveHtml.value = project.htmlContent
        val parsed = parseHtmlToComponents(project.htmlContent)
        _components.value = parsed.ifEmpty {
            //Prefill default landing page if completely empty
            listOf(
                WebComponent.createDefault(ComponentType.NAVBAR),
                WebComponent.createDefault(ComponentType.HERO),
                WebComponent.createDefault(ComponentType.FEATURES),
                WebComponent.createDefault(ComponentType.FOOTER)
            )
        }
        _currentScreen.value = Screen.Editor
    }

    fun navigateToProjectList() {
        // Save current project before exit
        saveCurrentProject()
        _currentProject.value = null
        _selectedComponent.value = null
        _currentScreen.value = Screen.ProjectList
    }

    fun createNewProject(name: String, templateType: String) {
        viewModelScope.launch {
            val defaultList = when (templateType) {
                "portfolio" -> listOf(
                    WebComponent.createDefault(ComponentType.NAVBAR).copy(properties = mapOf(
                        "logo" to "💼", "title" to "معرض أعمالي", "link1" to "من أنا", "link2" to "خدماتي", "link3" to "تواصل معي", "bg_color" to "#1E293B", "text_color" to "#F8FAFC"
                    )),
                    WebComponent.createDefault(ComponentType.HERO).copy(properties = mapOf(
                        "title" to "مرحباً، أنا مهندس ويب محترف", "subtitle" to "أقوم ببناء تطبيقات ويب حديثة، سريعة، ومتجاوبة تلبي احتياجات سوق العمل بأحدث التقنيات.", "btn_text" to "عرض المشاريع 🚀", "btn_link" to "#", "image_url" to "https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=800", "bg_color" to "#1E293B"
                    )),
                    WebComponent.createDefault(ComponentType.FEATURES).copy(properties = mapOf(
                        "title1" to "تطوير الواجهات", "desc1" to "تصميم واجهات مستخدم مذهلة وسلسة باستخدام React و Vue",
                        "title2" to "برمجة السيرفرات", "desc2" to "بناء قواعد بيانات آمنة وسريعة مع API متكامل",
                        "title3" to "تهيئة محركات البحث", "desc3" to "تحسين أداء الموقع وتصدر نتائج البحث الأولى في جوجل",
                        "bg_color" to "#F1F5F9"
                    )),
                    WebComponent.createDefault(ComponentType.CONTACT_FORM).copy(properties = mapOf(
                        "title" to "دعنا نعمل معاً", "desc" to "أرسل لي تفاصيل مشروعك وسأقوم بالرد عليك في غضون 24 ساعة.", "placeholder_name" to "الاسم بالكامل", "placeholder_email" to "عنوان البريد الإلكتروني", "btn_text" to "إرسال الطلب", "bg_color" to "#FFFFFF"
                    )),
                    WebComponent.createDefault(ComponentType.FOOTER).copy(properties = mapOf(
                        "text" to "تطوير وبرمجة © 2026 معرض الأعمال الشخصي", "link1" to "تويتر", "link2" to "لينكد إن", "bg_color" to "#0F172A", "text_color" to "#94A3B8"
                    ))
                )
                "store" -> listOf(
                    WebComponent.createDefault(ComponentType.NAVBAR).copy(properties = mapOf(
                        "logo" to "🛍️", "title" to "متجر الأناقة", "link1" to "المعروضات", "link2" to "العروض المميزة", "link3" to "اتصل بنا", "bg_color" to "#E91E63", "text_color" to "#FFFFFF"
                    )),
                    WebComponent.createDefault(ComponentType.HERO).copy(properties = mapOf(
                        "title" to "خصومات نهاية العام حتى 50%", "subtitle" to "اكتشف أحدث تشكيلات الأزياء والملابس العصرية بأجود الخامات وأفضل الأسعار المحلية.", "btn_text" to "تسوق الآن 🛒", "btn_link" to "#", "image_url" to "https://images.unsplash.com/photo-1441986300917-64674bd600d8?w=800", "bg_color" to "#252B46"
                    )),
                    WebComponent.createDefault(ComponentType.CARD_WIDGET).copy(properties = mapOf(
                        "title" to "جاكيت شتوي فاخر", "desc" to "جاكيت شتوي مبطن مقاوم للمياه متوفر بجميع المقاسات وبألوان رائعة.", "price" to "299 ر.س", "image_url" to "https://images.unsplash.com/photo-1544022613-e87ca75a784a?w=800", "btn_text" to "إضافة للسلة"
                    )),
                    WebComponent.createDefault(ComponentType.CONTACT_FORM).copy(properties = mapOf(
                        "title" to "انضم للنشرة البريدية", "desc" to "احصل على كوبون خصم 15% فور اشتراكك في القائمة البريدية ليصلك جديد عروضنا.", "placeholder_name" to "اسمك الأول", "placeholder_email" to "بريدك الإلكتروني", "btn_text" to "اشترك الآن", "bg_color" to "#F3F4F6"
                    )),
                    WebComponent.createDefault(ComponentType.FOOTER).copy(properties = mapOf(
                        "text" to "جميع الحقوق محفوظة © 2026 متجر الأناقة للملابس العصرية", "link1" to "سياسة الاسترجاع", "link2" to "طرق الدفع", "bg_color" to "#111827", "text_color" to "#9CA3AF"
                    ))
                )
                else -> listOf(
                    WebComponent.createDefault(ComponentType.NAVBAR),
                    WebComponent.createDefault(ComponentType.HERO),
                    WebComponent.createDefault(ComponentType.FEATURES),
                    WebComponent.createDefault(ComponentType.FOOTER)
                )
            }

            val html = defaultList.toHtml()
            val newProj = WebProject(name = name, htmlContent = html)
            val insertId = repository.insert(newProj)
            val insertedProj = newProj.copy(id = insertId.toInt())
            navigateToEditor(insertedProj)
        }
    }

    fun saveCurrentProject() {
        val proj = _currentProject.value ?: return
        val currentHtml = _liveHtml.value
        viewModelScope.launch {
            val updated = proj.copy(htmlContent = currentHtml, updatedAt = System.currentTimeMillis())
            repository.insert(updated)
            _currentProject.value = updated
        }
    }

    fun deleteProject(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    // --- Component visual actions ---

    fun addComponent(type: ComponentType) {
        val newList = _components.value.toMutableList()
        // Try to add footer at the very end or just insert at current position
        val insertIndex = if (newList.isNotEmpty() && newList.last().type == ComponentType.FOOTER) {
            newList.size - 1
        } else {
            newList.size
        }
        newList.add(insertIndex, WebComponent.createDefault(type))
        _components.value = newList
        saveCurrentProject()
    }

    fun removeComponent(id: String) {
        val newList = _components.value.filter { it.id != id }
        _components.value = newList
        if (_selectedComponent.value?.id == id) {
            _selectedComponent.value = null
        }
        saveCurrentProject()
    }

    fun moveComponent(id: String, up: Boolean) {
        val list = _components.value.toMutableList()
        val index = list.indexOfFirst { it.id == id }
        if (index == -1) return
        val newIndex = if (up) index - 1 else index + 1
        if (newIndex in 0 until list.size) {
            val temp = list[index]
            list[index] = list[newIndex]
            list[newIndex] = temp
            _components.value = list
            saveCurrentProject()
        }
    }

    fun selectComponent(component: WebComponent) {
        _selectedComponent.value = component
    }

    fun updateComponentProperties(id: String, props: Map<String, String>) {
        val newList = _components.value.map {
            if (it.id == id) {
                it.copy(properties = props)
            } else {
                it
            }
        }
        _components.value = newList
        _selectedComponent.value = null
        saveCurrentProject()
    }

    // --- Bidirectional "and vice versa" direct HTML Sync ---

    fun updateHtmlFromCodeEditor(newHtml: String) {
        _liveHtml.value = newHtml
        val parsed = parseHtmlToComponents(newHtml)
        if (parsed.isNotEmpty()) {
            _components.value = parsed
            saveCurrentProject()
        }
    }

    // --- AI Assist via Gemini ---

    fun generateWithAI(prompt: String, context: Context) {
        if (prompt.trim().isEmpty()) return
        _aiLoading.value = true
        viewModelScope.launch {
            val result = geminiService.generatePageFromPrompt(prompt)
            _aiLoading.value = false
            if (result == "API_KEY_MISSING") {
                Toast.makeText(context, "الرجاء إدخال مفتاح Gemini API Key في لوحة الأسرار (Secrets panel) لتفعيل الذكاء الاصطناعي!", Toast.LENGTH_LONG).show()
            } else if (result.startsWith("Error:")) {
                Toast.makeText(context, "فشل الاتصال: ${result.removePrefix("Error:")}", Toast.LENGTH_LONG).show()
            } else {
                _liveHtml.value = result
                val parsed = parseHtmlToComponents(result)
                if (parsed.isNotEmpty()) {
                    _components.value = parsed
                    saveCurrentProject()
                    Toast.makeText(context, "تم توليد صفحة الويب وتطبيقها بنجاح! ✨", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "قام الذكاء الاصطناعي بتوليد كود، يمكنك مراجعته وتعديله من تبويب الأكواد.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun applyAIPreset(instruction: String, context: Context) {
        val currentHtml = _liveHtml.value
        val fullPrompt = """
        Here is my current web page HTML:
        $currentHtml
        
        Please apply this exact change or preset to it:
        $instruction
        
        Make sure you only output the complete updated raw HTML. Do not change the custom data-ws attributes so I don't lose my visual builder blocks.
        """.trimIndent()
        generateWithAI(fullPrompt, context)
    }
}

class WebSketchViewModelFactory(private val repository: WebProjectRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WebSketchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return WebSketchViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}


// --- Main Screen Container ---

@Composable
fun WebSketchApp() {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val repo = remember { WebProjectRepository(db.webProjectDao()) }
    val viewModel: WebSketchViewModel = viewModel(factory = WebSketchViewModelFactory(repo))

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val savedProjects by viewModel.savedProjects.collectAsStateWithLifecycle()
    val currentProject by viewModel.currentProject.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F0F1A) // Premium deep space background
    ) {
        Crossfade(targetState = currentScreen, label = "ScreenTransition") { screen ->
            when (screen) {
                is WebSketchViewModel.Screen.ProjectList -> {
                    ProjectListScreen(
                        projects = savedProjects,
                        onCreateProject = { name, template -> viewModel.createNewProject(name, template) },
                        onSelectProject = { viewModel.navigateToEditor(it) },
                        onDeleteProject = { viewModel.deleteProject(it) }
                    )
                }
                is WebSketchViewModel.Screen.Editor -> {
                    currentProject?.let { project ->
                        EditorScreen(
                            project = project,
                            viewModel = viewModel,
                            onBackToProjects = { viewModel.navigateToProjectList() }
                        )
                    }
                }
            }
        }
    }
}


// --- Project List Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectListScreen(
    projects: List<WebProject>,
    onCreateProject: (String, String) -> Unit,
    onSelectProject: (WebProject) -> Unit,
    onDeleteProject: (Int) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newProjectName by remember { mutableStateOf("") }
    var selectedTemplate by remember { mutableStateOf("blank") }

    val gradientBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF1E1035), Color(0xFF090412))
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "ويب سكيتش برو 🚀",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0A0518)
                ),
                actions = {
                    IconButton(
                        onClick = { showCreateDialog = true },
                        modifier = Modifier.testTag("new_project_action")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "مشروع جديد", tint = Color(0xFF00E676))
                    }
                }
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.background(gradientBg)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual Banner Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF6200EE), Color(0xFFE91E63))
                        )
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "ابنِ صفحات الويب بذكاء وعصرية!",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "أقوى محرر ثنائي الاتجاه: اسحب وأفلت العناصر البصرية، عدل الأكواد البرمجية مباشرة، أو دع الذكاء الاصطناعي يتولى الأمر بنقرة واحدة.",
                            color = Color(0xFFE2E8F0),
                            fontSize = 12.sp,
                            lineHeight = 1.6.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    // App Logo preview
                    Surface(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        color = Color(0xFF0F0F1A)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("💻", fontSize = 36.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                "مشاريع الويب المحفوظة لديك 👇",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                textAlign = TextAlign.Right
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (projects.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0x1AFFFFFF))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👋", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "لا توجد مشاريع ويب بعد!",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "انقر على زر الإضافة (+) بالأعلى لإنشاء صفحة ويب مذهلة أو توليدها بالذكاء الاصطناعي.",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showCreateDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("create_first_project_btn")
                        ) {
                            Text("أنشئ أول صفحة الآن 🚀", color = Color.White)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(projects) { _, project ->
                        ProjectItemCard(
                            project = project,
                            onSelect = onSelectProject,
                            onDelete = { onDeleteProject(project.id) }
                        )
                    }
                }
            }
        }
    }

    // New Project Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = {
                Text(
                    "إنشاء مشروع ويب جديد 💡",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("اسم المشروع (مثال: متجر العسل):", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("project_name_input"),
                        singleLine = true,
                        placeholder = { Text("أدخل اسم الصفحة...") },
                        textStyle = TextStyle(textAlign = TextAlign.Right, textDirection = androidx.compose.ui.text.style.TextDirection.Rtl)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("اختر قالباً لبدء العمل:", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            Triple("blank", "صفحة افتراضية", "📂"),
                            Triple("portfolio", "معرض أعمال", "💼"),
                            Triple("store", "متجر إلكتروني", "🛍️")
                        ).forEach { (id, label, icon) ->
                            val isSelected = selectedTemplate == id
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(95.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        2.dp,
                                        if (isSelected) Color(0xFF00E676) else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedTemplate = id },
                                color = if (isSelected) Color(0xFF1E1B4B) else Color(0xFFF1F5F9)
                            ) {
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(icon, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        label,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color.Black,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newProjectName.trim().isNotEmpty()) {
                            onCreateProject(newProjectName, selectedTemplate)
                            newProjectName = ""
                            selectedTemplate = "blank"
                            showCreateDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                    modifier = Modifier.testTag("confirm_create_project_btn")
                ) {
                    Text("بدء التصميم 🚀", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun ProjectItemCard(
    project: WebProject,
    onSelect: (WebProject) -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(project.updatedAt) {
        val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault())
        sdf.format(Date(project.updatedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(project) }
            .testTag("project_card_${project.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A2E)),
        border = BorderStroke(1.dp, Color(0xFF2E2E4A))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444))
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    project.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "آخر تعديل: $dateString",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text("HTML5/CSS3", fontSize = 9.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            labelColor = Color(0xFF00E676),
                            containerColor = Color(0x1500E676)
                        ),
                        border = null
                    )
                    SuggestionChip(
                        onClick = {},
                        label = { Text("RTL متجاوب", fontSize = 9.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            labelColor = Color(0xFF90CAF9),
                            containerColor = Color(0x1590CAF9)
                        ),
                        border = null
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp)),
                color = Color(0xFF1E1035)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🌐", fontSize = 24.sp)
                }
            }
        }
    }
}


// --- Editor Screen ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    project: WebProject,
    viewModel: WebSketchViewModel,
    onBackToProjects: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("التصميم البصري 🎨", "محرر الكود 💻", "المعاينة الحية 🌐", "مساعد ذكي ✨")

    val components by viewModel.components.collectAsStateWithLifecycle()
    val liveHtml by viewModel.liveHtml.collectAsStateWithLifecycle()
    val selectedComponent by viewModel.selectedComponent.collectAsStateWithLifecycle()
    val aiLoading by viewModel.aiLoading.collectAsStateWithLifecycle()

    var showAddComponentDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.End, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            project.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "محرر ويب سكيتش برو",
                            color = Color(0xFF00E676),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToProjects) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        viewModel.saveCurrentProject()
                        Toast.makeText(context, "تم حفظ التعديلات بنجاح! 💾", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Check, contentDescription = "حفظ", tint = Color(0xFF00E676))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F1A)
                )
            )
        },
        bottomBar = {
            Column {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF0A0518),
                    contentColor = Color.White,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF6200EE)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = { showAddComponentDialog = true },
                    containerColor = Color(0xFF6200EE),
                    contentColor = Color.White,
                    modifier = Modifier.testTag("add_component_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "إضافة عنصر")
                }
            }
        },
        containerColor = Color(0xFF090412)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> {
                    VisualCanvasTab(
                        components = components,
                        onSelect = { viewModel.selectComponent(it) },
                        onMoveUp = { viewModel.moveComponent(it, true) },
                        onMoveDown = { viewModel.moveComponent(it, false) },
                        onDelete = { viewModel.removeComponent(it) }
                    )
                }
                1 -> {
                    CodeEditorTab(
                        html = liveHtml,
                        onSync = {
                            viewModel.updateHtmlFromCodeEditor(it)
                            Toast.makeText(context, "تم مزامنة الكود مع المصمم البصري! 🔄", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                2 -> {
                    LivePreviewTab(html = liveHtml)
                }
                3 -> {
                    GeminiAITab(
                        html = liveHtml,
                        isLoading = aiLoading,
                        onGenerate = { prompt -> viewModel.generateWithAI(prompt, context) },
                        onApplyPreset = { preset -> viewModel.applyAIPreset(preset, context) }
                    )
                }
            }
        }
    }

    // Add Component Selection Dialog
    if (showAddComponentDialog) {
        AlertDialog(
            onDismissRequest = { showAddComponentDialog = false },
            title = {
                Text(
                    "إضافة عنصر ويب جديد 🧱",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val grouped = ComponentType.values().groupBy { it.category }
                    for ((category, types) in grouped) {
                        item {
                            Text(
                                category,
                                color = Color(0xFF6200EE),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                textAlign = TextAlign.Right
                            )
                        }
                        items(types.size) { index ->
                            val type = types[index]
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addComponent(type)
                                        showAddComponentDialog = false
                                        Toast
                                            .makeText(
                                                context,
                                                "تمت إضافة: ${type.arabicName}",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }
                                    .testTag("comp_type_item_${type.name}"),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        type.arabicName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAddComponentDialog = false }) {
                    Text("إغلاق", color = Color.Gray)
                }
            }
        )
    }

    // Component Property Inspector Dialog
    selectedComponent?.let { component ->
        PropertyInspectorDialog(
            component = component,
            onDismiss = { viewModel.selectComponent(component) }, // Closes if passing current state, wait we can set null:
            onCancel = { viewModel.updateComponentProperties(component.id, component.properties) }, // cancel/close dialog
            onSave = { updatedProps ->
                viewModel.updateComponentProperties(component.id, updatedProps)
                Toast.makeText(context, "تم حفظ خصائص العنصر! ✏️", Toast.LENGTH_SHORT).show()
            }
        )
    }
}


// --- Tab 1: Visual Canvas View ---

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VisualCanvasTab(
    components: List<WebComponent>,
    onSelect: (WebComponent) -> Unit,
    onMoveUp: (String) -> Unit,
    onMoveDown: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (components.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🎨", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "مساحة التصميم فارغة",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "انقر على الزر العائم (+) بالأسفل لإدراج شريط تنقل، صورة، أو واجهة رئيسية لصفحتك.",
                    color = Color.LightGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(components, key = { _, item -> item.id }) { index, component ->
                VisualComponentCard(
                    component = component,
                    isFirst = index == 0,
                    isLast = index == components.size - 1,
                    onSelect = { onSelect(component) },
                    onMoveUp = { onMoveUp(component.id) },
                    onMoveDown = { onMoveDown(component.id) },
                    onDelete = { onDelete(component.id) }
                )
            }
            item {
                Spacer(modifier = Modifier.height(80.dp)) // Floating space for FAB
            }
        }
    }
}

@Composable
fun VisualComponentCard(
    component: WebComponent,
    isFirst: Boolean,
    isLast: Boolean,
    onSelect: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("visual_comp_${component.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A3C)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.2.dp, Color(0xFF382F6B))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header row: Icon, Type Name, and Operation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Operation actions (Delete, Edit, Reorder)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onSelect) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل الخصائص", tint = Color(0xFF00E676), modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onMoveUp, enabled = !isFirst) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "للأعلى",
                            tint = if (!isFirst) Color.White else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onMoveDown, enabled = !isLast) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "للأسفل",
                            tint = if (!isLast) Color.White else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Right: Component Category & Name with a nice colored indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            component.type.arabicName,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            component.type.category,
                            color = Color.LightGray,
                            fontSize = 10.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF3B2F75)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when (component.type) {
                                ComponentType.NAVBAR -> "🗂️"
                                ComponentType.HERO -> "⭐"
                                ComponentType.FEATURES -> "⚡"
                                ComponentType.TEXT_BLOCK -> "✍️"
                                ComponentType.IMAGE_BLOCK -> "🖼️"
                                ComponentType.BUTTON_LINK -> "🎯"
                                ComponentType.CARD_WIDGET -> "📦"
                                ComponentType.CONTACT_FORM -> "✉️"
                                ComponentType.FOOTER -> "📋"
                            },
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Divider(color = Color(0xFF2E245C), modifier = Modifier.padding(vertical = 10.dp))

            // Body: Component summary based on edited properties
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF140F2D))
                    .padding(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                when (component.type) {
                    ComponentType.NAVBAR -> {
                        Text("العنوان: ${component.properties["title"] ?: ""}", color = Color.LightGray, fontSize = 12.sp)
                        Text("الخلفية: ${component.properties["bg_color"] ?: ""}", color = Color.Gray, fontSize = 11.sp)
                    }
                    ComponentType.HERO -> {
                        Text("العنوان العريض: ${component.properties["title"] ?: ""}", color = Color.LightGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("الزر: ${component.properties["btn_text"] ?: ""}", color = Color.Gray, fontSize = 11.sp)
                    }
                    ComponentType.FEATURES -> {
                        Text("المميزات: ${component.properties["title1"] ?: ""} | ${component.properties["title2"] ?: ""} | ${component.properties["title3"] ?: ""}", color = Color.LightGray, fontSize = 12.sp, maxLines = 1)
                    }
                    ComponentType.TEXT_BLOCK -> {
                        Text("العنوان: ${component.properties["title"] ?: "بلا عنوان"}", color = Color.LightGray, fontSize = 12.sp)
                        Text("المحتوى: ${component.properties["content"] ?: ""}", color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    ComponentType.IMAGE_BLOCK -> {
                        Text("العنوان التوضيحي: ${component.properties["caption"] ?: ""}", color = Color.LightGray, fontSize = 12.sp)
                        Text("رابط الصورة: ${component.properties["image_url"] ?: ""}", color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    ComponentType.BUTTON_LINK -> {
                        Text("نص الزر: ${component.properties["text"] ?: ""}", color = Color.LightGray, fontSize = 12.sp)
                        Text("الرابط: ${component.properties["url"] ?: ""}", color = Color.Gray, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    ComponentType.CARD_WIDGET -> {
                        Text("العنوان: ${component.properties["title"] ?: ""}", color = Color.LightGray, fontSize = 12.sp)
                        Text("السعر: ${component.properties["price"] ?: ""}", color = Color(0xFF00E676), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    ComponentType.CONTACT_FORM -> {
                        Text("العنوان الرئيسي: ${component.properties["title"] ?: ""}", color = Color.LightGray, fontSize = 12.sp)
                        Text("الزر: ${component.properties["btn_text"] ?: ""}", color = Color.Gray, fontSize = 11.sp)
                    }
                    ComponentType.FOOTER -> {
                        Text("التذييل: ${component.properties["text"] ?: ""}", color = Color.LightGray, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}


// --- Tab 2: HTML Code Editor View ---

@Composable
fun CodeEditorTab(
    html: String,
    onSync: (String) -> Unit
) {
    var editedCode by remember(html) { mutableStateOf(html) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onSync(editedCode) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("sync_code_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("مزامنة التعديلات البرمجية 🔄", color = Color(0xFF0F0F1A), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }

            Text(
                "محرر شفرة المصدر (HTML5 / CSS3):",
                color = Color.LightGray,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = editedCode,
            onValueChange = { editedCode = it },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("code_editor_input"),
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = Color(0xFFE2E8F0)
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Default
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFF2E2E4A),
                focusedBorderColor = Color(0xFF6200EE),
                unfocusedContainerColor = Color(0xFF070211),
                focusedContainerColor = Color(0xFF070211)
            )
        )
    }
}


// --- Tab 3: Web Preview View (WebView with responsive toggler) ---

@Composable
fun LivePreviewTab(html: String) {
    val context = LocalContext.current
    var isMobileLayout by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Control Row: Toggle Mobile vs Desktop, copy and share action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(onClick = {
                    try {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, html)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "تصدير ومشاركة كود HTML"))
                    } catch (e: Exception) {
                        Toast.makeText(context, "فشل تصدير الكود", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Icon(Icons.Default.Share, contentDescription = "تصدير الكود", tint = Color(0xFF90CAF9))
                }
                IconButton(onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("HTML Code", html)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "تم نسخ الكود للحافظة! 📋", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Home, contentDescription = "نسخ الكود", tint = Color(0xFF00E676)) // Standard icon, lets use standard copy icon representation if we have, Menu/Home/Mail
                }
            }

            // Mobile vs Desktop toggle switch
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1F1A3A))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (!isMobileLayout) Color(0xFF6200EE) else Color.Transparent)
                        .clickable { isMobileLayout = false }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("شاشة ديسكتوب 💻", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isMobileLayout) Color(0xFF6200EE) else Color.Transparent)
                        .clickable { isMobileLayout = true }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("شاشة جوال 📱", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Browser Preview Container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E2F))
                .border(2.dp, Color(0xFF2D2D44), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            val contentWidth = if (isMobileLayout) 360.dp else Modifier.fillMaxWidth()
            
            Surface(
                modifier = if (isMobileLayout) {
                    Modifier
                        .width(360.dp)
                        .fillMaxHeight()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(6.dp, Color(0xFF111116), RoundedCornerShape(24.dp))
                } else {
                    Modifier.fillMaxSize()
                },
                color = Color.White
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.useWideViewPort = true
                            settings.loadWithOverviewMode = true
                            webViewClient = WebViewClient()
                        }
                    },
                    update = { webView ->
                        webView.loadDataWithBaseURL(null, html, "text/html", "utf-8", null)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}


// --- Tab 4: Gemini AI Assistant Tab ---

@Composable
fun GeminiAITab(
    html: String,
    isLoading: Boolean,
    onGenerate: (String) -> Unit,
    onApplyPreset: (String) -> Unit
) {
    var aiPrompt by remember { mutableStateOf("") }

    val presetInstructions = listOf(
        Pair("تحويل الألوان لسمة ليلية داكنة راقية 🌙", "تغيير نظام الألوان إلى سمة ليلية داكنة فاخرة (خلفيات بلون كحلي داكن أو أسود، نصوص بيضاء وألوان مميزة براقة كالتيل أو الأخضر الفسفوري)"),
        Pair("إضافة قسم اتصل بنا متطور ✉️", "أضف قسماً للتواصل المطور CONTACT_FORM كاملاً في نهاية الصفحة قبل التذييل مباشرة"),
        Pair("ترجمة الصفحة كاملة للعربية الفصحى 🗣️", "قم بترجمة جميع النصوص الموجودة في الصفحة إلى اللغة العربية الفصحى البليغة والمحفزة على اتخاذ إجراء"),
        Pair("إضافة بطاقة منتج مميز لزيادة المبيعات 📦", "أضف بطاقة منتج مميز CARD_WIDGET تحتوي على عنوان وسعر وزر شراء مميز تحت قسم المميزات")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color(0xFF00E676))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "جاري الاتصال بـ Gemini لتوليد الأكواد البرمجية وتصميم الصفحة بصرياً...",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "مساعد ويب سكيتش الذكي ✨ (مدعوم بـ Gemini)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "اكتب فكرة موقعك باللغة العربية وسيقوم Gemini ببرمجته وبنائه لك بصرياً وكودياً في ثوانٍ معدودة:",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 1.6.sp,
                    textAlign = TextAlign.Right
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = aiPrompt,
                    onValueChange = { aiPrompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("ai_prompt_input"),
                    placeholder = {
                        Text(
                            "مثال: صمم لي صفحة هبوط لمتجر بيع عطور طبيعية، مع شريط تصفح، وواجهة رئيسية جذابة بصورة فخمة، وقسم تواصل وعرض للمميزات الشحن والضمان.",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    },
                    textStyle = TextStyle(fontSize = 13.sp, color = Color.White, textAlign = TextAlign.Right),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color(0xFF2E2E4A),
                        focusedBorderColor = Color(0xFF00E676),
                        unfocusedContainerColor = Color(0xFF140F2D),
                        focusedContainerColor = Color(0xFF140F2D)
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (aiPrompt.trim().isNotEmpty()) {
                            onGenerate(aiPrompt)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("ai_generate_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("توليد وتصميم الموقع بالذكاء الاصطناعي 🚀", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "تعديلات وتأثيرات سريعة جاهزة (بلمسة واحدة):",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(presetInstructions.size) { index ->
                        val (label, prompt) = presetInstructions[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onApplyPreset(prompt) }
                                .testTag("ai_preset_$index"),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1A3C)),
                            border = BorderStroke(1.dp, Color(0xFF382F6B))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    label,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Right
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


// --- Property Inspector Dialog (Web Component visual editor) ---

@Composable
fun PropertyInspectorDialog(
    component: WebComponent,
    onDismiss: () -> Unit,
    onCancel: () -> Unit,
    onSave: (Map<String, String>) -> Unit
) {
    val editableProps = remember(component) { mutableStateMapOf<String, String>().apply { putAll(component.properties) } }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "تعديل خصائص: ${component.type.arabicName} ✏️",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val entries = editableProps.entries.toList()
                items(entries.size) { index ->
                    val entry = entries[index]
                    val propKey = entry.key
                    val propVal = entry.value

                    val localizedLabel = when (propKey) {
                        "logo" -> "الشعار أو الرمز التعبيري (Emoji)"
                        "title" -> "العنوان الرئيسي"
                        "subtitle" -> "العنوان الفرعي"
                        "content" -> "محتوى النص بالتفصيل"
                        "btn_text" -> "نص زر الإجراء"
                        "btn_link" -> "رابط زر الإجراء"
                        "image_url" -> "رابط عنوان الصورة (URL)"
                        "caption" -> "الوصف التوضيحي أسفل الصورة"
                        "height" -> "ارتفاع الصورة (مثال: 300px)"
                        "link1" -> "رابط التنقل الأول"
                        "link2" -> "رابط التنقل الثاني"
                        "link3" -> "رابط التنقل الثالث"
                        "title1" -> "الميزة الأولى: العنوان"
                        "desc1" -> "الميزة الأولى: الوصف"
                        "title2" -> "الميزة الثانية: العنوان"
                        "desc2" -> "الميزة الثانية: الوصف"
                        "title3" -> "الميزة الثالث: العنوان"
                        "desc3" -> "الميزة الثالث: الوصف"
                        "price" -> "سعر المنتج المعروض"
                        "desc" -> "وصف البطاقة أو النموذج"
                        "placeholder_name" -> "تلميح حقل الاسم"
                        "placeholder_email" -> "تلميح حقل البريد"
                        "bg_color" -> "لون الخلفية (كود سداسي Hex)"
                        "text_color" -> "لون الخط (كود سداسي Hex)"
                        "align" -> "محاذاة النص (center, right, left)"
                        else -> propKey
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(localizedLabel, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        if (propKey.endsWith("color")) {
                            // Specialized Hex Color entry with pre-set buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color preview circle
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(
                                            try {
                                                Color(android.graphics.Color.parseColor(propVal))
                                            } catch (e: Exception) {
                                                Color.Gray
                                            }
                                        )
                                        .border(1.dp, Color.LightGray, CircleShape)
                                )
                                OutlinedTextField(
                                    value = propVal,
                                    onValueChange = { editableProps[propKey] = it },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("prop_input_$propKey"),
                                    singleLine = true,
                                    textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                )
                            }
                            // Color preset shortcuts for fast branding
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("#6200EE", "#E91E63", "#00E676", "#FF5722", "#1F1F2E", "#111827", "#FFFFFF").forEach { colorHex ->
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color(android.graphics.Color.parseColor(colorHex)))
                                            .border(1.dp, Color.White, CircleShape)
                                            .clickable { editableProps[propKey] = colorHex }
                                    )
                                }
                            }
                        } else {
                            OutlinedTextField(
                                value = propVal,
                                onValueChange = { editableProps[propKey] = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("prop_input_$propKey"),
                                textStyle = TextStyle(textAlign = TextAlign.Right, textDirection = androidx.compose.ui.text.style.TextDirection.Rtl)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(editableProps.toMap()) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE)),
                modifier = Modifier.testTag("save_properties_btn")
            ) {
                Text("حفظ وتحديث ✅", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("إلغاء والتراجع", color = Color.Gray)
            }
        }
    )
}

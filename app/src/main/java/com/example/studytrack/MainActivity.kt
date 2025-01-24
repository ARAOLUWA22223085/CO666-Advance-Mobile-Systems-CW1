package com.example.studytrack

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.CalendarView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studytrack.ui.theme.StudyTrackTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class Event(
    val id: String = "",
    val name: String,
    val description: String,
    val date: Long
)

data class Module(
    val id: String,
    val name: String,
    val code: String
)

data class StudySession(
    val id: String,
    val timeSpent: Int, // Time in minutes
    val date: String // Date in YYYY-MM-DD format
)


@Suppress("NestedLambdaShadowedImplicitParameter")
class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        setContent {
            StudyTrackTheme {
                StudyTrackNavHost()
            }
        }
    }

    // Function to define navigation between screens
    @Composable
    fun StudyTrackNavHost() {
        val navController = rememberNavController()

        NavHost(navController = navController, startDestination = "intro") {
            composable("intro") {
                IntroScreen(
                    onRegisterClick = { navController.navigate("register") },
                    onLoginClick = { navController.navigate("login") }
                )
            }
            composable("register") {
                ProfileSetupScreen(onRegistrationSuccess = {
                    navController.navigate("main")
                })
            }
            composable("login") {
                LoginScreen(onLoginSuccess = {
                    navController.navigate("main")
                })
            }
            composable("main") {
                MainAppScreen(navController)
            }
            composable("subject_course_management") {
                SubjectCourseManagementScreen()
            }
            composable("study_schedule_planner") {
                StudySchedulePlannerScreen()
            }
            composable("pomodoro_timer") {
                PomodoroTimerScreen()
            }
            composable("progress_tracker_reports") {
                ProgressTrackerReportsScreen()
            }
        }
    }

    // Intro Screen
    @Composable
    fun IntroScreen(onRegisterClick: () -> Unit, onLoginClick: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to StudyTrack",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { onRegisterClick() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register an Account")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onLoginClick() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        }
    }

    // Profile Setup Screen
    @Composable
    fun ProfileSetupScreen(onRegistrationSuccess: () -> Unit) {
        var email by remember { mutableStateOf("") }
        var name by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Register",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    registerUser(email, name, password, onRegistrationSuccess) { error ->
                        errorMessage = error
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Login Screen
    @Composable
    fun LoginScreen(onLoginSuccess: () -> Unit) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    loginUser(email, password, onLoginSuccess) { error ->
                        errorMessage = error
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
            errorMessage?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Main app screen with navigation options
    @Composable
    fun MainAppScreen(navController: androidx.navigation.NavController) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to StudyTrack!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { navController.navigate("subject_course_management") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Subject & Course Management")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("study_schedule_planner") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Study Schedule Planner")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("pomodoro_timer") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pomodoro Timer")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("progress_tracker_reports") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Progress Tracker & Reports")
            }
        }
    }

    @SuppressLint("MutableCollectionMutableState")
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SubjectCourseManagementScreen() {
        // Firebase instances
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        // State to manage the list of modules
        var modules by remember { mutableStateOf(mutableListOf<Pair<String, String>>()) }
        var moduleIds by remember { mutableStateOf(mutableListOf<String>()) }

        // State for the input fields
        var moduleName by remember { mutableStateOf("") }
        var moduleCode by remember { mutableStateOf("") }
        var isDialogOpen by remember { mutableStateOf(false) }
        var selectedModuleId by remember { mutableStateOf<String?>(null) }
        var isEditMode by remember { mutableStateOf(false) }

        // Load modules for the current user
        LaunchedEffect(Unit) {
            val user = auth.currentUser
            user?.let {
                val userId = it.uid
                db.collection("users").document(userId).collection("modules")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        modules = mutableListOf<Pair<String, String>>().apply {
                            addAll(snapshot.documents.mapNotNull { doc ->
                                val name = doc.getString("moduleName")
                                val code = doc.getString("moduleCode")
                                if (name != null && code != null) Pair(name, code) else null
                            })
                        }
                        moduleIds = snapshot.documents.map { it.id }.toMutableList()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error loading modules", e)
                    }
            }
        }


        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Subject & Course Management") })
            },
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // Button to open the dialog for adding a module
                    Button(
                        onClick = {
                            isEditMode = false
                            moduleName = ""
                            moduleCode = ""
                            isDialogOpen = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add a Module")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Display list of added modules
                    if (modules.isEmpty()) {
                        Text("No modules added yet.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        modules.forEachIndexed { index, module ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.primary)
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Module Name: ${module.first}",
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            "Module Code: ${module.second}",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    // Menu button for options
                                    IconButton(onClick = {
                                        selectedModuleId = moduleIds[index]
                                        moduleName = module.first
                                        moduleCode = module.second
                                        isDialogOpen = true
                                        isEditMode = true
                                    }) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        )

        // Dialog for adding or editing a module
        if (isDialogOpen) {
            AlertDialog(
                onDismissRequest = { isDialogOpen = false },
                title = { Text(if (isEditMode) "Edit Module" else "Add a Module") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = moduleName,
                            onValueChange = { moduleName = it },
                            label = { Text("Module Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = moduleCode,
                            onValueChange = { moduleCode = it },
                            label = { Text("Module Code") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val user = auth.currentUser
                            user?.let {
                                val userId = it.uid
                                if (isEditMode) {
                                    // Update module in Firestore
                                    selectedModuleId?.let { moduleId ->
                                        db.collection("users").document(userId)
                                            .collection("modules")
                                            .document(moduleId)
                                            .set(
                                                mapOf(
                                                    "moduleName" to moduleName,
                                                    "moduleCode" to moduleCode
                                                )
                                            )
                                            .addOnSuccessListener {
                                                val index = moduleIds.indexOf(moduleId)
                                                if (index >= 0) {
                                                    modules[index] = Pair(moduleName, moduleCode)
                                                }
                                                isDialogOpen = false
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e("Firestore", "Error updating module", e)
                                            }
                                    }
                                } else {
                                    // Add module to Firestore
                                    db.collection("users").document(userId).collection("modules")
                                        .add(
                                            mapOf(
                                                "moduleName" to moduleName,
                                                "moduleCode" to moduleCode
                                            )
                                        )
                                        .addOnSuccessListener { documentReference ->
                                            modules.add(Pair(moduleName, moduleCode))
                                            moduleIds.add(documentReference.id)
                                            isDialogOpen = false
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Firestore", "Error adding module", e)
                                        }
                                }
                            }
                        }
                    ) {
                        Text(if (isEditMode) "Save" else "Add")
                    }
                },
                dismissButton = {
                    if (isEditMode) {
                        TextButton(onClick = {
                            // Delete module
                            val user = auth.currentUser
                            user?.let {
                                val userId = it.uid
                                selectedModuleId?.let { moduleId ->
                                    db.collection("users").document(userId).collection("modules")
                                        .document(moduleId)
                                        .delete()
                                        .addOnSuccessListener {
                                            val index = moduleIds.indexOf(moduleId)
                                            if (index >= 0) {
                                                modules.removeAt(index)
                                                moduleIds.removeAt(index)
                                            }
                                            isDialogOpen = false
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("Firestore", "Error deleting module", e)
                                        }
                                }
                            }
                        }) {
                            Text("Delete")
                        }
                    }
                    TextButton(onClick = { isDialogOpen = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }

    @Composable
    fun StudySchedulePlannerScreen() {
        val calendar = remember { Calendar.getInstance() }
        val events = remember { mutableStateListOf<Event>() }
        var selectedDate by remember { mutableStateOf<Date?>(null) }
        var isDialogOpen by remember { mutableStateOf(false) }
        var eventDescription by remember { mutableStateOf("") }
        var editEvent: Event? by remember { mutableStateOf(null) }

        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        // Fetch events from Firebase
        LaunchedEffect(Unit) {
            userId?.let {
                db.collection("users").document(it).collection("events")
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            Log.e("Firebase", "Error fetching events: ${error.message}")
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            val fetchedEvents = snapshot.documents.mapNotNull { doc ->
                                val id = doc.id
                                val name = doc.getString("name")
                                val description = doc.getString("description")
                                val date = doc.getLong("date")
                                if (name != null && description != null && date != null) {
                                    Event(id, name, description, date)
                                } else null
                            }
                            events.clear()
                            events.addAll(fetchedEvents)
                        }
                    }
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Scrollable Content
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                // Calendar View
                AndroidView(
                    factory = { context ->
                        CalendarView(context).apply {
                            setOnDateChangeListener { _, year, month, dayOfMonth ->
                                calendar.set(year, month, dayOfMonth)
                                selectedDate = calendar.time
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    update = { calendarView ->
                        // Add coloured dots for events
                        events.forEach { event ->
                            val eventCalendar = Calendar.getInstance().apply {
                                time = Date(event.date)
                            }
                            if (calendarView.date == eventCalendar.timeInMillis) {
                                calendarView.invalidate()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Add Study Date Button
                Button(onClick = {
                    selectedDate = calendar.time
                    eventDescription = ""
                    editEvent = null
                    isDialogOpen = true
                }) {
                    Text("Add Study Date")
                }

                // Dialog for Adding/Editing Events
                if (isDialogOpen) {
                    AlertDialog(
                        onDismissRequest = { isDialogOpen = false },
                        title = { Text(if (editEvent == null) "Add Study Date" else "Edit Study Date") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = eventDescription,
                                    onValueChange = { eventDescription = it },
                                    label = { Text("Description") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(onClick = {
                                if (selectedDate != null && eventDescription.isNotBlank() && userId != null) {
                                    if (editEvent != null) {
                                        // Update existing event
                                        val updatedEvent = editEvent!!.copy(description = eventDescription)
                                        db.collection("users").document(userId).collection("events")
                                            .document(updatedEvent.id)
                                            .set(
                                                mapOf(
                                                    "name" to updatedEvent.name,
                                                    "description" to updatedEvent.description,
                                                    "date" to updatedEvent.date
                                                )
                                            )
                                            .addOnSuccessListener {
                                                val index = events.indexOf(editEvent!!)
                                                if (index != -1) {
                                                    events[index] = updatedEvent
                                                }
                                                isDialogOpen = false
                                            }
                                            .addOnFailureListener { error ->
                                                Log.e("Firebase", "Error updating event: ${error.message}")
                                            }
                                    } else {
                                        // Add new event
                                        val newEventId = System.currentTimeMillis().toString()
                                        val newEvent = Event(
                                            id = newEventId,
                                            name = "Study Date",
                                            description = eventDescription,
                                            date = selectedDate!!.time
                                        )
                                        db.collection("users").document(userId).collection("events")
                                            .document(newEventId)
                                            .set(
                                                mapOf(
                                                    "name" to newEvent.name,
                                                    "description" to newEvent.description,
                                                    "date" to newEvent.date
                                                )
                                            )
                                            .addOnSuccessListener {
                                                events.add(newEvent)
                                                isDialogOpen = false
                                            }
                                            .addOnFailureListener { error ->
                                                Log.e("Firebase", "Error saving event: ${error.message}")
                                            }
                                    }
                                } else {
                                    // Provide feedback if the input is invalid
                                    Log.e("StudySchedule", "Failed to save event: Invalid input")
                                }
                            }) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { isDialogOpen = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // List of Events with Coloured Dots
                if (events.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    events.forEach { event ->
                        val eventColor = MaterialTheme.colorScheme.primary // Fixed placement
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .border(1.dp, eventColor)
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(eventColor, shape = CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Date: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(event.date))}",
                                        style = MaterialTheme.typography.bodyLarge)
                                    Text("Description: ${event.description}",
                                        style = MaterialTheme.typography.bodyMedium)
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                TextButton(onClick = {
                                    editEvent = event
                                    eventDescription = event.description
                                    selectedDate = Date(event.date)
                                    isDialogOpen = true
                                }) {
                                    Text("Edit")
                                }
                                TextButton(onClick = {
                                    userId?.let { uid ->
                                        db.collection("users").document(uid).collection("events")
                                            .document(event.id)
                                            .delete()
                                            .addOnSuccessListener {
                                                events.remove(event)
                                            }
                                            .addOnFailureListener { error ->
                                                Log.e("Firebase", "Error deleting event: ${error.message}")
                                            }
                                    }
                                }) {
                                    Text("Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    @SuppressLint("DefaultLocale")
    @Composable
    fun PomodoroTimerScreen() {
        var timeLeft by remember { mutableLongStateOf(50 * 60 * 1000L) } // Default 50 minutes in milliseconds
        var isRunning by remember { mutableStateOf(false) }
        var isBreak by remember { mutableStateOf(false) }
        var workDuration by remember { mutableLongStateOf(50 * 60 * 1000L) } // Default work duration: 50 minutes
        var breakDuration by remember { mutableLongStateOf(10 * 60 * 1000L) } // Default break duration: 10 minutes
        var is25Cycle by remember { mutableStateOf(false) } // Tracks which cycle is active

        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        fun playSound() {
            val mediaPlayer = MediaPlayer.create(context, R.raw.notification_sound) // Replace with your sound file
            mediaPlayer.setOnCompletionListener { it.release() }
            mediaPlayer.start()
        }

        fun startTimer() {
            isRunning = true
            scope.launch {
                while (timeLeft > 0 && isRunning) {
                    delay(1000L) // 1 second
                    timeLeft -= 1000L
                }
                if (timeLeft <= 0) {
                    playSound()
                    if (!isBreak) {
                        // Transition to break timer
                        timeLeft = breakDuration
                        isBreak = true
                    } else {
                        // End break and reset to work timer
                        timeLeft = workDuration
                        isBreak = false
                    }
                    if (isRunning) startTimer() // Automatically start the next timer
                }
            }
        }

        fun resetTimer() {
            isRunning = false
            timeLeft = if (isBreak) breakDuration else workDuration
        }

        fun switchCycles() {
            is25Cycle = !is25Cycle
            if (is25Cycle) {
                workDuration = 25 * 60 * 1000L // Set work duration to 25 minutes
                breakDuration = 5 * 60 * 1000L // Set break duration to 5 minutes
            } else {
                workDuration = 50 * 60 * 1000L // Set work duration to 50 minutes
                breakDuration = 10 * 60 * 1000L // Set break duration to 10 minutes
            }
            resetTimer()
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = String.format(
                    "%02d:%02d",
                    (timeLeft / 1000) / 60,
                    (timeLeft / 1000) % 60
                ),
                style = MaterialTheme.typography.headlineLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(onClick = {
                    if (!isRunning) startTimer()
                }) {
                    Text(if (isRunning) "Running" else "Start")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = { resetTimer() }) {
                    Text("Reset")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isBreak) "Break Time!" else "Work Time!",
                style = MaterialTheme.typography.bodyLarge,
                color = if (isBreak) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { switchCycles() }) {
                Text(if (is25Cycle) "Switch to 50/10 Cycle" else "Switch to 25/5 Cycle")
            }
        }
    }


    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    fun ProgressTrackerReportsScreen() {
        val db = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()
        val userId = auth.currentUser?.uid

        var modules by remember { mutableStateOf(listOf<Module>()) }
        var studySessions by remember { mutableStateOf(mapOf<String, MutableList<StudySession>>()) }
        var totalSessions by remember { mutableIntStateOf(0) }
        var selectedModuleId by remember { mutableStateOf<String?>(null) }

        // Load modules and study sessions from Firebase
        LaunchedEffect(userId) {
            if (userId != null) {
                // Fetch modules
                db.collection("users").document(userId).collection("modules")
                    .get()
                    .addOnSuccessListener { snapshot ->
                        val fetchedModules = snapshot.documents.mapNotNull { doc ->
                            val id = doc.id
                            val name = doc.getString("moduleName")
                            val code = doc.getString("moduleCode")
                            if (name != null && code != null) Module(id, name, code) else null
                        }
                        modules = fetchedModules

                        // Fetch study sessions for each module
                        val sessionsMap = mutableMapOf<String, MutableList<StudySession>>()
                        fetchedModules.forEach { module ->
                            db.collection("users").document(userId).collection("modules")
                                .document(module.id).collection("studySessions")
                                .get()
                                .addOnSuccessListener { sessionSnapshot ->
                                    val sessions = sessionSnapshot.documents.mapNotNull { sessionDoc ->
                                        val id = sessionDoc.id
                                        val timeSpent = sessionDoc.getLong("timeSpent")?.toInt()
                                        val date = sessionDoc.getString("date")
                                        if (timeSpent != null && date != null) {
                                            StudySession(id, timeSpent, date)
                                        } else null
                                    }.toMutableList()
                                    sessionsMap[module.id] = sessions
                                    studySessions = sessionsMap
                                    totalSessions = sessionsMap.values.flatten().size
                                }
                        }
                    }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Progress Tracker & Reports") })
            },
            content = { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Total Study Sessions
                    Text(
                        text = "Total Study Sessions: $totalSessions",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // List Modules and Study Sessions
                    modules.forEach { module ->
                        ModuleProgressCard(
                            module = module,
                            studySessions = studySessions[module.id] ?: mutableListOf(),
                            isSelected = module.id == selectedModuleId,
                            onSelect = { selectedModuleId = module.id },
                            onAddSession = {
                                addStudySession(db, userId, module.id) { newSession ->
                                    val updatedSessions = studySessions[module.id]?.apply {
                                        add(newSession)
                                    } ?: mutableListOf(newSession)
                                    studySessions = studySessions.toMutableMap().apply {
                                        this[module.id] = updatedSessions
                                    }
                                    totalSessions += 1
                                }
                            },
                            onRemoveSession = {
                                removeStudySession(db, userId, module.id) { removedSession ->
                                    val updatedSessions = studySessions[module.id]?.apply {
                                        remove(removedSession)
                                    } ?: mutableListOf()
                                    studySessions = studySessions.toMutableMap().apply {
                                        this[module.id] = updatedSessions
                                    }
                                    totalSessions -= 1
                                }
                            }
                        )
                    }
                }
            }
        )
    }

    @Composable
    fun ModuleProgressCard(
        module: Module,
        studySessions: List<StudySession>,
        isSelected: Boolean,
        onSelect: () -> Unit,
        onAddSession: () -> Unit,
        onRemoveSession: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .clickable { onSelect() },
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Module: ${module.name} (${module.code})",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Study Sessions: ${studySessions.size}",
                    style = MaterialTheme.typography.bodyLarge
                )

                if (isSelected) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(onClick = onAddSession) {
                            Text("+")
                        }
                        Button(onClick = onRemoveSession) {
                            Text("-")
                        }
                    }
                }
            }
        }
    }

    // Add a study session and pass the new session back
    private fun addStudySession(
        db: FirebaseFirestore,
        userId: String?,
        moduleId: String,
        onComplete: (StudySession) -> Unit
    ) {
        if (userId != null) {
            val newSession = hashMapOf(
                "timeSpent" to 60, // Default 1-hour session
                "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            )
            db.collection("users").document(userId).collection("modules")
                .document(moduleId).collection("studySessions")
                .add(newSession)
                .addOnSuccessListener { documentRef ->
                    val session = StudySession(
                        id = documentRef.id,
                        timeSpent = 60,
                        date = newSession["date"] as String
                    )
                    onComplete(session)
                }
                .addOnFailureListener { e -> Log.e("Firestore", "Error adding session", e) }
        }
    }

    // Remove the most recent study session and pass the removed session back
    private fun removeStudySession(
        db: FirebaseFirestore,
        userId: String?,
        moduleId: String,
        onComplete: (StudySession) -> Unit
    ) {
        if (userId != null) {
            db.collection("users").document(userId).collection("modules")
                .document(moduleId).collection("studySessions")
                .orderBy("date", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener { snapshot ->
                    val sessionDoc = snapshot.documents.firstOrNull()
                    sessionDoc?.let { doc ->
                        val removedSession = StudySession(
                            id = doc.id,
                            timeSpent = doc.getLong("timeSpent")?.toInt() ?: 60,
                            date = doc.getString("date") ?: ""
                        )
                        doc.reference.delete().addOnSuccessListener {
                            onComplete(removedSession)
                        }
                    }
                }
                .addOnFailureListener { e -> Log.e("Firestore", "Error removing session", e) }
        }
    }



    // Firebase Functions
    private fun registerUser(
        email: String,
        name: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods
                    if (signInMethods.isNullOrEmpty()) {
                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { createUserTask ->
                                if (createUserTask.isSuccessful) {
                                    val userId = auth.currentUser?.uid
                                    val user = hashMapOf("name" to name, "email" to email)
                                    userId?.let {
                                        db.collection("users").document(it)
                                            .set(user)
                                            .addOnSuccessListener { onSuccess() }
                                            .addOnFailureListener { e ->
                                                onError("Failed to save user data: ${e.message}")
                                            }
                                    }
                                } else {
                                    onError(
                                        createUserTask.exception?.localizedMessage
                                            ?: "Registration failed."
                                    )
                                }
                            }
                    } else {
                        onError("Email already in use.")
                    }
                } else {
                    onError(task.exception?.localizedMessage ?: "Failed to check email.")
                }
            }
    }

    private fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val auth = FirebaseAuth.getInstance()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("FirebaseAuth", "Login successful")
                    onSuccess()
                } else {
                    onError(
                        task.exception?.localizedMessage ?: "Login failed. Check your credentials."
                    )
                }
            }
    }
}



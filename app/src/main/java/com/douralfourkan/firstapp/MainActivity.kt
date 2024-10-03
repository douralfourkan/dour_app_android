package com.douralfourkan.firstapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.Button
import android.widget.EditText
import android.view.View
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.TextView
import android.widget.Toast
import android.widget.RadioGroup
import android.widget.RadioButton
import android.widget.ScrollView
import android.widget.LinearLayout
import android.widget.TableLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import android.widget.TableRow
import android.graphics.Color
import android.widget.HorizontalScrollView
import android.text.TextUtils
import android.view.Gravity
import android.graphics.Typeface
import android.widget.Switch
import com.douralfourkan.firstapp.LoadingCircleOnImage
import androidx.appcompat.app.AlertDialog
import com.douralfourkan.firstapp.PieChartView
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import java.util.Calendar
import android.app.DatePickerDialog
import android.widget.ImageView
import java.util.Locale
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.animation.AnimatorListenerAdapter
import android.animation.Animator
// time 1sec initial
import android.os.Handler
import android.os.Looper
import androidx.appcompat.widget.PopupMenu
// calculate age
import java.text.SimpleDateFormat
import java.util.*
import android.util.TypedValue
import android.os.Build
import android.content.Context
import com.google.gson.Gson
import java.util.concurrent.atomic.AtomicInteger
import com.google.android.material.textfield.TextInputEditText
import android.net.Uri
import android.content.Intent
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.room.Room
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.*
import androidx.room.RoomDatabase
import androidx.lifecycle.lifecycleScope
import com.google.gson.reflect.TypeToken
import com.google.gson.JsonParser
import org.json.JSONObject
import com.google.gson.JsonObject
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type
import com.google.gson.JsonSyntaxException
import kotlin.collections.ArrayList
import android.widget.CheckBox
class MainActivity : ComponentActivity() {
    ////////////////////////////////////////////////////////////////////////////////////////////////   Global variables
    /////////////////////////////////////////////////// all
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var currentLayout = "main"
    private var currentUser = ""
    private var currentUserStatus = "student"
    private var currentBranch =""
    var grade_Level_or_History = ""
    data class Teacher(
        val teacherID: String,
        val teacherName: String,
        val section: String,
        val branch: String,
        val teacherGender: String,
        val students: List<Student>,
    )
    private val database = FirebaseDatabase.getInstance("https://douralfourkan-6358c-default-rtdb.europe-west1.firebasedatabase.app/")
    /////////////////////////////////////////////////// Login
    private var userStatus: String = ""
    private var userBranch: String = ""
    data class AttendanceItem(val date: String, val day: String, val status: Int)
    data class Student(
        val id: Int,
        val name: String,
        val gender :String,
        val birthday: String,
        val section : String,
        val branch : String,
        val teacherName: String,
        val gradeLevel: List<Int>,
        var gradeHistory :List<Triple<Int, Int, Int>>,
        val attendanceHistory: MutableList<AttendanceItem>,
    )
    ///////////////////////////////////////////////////  Director
    var all_table_data =""
    var updateInterval = 1000L
    private val handler = Handler()
    /////////////////////////////////////////////////// Supervisor
    var all_table_supervisor =""
    ////////////////////////////////////////////////// Teacher
    var GlobalstudentspinnerIndex = 0
    var all_table_teacher = ""
    var teacher_section = ""
    ////////////////////////////////////////////////// Student
    var all_table_student = ""
    var student_section = ""
    ////////////////////////////////////////////////// Admin
    private var newStatus: String = ""
    private var selectedGender: String = ""
    private lateinit var editTextBirthday: EditText
    data class director_Users(val name: String, val password: String,val phone: String)
    ////////////////////////////////////////////////////////////////////////////////////////////////  ROOM - Json database
    @Entity(tableName = "branches_room")
    data class branchesEntity(
        @PrimaryKey
        val id: Int = 0,
        val branchesTable: String,
    )
    @Entity(tableName = "directors_room")
    data class directorsEntity(
        @PrimaryKey
        val id: Int = 0,
        val directorsTable: String,
    )
    @Dao
    interface branchesDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertbranches(branches: branchesEntity)
        @Query("SELECT branchesTable FROM branches_room WHERE id = :branchesID")
        suspend fun getbranchesTable(branchesID: Int): String?
        @Query("DELETE FROM branches_room")
        suspend fun removeAllBranches()
    }
    @Dao
    interface directorsDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertdirectors(directors: directorsEntity)
        @Query("SELECT directorsTable FROM directors_room WHERE id = :directorsID")
        suspend fun getdirectorsTable(directorsID: Int): String?
    }
    @Database(entities = [branchesEntity::class, directorsEntity::class], version = 1)
    abstract class AppDatabase : RoomDatabase() {
        abstract fun branchesDao(): branchesDao
        abstract fun directorsDao(): directorsDao
        companion object {
            private lateinit var instance: AppDatabase
            fun getDatabase(context: Context): AppDatabase {
                synchronized(this) {
                    if (!::instance.isInitialized) {
                        instance = createDatabase(context)
                    }
                    return instance
                }
            }
            private fun createDatabase(context: Context) =
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "room_database_v1-21"
                ).build()
        }
    }

    data class JR_Student(
        val info: JR_StudentInfo?,
        val attendance: Map<String, Int>?,
        var grade_history: JR_grade_history?,
        val grade_vector: List<Int>?,
        val messages: Map<String, String>?
    ) {
        constructor() : this(
            JR_StudentInfo("", "", "", "", ""), emptyMap(),
            JR_grade_history(), emptyList(), emptyMap()
        )
    }
    data class JR_grade_history(
        val dates: Map<String, JR_grade_history_item>?
    ) {
        constructor() : this(emptyMap())
    }
    data class JR_grade_history_item(
        val grade_history_item : Map<String, Int>?
    ){
        constructor() : this(emptyMap())
    }

    data class JR_StudentInfo(
        val birthday: String,
        val gender: String,
        val name: String,
        val password: String,
        val phone: String
    ){
        constructor() : this("", "", "", "", "")
    }
    data class JR_Teacher(
        val name: String,
        val password: String,
        val gender: String,
        val phone: String
    ) {
        constructor() : this("", "", "", "")
    }
    data class JR_Supervisor(
        val name: String,
        val password: String,
        val phone: String
    ){
        constructor() : this("", "", "")
    }
    data class JR_Section(
        val students: List<JR_Student?>?,
        val teachers: List<JR_Teacher?>?
    ) {
        constructor() : this(emptyList(), emptyList())
    }

    data class JR_Branch(
        val sections: Map<String, JR_Section?>,
        val supervisors: List<JR_Supervisor?>
    ){
        constructor() : this(emptyMap(), emptyList())
    }

    data class JR_SchoolData(
        val branches: Map<String, JR_Branch>
    ){
        constructor() : this(emptyMap())
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////   On create - Weare - explain - contact layouts
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.initial_layout)
        Handler(Looper.getMainLooper()).postDelayed({to_Login_Layout()}, 2000)}
    private fun  to_Weare_Layout(){
        setContentView(R.layout.weare_layout)
        currentLayout = "weare"
        currentUser = "ghest"
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            to_Login_Layout()
        }
    }
    private fun  to_explain_Layout(){
        setContentView(R.layout.explain_layout)
        currentLayout = "explain"
        currentUser = "ghest"
        currentUserStatus = "ghest"
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            to_Login_Layout()
        }
    }
    private fun  to_contact_Layout(){
        setContentView(R.layout.contact_layout)
        currentLayout = "contact"
        currentUser = "ghest"
        currentUserStatus = "ghest"
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            to_Login_Layout()
        }
        val facebookImageView: ImageView = findViewById(R.id.facebook)
        facebookImageView.setOnClickListener {
            val uri = Uri.parse("https://www.facebook.com/maahadelfourkan")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        val instaImageView: ImageView = findViewById(R.id.instagram)
        instaImageView.setOnClickListener {
            val uri = Uri.parse("https://www.instagram.com/maahad.alfurqan/")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        }
        val sendEmailButton = findViewById<Button>(R.id.send_email_button)
        sendEmailButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:douralfourkan@gmail.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Subject of your email")
            intent.putExtra(Intent.EXTRA_TEXT, "Body of your email")
            startActivity(Intent.createChooser(intent, "Choose an email app"))
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////   Log in layout
    private fun to_Login_Layout (){
        setContentView(R.layout.activity_main)
        window.navigationBarColor = resources.getColor((R.color.customButtonColor))
        currentLayout = "main"
        currentUser = "none"
        currentUserStatus = "none"
        userStatus = "none"
        userBranch = "none"
        val wearebtn: LinearLayout = findViewById(R.id.wearebtn)
        wearebtn.setOnClickListener{
            to_Weare_Layout()
        }
        val explainbtn : LinearLayout= findViewById(R.id.explainbtn)
        explainbtn.setOnClickListener{
            to_explain_Layout()
        }
        val contactbtn : LinearLayout= findViewById(R.id.contactbtn)
        contactbtn.setOnClickListener{
            to_contact_Layout()
        }
        /////////////////////////////////////////////////////////// Update the database Periodically

        val updateRunnable = object : Runnable {

            override fun run() {
                try {
                    if (currentLayout == "student_layout") {
                        val last_update_textView = findViewById<TextView>(R.id.last_update)
                        val lastUpdateDate_stored_string = readLASTUPDATEData_Student()
                        if (lastUpdateDate_stored_string != null) {
                            val lastUpdateDate_stored = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastUpdateDate_stored_string)
                            val formattedTimeDifference_stored_current =formatTimeDifference(System.currentTimeMillis() - lastUpdateDate_stored.time)
                            last_update_textView.text = formattedTimeDifference_stored_current
                        }
                    }

                if (currentLayout == "teacher_layout") {
                    val last_update_textView = findViewById<TextView>(R.id.last_update)
                    val lastUpdateDate_stored_string = readLASTUPDATEData_Teacher()
                    if (lastUpdateDate_stored_string != null) {
                        val lastUpdateDate_stored = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastUpdateDate_stored_string)
                        val formattedTimeDifference_stored_current =formatTimeDifference(System.currentTimeMillis() - lastUpdateDate_stored.time)
                        last_update_textView.text = formattedTimeDifference_stored_current
                    }
                }

                if (currentLayout == "supervisor_layout") {
                    val last_update_textView = findViewById<TextView>(R.id.last_update)
                    val lastUpdateDate_stored_string = readLASTUPDATEData_Supervisror()
                    if (lastUpdateDate_stored_string != null) {
                        val lastUpdateDate_stored = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastUpdateDate_stored_string)
                        val formattedTimeDifference_stored_current =formatTimeDifference(System.currentTimeMillis() - lastUpdateDate_stored.time)
                        last_update_textView.text = formattedTimeDifference_stored_current
                    }
                }

                if (currentLayout == "director_layout") {
                    val last_update_textView = findViewById<TextView>(R.id.last_update)
                    val lastUpdateDate_stored_string = readLASTUPDATEData()
                    if (lastUpdateDate_stored_string != null) {
                        val lastUpdateDate_stored = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(lastUpdateDate_stored_string)
                        val formattedTimeDifference_stored_current =formatTimeDifference(System.currentTimeMillis() - lastUpdateDate_stored.time)
                        last_update_textView.text = formattedTimeDifference_stored_current
                    }
                }
                handler.postDelayed(this, updateInterval)
                }
                catch (e: Exception) {
                    showToast(e.message.toString())
                }
            }
        }
        handler.postDelayed(updateRunnable, 0)

        /////////////// check if already logged in, if no, Read account data from local account data
        val mainloginButton: Button = findViewById(R.id.main_login_Button)
        val usernameEditText: EditText = findViewById(R.id.usernameEditText)
        val typeSpinner: Spinner = findViewById(R.id.mainstatuscheck)
        val branchSpinner: Spinner = findViewById(R.id.mainbranchcheck)
        val passwordEditText: TextInputEditText = findViewById(R.id.passwordEditText)
        val account_data = getSharedPreferences("account_login_data", Context.MODE_PRIVATE)
        val stored_username = account_data.getString("username", null)
        val stored_userType = account_data.getString("userType", null)
        val stored_userBranch = account_data.getString("userBranch", null)

        if (stored_username == null) {
            typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    userStatus = User_Type_Spinner_handleTypeSelection(position)
                    currentUserStatus = userStatus
                }
                override fun onNothingSelected(parent: AdapterView<*>) {
                }
            }
            mainloginButton.setOnClickListener {
                userBranch = branchSpinner.selectedItem.toString()
                val enteredName = usernameEditText.text?.toString()
                val enteredPass = passwordEditText.text?.toString()

                try {
                    if(enteredName.isNullOrBlank()||enteredName==""){
                        showToast("الرجاء إدخال الإسم أولا")
                    }
                    else{
                        handle_Online_loginButtonClick(
                            enteredName,
                            enteredPass?:"",
                            userStatus,
                            userBranch
                        )
                    }

                } catch (e: Exception) {
                    showToast(e.toString())
                }
            }
            // rotating the logo
            val imageView = findViewById<ImageView>(R.id.logoImageView)
            val rotationAnimator = ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 360f).apply {
                duration = 2000 // Set the duration of each rotation (in milliseconds)
            }
            val pauseDuration: Long = 1000 // Set the duration of the pause (in milliseconds) as Long
            val animatorSet = AnimatorSet().apply {
                play(rotationAnimator).before(ObjectAnimator.ofFloat(imageView, "rotationY", 0f, 0f).setDuration(pauseDuration))
            }
            animatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    imageView.postDelayed({
                        animatorSet.start()
                    }, pauseDuration)
                }
            })
            animatorSet.start()
        }
        else{
            userStatus = stored_userType.toString()
            userBranch = stored_userBranch.toString()
            handle_Offline_loginButtonClick(stored_username,userStatus, userBranch)
        }
    }
    private fun User_Type_Spinner_handleTypeSelection(position: Int): String {
        return when (position) {
            0 -> "student"
            1 -> "teacher"
            2 -> "supervisor"
            3 -> "director"
            4 -> "admin"
            else -> "unknown" // Handle other cases if needed
        }
    }
    private fun handle_Offline_loginButtonClick(username: String,
                                                status: String, branch: String) {
        when (status) {
            "admin" -> switchToAdminShowLayout(username)
            "student" -> to_Student_Layout(username, branch)
            "teacher" -> to_Teacher_Layout(username, branch, 0)
            "supervisor" -> to_Supervisor_Layout(username, branch)
            "director" -> to_Director_Layout(username)
            else -> {}
        }
    }
    private fun handle_Online_loginButtonClick(username: String, password: String,
                                               status: String, branch: String) {
        if (status == "admin") {
            checkifAdmin(database.getReference("admin"), username, password) { isAdmin ->
                if (isAdmin) {
                    saveaccount_data(username, password,  "admin", branch)
                    switchToAdminShowLayout(username)
                }
            }
        } else if (status == "student") {
            checkifStudent(username, password, branch) { isStudent ->
                if (isStudent) {
                    saveaccount_data(username, password,  "student", branch)
                    to_Student_Layout(username, branch)
                }
            }
        } else if (status == "teacher") {
            checkifTeacher(username, password, branch) { isTeacher ->
                if (isTeacher) {
                    saveaccount_data(username, password,  "teacher", branch)
                    to_Teacher_Layout(username, branch, 0)
                }
            }
        } else if (status == "supervisor") {
            checkifSupervisor(username, password, branch) { isSupervisor ->
                if (isSupervisor) {
                    saveaccount_data(username, password,  "supervisor", branch)
                    to_Supervisor_Layout(username, branch)
                }
            }
        } else if (status == "director") {
            checkifDirector(database.getReference("directors"), username, password) { isDirector ->
                if (isDirector) {
                    saveaccount_data(username, password,  "director", "")
                    to_Director_Layout(username)
                }
            }
        }
    }
    private fun saveaccount_data(username: String, password: String, userType: String,
                                 userBranch : String) {
        val account_data = getSharedPreferences("account_login_data", Context.MODE_PRIVATE)
        val editor = account_data.edit()
        editor.putString("username", username)
        editor.putString("password", password)
        editor.putString("userType", userType)
        editor.putString("userBranch", userBranch)
        editor.apply()
    }
    private fun checkifAdmin(table: DatabaseReference, username: String, password: String,
                             callback: (Boolean) -> Unit) {
        checkIf_admin_director_ExiststoLOGIN(table, username, password) { isAdmin ->
            callback(isAdmin)
        }
    }
    private fun checkifDirector(table: DatabaseReference, username: String, password: String,
                                callback: (Boolean) -> Unit) {
        checkIf_admin_director_ExiststoLOGIN(table, username, password) { isDirector ->
            callback(isDirector)
        }
    }
    private fun checkifStudent(username: String, password: String, branch: String,
                               callback: (Boolean) -> Unit) {
        checkIf_student_ExiststoLOGIN(username, password, branch) { isStudent ->
            callback(isStudent)
        }
    }
    private fun checkifTeacher(username: String, password: String, branch: String,
                               callback: (Boolean) -> Unit) {
        checkIf_teacher_ExiststoLOGIN(username, password, branch) { isTeacher ->
            callback(isTeacher)
        }
    }
    private fun checkifSupervisor(username: String, password: String, branch: String,
                                  callback: (Boolean) -> Unit) {
        checkIf_supervisor_ExiststoLOGIN(username, password, branch) { isSupervisor ->
            callback(isSupervisor)
        }
    }
    private fun checkIf_admin_director_ExiststoLOGIN(table: DatabaseReference, username: String,
                                                     password: String,
                                                     callback: (Boolean) -> Unit) {
        val query = table.orderByChild("name").equalTo(username)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val userSnapshot = dataSnapshot.children.first()
                    val storedPassword = userSnapshot.child("password").getValue(String::class.java)
                    if (storedPassword == password) {
                        callback(true)
                    } else {
                        val errorText: TextView = findViewById(R.id.errortext)
                        errorText.text = "كلمة المرور خاطئة"
                        callback(false)
                    }
                } else {
                    val errorText: TextView = findViewById(R.id.errortext)
                    errorText.text = "اسم المستخدم غير موجود"
                    callback(false)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                callback(false)
            }
        })
    }
    private fun checkIf_supervisor_ExiststoLOGIN(username: String, password: String, branch: String,
                                                 callback: (Boolean) -> Unit) {
        var found = false
        val branchesQuery = database.getReference("branches").orderByKey().equalTo(branch)
        branchesQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(branchNode: DataSnapshot) {
                if (branchNode.exists()) {
                    val supervisorsNode = branchNode.child(branch).child("supervisors")
                    for (supervisorSnapshot in supervisorsNode.children) {
                        val Fire_name = supervisorSnapshot.child("name").getValue(String::class.java)
                        val Fire_password = supervisorSnapshot.child("password").getValue(String::class.java)
                        if(Fire_name == username){
                            if (Fire_password == password) {
                                callback(true)
                            } else {
                                val errorText: TextView = findViewById(R.id.errortext)
                                errorText.text = "كلمة المرور خاطئة"
                                callback(false)
                            }
                            found = true
                            return
                        }
                    }
                    if (!found)
                    {
                        val errorText: TextView = findViewById(R.id.errortext)
                        errorText.text = "اسم المستخدم غير موجود"
                        callback(false)
                    }
                } else {
                    val errorText: TextView = findViewById(R.id.errortext)
                    errorText.text = "اسم المستخدم غير موجود"
                    callback(false)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }
    private fun checkIf_teacher_ExiststoLOGIN(username: String, password: String, branch: String,
                                              callback: (Boolean) -> Unit) {
        var found = false
        val errorText: TextView = findViewById(R.id.errortext)
        val branchesQuery = database.getReference("branches").orderByKey().equalTo(branch)
        branchesQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(branchNode: DataSnapshot) {
                val selectedBranchSnapshot = branchNode.child(branch)
                val sectionsNode = selectedBranchSnapshot.child("sections")
                for (section_i in sectionsNode.children) {
                    val teachersNode = section_i.child("teachers")
                    for (teacherSnapshot in teachersNode.children) {
                        val Fire_name = teacherSnapshot.child("name").getValue(String::class.java)
                        val Fire_password = teacherSnapshot.child("password").getValue(String::class.java)
                        if(Fire_name == username){
                            if (Fire_password == password) {
                                callback(true)
                            } else {
                                errorText.text = "كلمة المرور خاطئة"
                                callback(false)
                            }
                            found = true
                            return
                        }
                    }
                }
                if(!found){
                    errorText.text = "اسم المستخدم غير موجود"
                }
                callback(false)
            }
            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })

    }
    private fun checkIf_student_ExiststoLOGIN(username: String, password: String, branch: String,
                                              callback: (Boolean) -> Unit) {
        var found = false
        val errorText: TextView = findViewById(R.id.errortext)
        val branchesQuery = database.getReference("branches").orderByKey().equalTo(branch)
        branchesQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(branchNode: DataSnapshot) {
                if (branchNode.exists()) {
                    val sectionsNode = branchNode.child(branch).child("sections")
                    for (section_i in sectionsNode.children) {
                        val studentsNode = section_i.child("students")
                        for (studentSnapshot in studentsNode.children) {
                            val Fire_name = studentSnapshot.child("info").child("name").getValue(String::class.java)
                            val Fire_password = studentSnapshot.child("info").child("password").getValue(String::class.java)
                            if(Fire_name == username){
                                if (Fire_password == password) {
                                    callback(true)
                                } else {
                                    errorText.text = "كلمة المرور خاطئة"
                                    callback(false)
                                }
                                found = true
                                return
                            }
                        }
                    }
                    if(!found){
                        errorText.text = "اسم المستخدم غير موجود"
                    }
                    callback(false)
                } else {
                    errorText.text = "اسم المستخدم غير موجود"
                    callback(false)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }
    //////////////////////////////////////////////////////////////////////////////////////////////// Director
    ///////// load/save/update database
    fun readLASTUPDATEData(): String {
        val sharedPreferences = getSharedPreferences("director_last_update", Context.MODE_PRIVATE)
        val lastUpdate = sharedPreferences.getString("last_update", null)
        return lastUpdate.toString()
    }
    fun save_all_table(callback: (Boolean) -> Unit) {
        val stdQueryFire = database.getReference("branches")
        stdQueryFire.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val gson = Gson()
                val branchesMap = mutableMapOf<String, JR_Branch>()

                for (branchSnapshot in dataSnapshot.children) {
                    val branchName = branchSnapshot.key.toString()
                    val sectionMap = mutableMapOf<String, JR_Section>()

                    for (sectionSnapshot in branchSnapshot.child("sections").children) {
                        val sectionName = sectionSnapshot.key.toString()
                        val studentsList = mutableListOf<JR_Student?>()
                        val teachersList = mutableListOf<JR_Teacher?>()


                        for (studentSnapshot in sectionSnapshot.child("students").children) {
                            val studentData = studentSnapshot.getValue(JR_Student::class.java)
                            val gradeHistoryMap = mutableMapOf<String, JR_grade_history_item>()

                            for (grade_historySnapshot in studentSnapshot.child("grade_history").children) {
                                val date = grade_historySnapshot.key.toString()
                                val grade_history_item = mutableMapOf<String, Int>()

                                for (assignmentSnapshot in grade_historySnapshot.children) {
                                    val assignmentNumber = assignmentSnapshot.key.toString()
                                    val grade = assignmentSnapshot.getValue(Int::class.java) ?: 0 // Assuming default value is 0 if grade is null
                                    grade_history_item[assignmentNumber] = grade
                                }

                                gradeHistoryMap[date] = JR_grade_history_item(grade_history_item)
                            }

                            studentData?.grade_history = JR_grade_history(gradeHistoryMap) // Creating JR_grade_history instance
                            studentsList.add(studentData)
                        }


                        for (teacherSnapshot in sectionSnapshot.child("teachers").children) {
                            val teacherData = teacherSnapshot.getValue(JR_Teacher::class.java)
                            teachersList.add(teacherData)
                        }

                        val section = JR_Section(studentsList, teachersList)
                        sectionMap[sectionName] = section
                    }

                    val supervisorsList = mutableListOf<JR_Supervisor?>()
                    for (supervisorSnapshot in branchSnapshot.child("supervisors").children) {
                        val supervisorData = supervisorSnapshot.getValue(JR_Supervisor::class.java)
                        supervisorsList.add(supervisorData)
                    }

                    val branch = JR_Branch(sectionMap, supervisorsList)
                    branchesMap[branchName] = branch
                }

                lifecycleScope.launch {
                    try {
                        val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                        val schoolData = JR_SchoolData(branchesMap)
                        val jsonData = gson.toJson(schoolData)
                        val branchesEntity = branchesEntity(1, jsonData)
                        branchesDao.insertbranches(branchesEntity)
                        // Save date
                        val currentTime = System.currentTimeMillis()
                        val formattedDateTime =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                                Date(currentTime)
                            )
                        saveLASTUPDATEData(formattedDateTime)
                        callback(true)
                    } catch (e: Exception) {
                        showToast(e.message.toString())
                        callback(false)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled event if needed
                callback(false)
            }
        })
    }
    fun saveLASTUPDATEData(last_update: String) {
        val sharedPreferences = getSharedPreferences("director_last_update", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("last_update", last_update)
        editor.apply()
    }
    fun clear_Director_data() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@MainActivity)
                // Invoke the method to remove all directors
                database.branchesDao().removeAllBranches()
            } catch (e: Exception) {
                showToast("Failed to remove all branches: ${e.message}")
            }
        }
    }
    //////////  go to sub layouts
    private fun to_Director_Layout(username: String) {

        setContentView(R.layout.director_layout)
        currentLayout = "director_layout"
        currentUser = username
        currentUserStatus = "director"
        /////////////////////////////////////////////////////////////////////////////////////// Menu
        val toolbarTitle : TextView=findViewById(R.id.toolbarTitle)
        toolbarTitle.text="مدير"
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        val branchtopview: TextView = findViewById(R.id.branch)
        branchtopview.text = "مدير دور الفرقان للحفظ و التلاوة"
        val nametopview: TextView = findViewById(R.id.directorname)
        nametopview.text = username
        /////////////////////////////////////////////////////////////// load database  btns listrner
        AppDatabase.getDatabase(this@MainActivity)
        val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
        lifecycleScope.launch {
            try {
                val all_table = branchesDao.getbranchesTable(1)
                if(all_table==null){
                    save_all_table{_->
                        showToast("تم تحديث البيانات")
                        val update_btn = findViewById<Button>(R.id.updateBtn)
                        update_btn.setOnClickListener{
                            save_all_table {_->
                                showToast("تم تحديث البيانات")
                                // Statistics btn
                                val statisticsbtn: RelativeLayout = findViewById(R.id.directorstatisticsbtn)
                                statisticsbtn.setOnClickListener {
                                    lifecycleScope.launch {
                                        try {
                                            AppDatabase.getDatabase(this@MainActivity)
                                            val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                            val all_table = branchesDao.getbranchesTable(1)
                                            to_Director_Statistics_Layout(username, all_table.toString())
                                        } catch (e: Exception) {
                                            showToast(e.message.toString())
                                        }
                                    }
                                }
                                // Students Monitor btn
                                val monitoringstudents: RelativeLayout = findViewById(R.id.directormonitoringstudents)
                                monitoringstudents.setOnClickListener {
                                    lifecycleScope.launch {
                                        try {
                                            AppDatabase.getDatabase(this@MainActivity)
                                            val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                            all_table_data = branchesDao.getbranchesTable(1)?:""
                                            to_Director_Monitoring_Students(username, all_table_data)
                                        } catch (e: Exception) {
                                            showToast(e.message.toString())
                                        }
                                    }
                                }
                                // Sections Monitor btn
                                val monitoringSections: RelativeLayout = findViewById(R.id.monitoringsections)
                                monitoringSections.setOnClickListener {
                                    lifecycleScope.launch {
                                        try {
                                            AppDatabase.getDatabase(this@MainActivity)
                                            val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                            all_table_data = branchesDao.getbranchesTable(1)?:""
                                            to_Director_Monitoring_Sections(username, all_table_data)
                                        } catch (e: Exception) {
                                            showToast(e.message.toString())
                                        }
                                    }
                                }
                                // Sections topList btn
                                val toplist_btn: RelativeLayout = findViewById(R.id.toplist_btn)
                                toplist_btn.setOnClickListener {
                                    lifecycleScope.launch {
                                        try {
                                            AppDatabase.getDatabase(this@MainActivity)
                                            val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                            all_table_data = branchesDao.getbranchesTable(1)?:""
                                            to_Director_Toplist_Layout(username, all_table_data)
                                        } catch (e: Exception) {
                                            showToast(e.message.toString())
                                        }
                                    }
                                }
                            }
                        }
                        // Statistics btn
                        val statisticsbtn: RelativeLayout = findViewById(R.id.directorstatisticsbtn)
                        statisticsbtn.setOnClickListener {
                            lifecycleScope.launch {
                                try {
                                    AppDatabase.getDatabase(this@MainActivity)
                                    val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                    val all_table = branchesDao.getbranchesTable(1)
                                    to_Director_Statistics_Layout(username, all_table.toString())
                                } catch (e: Exception) {
                                    showToast(e.message.toString())
                                }
                            }
                        }
                        // Students Monitor btn
                        val monitoringstudents: RelativeLayout = findViewById(R.id.directormonitoringstudents)
                        monitoringstudents.setOnClickListener {
                            lifecycleScope.launch {
                                try {
                                    AppDatabase.getDatabase(this@MainActivity)
                                    val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                    all_table_data = branchesDao.getbranchesTable(1)?:""
                                    to_Director_Monitoring_Students(username, all_table_data)
                                } catch (e: Exception) {
                                    showToast(e.message.toString())
                                }
                            }
                        }
                        // Sections Monitor btn
                        val monitoringSections: RelativeLayout = findViewById(R.id.monitoringsections)
                        monitoringSections.setOnClickListener {
                            lifecycleScope.launch {
                                try {
                                    AppDatabase.getDatabase(this@MainActivity)
                                    val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                    all_table_data = branchesDao.getbranchesTable(1)?:""
                                    to_Director_Monitoring_Sections(username, all_table_data)
                                } catch (e: Exception) {
                                    showToast(e.message.toString())
                                }
                            }
                        }
                        // Sections topList btn
                        val toplist_btn: RelativeLayout = findViewById(R.id.toplist_btn)
                        toplist_btn.setOnClickListener {
                            lifecycleScope.launch {
                                try {
                                    AppDatabase.getDatabase(this@MainActivity)
                                    val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                    all_table_data = branchesDao.getbranchesTable(1)?:""
                                    to_Director_Toplist_Layout(username, all_table_data)
                                } catch (e: Exception) {
                                    showToast(e.message.toString())
                                }
                            }
                        }
                    }
                }
                else{
                    val last_update_data = readLASTUPDATEData()
                    val last_update_textview = findViewById<TextView>(R.id.last_update)
                    if (last_update_data != null) {
                        val lastUpdateDate_stored = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(last_update_data)
                        val formattedTimeDifference_stored_current =formatTimeDifference(System.currentTimeMillis() - lastUpdateDate_stored.time)
                        last_update_textview.text = formattedTimeDifference_stored_current
                    }
                    val update_btn = findViewById<Button>(R.id.updateBtn)
                    update_btn.setOnClickListener{
                        save_all_table {_->
                            showToast("تم تحديث البيانات")
                            // Statistics btn
                            val statisticsbtn: RelativeLayout = findViewById(R.id.directorstatisticsbtn)
                            statisticsbtn.setOnClickListener {
                                lifecycleScope.launch {
                                    try {
                                        AppDatabase.getDatabase(this@MainActivity)
                                        val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                        val all_table = branchesDao.getbranchesTable(1)
                                        to_Director_Statistics_Layout(username, all_table.toString())
                                    } catch (e: Exception) {
                                        showToast(e.message.toString())
                                    }
                                }
                            }
                            // Students Monitor btn
                            val monitoringstudents: RelativeLayout = findViewById(R.id.directormonitoringstudents)
                            monitoringstudents.setOnClickListener {
                                lifecycleScope.launch {
                                    try {
                                        AppDatabase.getDatabase(this@MainActivity)
                                        val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                        all_table_data = branchesDao.getbranchesTable(1)?:""
                                        to_Director_Monitoring_Students(username, all_table_data)
                                    } catch (e: Exception) {
                                        showToast(e.message.toString())
                                    }
                                }
                            }
                            // Sections Monitor btn
                            val monitoringSections: RelativeLayout = findViewById(R.id.monitoringsections)
                            monitoringSections.setOnClickListener {
                                lifecycleScope.launch {
                                    try {
                                        AppDatabase.getDatabase(this@MainActivity)
                                        val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                        all_table_data = branchesDao.getbranchesTable(1)?:""
                                        to_Director_Monitoring_Sections(username, all_table_data)
                                    } catch (e: Exception) {
                                        showToast(e.message.toString())
                                    }
                                }
                            }
                            // Sections topList btn
                            val toplist_btn: RelativeLayout = findViewById(R.id.toplist_btn)
                            toplist_btn.setOnClickListener {
                                lifecycleScope.launch {
                                    try {
                                        AppDatabase.getDatabase(this@MainActivity)
                                        val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                        all_table_data = branchesDao.getbranchesTable(1)?:""
                                        to_Director_Toplist_Layout(username, all_table_data)
                                    } catch (e: Exception) {
                                        showToast(e.message.toString())
                                    }
                                }
                            }
                        }
                    }
                    // Statistics btn
                    val statisticsbtn: RelativeLayout = findViewById(R.id.directorstatisticsbtn)
                    statisticsbtn.setOnClickListener {
                        lifecycleScope.launch {
                            try {
                                AppDatabase.getDatabase(this@MainActivity)
                                val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                val all_table = branchesDao.getbranchesTable(1)
                                to_Director_Statistics_Layout(username, all_table.toString())
                            } catch (e: Exception) {
                                showToast(e.message.toString())
                            }
                        }
                    }
                    // Students Monitor btn
                    val monitoringstudents: RelativeLayout = findViewById(R.id.directormonitoringstudents)
                    monitoringstudents.setOnClickListener {
                        lifecycleScope.launch {
                            try {
                                AppDatabase.getDatabase(this@MainActivity)
                                val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                all_table_data = branchesDao.getbranchesTable(1)?:""
                                to_Director_Monitoring_Students(username, all_table_data)
                            } catch (e: Exception) {
                                showToast(e.message.toString())
                            }
                        }
                    }
                    // Sections Monitor btn
                    val monitoringSections: RelativeLayout = findViewById(R.id.monitoringsections)
                    monitoringSections.setOnClickListener {
                        lifecycleScope.launch {
                            try {
                                AppDatabase.getDatabase(this@MainActivity)
                                val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                all_table_data = branchesDao.getbranchesTable(1)?:""
                                to_Director_Monitoring_Sections(username, all_table_data)
                            } catch (e: Exception) {
                                showToast(e.message.toString())
                            }
                        }
                    }
                    // Sections topList btn
                    val toplist_btn: RelativeLayout = findViewById(R.id.toplist_btn)
                    toplist_btn.setOnClickListener {
                        lifecycleScope.launch {
                            try {
                                AppDatabase.getDatabase(this@MainActivity)
                                val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                all_table_data = branchesDao.getbranchesTable(1)?:""
                                to_Director_Toplist_Layout(username, all_table_data)
                            } catch (e: Exception) {
                                showToast(e.message.toString())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                showToast(e.message.toString())
            }
        }
    }
    fun to_Director_Statistics_Layout(username: String, all_table: String){
        setContentView(R.layout.director_statistics_layout)
        currentLayout = "director_statistics_layout"
        currentUser = username
        currentUserStatus = "director"
        val branchview = findViewById<TextView>(R.id.branch)
        val nametopview = findViewById<TextView>(R.id.supervisorname)
        branchview.text = "مدير دور الفرقان للحفظ و التلاوة"
        nametopview.text = username

        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val toolbarTitle : TextView=findViewById(R.id.toolbarTitle)
        toolbarTitle.text="مدير"
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            to_Director_Layout(username)
        }
        //////////////////////////////////////////////////////////////////////////////////////////// Select (numbers or statistics)
        val numbers_btn = findViewById<Button>(R.id.numbersBtn)
        val stat_btn = findViewById<Button>(R.id.stat_Btn)
        // first time before clicking the choice (grade : Level / History)

        Director_numbers_show(username, all_table)
        // choose stat_btn
        stat_btn.setOnClickListener {
            numbers_btn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            numbers_btn.setTextColor(ContextCompat.getColor(this, R.color.teal_700))
            stat_btn.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700))
            stat_btn.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            Director_stat_show(username, all_table)
        }
        // choose numbers_btn
        numbers_btn.setOnClickListener {
            numbers_btn.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700))
            numbers_btn.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            stat_btn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            stat_btn.setTextColor(ContextCompat.getColor(this, R.color.teal_700))
            Director_numbers_show(username, all_table)
        }
    }
    fun Director_numbers_show(username : String, all_table: String) {
        val parentLayout: LinearLayout = findViewById(R.id.parent_linear_layout)
        val spinner_sections = Spinner(this)
        val spinner_Branches = Spinner(this)
        parentLayout.removeAllViews()
        // Create a linear layout to contain the two spinners
        val containerLayout = LinearLayout(this)
        containerLayout.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        containerLayout.orientation = LinearLayout.HORIZONTAL
        // Spinner for section
        val cardViewSpinner = CardView(this)
        val cardParamsSpinner = LinearLayout.LayoutParams(
            0, // Set layout width to 0
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f // Set layout weight to 0.5
        )
        cardParamsSpinner.setMargins(
            8.dpToPx(),
            8.dpToPx(),
            8.dpToPx(),
            8.dpToPx()
        ) // Adjust margins
        cardViewSpinner.layoutParams = cardParamsSpinner
        cardViewSpinner.cardElevation = 4.dpToPx().toFloat()
        cardViewSpinner.radius = 8.dpToPx().toFloat()
        cardViewSpinner.setCardBackgroundColor(
            ContextCompat.getColor(
                this,
                android.R.color.white
            )
        )

        val spinnerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // Set layout width to MATCH_PARENT
            40.dpToPx()
        )
        spinnerParams.gravity = Gravity.CENTER
        spinnerParams.setMargins(0, 10.dpToPx(), 0, 10.dpToPx())
        spinner_sections.layoutParams = spinnerParams
        spinner_sections.layoutDirection = View.LAYOUT_DIRECTION_RTL
        cardViewSpinner.addView(spinner_sections)
        containerLayout.addView(cardViewSpinner)
        // Spinner for branches
        val cardViewBranchSpinner = CardView(this)
        val cardParamsBranchSpinner = LinearLayout.LayoutParams(
            0, // Set layout width to 0
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f // Set layout weight to 0.5
        )
        cardParamsBranchSpinner.setMargins(
            8.dpToPx(),
            8.dpToPx(),
            8.dpToPx(),
            8.dpToPx()
        ) // Adjust margins as needed
        cardViewBranchSpinner.layoutParams = cardParamsBranchSpinner
        cardViewBranchSpinner.cardElevation = 4.dpToPx().toFloat()
        cardViewBranchSpinner.radius = 8.dpToPx().toFloat()
        cardViewBranchSpinner.setCardBackgroundColor(
            ContextCompat.getColor(
                this,
                android.R.color.white
            )
        )

        val spinnerParamsBranch = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // Set layout width to MATCH_PARENT
            40.dpToPx()
        )
        spinnerParamsBranch.setMargins(0, 10.dpToPx(), 0, 10.dpToPx())
        spinner_Branches.layoutParams = spinnerParamsBranch
        spinner_Branches.layoutDirection = View.LAYOUT_DIRECTION_RTL
        val spinnerEntries = resources.getStringArray(R.array.branches_with_all)
        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, spinnerEntries)
        spinner_Branches.adapter = adapter
        cardViewBranchSpinner.addView(spinner_Branches)
        containerLayout.addView(cardViewBranchSpinner)
        // Add the container layout to the parent layout
        parentLayout.addView(containerLayout)
        // Create and add TextView for student count dynamically
        val textStudentsCount = TextView(this)
        val textParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            35.dpToPx()
        )
        textParams.setMargins(0, 10.dpToPx(), 0, 0)
        textStudentsCount.layoutParams = textParams
        textStudentsCount.gravity = Gravity.CENTER
        textStudentsCount.textSize = 20f
        parentLayout.addView(textStudentsCount)
        // Create and add PieChartView for student count dynamically
        val pieChartViewStudents = PieChartView(this)
        val pieParamsStudents = LinearLayout.LayoutParams(
            170.dpToPx(),
            170.dpToPx()
        )
        pieParamsStudents.gravity = Gravity.CENTER_HORIZONTAL
        pieChartViewStudents.layoutParams = pieParamsStudents
        parentLayout.addView(pieChartViewStudents)
        // Create and add TextView for teachers count dynamically
        val textTeachersCount = TextView(this)
        val textParamsTeachers = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            35.dpToPx()
        )
        textParamsTeachers.setMargins(0, 5.dpToPx(), 0, 0)
        textTeachersCount.layoutParams = textParamsTeachers
        textTeachersCount.gravity = Gravity.CENTER
        textTeachersCount.textSize = 20f
        parentLayout.addView(textTeachersCount)
        // Create and add PieChartView for teachers count dynamically
        val pieChartViewTeachers = PieChartView(this)
        val pieParamsTeachers = LinearLayout.LayoutParams(
            170.dpToPx(),
            170.dpToPx()
        )
        pieParamsTeachers.gravity = Gravity.CENTER_HORIZONTAL
        pieChartViewTeachers.layoutParams = pieParamsTeachers
        parentLayout.addView(pieChartViewTeachers)
        // Update the section spinner with the branch
        spinner_Branches.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                val selectedBranch =  parentView?.getItemAtPosition(position).toString()
                val schoolData = Gson().fromJson(all_table, JR_SchoolData::class.java)
                val sectionsList = mutableListOf<String>()
                sectionsList.add("جميع الحلقات")
                for ((branch_name, branch) in schoolData.branches) {
                    for ((sectionName, _) in branch.sections) {
                        if (selectedBranch == "جميع الفروع" || selectedBranch == branch_name) {
                            sectionsList.add(sectionName)
                        }
                    }
                }
                val sectionAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, sectionsList)
                sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner_sections.adapter = sectionAdapter
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
        // section listener
        spinner_sections.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val selectedBranch = spinner_Branches.selectedItem.toString()
                val selectedSection = parentView?.getItemAtPosition(position).toString()
                var maleCount = 0
                var femaleCount = 0
                var maleTeacherCount = 0
                var femaleTeacherCount = 0
                // Filter students based on the selected branch and section
                val schoolData = Gson().fromJson(all_table, JR_SchoolData::class.java)
                var found = false
                for ((branch_name, branch) in schoolData.branches) {
                    for ((sectionName, section) in branch.sections) {
                        if (((selectedBranch == "جميع الفروع") || (selectedBranch == branch_name)) && ((selectedSection == sectionName) || (selectedSection== "جميع الحلقات"))) {
                            found = true
                            for(student in section?.students?: emptyList()){
                                val gender = student?.info?.gender
                                if (gender == "ذكر") {
                                    maleCount++
                                } else if (gender == "أنثى") {
                                    femaleCount++
                                }
                            }
                            val students_totalNumber: Int = maleCount + femaleCount
                            textStudentsCount.text = "العدد الإجمالي للطلاب : $students_totalNumber"
                            pieChartViewStudents.setCounts(maleCount, femaleCount)
                            for(teacher in section?.teachers?: emptyList()){
                                val gender = teacher?.gender
                                if (gender == "ذكر") {
                                    maleTeacherCount++
                                } else if (gender == "أنثى") {
                                    femaleTeacherCount++
                                }
                            }
                            val teachers_totalNumber: Int = maleTeacherCount + femaleTeacherCount
                            textTeachersCount.text = "العدد الإجمالي للحلقات : $teachers_totalNumber"
                            pieChartViewTeachers.setCounts(maleTeacherCount, femaleTeacherCount)
                        }
                    }
                }
                if(!found){
                    textStudentsCount.text = "العدد الإجمالي للطلاب : 0"
                    pieChartViewStudents.setCounts(0, 0)
                    textTeachersCount.text = "العدد الإجمالي للحلقات : 0"
                    pieChartViewTeachers.setCounts(0, 0)
                }
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }
    fun Director_stat_show(username : String, all_table: String) {
        val parentLayout: LinearLayout = findViewById(R.id.parent_linear_layout)
        parentLayout.removeAllViews()
        // Spinners and text filters
        val parentLinearLayout = LinearLayout(this)
        val parentLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        parentLayoutParams.marginStart = 8.dpToPx()
        parentLayoutParams.marginEnd = 8.dpToPx()
        parentLinearLayout.layoutParams = parentLayoutParams
        parentLinearLayout.orientation = LinearLayout.VERTICAL
        parentLinearLayout.layoutDirection = View.LAYOUT_DIRECTION_RTL
        ////////////////////////////////////////////////////////////////// First Filter line (grade)
        val firstLineLinearLayout1 = LinearLayout(this)
        val firstLineParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            60.dpToPx()
        )
        firstLineParams.bottomMargin = 5.dpToPx()
        firstLineLinearLayout1.layoutParams = firstLineParams
        firstLineLinearLayout1.orientation = LinearLayout.HORIZONTAL
        // Creating the TextView
        val textView1 = TextView(this)
        val textViewParams1 = LinearLayout.LayoutParams(
            150.dpToPx(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textViewParams1.gravity = Gravity.START or Gravity.CENTER
        textViewParams1.marginStart = 20.dpToPx()
        textView1.layoutParams = textViewParams1
        textView1.gravity = Gravity.START
        textView1.textSize = 14f
        textView1.setTypeface(null, Typeface.BOLD)
        textView1.layoutDirection = View.LAYOUT_DIRECTION_RTL
        textView1.text = "الطلاب الذين أتموا حفظ :"
        // CardView with Spinner
        val cardView1 = CardView(this)
        val cardViewParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            50.dpToPx()
        )
        cardViewParams.gravity = Gravity.START or Gravity.CENTER
        cardView1.layoutParams = cardViewParams
        cardView1.radius = 8.dpToPx().toFloat()
        cardView1.cardElevation = 4.dpToPx().toFloat()
        val spinner_grade = Spinner(this)
        val spinnerParams1 = LinearLayout.LayoutParams(
            450,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        spinnerParams1.layoutDirection = View.LAYOUT_DIRECTION_RTL
        spinner_grade.layoutParams = spinnerParams1
        val spinnerEntries = resources.getStringArray(R.array.grades_level)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerEntries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_grade.adapter = adapter
        cardView1.addView(spinner_grade)
        firstLineLinearLayout1.addView(textView1)
        firstLineLinearLayout1.addView(cardView1)
        parentLinearLayout.addView(firstLineLinearLayout1)
        /////////////////////////////////////////////////////////////////// Second Filter line (age)
        val secondLineLinearLayout1 = LinearLayout(this)
        secondLineLinearLayout1.layoutParams = firstLineParams
        secondLineLinearLayout1.orientation = LinearLayout.HORIZONTAL
        // Creating the TextView
        val textView2 = TextView(this)
        textView2.layoutParams = textViewParams1
        // textView2.gravity = Gravity.CENTER
        textView2.layoutDirection = View.LAYOUT_DIRECTION_RTL
        textView2.text = "العمر :"
        textView2.textSize = 14f
        textView2.setTypeface(null, Typeface.BOLD)
        // CardView with Spinner
        val cardView2 = CardView(this)
        cardView2.layoutParams = cardViewParams
        cardView2.radius = 8.dpToPx().toFloat()
        cardView2.cardElevation = 4.dpToPx().toFloat()
        val spinner_age = Spinner(this)
        spinner_age.layoutParams = spinnerParams1
        val spinnerEntries2 = resources.getStringArray(R.array.age_level)
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerEntries2)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_age.adapter = adapter2
        cardView2.addView(spinner_age)
        secondLineLinearLayout1.addView(textView2)
        secondLineLinearLayout1.addView(cardView2)
        parentLinearLayout.addView(secondLineLinearLayout1)
        ///////////////////////////////////////////////////////////////// Third Filter line (Branch)
        val thirdLineLinearLayout1 = LinearLayout(this)
        thirdLineLinearLayout1.layoutParams = firstLineParams
        thirdLineLinearLayout1.orientation = LinearLayout.HORIZONTAL
        // Creating the TextView
        val textView3 = TextView(this)
        textView3.layoutParams = textViewParams1
        textView3.layoutDirection = View.LAYOUT_DIRECTION_RTL
        textView3.text = "الفرع :"
        textView3.textSize = 14f
        textView3.setTypeface(null, Typeface.BOLD)
        // CardView with Spinner
        val cardView3 = CardView(this)
        cardView3.layoutParams = cardViewParams
        cardView3.radius = 8.dpToPx().toFloat()
        cardView3.cardElevation = 4.dpToPx().toFloat()
        val spinner_branch = Spinner(this)
        spinner_branch.layoutParams = spinnerParams1
        val spinnerEntries3 = resources.getStringArray(R.array.branches_with_all)
        val adapter3 = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerEntries3)
        adapter3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_branch.adapter = adapter3
        cardView3.addView(spinner_branch)
        thirdLineLinearLayout1.addView(textView3)
        thirdLineLinearLayout1.addView(cardView3)
        parentLinearLayout.addView(thirdLineLinearLayout1)
        parentLayout.addView(parentLinearLayout)
        /////////////////////////////////////////////////////////////////////////////////////////////////// Table
        // Create a ScrollView
        val scrollView = ScrollView(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        layoutParams.marginStart = 50
        layoutParams.marginEnd = 50
        layoutParams.topMargin = 50
        layoutParams.bottomMargin = 50
        scrollView.layoutParams = layoutParams
        parentLayout.addView(scrollView)

        // Creating the LinearLayout for student names
        val studentsLinearLayout = LinearLayout(this)
        studentsLinearLayout.orientation = LinearLayout.VERTICAL
        val horizontalScrollView = HorizontalScrollView(this@MainActivity)
        horizontalScrollView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // Create a TableLayout
        val tableLayout = TableLayout(this@MainActivity)
        tableLayout.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.WRAP_CONTENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableLayout.removeAllViews() // Clear existing views
        ///////////////////////////////////////////////////////////////////////// 3 Spinners listener
        var selectedBranch : String? = null

        spinner_branch.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                selectedBranch = parentView?.getItemAtPosition(position).toString()
                director_applyFilters_grade_age_branch(all_table, spinner_grade.selectedItemPosition,
                    spinner_age.selectedItemPosition,
                    selectedBranch ?: "كل الفروع",tableLayout, horizontalScrollView )
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        })
        spinner_grade.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                director_applyFilters_grade_age_branch(all_table, position,
                    spinner_age.selectedItemPosition, selectedBranch ?: "كل الفروع",
                    tableLayout, horizontalScrollView )
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        })
        spinner_age.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                director_applyFilters_grade_age_branch(all_table, spinner_grade.selectedItemPosition,
                    position,
                    selectedBranch ?: "كل الفروع",
                    tableLayout, horizontalScrollView )
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Handle case where nothing is selected (optional)
            }
        })

        horizontalScrollView.addView(tableLayout)
        scrollView.addView(horizontalScrollView)
    }
    private fun to_Director_Monitoring_Students(username: String, all_table :  String){
        setContentView(R.layout.director_monitoring_students_layout)
        currentLayout = "director_monitoring_students_layout"
        currentUser = username
        currentUserStatus = "director"
        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            to_Director_Layout(username)
        }
        val toolbarTitle : TextView=findViewById(R.id.toolbarTitle)
        toolbarTitle.text="مدير"
        //////////////////////////////////////////////////////////////////////////////////////////// Top view
        val branchtopview = findViewById<TextView>(R.id.branch)
        val nametopview = findViewById<TextView>(R.id.supervisorname)
        branchtopview.text = "مدير دور الفرقان للحفظ و التلاوة"
        nametopview.text = username

        val spinnerSection = findViewById<Spinner>(R.id.spinnerSection)
        val spinnerBranch = findViewById<Spinner>(R.id.spinnerBranch)

        val scrollView = findViewById<ScrollView>(R.id.scrollview)
        // Build the section spinner list
        // Update the section spinner with the branch
        spinnerBranch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                val selectedBranch =  parentView?.getItemAtPosition(position).toString()
                val schoolData = Gson().fromJson(all_table, JR_SchoolData::class.java)
                val sectionsList = mutableListOf<String>()
                sectionsList.add("جميع الحلقات")
                for ((branch_name, branch) in schoolData.branches) {
                    for ((sectionName, _) in branch.sections) {
                        if (selectedBranch == branch_name) {
                            sectionsList.add(sectionName)
                        }
                    }
                }
                val sectionAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, sectionsList)
                sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerSection.adapter = sectionAdapter
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }


        // Spinner listener
        spinnerSection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                val selectedSection = parentView?.getItemAtPosition(position).toString()
                val linearContainer = LinearLayout(applicationContext)
                linearContainer.layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                linearContainer.orientation = LinearLayout.VERTICAL
                // Filter students based on the selected branch and section
                val selectedBranch = spinnerBranch.selectedItem.toString()
                val schoolData = Gson().fromJson(all_table, JR_SchoolData::class.java)
                for ((branch_name, branch) in schoolData.branches) {
                    for ((sectionName, section) in branch.sections) {
                        if ((selectedBranch == branch_name) && ((selectedSection == sectionName) || (selectedSection== "جميع الحلقات"))) {
                            for(student_item in section?.students?: emptyList()){
                                if ((selectedSection == "جميع الحلقات" || sectionName == selectedSection)) {
                                    val color_text = R.color.color4
                                    val cardView = CardView(this@MainActivity)
                                    val card_layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    card_layoutParams.gravity = Gravity.CENTER
                                    card_layoutParams.setMargins(
                                        16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 10.dpToPx())
                                    cardView.layoutParams = card_layoutParams
                                    cardView.cardElevation = 4.dpToPx().toFloat()
                                    cardView.setCardBackgroundColor(
                                        ContextCompat.getColor(
                                            this@MainActivity, android.R.color.white))
                                    // horizontal bar
                                    val horizontalBar = View(this@MainActivity)
                                    val horizontalBarParams = LinearLayout.LayoutParams(
                                        2.dpToPx(), LinearLayout.LayoutParams.MATCH_PARENT)
                                    horizontalBar.setBackgroundColor(
                                        ContextCompat.getColor(this@MainActivity, color_text))
                                    horizontalBar.layoutParams = horizontalBarParams
                                    cardView.addView(horizontalBar)
                                    // Student Name
                                    val Name_textView = TextView(this@MainActivity)
                                    val name_text_layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    name_text_layoutParams.setMargins(
                                        8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                                    name_text_layoutParams.gravity = Gravity.CENTER
                                    Name_textView.layoutParams = name_text_layoutParams
                                    Name_textView.gravity = Gravity.CENTER
                                    Name_textView.text = student_item?.info?.name
                                    Name_textView.layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
                                    Name_textView.setTextColor(
                                        ContextCompat.getColor(
                                            this@MainActivity, android.R.color.black)
                                    )
                                    Name_textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                                    Name_textView.setTypeface(null, Typeface.BOLD)
                                    cardView.addView(Name_textView)
                                    // Student Section
                                    val Section_textView = TextView(this@MainActivity)
                                    val section_text_layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT)
                                    section_text_layoutParams.setMargins(
                                        8.dpToPx(), 50.dpToPx(), 8.dpToPx(), 8.dpToPx())
                                    section_text_layoutParams.gravity = Gravity.CENTER
                                    Section_textView.layoutParams = section_text_layoutParams
                                    Section_textView.gravity = Gravity.CENTER
                                    Section_textView.text = "$sectionName - $branch_name"
                                    Section_textView.setTextColor(
                                        ContextCompat.getColor(this@MainActivity,
                                            android.R.color.black))
                                    cardView.addView(Section_textView)
                                    // Vertical bar
                                    val VerticalBar = View(this@MainActivity)
                                    val VerticalBarParams = LinearLayout.LayoutParams(
                                        2.dpToPx(), 30.dpToPx())
                                    VerticalBarParams.gravity = Gravity.CENTER_VERTICAL
                                    VerticalBarParams.setMargins(8.dpToPx(), 8.dpToPx(),
                                        8.dpToPx(), 8.dpToPx())
                                    VerticalBar.setBackgroundColor(
                                        ContextCompat.getColor(this@MainActivity, R.color.black))
                                    VerticalBar.layoutParams = VerticalBarParams
                                    // Left TextView
                                    val leftTextView = TextView(this@MainActivity)
                                    val leftTextParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    leftTextParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                                    leftTextView.layoutParams = leftTextParams
                                    leftTextView.setTextColor(
                                        ContextCompat.getColor(this@MainActivity,
                                            color_text)
                                    )
                                    leftTextView.setTextSize(
                                        TypedValue.COMPLEX_UNIT_SP, 18f)
                                    leftTextView.setTypeface(null, Typeface.BOLD)
                                    leftTextView.setOnClickListener {
                                        if(student_item!=null) {
                                            JR_get_grade_history(student_item.grade_history){grade_history ->
                                                JR_get_attendance_History(student_item.attendance){attendanceHistory->
                                                    val student = Student(
                                                        id = section?.students?.indexOf(student_item)?:0,
                                                        name = student_item.info?.name.toString(),
                                                        gender = student_item.info?.gender.toString(),
                                                        birthday = student_item.info?.birthday?.replace("-","/").toString(),
                                                        section = sectionName,
                                                        branch = branch_name,
                                                        teacherName =  (section?.teachers?.get(0)?.name) ?: "Unknown",
                                                        gradeLevel = student_item.grade_vector?: emptyList(),
                                                        gradeHistory =grade_history,
                                                        attendanceHistory = attendanceHistory
                                                    )
                                                    to_Student_Attendence_layout(username, "director", student, all_table_data, GlobalstudentspinnerIndex)

                                                }
                                            }
                                        }
                                    }
                                    leftTextView.text = "عرض الحضور"
                                    // Right TextView
                                    val rightTextView = TextView(this@MainActivity)
                                    val rightTextParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    rightTextParams.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                                    rightTextView.layoutParams = rightTextParams
                                    rightTextView.setTextColor(
                                        ContextCompat.getColor(this@MainActivity, color_text))
                                    rightTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                                    rightTextView.setTypeface(null, Typeface.BOLD)
                                    rightTextView.setOnClickListener {
                                        if(student_item!=null) {
                                            JR_get_grade_history(student_item.grade_history){grade_history ->
                                                val student = Student(
                                                    id = section?.students?.indexOf(student_item)?:0,
                                                    name = student_item.info?.name.toString(),
                                                    gender = student_item.info?.gender.toString(),
                                                    birthday = student_item.info?.birthday?.replace("-","/").toString(),
                                                    section = sectionName,
                                                    branch = branch_name,
                                                    teacherName =  (section?.teachers?.get(0)?.name) ?: "Unknown",
                                                    gradeLevel = student_item.grade_vector?: emptyList(),
                                                    gradeHistory =grade_history,
                                                    attendanceHistory = student_item.attendance?.map { (date, status) ->
                                                        AttendanceItem(date, "", status)
                                                    }?.toMutableList() ?: mutableListOf()
                                                )

                                                to_Student_Grade_layout(username, "director", student, all_table_data, 0)
                                            }
                                        }
                                    }
                                    rightTextView.text = "عرض الحفظ"
                                    // Create a horizontal LinearLayout for the text views
                                    val textContainer = LinearLayout(this@MainActivity)
                                    textContainer.orientation = LinearLayout.HORIZONTAL
                                    val textContainerParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    textContainerParams.setMargins(
                                        8.dpToPx(), 80.dpToPx(), 8.dpToPx(), 8.dpToPx())
                                    textContainer.layoutParams = textContainerParams
                                    textContainer.gravity = Gravity.CENTER_HORIZONTAL
                                    textContainer.addView(leftTextView)
                                    textContainer.addView(VerticalBar)
                                    textContainer.addView(rightTextView)
                                    cardView.addView(textContainer)
                                    linearContainer.addView(cardView)
                                }
                            }
                        }
                    }
                }
                scrollView.removeAllViews()
                scrollView.addView(linearContainer)
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }
    private fun to_Director_Monitoring_Sections(username: String, all_table: String) {
        setContentView(R.layout.director_monitoring_sections_layout)
        currentLayout = "director_monitoring_sections_layout"
        currentUser = username
        currentUserStatus = "director"
        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            to_Director_Layout(username)
        }
        val toolbarTitle: TextView = findViewById(R.id.toolbarTitle)
        toolbarTitle.text = "مدير"
        //////////////////////////////////////////////////////////////////////////////////////////// Top view
        val branchtopview = findViewById<TextView>(R.id.branch)
        val nametopview = findViewById<TextView>(R.id.supervisorname)
        branchtopview.text = "مدير دور الفرقان للحفظ و التلاوة"
        nametopview.text = username
        ///////////////////////////////////////////////////////////////////////////////////////////// Fill
        val spinner_Branches = findViewById<Spinner>(R.id.spinnerBranch)
        val scrollView = findViewById<ScrollView>(R.id.scrollview_in_section)

        spinner_Branches.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                scrollView.removeAllViews()
                val selectedBranch = spinner_Branches.selectedItem.toString()
                val schoolData = Gson().fromJson(all_table, JR_SchoolData::class.java)
                for ((branch_name, branch) in schoolData.branches) {
                    val linearContainer = LinearLayout(applicationContext)
                    linearContainer.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    linearContainer.orientation = LinearLayout.VERTICAL
                    if (branch_name == selectedBranch) {

                        for ((sectionName, section) in branch.sections) {
                            val color_text = R.color.color4
                            val cardView = CardView(this@MainActivity)
                            val card_layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            card_layoutParams.gravity = Gravity.CENTER
                            card_layoutParams.setMargins(16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 10.dpToPx())
                            cardView.layoutParams = card_layoutParams
                            cardView.cardElevation = 4.dpToPx().toFloat()
                            cardView.setCardBackgroundColor(
                                ContextCompat.getColor(this@MainActivity, android.R.color.white)
                            )
                            // Section Name
                            val Name_textView = TextView(this@MainActivity)
                            val name_text_layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            name_text_layoutParams.setMargins(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                            name_text_layoutParams.gravity = Gravity.CENTER
                            Name_textView.layoutParams = name_text_layoutParams
                            Name_textView.gravity = Gravity.CENTER
                            Name_textView.text = sectionName
                            Name_textView.layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
                            Name_textView.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
                            Name_textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                            Name_textView.setTypeface(null, Typeface.BOLD)
                            cardView.addView(Name_textView)
                            // Create a RelativeLayout for the text views
                            val relativeLayout = RelativeLayout(this@MainActivity)
                            val relativeParam = RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.MATCH_PARENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                            )
                            relativeParam.setMargins(8.dpToPx(), 50.dpToPx(), 8.dpToPx(), 8.dpToPx())
                            relativeLayout.layoutParams = relativeParam
                            // Left TextView (teachername)
                            val teacherNameTextView = TextView(this@MainActivity)
                            val teacherNameParams = RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                addRule(RelativeLayout.ALIGN_PARENT_START)
                                setMargins(0, 0, 0, 0)
                            }
                            teacherNameTextView.layoutParams = teacherNameParams
                            teacherNameTextView.text = "الأستاذ(ة): ${section?.teachers?.get(0)?.name}"
                            teacherNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                            teacherNameTextView.setTypeface(null, Typeface.BOLD)
                            teacherNameTextView.maxWidth = 140.dpToPx()
                            relativeLayout.addView(teacherNameTextView)

                            // Right TextView (number of students)
                            val sectionTextView = TextView(this@MainActivity)
                            val sectionParams = RelativeLayout.LayoutParams(
                                RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                addRule(RelativeLayout.ALIGN_PARENT_END)
                                setMargins(0, 0, 0, 0)
                            }
                            sectionTextView.layoutParams = sectionParams
                            sectionTextView.text = "عدد الطلاب: ${section?.students?.size}"
                            sectionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                            sectionTextView.setTypeface(null, Typeface.BOLD)
                            sectionTextView.maxWidth = 140.dpToPx()
                            relativeLayout.addView(sectionTextView)

                            // Add the RelativeLayout to the CardView
                            cardView.addView(relativeLayout)

                            // vertival bar
                            val vertivalBar = View(this@MainActivity)
                            val vertivalBarParams = LinearLayout.LayoutParams(
                                2.dpToPx(),
                                LinearLayout.LayoutParams.MATCH_PARENT
                            )
                            vertivalBarParams.gravity = Gravity.START
                            vertivalBar.layoutDirection = View.LAYOUT_DIRECTION_RTL
                            vertivalBar.setBackgroundColor(
                                ContextCompat.getColor(this@MainActivity, color_text)
                            )
                            vertivalBar.layoutParams = vertivalBarParams
                            cardView.addView(vertivalBar)
                            cardView.setOnClickListener {
                                JR_get_students_list_SECTION(section, sectionName, branch_name,
                                    section?.teachers?.get(0)?.name.toString()){sectionstudents->
                                    val teacheritem = Teacher(
                                        teacherID = "1",
                                        teacherName = section?.teachers?.get(0)?.name.toString(),
                                        section = sectionName,
                                        branch = branch_name,
                                        teacherGender = section?.teachers?.get(0)?.gender.toString(),
                                        students = sectionstudents
                                    )
                                    to_Section_Layout(username, "director", teacheritem)
                                }
                            }
                            linearContainer.addView(cardView)
                        }
                        scrollView.removeAllViews()
                        scrollView.addView(linearContainer)
                    }
                }
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }
    private fun to_Director_Toplist_Layout( username: String, all_table: String){
        setContentView(R.layout.director_toplist_layout)
        currentLayout = "director_toplist_layout"
        currentUser = username
        currentUserStatus = "director"
        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            to_Director_Layout(username)
        }
        val toolbarTitle : TextView=findViewById(R.id.toolbarTitle)
        toolbarTitle.text="مدير"
        //////////////////////////////////////////////////////////////////////////////////////////// Top view
        val branchtopview = findViewById<TextView>(R.id.branch)
        val nametopview = findViewById<TextView>(R.id.supervisorname)
        branchtopview.text = "مدير دور الفرقان للحفظ و التلاوة"
        nametopview.text = username
        //////////////////////////////////////////////////////////////////////  Fill table
        val spinnerYears = findViewById<Spinner>(R.id.years_spinner)
        var selectedYear: String? = null
        val spinnerMonths = findViewById<Spinner>(R.id.months_spinner)
        var selectedMonth: String? = null
        val spinnerBranch = findViewById<Spinner>(R.id.Spinner_Branches)
        var selectedBranch: String? = null
        /////////// Add table first time
        val scrollView = findViewById<ScrollView>(R.id.scrollview)
        scrollView.removeAllViews()
        val horizontalScrollView = HorizontalScrollView(this@MainActivity)
        horizontalScrollView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val tableLayout = TableLayout(this@MainActivity)
        //title
        director_toplist_addTitles(tableLayout)
        // elements
        spinnerYears.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                selectedYear = parentView?.getItemAtPosition(position).toString()
                selectedBranch= spinnerBranch.selectedItem.toString()
                selectedMonth=spinnerMonths.selectedItem.toString()
                tableLayout.removeAllViews()
                director_toplist_addTitles(tableLayout)
                lifecycleScope.launch {
                    try {
                        val schoolData = Gson().fromJson(all_table, JR_SchoolData::class.java)
                        var all_student_list = ArrayList<Student>()
                        for ((branch_name, branch) in schoolData.branches) {
                            if(selectedBranch==branch_name || selectedBranch=="جميع الفروع") {
                                for ((sectionName, section) in branch.sections) {
                                    for (student_item in section?.students ?: emptyList()) {
                                        if (student_item != null) {
                                            JR_get_grade_history(student_item.grade_history) { grade_history ->
                                                val student = Student(
                                                    id = section?.students?.indexOf(student_item) ?: 0,
                                                    name = student_item.info?.name.toString(),
                                                    gender = "",
                                                    birthday =  student_item.info?.birthday?.replace("-","/").toString(),
                                                    section = sectionName,
                                                    branch = branch_name,
                                                    teacherName = (section?.teachers?.get(0)?.name)
                                                        ?: "Unknown",
                                                    gradeLevel = student_item.grade_vector
                                                        ?: emptyList(),
                                                    gradeHistory = grade_history,
                                                    attendanceHistory = mutableListOf()
                                                )
                                                all_student_list.add(student)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        val sortedStudents = all_student_list.map { student ->
                            var score = 0
                            JR_get_toplist_info(student, selectedYear ?: "كل السنوات", selectedMonth ?: "كل الأشهر") {studentScore ->
                                score = studentScore
                            }
                            Pair(student, score)
                        }.sortedByDescending { it.second }
                        var Id = 1
                        for ((sortedStudent, score) in sortedStudents) {
                            director_topilist_Table(Id, sortedStudent, score, selectedBranch?: "جميع الفروع",tableLayout)
                            Id += 1
                            if (Id==51){
                                break
                            }
                        }
                    } catch (e: Exception) {
                        showToast(e.message.toString())
                    }
                }
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
        spinnerMonths.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                selectedMonth = parentView?.getItemAtPosition(position).toString()
                selectedBranch = spinnerBranch.selectedItem.toString()
                tableLayout.removeAllViews()
                director_toplist_addTitles(tableLayout)
                lifecycleScope.launch {
                    try {
                        val schoolData = Gson().fromJson(all_table, JR_SchoolData::class.java)
                        var all_student_list = ArrayList<Student>()
                        for ((branch_name, branch) in schoolData.branches) {
                            if (selectedBranch == branch_name || selectedBranch == "جميع الفروع") {
                                for ((sectionName, section) in branch.sections) {
                                    for (student_item in section?.students ?: emptyList()) {
                                        if (student_item != null) {
                                            JR_get_grade_history(student_item.grade_history) { grade_history ->
                                                val student = Student(
                                                    id = section?.students?.indexOf(student_item)
                                                        ?: 0,
                                                    name = student_item.info?.name.toString(),
                                                    gender = "",
                                                    birthday =  student_item.info?.birthday?.replace("-","/").toString(),
                                                    section = sectionName,
                                                    branch = branch_name,
                                                    teacherName = (section?.teachers?.get(0)?.name)
                                                        ?: "Unknown",
                                                    gradeLevel = emptyList(),
                                                    gradeHistory = grade_history,
                                                    attendanceHistory = mutableListOf()
                                                )
                                                all_student_list.add(student)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        val sortedStudents = all_student_list.map { student ->
                            // Calculate the score for each student for the selected year
                            var score = 0
                            JR_get_toplist_info(student, selectedYear ?: "كل السنوات",
                                selectedMonth ?: "كل الأشهر") {studentScore ->
                                score = studentScore
                            }
                            Pair(student, score)
                        }.sortedByDescending { it.second }
                        var Id = 1
                        for ((sortedStudent, score) in sortedStudents) {
                            director_topilist_Table(Id, sortedStudent, score,
                                selectedBranch ?: "جميع الفروع", tableLayout
                            )
                            Id += 1
                            if (Id==51){
                                break
                            }
                        }
                    } catch (e: Exception) {
                        showToast(e.message.toString())
                    }
                }
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
        spinnerBranch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                selectedBranch = parentView?.getItemAtPosition(position).toString()
                tableLayout.removeAllViews()
                director_toplist_addTitles(tableLayout)
                lifecycleScope.launch {
                    try {
                        val schoolData = Gson().fromJson(all_table, JR_SchoolData::class.java)
                        var all_student_list = ArrayList<Student>()
                        for ((branch_name, branch) in schoolData.branches) {
                            if(selectedBranch==branch_name || selectedBranch=="جميع الفروع"){
                                for ((sectionName, section) in branch.sections) {
                                    for (student_item in section?.students ?: emptyList()) {
                                        if (student_item != null) {
                                            JR_get_grade_history(student_item.grade_history) { grade_history ->
                                                val student = Student(
                                                    id = section?.students?.indexOf(student_item) ?: 0,
                                                    name = student_item.info?.name.toString(),
                                                    gender = "",
                                                    birthday =  student_item.info?.birthday?.replace("-","/").toString(),
                                                    section = sectionName,
                                                    branch = branch_name,
                                                    teacherName = (section?.teachers?.get(0)?.name)
                                                        ?: "Unknown",
                                                    //gradeLevel = student_item.grade_vector ?: emptyList(),
                                                    gradeLevel = emptyList(),
                                                    gradeHistory = grade_history,
                                                    attendanceHistory = mutableListOf()
                                                )
                                                all_student_list.add(student)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        val sortedStudents = all_student_list.map { student ->
                            // Calculate the score for each student for the selected year
                            var score = 0
                            JR_get_toplist_info(student, selectedYear ?: "كل السنوات", selectedMonth ?: "كل الأشهر") { studentScore ->
                                score = studentScore
                            }
                            Pair(student, score)
                        }.sortedByDescending { it.second }
                        var Id = 1
                        for ((sortedStudent, score) in sortedStudents) {
                            if ((selectedBranch == sortedStudent.branch) || (selectedBranch ==  "جميع الفروع") ) {
                                director_topilist_Table(
                                    Id,
                                    sortedStudent,
                                    score,
                                    selectedBranch ?: "جميع الفروع",
                                    tableLayout
                                )
                                Id += 1
                                if (Id==51){
                                    break
                                }
                            }
                        }
                    } catch (e: Exception) {
                        showToast(e.message.toString())
                    }
                }
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        horizontalScrollView.post {
            horizontalScrollView.scrollTo(2000, 0)
        }
        scrollView.removeAllViews()
        horizontalScrollView.addView(tableLayout)
        scrollView.addView(horizontalScrollView)
    }

    fun director_applyFilters_grade_age_branch(all_table: String, gradePosition: Int,
                                               agePosition: Int, Selectedbranch: String,
                                               tableLayout : TableLayout,
                                               horizontalScrollView : HorizontalScrollView) {
        tableLayout.removeAllViews()
        director_stat_addTitles(tableLayout)
        var Id_counting = 1
        val schoolData = Gson().fromJson(all_table, JR_SchoolData::class.java)
        for ((branch_name, branch) in schoolData.branches) {
            if ((branch_name == Selectedbranch)|| Selectedbranch == "جميع الفروع") {
                for ((sectionName, section) in branch.sections) {
                    for (student in section?.students ?: emptyList()) {
                        val studentInfo = student?.info
                        val studentGradeVector = student?.grade_vector ?: emptyList()
                        if (studentInfo != null) {
                            val student_age = (getAge(dateFormat.parse(studentInfo.birthday.replace("-", "/")), Calendar.getInstance().time).toString()).toInt()
                            val grade30: Int = studentGradeVector.getOrNull(29) ?: 0
                            val grade29: Int = studentGradeVector.getOrNull(28) ?: 0
                            val student_score = studentGradeVector.sum() ?: 0
                            val okGrade30 =
                                grade30 == 100 && grade29 != null && grade29 < 100 && student_score < 500
                            val okGrade29_30 = grade30 == 100 && grade29 == 100 && student_score < 500
                            val okAll = student_score >= 3000
                            val ok20 = student_score >= 2000 && student_score < 3000
                            val ok15 = student_score >= 1500 && student_score < 2000
                            val ok10 = student_score >= 1000 && student_score < 1500
                            val ok5 = student_score >= 500 && student_score < 1000
                            val okAge6 = student_age <= 6
                            val okAge8 = student_age <= 8
                            val okAge10 = student_age <= 10
                            val okAge12 = student_age <= 12
                            val okAge15 = student_age <= 15
                            val okAge20 = student_age <= 20
                            val okAgeAbove20 = student_age > 20
                            if ((gradePosition == 0 || (gradePosition == 1 && okGrade30) || (gradePosition == 2 && okGrade29_30) ||
                                        (gradePosition == 3 && ok5) || (gradePosition == 4 && ok10) || (gradePosition == 5 && ok15) ||
                                        (gradePosition == 6 && ok20) || (gradePosition == 7 && okAll))
                                && (agePosition == 0 || (agePosition == 1 && okAge6) || (agePosition == 2 && okAge8) ||
                                        (agePosition == 3 && okAge10) || (agePosition == 4 && okAge12) || (agePosition == 5 && okAge15) ||
                                        (agePosition == 6 && okAge20) || (agePosition == 7 && okAgeAbove20))
                            ) {

                                director_stat_createTableRow(
                                    student,
                                    sectionName,
                                    branch_name,
                                    Id_counting,
                                    tableLayout
                                )
                                Id_counting += 1
                            }
                        }
                    }
                }
            }
        }
        horizontalScrollView.post {
            horizontalScrollView.scrollTo(2000, 0)
        }
    }
    fun director_stat_createTableRow(student: JR_Student?,section:String, branch:String, id: Int,
                                     tableLayout : TableLayout) {
        // Create a TableRow for each student
        val row = TableRow(this@MainActivity)
        val cardView = CardView(this@MainActivity)
        val cardViewParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            120
        )
        cardView.layoutParams = cardViewParams
        val innerLayout = LinearLayout(this@MainActivity)
        // Create TextViews for student data
        val age = getAge(dateFormat.parse(student?.info?.birthday!!.replace("-", "/")), Calendar.getInstance().time).toString()
        val number_of_pages = student?.grade_vector!!.sum()/5
        val grade = pages_to_jiz2(number_of_pages)  // % to nimber of pages : /5
        val columnsinformation = arrayOf(id.toString(), student.info?.name, age, grade, section, branch)
        val ID_size  = 100
        val name_size = 400
        val Age_size = 150
        val Grade_size = 400
        val section_size = 300
        val branch_size = 300
        val titlessizes = intArrayOf(ID_size, name_size, Age_size, Grade_size, section_size, branch_size)
        for (i in columnsinformation.indices) {
            val textView = TextView(this@MainActivity)
            textView.text = columnsinformation[i]
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.layoutParams = LinearLayout.LayoutParams(
                titlessizes[i],
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            textView.textDirection = View.TEXT_DIRECTION_RTL // arabic direction
            textView.gravity = Gravity.CENTER_VERTICAL
            innerLayout.addView(textView, 0)
        }
        cardView.addView(innerLayout)
        row.addView(cardView)
        tableLayout.addView(row)
        // Add underline after each row
        val rowUnderline = View(this@MainActivity)
        rowUnderline.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            2
        )
        rowUnderline.setBackgroundColor(Color.BLACK)
        tableLayout.addView(rowUnderline)
    }
    fun director_stat_addTitles(tableLayout : TableLayout) {
        val row = TableRow(this@MainActivity)
        val cardView = CardView(this@MainActivity)
        val innerLayout = LinearLayout(this@MainActivity)
        var titles: Array<String>
        titles = arrayOf(" ", "الاسم", "العمر", "الحفظ", "الحلقة", "الفرع")
        val ID_size  = 100
        val name_size = 400
        val Age_size = 150
        val Grade_size = 400
        val section_size = 300
        val branch_size = 300
        var titlessizes: IntArray
        // Titles
        titlessizes = intArrayOf(ID_size, name_size, Age_size, Grade_size, section_size, branch_size)

        for (i in titles.indices) {
            val textView = TextView(this@MainActivity)
            innerLayout.gravity = Gravity.END
            textView.text = titles[i]
            textView.setTextColor(resources.getColor(R.color.bottom_white))
            textView.setBackgroundColor(resources.getColor(R.color.teal_700))

            textView.setTypeface(null, Typeface.BOLD)
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.layoutParams = LinearLayout.LayoutParams(
                titlessizes[i],
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView.textDirection = View.TEXT_DIRECTION_RTL
            innerLayout.addView(textView, 0)
        }

        cardView.addView(innerLayout)
        row.addView(cardView)
        tableLayout.addView(row)

        // Add underline after the titles
        val titleRowUnderline = View(this@MainActivity)
        titleRowUnderline.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            2
        )
        titleRowUnderline.setBackgroundColor(Color.BLACK)
        tableLayout.addView(titleRowUnderline)
    }

    fun JR_get_grade_history(grade_history_table : JR_grade_history?,
                             callback: (MutableList<Triple<Int, Int, Int>>) -> Unit) {
        val resultList = mutableListOf<Triple<Int, Int, Int>>()
        for (year in 2030 downTo 2020) {
            for (month in 12 downTo 1) { // Adjusted to iterate from 12 to 1 for months
                JR_calculate_Student_Month_Progress(year, month, grade_history_table) { monthProgressCount ->
                    if (monthProgressCount > 0) {
                        resultList.add(Triple(year, month, monthProgressCount))
                    }
                    // Check if all results are collected before calling the callback
                    if (year == 2020 && month == 1) { // Adjusted to check for the end of the iteration
                        callback(resultList)
                    }
                }
            }
        }
    }

    fun JR_calculate_Student_Month_Progress(year: Int, month: Int,
                                            grade_history_table : JR_grade_history?,
                                            callback: (Int) -> Unit) {
        var count = 0
        grade_history_table?.dates?.forEach { (date, grades) ->
            val (jsonDay, jsonMonth, jsonYear) = date.split("-").map { it.toIntOrNull() }
            if (jsonYear == year && jsonMonth == month) {
                val gradeHistoryItem = grades as? JR_grade_history_item
                for ((_, grade) in gradeHistoryItem?.grade_history_item ?: emptyMap()) {
                    count += grade ?: 0
                }
            }
        }
        callback(count)
    }
    fun JR_get_attendance_History(attendanceItem : Map<String, Int>?,
                                  callback: (MutableList<AttendanceItem>) -> Unit){
        val attendanceList: MutableList<AttendanceItem> = mutableListOf()
        attendanceItem?.forEach { (date, status) ->
            val dateObj: Date = dateFormat.parse(date.replace("-", "/"))!!
            val dayFormat = SimpleDateFormat("EEEE", Locale("ar"))
            val day = dayFormat.format(dateObj)
            val attendanceItem = AttendanceItem(date, day, status)
            attendanceList.add(attendanceItem)
        }
        callback(attendanceList)
    }
    fun JR_get_toplist_info(student: Student, selectedYear: String, selectedMonth:String,
                            callback: (grade: Int) -> Unit) {
        var arabic_month_int = when (selectedMonth) {
            "كانون الثاني" -> 1
            "شباط" -> 2
            "آذار" -> 3
            "نيسان" -> 4
            "أيار" -> 5
            "حزيران" -> 6
            "تموز" -> 7
            "آب" -> 8
            "أيلول" -> 9
            "تشرين الأول" -> 10
            "تشرين الثاني" -> 11
            "كانون الأول" ->  12
            else -> 0
        }
        var month_score = 0
        // Fetch grade history for each year and month
        for (year in 2021..2025) {
            for (month in 1..12) {
                if (((year.toString() == selectedYear) || (selectedYear == "كل السنوات"))&&((month == arabic_month_int) || (arabic_month_int == 0))) {
                    var monthProgressCount = 0
                    for (gradeHistoryitem in student.gradeHistory) {
                        val currentYear = gradeHistoryitem.first
                        val databaseMonth = gradeHistoryitem.second
                        if ((year == currentYear) && (databaseMonth == month)) {
                            monthProgressCount += gradeHistoryitem.third
                        }
                    }
                    if (monthProgressCount > 0) {
                        month_score += monthProgressCount
                    }
                }
            }
        }
        callback(month_score)
    }

    private fun JR_get_students_list_SECTION(section: JR_Section?, sectionName:String,
                                             branchName: String, teacherName :String,
                                             callback: (List<Student>) -> Unit){
        val students = mutableListOf<Student>()
        section?.students?.forEachIndexed { index, jrStudent ->
            jrStudent?.info?.let { studentInfo ->
                JR_get_grade_history(jrStudent.grade_history){grade_history ->
                    JR_get_attendance_History(jrStudent.attendance){attendanceHistory->
                        val student = Student(
                            id = index,
                            name = studentInfo.name,
                            gender = studentInfo.gender,
                            birthday = studentInfo.birthday.replace("-","/"),
                            section = sectionName,
                            branch = branchName,
                            teacherName = teacherName,
                            gradeLevel = jrStudent.grade_vector ?: emptyList(),
                            gradeHistory = grade_history,
                            attendanceHistory =  attendanceHistory
                        )
                        students.add(student)
                    }
                }

            }
        }
        callback(students)
    }

    fun director_toplist_addTitles(tableLayout : TableLayout) {
        val row = TableRow(this@MainActivity)
        val cardView = CardView(this@MainActivity)
        val innerLayout = LinearLayout(this@MainActivity)
        var titles: Array<String>
        titles = arrayOf(" ", "الاسم", "الحفظ", "الحلقة","الأستاذ", "الفرع")
        val ID_size  = 100
        val name_size = 400
        val section_size = 400
        val Teacher_name_size = 300
        val score_size = 300
        val branch_size = 300
        var titlessizes: IntArray
        // Titles
        titlessizes = intArrayOf(ID_size, name_size, score_size, section_size, Teacher_name_size, branch_size)

        for (i in titles.indices) {
            val textView = TextView(this@MainActivity)
            innerLayout.gravity = Gravity.END
            textView.text = titles[i]
            textView.setTextColor(resources.getColor(R.color.bottom_white))
            textView.setBackgroundColor(resources.getColor(R.color.teal_700))

            textView.setTypeface(null, Typeface.BOLD)
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.layoutParams = LinearLayout.LayoutParams(
                titlessizes[i],
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView.textDirection = View.TEXT_DIRECTION_RTL
            innerLayout.addView(textView, 0)
        }

        cardView.addView(innerLayout)
        row.addView(cardView)
        tableLayout.addView(row)

        // Add underline after the titles
        val titleRowUnderline = View(this@MainActivity)
        titleRowUnderline.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            2
        )
        titleRowUnderline.setBackgroundColor(Color.BLACK)
        tableLayout.addView(titleRowUnderline)
    }
    fun director_topilist_Table(id_list: Int, student: Student, score : Int,selectedBranch : String,
                                tableLayout : TableLayout) {

        // Create a TableRow for each student
        val row = TableRow(this@MainActivity)
        val cardView = CardView(this@MainActivity)
        val cardViewParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            120
        )
        cardView.layoutParams = cardViewParams
        val innerLayout = LinearLayout(this@MainActivity)
        // Create TextViews for student data
        val columnsinformation = arrayOf(
            id_list.toString(),
            student.name,
            pages_to_jiz2(score),
            student.section,
            student.teacherName,
            student.branch
        )
        val ID_size = 100
        val name_size = 400
        val section_size = 400
        val Teacher_name_size = 300
        val score_size = 300
        val branch_size = 300

        val titlessizes =
            intArrayOf(ID_size, name_size, score_size, section_size, Teacher_name_size, branch_size)

        for (i in columnsinformation.indices) {
            val textView = TextView(this@MainActivity)
            textView.text = columnsinformation[i]
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.layoutParams = LinearLayout.LayoutParams(
                titlessizes[i],
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            textView.textDirection = View.TEXT_DIRECTION_RTL // arabic direction
            textView.gravity = Gravity.CENTER_VERTICAL
            innerLayout.addView(textView, 0)
        }

        cardView.addView(innerLayout)
        row.addView(cardView)
        tableLayout.addView(row)

        // Add underline after each row
        val rowUnderline = View(this@MainActivity)
        rowUnderline.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            2
        )
        rowUnderline.setBackgroundColor(Color.BLACK)
        tableLayout.addView(rowUnderline)

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////    Section show
    fun to_Section_Layout(username: String, status: String, teacher : Teacher) {
        setContentView(R.layout.section_layout)
        currentLayout = "section_layout"
        currentUser = username
        currentUserStatus = status

        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            when (status) {
                "teacher" -> to_Teacher_Layout(username, userBranch, 0)
                "supervisor" -> to_Supervisor_Monitoring_Sections(username, currentBranch, all_table_supervisor)
                "director" -> to_Director_Monitoring_Sections(username, all_table_data)
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////// Top view
        val BranchTopView: TextView = findViewById(R.id.branch)
        BranchTopView.text = teacher.branch
        val SectionTopView: TextView = findViewById(R.id.section_name)
        SectionTopView.text = teacher.section
        val toolbarTitle: TextView = findViewById(R.id.toolbarTitle)
        toolbarTitle.text = when (status) {
            "student" -> "طالب"
            "teacher" -> "أستاذ"
            "supervisor" -> "مشرف"
            "director" -> "مدير"
            else -> "Unknown Status"
        }
        val teachernamebtn: TextView = findViewById(R.id.teacher_name)
        teachernamebtn.text = "الأستاذ(ة): ${teacher.teacherName}"
        //// get the teacher ID and fill the top view data
        val studentscount = teacher.students.size
        val studentCountView = findViewById<TextView>(R.id.stendents_count)
        studentCountView.text = "عدد الطلاب: $studentscount"

        //////////////////////////////////////////////////////////////////////////////////////////// Use spinners to update scroll view table
        val spinnerYears = findViewById<Spinner>(R.id.years_spinner)
        var selectedYear: String? = null
        val spinnerMonths = findViewById<Spinner>(R.id.months_spinner)
        var selectedMonth: String? = null

        // SET By default the current year and month
        // Array of years and months from your string resources
        val yearsArray = resources.getStringArray(R.array.years)
        val monthsArray = resources.getStringArray(R.array.months)

        // Create an ArrayAdapter for the years Spinner
        val adapterYears = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearsArray)
        adapterYears.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYears.adapter = adapterYears

        // Create an ArrayAdapter for the months Spinner
        val adapterMonths = ArrayAdapter(this, android.R.layout.simple_spinner_item, monthsArray)
        adapterMonths.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonths.adapter = adapterMonths

        // Get the current year and month
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) // 0-based (January = 0)

        // Find the index of the current year in the yearsArray
        val indexOfCurrentYear = yearsArray.indexOf(currentYear.toString())

        // Find the index of the current month (increment by 1 to align with the string array)
        val indexOfCurrentMonth = currentMonth // No need for adjustment as array is 0-based

        // Set the current year as the selected item in the Spinner
        if (indexOfCurrentYear >= 0) {
            spinnerYears.setSelection(indexOfCurrentYear)
        }

        // Set the current month as the selected item in the Spinner
        if (indexOfCurrentMonth >= 0) {
            spinnerMonths.setSelection(indexOfCurrentMonth)
        }
        ////////////////////////////////////////////////////////////

        /////////// Add table first time

        val scrollView = findViewById<ScrollView>(R.id.allstudentstable)

        val horizontalScrollView = HorizontalScrollView(this@MainActivity)

        horizontalScrollView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val tableLayout = TableLayout(this@MainActivity)
        tableLayout.removeAllViews()
        //title
        section_addTitles(tableLayout)

        // elements
        var Id = 1
        for (student_i in teacher.students) {
            // get absence, grade and total grade
            get_absence_grade_count(student_i, "كل السنوات", "كل الأشهر") { absenceCount, total, grade ->
                val absence ="$absenceCount من أصل : $total"
                section_createTableRow(student_i, Id,  absence, total, grade, tableLayout)
                Id+=1
            }
        }
        // update table after selecting spinner

        spinnerYears.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?,selectedItemView: View?, position: Int,id: Long) {
                selectedYear = parentView?.getItemAtPosition(position).toString()
                tableLayout.removeAllViews()
                section_addTitles(tableLayout)
                var Id_0 = 1
                for (student_i in teacher.students) {
                    // get absence, grade and total grade
                    get_absence_grade_count(student_i, selectedYear?:"كل السنوات", selectedMonth?: "كل الأشهر") { absenceCount, total, grade ->
                        val absence ="$absenceCount من أصل : $total"
                        section_createTableRow(student_i, Id_0,  absence, total, grade, tableLayout)
                        Id_0+=1
                    }
                }
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
        spinnerMonths.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?,position: Int, id: Long) {
                selectedMonth = parentView?.getItemAtPosition(position).toString()
                tableLayout.removeAllViews()
                section_addTitles(tableLayout)
                var Id_1 = 1
                for (student_i in teacher.students) {
                    // get absence, grade and total grade
                    get_absence_grade_count(student_i, selectedYear?:"كل السنوات", selectedMonth?: "كل الأشهر") { absenceCount, total, grade ->
                        val absence ="$absenceCount من أصل : $total"
                        section_createTableRow(student_i, Id_1,  absence, total, grade,tableLayout)
                        Id_1+=1
                    }
                }
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
        horizontalScrollView.post {
            horizontalScrollView.scrollTo(2000, 0)
        }
        scrollView.removeAllViews()
        horizontalScrollView.addView(tableLayout)
        scrollView.addView(horizontalScrollView)

    }

    fun section_addTitles(tableLayout : TableLayout) {
        val row = TableRow(this@MainActivity)
        val cardView = CardView(this@MainActivity)
        val innerLayout = LinearLayout(this@MainActivity)
        var titles: Array<String>
        titles = arrayOf(" ", "الاسم", "العمر","الحفظ", "الغياب")
        val ID_size  = 100
        val name_size = 400
        val Age_size = 150
        val Grade_size = 300
        val absence_size = 300
        var titlessizes: IntArray
        // Titles
        titlessizes = intArrayOf(ID_size, name_size, Age_size, Grade_size, absence_size)

        for (i in titles.indices) {
            val textView = TextView(this@MainActivity)
            innerLayout.gravity = Gravity.END
            textView.text = titles[i]
            textView.setTextColor(resources.getColor(R.color.bottom_white))
            textView.setBackgroundColor(resources.getColor(R.color.teal_700))

            textView.setTypeface(null, Typeface.BOLD)
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.layoutParams = LinearLayout.LayoutParams(
                titlessizes[i],
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView.textDirection = View.TEXT_DIRECTION_RTL
            innerLayout.addView(textView, 0)
        }

        cardView.addView(innerLayout)
        row.addView(cardView)
        tableLayout.addView(row)

        // Add underline after the titles
        val titleRowUnderline = View(this@MainActivity)
        titleRowUnderline.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            2
        )
        titleRowUnderline.setBackgroundColor(Color.BLACK)
        tableLayout.addView(titleRowUnderline)
    }
    fun get_absence_grade_count(student: Student, selectedYear: String, selectedMonth:String,
                                callback: (absence: Int, total: Int, grade: Int) -> Unit) {
        var arabic_month = when (selectedMonth) {
            "كانون الثاني" -> "01"
            "شباط" -> "02"
            "آذار" -> "03"
            "نيسان" -> "04"
            "أيار" -> "05"
            "حزيران" -> "06"
            "تموز" -> "07"
            "آب" -> "08"
            "أيلول" -> "09"
            "تشرين الأول" -> "10"
            "تشرين الثاني" -> "11"
            "كانون الأول" -> "12"
            else -> "كل الأشهر"
        }
        var arabic_month_int = when (selectedMonth) {
            "كانون الثاني" -> 1
            "شباط" -> 2
            "آذار" -> 3
            "نيسان" -> 4
            "أيار" -> 5
            "حزيران" -> 6
            "تموز" -> 7
            "آب" -> 8
            "أيلول" -> 9
            "تشرين الأول" -> 10
            "تشرين الثاني" -> 11
            "كانون الأول" ->  12
            else -> 0
        }

        // Fetch absence
        var presentCount = 0
        var absentCount = 0
        val attendanceList: MutableList<AttendanceItem> = mutableListOf()
        for (attendance in student.attendanceHistory) {
            val date = attendance.date.replace("-", "/")
            val dateObj: Date = dateFormat.parse(date)!!
            val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)
            val year = yearFormat.format(dateObj)
            val monthFormat = SimpleDateFormat("MM", Locale.ENGLISH)
            val month = monthFormat.format(dateObj)
            val dayFormat = SimpleDateFormat("EEEE", Locale("ar"))
            val day = dayFormat.format(dateObj)
            if (((year == selectedYear) || (selectedYear == "كل السنوات"))&&((month == arabic_month) || (selectedMonth == "كل الأشهر"))) {
                val status = attendance.status
                val attendanceItem = AttendanceItem(date, day, status)
                attendanceList.add(attendanceItem)
            }
        }
        for (attendanceItem in attendanceList) {

            val linearLayout = LinearLayout(this@MainActivity)
            val textView = TextView(this@MainActivity)
            textView.text = "${attendanceItem.day}، ${attendanceItem.date}"
            textView.textSize = 16f
            // Create an ImageView for the icon
            if (attendanceItem.status == 1) {
                presentCount++
            } else {
                absentCount++
            }
        }

        val total =  absentCount + presentCount

        // Fetch grade data
        var month_score = 0
        // Fetch grade history for each year and month
        for (year in 2020..2030) {
            for (month in 1..12) {
                if (((year.toString() == selectedYear) || (selectedYear == "كل السنوات"))&&((month == arabic_month_int) || (arabic_month_int == 0))) {
                    var monthProgressCount = 0
                    for (gradeHistoryitem in student.gradeHistory) {
                        val currentYear = gradeHistoryitem.first
                        val databaseMonth = gradeHistoryitem.second
                        if ((year == currentYear) && (databaseMonth == month)) {
                            monthProgressCount += gradeHistoryitem.third
                        }
                    }
                    if (monthProgressCount > 0) {
                        month_score += monthProgressCount
                    }
                }
            }
        }
        callback(absentCount, total, month_score)
    }
    fun section_createTableRow(student: Student, id: Int, absence:String, total: Int, grade :Int,
                               tableLayout : TableLayout) {
        // Create a TableRow for each student
        val row = TableRow(this@MainActivity)
        val cardView = CardView(this@MainActivity)
        val cardViewParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            120
        )

        cardView.layoutParams = cardViewParams
        val innerLayout = LinearLayout(this@MainActivity)
        // calculate age _ birthday
        val birthdayDate = dateFormat.parse(student.birthday.replace("-", "/"))
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        val age = getAge(birthdayDate, currentDate)

        // Create TextViews for student data

        val columnsinformation = arrayOf(id.toString(), student.name, age, pages_to_jiz2(grade), absence)
        val ID_size  = 100
        val name_size = 400
        val Age_size = 150
        val Grade_size = 300
        val absence_size = 300
        val titlessizes = intArrayOf(ID_size, name_size, Age_size, Grade_size, absence_size)

        for (i in columnsinformation.indices) {
            val textView = TextView(this@MainActivity)
            textView.text = columnsinformation[i].toString()
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.layoutParams = LinearLayout.LayoutParams(
                titlessizes[i],
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            textView.textDirection = View.TEXT_DIRECTION_RTL // arabic direction
            textView.gravity = Gravity.CENTER_VERTICAL
            innerLayout.addView(textView, 0)
        }

        cardView.addView(innerLayout)
        row.addView(cardView)
        tableLayout.addView(row)

        // Add underline after each row
        val rowUnderline = View(this@MainActivity)
        rowUnderline.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            2
        )
        rowUnderline.setBackgroundColor(Color.BLACK)
        tableLayout.addView(rowUnderline)
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////   Student Grade / Grade History
    private fun to_Student_Grade_layout(username: String, status: String, student : Student,
                                        all_table: String, studentspinnerIndex: Int) {
        GlobalstudentspinnerIndex = studentspinnerIndex
        setContentView(R.layout.show_student_layout)
        currentLayout = "show_student_grade_layout"
        currentUserStatus = status
        currentUser = username
        currentBranch = student.branch
        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = when (status) {
            "student" -> "طالب - متابعة الحفظ"
            "teacher" -> "أستاذ - متابعة الحفظ"
            "supervisor" -> "مشرف - متابعة الحفظ"
            "director" -> "مدير - متابعة الحفظ"
            else -> "Unknown Status"
        }
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            when (status) {
                "teacher" -> to_Teacher_Layout(username, userBranch, studentspinnerIndex)
                "supervisor" -> to_Supervisor_Monitoring_Students(username, currentBranch, all_table_supervisor)
                "director" -> to_Director_Monitoring_Students(username, all_table_data)
                "student" -> to_Student_Layout(username, userBranch)
            }
        }

        // Fill top view
        val sectionTopView: TextView = findViewById(R.id.section)
        sectionTopView.text = student.section
        val branchTopView: TextView = findViewById(R.id.branch)
        branchTopView.text = "فرع : ${student.branch}"
        val studentname : TextView = findViewById(R.id.studentname)
        studentname.text= student.name
        val imageViewInfo : ImageView = findViewById(R.id.imageViewInfo)
        if (student.gender == "أنثى") {
            imageViewInfo.setImageResource(R.drawable.girlicon)
        }
        else {
            imageViewInfo.setImageResource(R.drawable.boyicon)
        }
        val studentagetopview : TextView = findViewById(R.id.studentage)

        val birthdayDate = dateFormat.parse(student.birthday)
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        val age = getAge(birthdayDate, currentDate)
        val year = when {
            (age < 11 || age >= 100) -> "سنوات"
            (age in 11..99) -> "سنة" // Adjust this condition as needed
            else -> "سنوات"
        }

        studentagetopview.text = "العمر: $age $year"
        val teachername: TextView = findViewById(R.id.teachername)

        teachername.text = "الأستاذ(ة): ${student.teacherName}"

        //////////////////////////////////////////////////////////////////////////////////////////// Select (level or history) grade
        val grade_history_btn = findViewById<Button>(R.id.gradehistorybtn)
        val grade_level_btn = findViewById<Button>(R.id.gradelevelbtn)

        // first time before clicking the choice (grade : Level / History)
        student_Grade_Level(username, status, student)

        // choose Level btn
        grade_level_btn.setOnClickListener {
            grade_Level_or_History = "Grade_Level"
            grade_history_btn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            grade_history_btn.setTextColor(ContextCompat.getColor(this, R.color.teal_700))
            grade_level_btn.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700))
            grade_level_btn.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            student_Grade_Level(username, status, student)
        }

        // choose History btn
        grade_history_btn.setOnClickListener {
            grade_Level_or_History = "Grade_History"
            grade_history_btn.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700))
            grade_history_btn.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            grade_level_btn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            grade_level_btn.setTextColor(ContextCompat.getColor(this, R.color.teal_700))
            student_Grade_History(student)
        }
    }
    fun student_Grade_Level (username: String, status: String, student: Student){
        val Gradescrollview: ScrollView = findViewById(R.id.gradescrollview)
        Gradescrollview.removeAllViews()
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val firstRowLayout = LinearLayout(this)
        firstRowLayout.orientation = LinearLayout.HORIZONTAL
        firstRowLayout.gravity = Gravity.CENTER_HORIZONTAL

        for (itemLabel in listOf("جزء عم", "جزء تبارك"))
        {
            val singleItemLayout = LinearLayout(this)
            singleItemLayout.orientation = LinearLayout.VERTICAL
            singleItemLayout.gravity = Gravity.CENTER  // Center items vertically

            val loadingCircle = LoadingCircleOnImage(this, null)
            if (itemLabel == "جزء عم") {
                val elementAtIndex29 = student.gradeLevel[29]
                loadingCircle.setPercentage(elementAtIndex29)
                if (status == "teacher") {
                    loadingCircle.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View) {
                            dialogGrade {newGrade, date ->
                                teacherChangeGrade(username, student, 29,newGrade, date){done_error, student->
                                    if(done_error == "done"){
                                        to_Student_Grade_layout(username, status, student, "", 0)
                                    }
                               }
                           }
                        }
                    })
                }
            }
            else{
                val elementAtIndex28 = student.gradeLevel[28]
                loadingCircle.setPercentage(elementAtIndex28)

                if (status == "teacher") {
                    loadingCircle.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View) {
                            dialogGrade { newGrade, date ->
                                teacherChangeGrade(username, student, 28,newGrade, date){done_error, student->
                                    if(done_error == "done"){
                                        to_Student_Grade_layout(username, status, student, "", 0)
                                    }
                                }
                            }
                        }
                    })
                }
            }
            val circleParams = LinearLayout.LayoutParams(300, 300)
            loadingCircle.layoutParams = circleParams
            singleItemLayout.addView(loadingCircle)

            val textView = TextView(this)
            textView.text = itemLabel
            textView.textSize = 15f
            val textParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView.layoutParams = textParams
            textView.setTypeface(null, Typeface.BOLD)
            singleItemLayout.addView(textView)

            firstRowLayout.addView(singleItemLayout)
        }
        linearLayout.addView(firstRowLayout)
        for (row in 1 until 11)
        {
            val rowLayout = LinearLayout(this)
            rowLayout.orientation = LinearLayout.HORIZONTAL
            rowLayout.gravity = Gravity.CENTER_HORIZONTAL  // Center items horizontally

            val numItemsInRow = if (row == 10) 1 else 3

            // Rows with items 1 to 27
            for (column in (row - 1) * 3 + 1 until (row - 1) * 3 + numItemsInRow + 1) {
                val itemLabel = "الجزء $column"
                val singleItemLayout = LinearLayout(this)
                singleItemLayout.orientation = LinearLayout.VERTICAL
                singleItemLayout.gravity = Gravity.CENTER  // Center items vertically

                val loadingCircle = LoadingCircleOnImage(this, null)
                val elementAtIndexi = student.gradeLevel[column-1]

                loadingCircle.setPercentage(elementAtIndexi)
                val circleParams = LinearLayout.LayoutParams(300, 300)
                loadingCircle.layoutParams = circleParams
                singleItemLayout.addView(loadingCircle)

                val textView = TextView(this)
                textView.text = itemLabel
                textView.textSize = 15f
                val textParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                textView.layoutParams = textParams
                textView.setTypeface(null, Typeface.BOLD)
                singleItemLayout.addView(textView)
                rowLayout.addView(singleItemLayout)
                /////////////////////////////////////////////////////////////////// If teacher : add new grade

                if (status == "teacher") {
                    loadingCircle.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View) {
                            dialogGrade{ newGrade, date ->
                                teacherChangeGrade(username, student, column - 1, newGrade, date){done_error, student->
                                    if(done_error == "done"){
                                        to_Student_Grade_layout(username, status, student, "", 0)
                                    }
                                }
                            }
                        }
                    })
                }
            }
            linearLayout.addView(rowLayout)
        }
        Gradescrollview.addView(linearLayout)
    }
    fun student_Grade_History(student: Student) {
        val Gradescrollview: ScrollView = findViewById(R.id.gradescrollview)
        Gradescrollview.removeAllViews()
        // Sort the data in descending order by year and month

        val sortedGradeHistory = student.gradeHistory.sortedWith(compareByDescending<Triple<Int, Int, Int>> { it.first }.thenByDescending { it.second })
        val tableLayout = TableLayout(this@MainActivity)
        // Group data by year
        val groupedByYear = sortedGradeHistory.groupBy { it.first }

        for ((year, dataForYear) in groupedByYear) {
            val yearTitle = TextView(this@MainActivity)
            yearTitle.text = "           $year  \n"
            // Set text style
            yearTitle.setTypeface(null, Typeface.BOLD)
            yearTitle.setTextColor(ContextCompat.getColor(this@MainActivity, R.color.teal_700))
            yearTitle.textSize = 22f
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.gravity = Gravity.END
            params.layoutDirection = View.LAYOUT_DIRECTION_LTR
            yearTitle.layoutParams = params
            val linearLayout = LinearLayout(this@MainActivity)
            linearLayout.orientation = LinearLayout.VERTICAL
            linearLayout.setPadding(16, 24, 24, 16)
            linearLayout.addView(yearTitle)

            val maxScoreForYear = dataForYear.filter { it.third > 0 }.maxOfOrNull { it.third } ?: 0

            for ((_, month, monthProgressCount) in dataForYear) {
                val monthLabel = TextView(this@MainActivity)
                monthLabel.text = "${getArabicMonthName(month)} "
                val monthscore = TextView(this@MainActivity)
                val prefix = when {
                    monthProgressCount == 1 -> "صفحة"
                    monthProgressCount <= 10 -> "صفحات"
                    else -> "صفحة"
                }
                monthscore.text = "  $monthProgressCount  $prefix"
                val progressBar = View(this@MainActivity)
                val progressBarColor = ContextCompat.getColor(this@MainActivity,R.color.colorMale)
                progressBar.setBackgroundColor(progressBarColor)
                // Add a margin to the progress bar
                val progressBarLayoutParams = LinearLayout.LayoutParams(
                    calculateProgressBarWidth(monthProgressCount, maxScoreForYear),
                    30
                )
                progressBar.layoutParams = progressBarLayoutParams
                // Wrap the progress bar with a CardView for rounded corners
                val cardView = CardView(this@MainActivity)
                cardView.radius = 50f
                cardView.addView(progressBar)
                val horizontalLayout = LinearLayout(this@MainActivity)
                horizontalLayout.orientation = LinearLayout.HORIZONTAL
                horizontalLayout.layoutDirection = View.LAYOUT_DIRECTION_RTL
                horizontalLayout.setPadding(24, 0, 16, 16)
                horizontalLayout.gravity = Gravity.CENTER_VERTICAL
                horizontalLayout.addView(monthLabel, LinearLayout.LayoutParams(200, LinearLayout.LayoutParams.WRAP_CONTENT))
                horizontalLayout.addView(cardView)
                horizontalLayout.addView(monthscore, LinearLayout.LayoutParams(220, LinearLayout.LayoutParams.WRAP_CONTENT))
                linearLayout.addView(horizontalLayout)
            }

            tableLayout.addView(linearLayout)
        }
        Gradescrollview.addView(tableLayout)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////   Student Attendence
    private fun to_Student_Attendence_layout(username: String, status: String,
                                             student : Student, all_table: String, studentspinnerIndex: Int) {
        setContentView(R.layout.student_attendance_layout)
        GlobalstudentspinnerIndex = studentspinnerIndex
        currentLayout = "show_student_attendance_layout"
        currentUserStatus = status
        currentUser = username
        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = when (status) {
            "student" -> "طالب - متابعة الحضور"
            "teacher" -> "أستاذ - متابعة الحضور"
            "supervisor" -> "مشرف - متابعة الحضور"
            "director" -> "مدير - متابعة الحضور"
            else -> "Unknown Status"
        }

        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            when (status) {
                "teacher" -> to_Teacher_Layout(username, userBranch, GlobalstudentspinnerIndex)
                "supervisor" -> to_Supervisor_Monitoring_Students(username, currentBranch, all_table_supervisor)
                "director" -> to_Director_Monitoring_Students(username, all_table_data)
                "student" -> to_Student_Layout(username, userBranch)
            }
        }
        // Fill top view
        val sectionTopView: TextView = findViewById(R.id.section)
        sectionTopView.text = student.section
        val branchTopView: TextView = findViewById(R.id.branch)
        branchTopView.text = "فرع : ${student.branch}"
        val studentname: TextView = findViewById(R.id.studentname)
        studentname.text = student.name
        // get the teacher name knowing the section
        val imageViewInfo: ImageView = findViewById(R.id.imageViewInfo)
        if (student.gender == "أنثى") {
            imageViewInfo.setImageResource(R.drawable.girlicon)
        } else {
            imageViewInfo.setImageResource(R.drawable.boyicon)
        }
        val studentagetopview: TextView = findViewById(R.id.studentage)
        val birthdayDate = dateFormat.parse(student.birthday)
        val calendar = Calendar.getInstance()
        val currentDate = calendar.time
        val age = getAge(birthdayDate, currentDate)
        val year = when {
            (age < 11 || age >= 100) -> "سنوات"
            (age in 11..99) -> "سنة" // Adjust this condition as needed
            else -> "سنوات"
        }
        studentagetopview.text = "العمر: $age $year"
        val teachername: TextView = findViewById(R.id.teachername)
        teachername.text = "الأستاذ(ة): ${student.teacherName}"
        //////////////////////////////////////////////////////////////////////////////////////////// show attendance
        val spinnerYears = findViewById<Spinner>(R.id.years_spinner)
        val spinnerMonths = findViewById<Spinner>(R.id.months_spinner)

                                                            // SET By default the current year and month
        // Array of years and months from your string resources
        val yearsArray = resources.getStringArray(R.array.years)
        val monthsArray = resources.getStringArray(R.array.months)

        // Create an ArrayAdapter for the years Spinner
        val adapterYears = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearsArray)
        adapterYears.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYears.adapter = adapterYears

        // Create an ArrayAdapter for the months Spinner
        val adapterMonths = ArrayAdapter(this, android.R.layout.simple_spinner_item, monthsArray)
        adapterMonths.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonths.adapter = adapterMonths

        // Get the current year and month
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) // 0-based (January = 0)

        // Find the index of the current year in the yearsArray
        val indexOfCurrentYear = yearsArray.indexOf(currentYear.toString())

        // Find the index of the current month (increment by 1 to align with the string array)
        val indexOfCurrentMonth = currentMonth // No need for adjustment as array is 0-based

        // Set the current year as the selected item in the Spinner
        if (indexOfCurrentYear >= 0) {
            spinnerYears.setSelection(indexOfCurrentYear)
        }

        // Set the current month as the selected item in the Spinner
        if (indexOfCurrentMonth >= 0) {
            spinnerMonths.setSelection(indexOfCurrentMonth)
        }
        ////////////////////////////////////////////////////////////



        var selectedYear: String? = null
        var selectedMonth: String? = null
        spinnerYears.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                selectedYear = parentView?.getItemAtPosition(position).toString()
                update_show_attendance(
                    student.attendanceHistory,
                    selectedYear ?: "كل السنوات",
                    selectedMonth ?: "كل الأشهر"
                )
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }
        spinnerMonths.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                selectedMonth = parentView?.getItemAtPosition(position).toString()
                update_show_attendance(
                    student.attendanceHistory,
                    selectedYear ?: "كل السنوات",
                    selectedMonth ?: "كل الأشهر"
                )
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }
    }
    fun update_show_attendance(attendanceHistory: MutableList<AttendanceItem>, selectedYear: String,
                               selectedMonth : String){
        var arabic_month = when (selectedMonth) {
            "كانون الثاني" -> "01"
            "شباط" -> "02"
            "آذار" -> "03"
            "نيسان" -> "04"
            "أيار" -> "05"
            "حزيران" -> "06"
            "تموز" -> "07"
            "آب" -> "08"
            "أيلول" -> "09"
            "تشرين الأول" -> "10"
            "تشرين الثاني" -> "11"
            "كانون الأول" -> "12"
            else -> "كل الأشهر"
        }
        val attendancescrollview: ScrollView = findViewById(R.id.attendancescrollview)
        var presentCount = 0
        var absentCount = 0
        attendanceHistory.sortByDescending { SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(it.date) }
        var tableLayout = TableLayout(this@MainActivity)
        for (attendanceItem in attendanceHistory) {
            val date = attendanceItem.date
            val dateObj: Date = dateFormat.parse(date.replace("-", "/"))!!
            val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)
            val year = yearFormat.format(dateObj)
            val monthFormat = SimpleDateFormat("MM", Locale.ENGLISH)
            val month = monthFormat.format(dateObj)
            if (((year == selectedYear) || (selectedYear == "كل السنوات"))&&((month == arabic_month) || (arabic_month == "كل الأشهر"))) {
                val linearLayout = LinearLayout(this@MainActivity)
                val textView = TextView(this@MainActivity)
                textView.text = "${attendanceItem.day}، ${attendanceItem.date}"
                textView.textSize = 16f
                // Create an ImageView for the icon
                val iconImageView = ImageView(this@MainActivity)
                if (attendanceItem.status == 1) {
                    iconImageView.setImageResource(R.drawable.icon_present)
                    presentCount++
                } else {
                    iconImageView.setImageResource(R.drawable.icon_absecnt)
                    absentCount++
                }
                // Set layout parameters for ImageView
                val iconParams = LinearLayout.LayoutParams(
                    60,  // Width of the ImageView
                    60   // Height of the ImageView
                )
                iconParams.marginStart = 24
                iconParams.marginEnd = 24
                iconImageView.layoutParams = iconParams
                // Add the TextView and ImageView to the LinearLayout
                linearLayout.orientation = LinearLayout.HORIZONTAL
                linearLayout.gravity = Gravity.END
                linearLayout.setPadding(16, 16, 16, 16) // Adjust as needed
                linearLayout.addView(textView)
                linearLayout.addView(iconImageView)
                tableLayout.addView(linearLayout)
            }
        }
        attendancescrollview.removeAllViews()
        attendancescrollview.addView(tableLayout)
        // update red/green bar
        val title_presence = findViewById<TextView>(R.id.title_presence)
        val title_absence = findViewById<TextView>(R.id.title_absence)
        val total =  absentCount + presentCount
        title_absence.text = "مرات الغياب : $absentCount (من أصل $total)"
        title_presence.text =  "مرات الحضور : $presentCount (من أصل $total)"
        val presence_green = findViewById<View>(R.id.presence_green)
        val presence_gray = findViewById<View>(R.id.presence_gray)
        val greenLayoutParams = presence_green.layoutParams as LinearLayout.LayoutParams
        val grayLayoutParams = presence_gray.layoutParams as LinearLayout.LayoutParams
        greenLayoutParams.weight = (presentCount).toFloat()
        grayLayoutParams.weight = (total-presentCount).toFloat()
        presence_green.layoutParams = greenLayoutParams
        presence_gray.layoutParams = grayLayoutParams

        val absence_red = findViewById<View>(R.id.absence_red)
        val absence_gray = findViewById<View>(R.id.absence_gray)
        val redLayoutParams = absence_red.layoutParams as LinearLayout.LayoutParams
        val absencegrayLayoutParams = absence_gray.layoutParams as LinearLayout.LayoutParams
        redLayoutParams.weight = (absentCount).toFloat()
        absencegrayLayoutParams.weight =(total-absentCount).toFloat()
        absence_red.layoutParams = redLayoutParams
        absence_gray.layoutParams = absencegrayLayoutParams
    }


////////////////////////////////////////////////////////////////////////////////////////////////////  Supervisor
    /////////////////////////////////////////////////////// Read write database
    fun readLASTUPDATEData_Supervisror(): String {
    val sharedPreferences = getSharedPreferences("supervisor_last_update", Context.MODE_PRIVATE)
    val lastUpdate = sharedPreferences.getString("last_update", null)
    return lastUpdate.toString()
}
    fun save_all_table_Supervisor(Supervisor_branch : String, callback: (Boolean) -> Unit) {
        val stdQueryFire = database.getReference("branches")
        stdQueryFire.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val gson = Gson()
                val branchesMap = mutableMapOf<String, JR_Branch>()

                for (branchSnapshot in dataSnapshot.children) {
                    val branchName = branchSnapshot.key.toString()
                    if (Supervisor_branch == branchName){
                        val sectionMap = mutableMapOf<String, JR_Section>()

                        for (sectionSnapshot in branchSnapshot.child("sections").children) {
                            val sectionName = sectionSnapshot.key.toString()
                            val studentsList = mutableListOf<JR_Student?>()
                            val teachersList = mutableListOf<JR_Teacher?>()


                            for (studentSnapshot in sectionSnapshot.child("students").children) {
                                val studentData = studentSnapshot.getValue(JR_Student::class.java)
                                val gradeHistoryMap = mutableMapOf<String, JR_grade_history_item>()

                                for (grade_historySnapshot in studentSnapshot.child("grade_history").children) {
                                    val date = grade_historySnapshot.key.toString()
                                    val grade_history_item = mutableMapOf<String, Int>()

                                    for (assignmentSnapshot in grade_historySnapshot.children) {
                                        val assignmentNumber = assignmentSnapshot.key.toString()
                                        val grade = assignmentSnapshot.getValue(Int::class.java)
                                            ?: 0 // Assuming default value is 0 if grade is null
                                        grade_history_item[assignmentNumber] = grade
                                    }

                                    gradeHistoryMap[date] = JR_grade_history_item(grade_history_item)
                                }

                                studentData?.grade_history =
                                    JR_grade_history(gradeHistoryMap) // Creating JR_grade_history instance
                                studentsList.add(studentData)
                            }


                            for (teacherSnapshot in sectionSnapshot.child("teachers").children) {
                                val teacherData = teacherSnapshot.getValue(JR_Teacher::class.java)
                                teachersList.add(teacherData)
                            }

                            val section = JR_Section(studentsList, teachersList)
                            sectionMap[sectionName] = section
                        }

                        val supervisorsList = mutableListOf<JR_Supervisor?>()
                        for (supervisorSnapshot in branchSnapshot.child("supervisors").children) {
                            val supervisorData = supervisorSnapshot.getValue(JR_Supervisor::class.java)
                            supervisorsList.add(supervisorData)
                        }

                        val branch = JR_Branch(sectionMap, supervisorsList)
                        branchesMap[branchName] = branch
                    }
                }

                lifecycleScope.launch {
                    try {
                        val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                        val schoolData = JR_SchoolData(branchesMap)
                        val jsonData = gson.toJson(schoolData)
                        val branchesEntity = branchesEntity(1, jsonData)
                        branchesDao.insertbranches(branchesEntity)
                        // Save date
                        val currentTime = System.currentTimeMillis()
                        val formattedDateTime =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                                Date(currentTime)
                            )
                        saveLASTUPDATEData_Supervisor(formattedDateTime)
                        callback(true)
                    } catch (e: Exception) {
                        showToast(e.message.toString())
                        callback(false)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled event if needed
                callback(false)
            }
        })
    }
    fun saveLASTUPDATEData_Supervisor(last_update: String) {
        val sharedPreferences = getSharedPreferences("supervisor_last_update", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("last_update", last_update)
        editor.apply()
    }
    fun clear_Supervisor_data() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@MainActivity)
                // Invoke the method to remove all directors
                database.branchesDao().removeAllBranches()
            } catch (e: Exception) {
                showToast("Failed to remove all branches: ${e.message}")
            }
        }
    }
    ////////////////////////////////////////////////////// go to sub layouts
    private fun to_Supervisor_Layout(username: String, branch: String) {
        setContentView(R.layout.supervisor_layout)
        currentLayout = "supervisor_layout"
        currentUser = username
        currentUserStatus = "supervisor"
        currentBranch = branch
        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val toolbarTitle : TextView=findViewById(R.id.toolbarTitle)
        toolbarTitle.text="مشرف"
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        // get the branch
        val branchtopview: TextView = findViewById(R.id.branch)
        val nametopview: TextView = findViewById(R.id.supervisorname)

        AppDatabase.getDatabase(this@MainActivity)
        val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
        lifecycleScope.launch {
            try {
                val supervisor_all_table = branchesDao.getbranchesTable(1)
                if (supervisor_all_table == null) {
                    save_all_table_Supervisor(branch){_->
                        currentBranch = branch
                        showToast("تم تحديث البيانات")
                        nametopview.text = username
                        branchtopview.text = "فرع : $branch"
                        // refresh btn
                        val update_btn = findViewById<Button>(R.id.updateBtn)
                        update_btn.setOnClickListener{
                            save_all_table_Supervisor(currentBranch) { _ ->
                                lifecycleScope.launch {
                                    try {
                                        AppDatabase.getDatabase(this@MainActivity)
                                        val branchesDao =
                                            AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                        all_table_supervisor = branchesDao.getbranchesTable(1).toString()
                                        showToast("تم تحديث البيانات")
                                        to_Supervisor_Layout(username, currentBranch)
                                    } catch (e: Exception) {
                                        showToast(e.message.toString())
                                    }
                                }
                            }
                        }
                        // Statistics btn
                        val statisticsbtn: RelativeLayout = findViewById(R.id.supervisorstatisticsbtn)
                        statisticsbtn.setOnClickListener {
                            lifecycleScope.launch {
                                try {
                                    AppDatabase.getDatabase(this@MainActivity)
                                    val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                    all_table_supervisor= branchesDao.getbranchesTable(1).toString()
                                    to_Supervisor_Statistics_Layout(username, branch, all_table_supervisor)
                                } catch (e: Exception) {
                                    showToast(e.message.toString())
                                }
                            }
                        }
                            // Students Monitor btn
                            val monitoringstudents: RelativeLayout = findViewById(R.id.supervisormonitoringstudents)
                            monitoringstudents.setOnClickListener {
                                lifecycleScope.launch {
                                    try {
                                        AppDatabase.getDatabase(this@MainActivity)
                                        val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                        all_table_supervisor = branchesDao.getbranchesTable(1)?:""
                                        to_Supervisor_Monitoring_Students(username, branch, all_table_supervisor)
                                    } catch (e: Exception) {
                                        showToast(e.message.toString())
                                    }
                                }
                            }

                           // Sections Monitor btn
                           val monitoringSections: RelativeLayout = findViewById(R.id.monitoringsections)
                           monitoringSections.setOnClickListener {
                               lifecycleScope.launch {
                                   try {
                                       AppDatabase.getDatabase(this@MainActivity)
                                       val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                       all_table_supervisor = branchesDao.getbranchesTable(1)?:""
                                       to_Supervisor_Monitoring_Sections(username, branch, all_table_supervisor)
                                   } catch (e: Exception) {
                                       showToast(e.message.toString())
                                   }
                               }
                           }

                          // Sections topList btn
                          val toplist_btn: RelativeLayout = findViewById(R.id.toplist_btn)
                          toplist_btn.setOnClickListener {
                              lifecycleScope.launch {
                                  try {
                                      AppDatabase.getDatabase(this@MainActivity)
                                      val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                      all_table_supervisor = branchesDao.getbranchesTable(1)?:""
                                      to_Supervisor_Toplist_Layout(username, branch, all_table_supervisor)
                                  } catch (e: Exception) {
                                      showToast(e.message.toString())
                                  }
                              }
                          }
                    }
                }
                else{
                    val schoolData = Gson().fromJson(supervisor_all_table, JR_SchoolData::class.java)
                    currentBranch = schoolData.branches.keys.first()
                    branchtopview.text ="فرع : ${schoolData.branches.keys.first()}"
                    nametopview.text = username
                    val last_update_data = readLASTUPDATEData_Supervisror()
                    val last_update_textview = findViewById<TextView>(R.id.last_update)
                    if (last_update_data != null) {
                        val lastUpdateDate_stored = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(last_update_data)
                        val formattedTimeDifference_stored_current =formatTimeDifference(System.currentTimeMillis() - lastUpdateDate_stored.time)
                        last_update_textview.text = formattedTimeDifference_stored_current
                    }
                    // refresh btn
                    val update_btn = findViewById<Button>(R.id.updateBtn)
                    update_btn.setOnClickListener{
                        save_all_table_Supervisor(currentBranch) { _ ->
                            lifecycleScope.launch {
                                try {
                                    AppDatabase.getDatabase(this@MainActivity)
                                    val branchesDao =
                                        AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                    all_table_supervisor = branchesDao.getbranchesTable(1).toString()
                                    showToast("تم تحديث البيانات")
                                    to_Supervisor_Layout(username, currentBranch)
                                } catch (e: Exception) {
                                    showToast(e.message.toString())
                                }
                            }
                        }
                    }
                    // Statistics btn
                    val statisticsbtn: RelativeLayout = findViewById(R.id.supervisorstatisticsbtn)
                    statisticsbtn.setOnClickListener {
                        lifecycleScope.launch {
                            try {
                                AppDatabase.getDatabase(this@MainActivity)
                                val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                val all_table_supervisor = branchesDao.getbranchesTable(1)
                                to_Supervisor_Statistics_Layout(username, currentBranch, all_table_supervisor.toString())
                            } catch (e: Exception) {
                                showToast(e.message.toString())
                            }
                        }
                    }
                    // Students Monitor btn
                    val monitoringstudents: RelativeLayout = findViewById(R.id.supervisormonitoringstudents)
                    monitoringstudents.setOnClickListener {
                        lifecycleScope.launch {
                            try {
                                AppDatabase.getDatabase(this@MainActivity)
                                val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                all_table_supervisor = branchesDao.getbranchesTable(1)?:""
                                to_Supervisor_Monitoring_Students(username, currentBranch, all_table_supervisor)
                            } catch (e: Exception) {
                                showToast(e.message.toString())
                            }
                        }
                    }
                    // Sections Monitor btn
                    val monitoringSections: RelativeLayout = findViewById(R.id.monitoringsections)
                    monitoringSections.setOnClickListener {
                        lifecycleScope.launch {
                            try {
                                AppDatabase.getDatabase(this@MainActivity)
                                val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                all_table_supervisor = branchesDao.getbranchesTable(1)?:""
                                to_Supervisor_Monitoring_Sections(username, currentBranch, all_table_supervisor)
                            } catch (e: Exception) {
                                showToast(e.message.toString())
                            }
                        }
                    }
                    // Sections topList btn
                    val toplist_btn: RelativeLayout = findViewById(R.id.toplist_btn)
                    toplist_btn.setOnClickListener {
                        lifecycleScope.launch {
                            try {
                                AppDatabase.getDatabase(this@MainActivity)
                                val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                all_table_supervisor = branchesDao.getbranchesTable(1)?:""
                                to_Supervisor_Toplist_Layout(username, currentBranch, all_table_supervisor)
                            } catch (e: Exception) {
                                showToast(e.message.toString())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                showToast(e.message.toString())
            }
        }
    }
    private fun to_Supervisor_Statistics_Layout(username: String, branch: String,
                                                all_table_supervisor : String){
        setContentView(R.layout.supervisor_statistics_layout)
        currentLayout = "supervisor_statistics_layout"
        currentUser = username
        currentUserStatus = "supervisor"
        val branchview = findViewById<TextView>(R.id.branch)
        val nametopview = findViewById<TextView>(R.id.supervisorname)
        nametopview.text = username
        val schoolData = Gson().fromJson(all_table_supervisor, JR_SchoolData::class.java)
        currentBranch = branch
        branchview.text = "فرع : ${branch}"
        nametopview.text = username
        //////////////////////////////////////////////////////////////////////////////////////////// Show total number

        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val toolbarTitle : TextView=findViewById(R.id.toolbarTitle)
        toolbarTitle.text="مشرف"
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
             showOptionsMenu(view, username)
        }
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            to_Supervisor_Layout(username, branch)
        }

        //////////////////////////////////////////////////////////////////////////////////////////// Select (numbers or statistics)
        val numbers_btn = findViewById<Button>(R.id.numbersBtn)
        val stat_btn = findViewById<Button>(R.id.stat_Btn)
        // first time before clicking the choice (grade : Level / History)
        Supervisor_numbers_show(username, branch, all_table_supervisor)
        // choose stat_btn
        stat_btn.setOnClickListener {

            numbers_btn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            numbers_btn.setTextColor(ContextCompat.getColor(this, R.color.teal_700))
            stat_btn.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700))
            stat_btn.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            Supervisor_stat_show(username, branch, all_table_supervisor)
        }
        // choose numbers_btn
        numbers_btn.setOnClickListener {
            numbers_btn.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_700))
            numbers_btn.setTextColor(ContextCompat.getColor(this, android.R.color.white))
            stat_btn.setBackgroundColor(ContextCompat.getColor(this, android.R.color.white))
            stat_btn.setTextColor(ContextCompat.getColor(this, R.color.teal_700))
            Supervisor_numbers_show(username, branch, all_table_supervisor)
        }
    }

    fun Supervisor_numbers_show(username: String, branch: String,
                                all_table_supervisor : String) {

        val parentLayout: LinearLayout = findViewById(R.id.parent_linear_layout)
        parentLayout.removeAllViews()
        val cardViewSpinner = CardView(this)
        val cardParamsSpinner = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        cardParamsSpinner.setMargins(16.dpToPx(), 10.dpToPx(), 16.dpToPx(), 0)
        cardParamsSpinner.gravity = Gravity.CENTER

        cardViewSpinner.layoutParams = cardParamsSpinner
        cardViewSpinner.cardElevation = 4.dpToPx().toFloat()
        cardViewSpinner.radius = 8.dpToPx().toFloat()
        cardViewSpinner.setCardBackgroundColor(ContextCompat.getColor(this, android.R.color.white))

        val spinner = Spinner(this)
        val spinnerParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            40.dpToPx()
        )
        spinnerParams.gravity = Gravity.CENTER
        spinnerParams.setMargins(0, 10.dpToPx(), 0, 10.dpToPx())
        spinner.layoutParams = spinnerParams
        spinner.layoutDirection = View.LAYOUT_DIRECTION_RTL

        cardViewSpinner.addView(spinner)
        parentLayout.addView(cardViewSpinner)

        // Create and add TextView for student count dynamically
        val textStudentsCount = TextView(this)
        val textParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            35.dpToPx()
        )
        textParams.setMargins(0, 10.dpToPx(), 0, 0)
        textStudentsCount.layoutParams = textParams
        textStudentsCount.gravity = Gravity.CENTER
        textStudentsCount.textSize = 20f

        parentLayout.addView(textStudentsCount)

        // Create and add PieChartView for student count dynamically
        val pieChartViewStudents = PieChartView(this)
        val pieParamsStudents = LinearLayout.LayoutParams(
            170.dpToPx(),
            170.dpToPx()
        )
        pieParamsStudents.gravity = Gravity.CENTER_HORIZONTAL
        pieChartViewStudents.layoutParams = pieParamsStudents
        parentLayout.addView(pieChartViewStudents)

        // Create and add TextView for teachers count dynamically
        val textTeachersCount = TextView(this)
        val textParamsTeachers = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            35.dpToPx()
        )
        textParamsTeachers.setMargins(0, 5.dpToPx(), 0, 0)
        textTeachersCount.layoutParams = textParamsTeachers
        textTeachersCount.gravity = Gravity.CENTER
        textTeachersCount.textSize = 20f
        parentLayout.addView(textTeachersCount)

        // Create and add PieChartView for teachers count dynamically
        val pieChartViewTeachers = PieChartView(this)
        val pieParamsTeachers = LinearLayout.LayoutParams(
            170.dpToPx(),
            170.dpToPx()
        )
        pieParamsTeachers.gravity = Gravity.CENTER_HORIZONTAL
        pieChartViewTeachers.layoutParams = pieParamsTeachers
        parentLayout.addView(pieChartViewTeachers)

        // Update the section spinner with the retrieved sections
        val schoolData = Gson().fromJson(all_table_supervisor, JR_SchoolData::class.java)
        val sectionsList = mutableListOf<String>()
        sectionsList.add("جميع الحلقات")
        for ((branch_name, branch_node) in schoolData.branches) {
            for ((sectionName, _) in branch_node.sections) {
                sectionsList.add(sectionName)
            }
        }
        val sectionAdapter1 = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, sectionsList)
        sectionAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = sectionAdapter1
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                val selectedSection = parentView?.getItemAtPosition(position).toString()
                // Filter students based on the selected branch and section
                var maleCount = 0
                var femaleCount = 0
                var maleTeacherCount = 0
                var femaleTeacherCount = 0
                val schoolData = Gson().fromJson(all_table_supervisor, JR_SchoolData::class.java)
                var found = false

                for ((branch_name, branch_node) in schoolData.branches) {
                    for ((sectionName, section) in branch_node.sections) {
                        if (((selectedSection == sectionName) || (selectedSection== "جميع الحلقات"))) {
                            found = true
                            for(student in section?.students?: emptyList()){
                                val gender = student?.info?.gender
                                if (gender == "ذكر") {
                                    maleCount++
                                } else if (gender == "أنثى") {
                                    femaleCount++
                                }
                            }
                            val students_totalNumber: Int = maleCount + femaleCount
                            textStudentsCount.text = "العدد الإجمالي للطلاب : $students_totalNumber"
                            pieChartViewStudents.setCounts(maleCount, femaleCount)
                            for(teacher in section?.teachers?: emptyList()){
                                val gender = teacher?.gender
                                if (gender == "ذكر") {
                                    maleTeacherCount++
                                } else if (gender == "أنثى") {
                                    femaleTeacherCount++
                                }
                            }
                            val teachers_totalNumber: Int = maleTeacherCount + femaleTeacherCount
                            textTeachersCount.text = "العدد الإجمالي للحلقات : $teachers_totalNumber"
                            pieChartViewTeachers.setCounts(maleTeacherCount, femaleTeacherCount)
                        }
                    }

                }
                if(!found){
                    textStudentsCount.text = "العدد الإجمالي للطلاب : 0"
                    pieChartViewStudents.setCounts(0, 0)
                    textTeachersCount.text = "العدد الإجمالي للحلقات : 0"
                    pieChartViewTeachers.setCounts(0, 0)
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    fun Supervisor_stat_show(username: String, branch: String, all_table_supervisor : String) {
        val parentLayout: LinearLayout = findViewById(R.id.parent_linear_layout)
        parentLayout.removeAllViews()
        // Spinners and text filters
        val parentLinearLayout = LinearLayout(this)
        val parentLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        parentLayoutParams.marginStart = 8.dpToPx()
        parentLayoutParams.marginEnd = 8.dpToPx()
        parentLinearLayout.layoutParams = parentLayoutParams
        parentLinearLayout.orientation = LinearLayout.VERTICAL
        parentLinearLayout.layoutDirection = View.LAYOUT_DIRECTION_RTL
        // First Filter line (grade)
        val firstLineLinearLayout1 = LinearLayout(this)
        val firstLineParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            60.dpToPx()
        )
        firstLineParams.bottomMargin = 5.dpToPx()
        firstLineLinearLayout1.layoutParams = firstLineParams
        firstLineLinearLayout1.orientation = LinearLayout.HORIZONTAL
        // Creating the TextView
        val textView1 = TextView(this)
        val textViewParams1 = LinearLayout.LayoutParams(
            150.dpToPx(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        textViewParams1.gravity = Gravity.START or Gravity.CENTER
        textViewParams1.marginStart = 20.dpToPx()
        textView1.layoutParams = textViewParams1
        textView1.gravity = Gravity.START
        textView1.textSize = 14f
        textView1.setTypeface(null, Typeface.BOLD)
        textView1.layoutDirection = View.LAYOUT_DIRECTION_RTL
        textView1.text = "الطلاب الذين أتموا حفظ :"
        // CardView with Spinner
        val cardView1 = CardView(this)
        val cardViewParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            50.dpToPx()
        )
        cardViewParams.gravity = Gravity.START or Gravity.CENTER
        cardView1.layoutParams = cardViewParams
        cardView1.radius = 8.dpToPx().toFloat()
        cardView1.cardElevation = 4.dpToPx().toFloat()
        val spinner_grade = Spinner(this)
        val spinnerParams1 = LinearLayout.LayoutParams(
            400,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        spinnerParams1.layoutDirection = View.LAYOUT_DIRECTION_RTL
        spinner_grade.layoutParams = spinnerParams1
        val spinnerEntries = resources.getStringArray(R.array.grades_level)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerEntries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_grade.adapter = adapter
        cardView1.addView(spinner_grade)
        firstLineLinearLayout1.addView(textView1)
        firstLineLinearLayout1.addView(cardView1)
        parentLinearLayout.addView(firstLineLinearLayout1)

        // Second Filter line (age)
        val secondLineLinearLayout1 = LinearLayout(this)
        secondLineLinearLayout1.layoutParams = firstLineParams
        secondLineLinearLayout1.orientation = LinearLayout.HORIZONTAL

        // Creating the TextView
        val textView2 = TextView(this)
        textView2.layoutParams = textViewParams1

        // textView2.gravity = Gravity.CENTER
        textView2.layoutDirection = View.LAYOUT_DIRECTION_RTL
        textView2.text = "العمر :"
        textView2.textSize = 14f
        textView2.setTypeface(null, Typeface.BOLD)

        // CardView with Spinner
        val cardView2 = CardView(this)
        // cardViewParams2.gravity = Gravity.START or Gravity.CENTER // Adjusted gravity to START
        cardView2.layoutParams = cardViewParams
        cardView2.radius = 8.dpToPx().toFloat()
        cardView2.cardElevation = 4.dpToPx().toFloat()

        val spinner_age = Spinner(this)
        spinner_age.layoutParams = spinnerParams1

        val spinnerEntries2 = resources.getStringArray(R.array.age_level)
        val adapter2 = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerEntries2)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner_age.adapter = adapter2

        cardView2.addView(spinner_age)
        secondLineLinearLayout1.addView(textView2)
        secondLineLinearLayout1.addView(cardView2)
        parentLinearLayout.addView(secondLineLinearLayout1)
        parentLayout.addView(parentLinearLayout)
        /////////////////////////////////////////////////////////////////////////////////////////////////// Table
        // Create a ScrollView
        val scrollView = ScrollView(this)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        layoutParams.marginStart = 50
        layoutParams.marginEnd = 50
        layoutParams.topMargin = 50
        layoutParams.bottomMargin = 50
        scrollView.layoutParams = layoutParams
        parentLayout.addView(scrollView)

        // Creating the LinearLayout for student names
        val studentsLinearLayout = LinearLayout(this)
        studentsLinearLayout.orientation = LinearLayout.VERTICAL
        val horizontalScrollView = HorizontalScrollView(this@MainActivity)
        horizontalScrollView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        // Create a TableLayout
        val tableLayout = TableLayout(this@MainActivity)
        tableLayout.layoutParams = TableLayout.LayoutParams(
            TableLayout.LayoutParams.WRAP_CONTENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableLayout.removeAllViews() // Clear existing views
        ///////////////////////////////////////////////////////////////////////// Spinners listener
        spinner_grade.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                supervisor_applyFilters_grade_age(all_table_supervisor, position, spinner_age.selectedItemPosition, tableLayout, horizontalScrollView )
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        })


        spinner_age.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                supervisor_applyFilters_grade_age(all_table_supervisor, spinner_grade.selectedItemPosition, position,tableLayout, horizontalScrollView )
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        })

        horizontalScrollView.addView(tableLayout)
        scrollView.addView(horizontalScrollView)

    }
    fun supervisor_applyFilters_grade_age(all_table_supervisor: String, gradePosition: Int,
                                          agePosition: Int, tableLayout : TableLayout,
                                          horizontalScrollView : HorizontalScrollView) {
        tableLayout.removeAllViews()
        supervisor_stat_addTitles(tableLayout)
        var Id_counting = 1
        val schoolData = Gson().fromJson(all_table_supervisor, JR_SchoolData::class.java)
        for ((branch_name, branch_node) in schoolData.branches) {
                for ((sectionName, section) in branch_node.sections) {
                    for (student in section?.students ?: emptyList()) {
                        val studentInfo = student?.info
                        val studentGradeVector = student?.grade_vector ?: emptyList()
                        if (studentInfo != null) {
                            val student_age = (getAge(dateFormat.parse(studentInfo.birthday.replace("-", "/")), Calendar.getInstance().time).toString()).toInt()
                            val grade30: Int = studentGradeVector.getOrNull(29) ?: 0
                            val grade29: Int = studentGradeVector.getOrNull(28) ?: 0
                            val student_score = studentGradeVector.sum() ?: 0
                            val okGrade30 =
                                grade30 == 100 && grade29 != null && grade29 < 100 && student_score < 500
                            val okGrade29_30 = grade30 == 100 && grade29 == 100 && student_score < 500
                            val okAll = student_score >= 3000
                            val ok20 = student_score >= 2000 && student_score < 3000
                            val ok15 = student_score >= 1500 && student_score < 2000
                            val ok10 = student_score >= 1000 && student_score < 1500
                            val ok5 = student_score >= 500 && student_score < 1000
                            val okAge6 = student_age <= 6
                            val okAge8 = student_age <= 8
                            val okAge10 = student_age <= 10
                            val okAge12 = student_age <= 12
                            val okAge15 = student_age <= 15
                            val okAge20 = student_age <= 20
                            val okAgeAbove20 = student_age > 20
                            if ((gradePosition == 0 || (gradePosition == 1 && okGrade30) || (gradePosition == 2 && okGrade29_30) ||
                                        (gradePosition == 3 && ok5) || (gradePosition == 4 && ok10) || (gradePosition == 5 && ok15) ||
                                        (gradePosition == 6 && ok20) || (gradePosition == 7 && okAll))
                                && (agePosition == 0 || (agePosition == 1 && okAge6) || (agePosition == 2 && okAge8) ||
                                        (agePosition == 3 && okAge10) || (agePosition == 4 && okAge12) || (agePosition == 5 && okAge15) ||
                                        (agePosition == 6 && okAge20) || (agePosition == 7 && okAgeAbove20))
                            ) {

                                supervisor_stat_createTableRow(
                                    student,
                                    sectionName,
                                    Id_counting,
                                    tableLayout
                                )
                                Id_counting += 1
                            }
                        }
                    }
                }
        }

        horizontalScrollView.post {
            horizontalScrollView.scrollTo(2000, 0)
        }
    }
    fun supervisor_stat_addTitles(tableLayout : TableLayout) {
        val row = TableRow(this@MainActivity)
        val cardView = CardView(this@MainActivity)
        val innerLayout = LinearLayout(this@MainActivity)
        var titles: Array<String>
        titles = arrayOf(" ", "الاسم", "العمر", "الحفظ", "الحلقة")
        val ID_size  = 100
        val name_size = 400
        val Age_size = 150
        val Grade_size = 400
        val section_size = 300
        var titlessizes: IntArray
        // Titles
        titlessizes = intArrayOf(ID_size, name_size, Age_size, Grade_size, section_size)

        for (i in titles.indices) {
            val textView = TextView(this@MainActivity)
            innerLayout.gravity = Gravity.END
            textView.text = titles[i]
            textView.setTextColor(resources.getColor(R.color.bottom_white))
            textView.setBackgroundColor(resources.getColor(R.color.teal_700))

            textView.setTypeface(null, Typeface.BOLD)
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.layoutParams = LinearLayout.LayoutParams(
                titlessizes[i],
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView.textDirection = View.TEXT_DIRECTION_RTL
            innerLayout.addView(textView, 0)
        }

        cardView.addView(innerLayout)
        row.addView(cardView)
        tableLayout.addView(row)

        // Add underline after the titles
        val titleRowUnderline = View(this@MainActivity)
        titleRowUnderline.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            2
        )
        titleRowUnderline.setBackgroundColor(Color.BLACK)
        tableLayout.addView(titleRowUnderline)
    }
    fun supervisor_stat_createTableRow(student: JR_Student?,section:String, id: Int,
                                       tableLayout : TableLayout) {
        // Create a TableRow for each student
        val row = TableRow(this@MainActivity)
        val cardView = CardView(this@MainActivity)
        val cardViewParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            120
        )
        cardView.layoutParams = cardViewParams
        val innerLayout = LinearLayout(this@MainActivity)
        // Create TextViews for student data
        val age = getAge(dateFormat.parse(student?.info?.birthday!!.replace("-", "/")), Calendar.getInstance().time).toString()
        val number_of_pages = student.grade_vector!!.sum()/5
        val grade = pages_to_jiz2(number_of_pages)  // % to nimber of pages : /5
        val columnsinformation = arrayOf(id.toString(), student.info.name, age, grade, section)
        val ID_size  = 100
        val name_size = 400
        val Age_size = 150
        val Grade_size = 400
        val section_size = 300
        val branch_size = 300
        val titlessizes = intArrayOf(ID_size, name_size, Age_size, Grade_size, section_size, branch_size)
        for (i in columnsinformation.indices) {
            val textView = TextView(this@MainActivity)
            textView.text = columnsinformation[i]
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.layoutParams = LinearLayout.LayoutParams(
                titlessizes[i],
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            textView.textDirection = View.TEXT_DIRECTION_RTL // arabic direction
            textView.gravity = Gravity.CENTER_VERTICAL
            innerLayout.addView(textView, 0)
        }
        cardView.addView(innerLayout)
        row.addView(cardView)
        tableLayout.addView(row)
        // Add underline after each row
        val rowUnderline = View(this@MainActivity)
        rowUnderline.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            2
        )
        rowUnderline.setBackgroundColor(Color.BLACK)
        tableLayout.addView(rowUnderline)
    }

    private fun to_Supervisor_Monitoring_Students(username: String, branch: String,
                                                  all_table_supervisor :  String){
        setContentView(R.layout.supervisor_monitoring_students_layout)
        currentLayout = "supervisor_monitoring_students_layout"
        currentUser = username
        currentUserStatus = "supervisor"
        currentBranch = branch
        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            to_Supervisor_Layout(username, userBranch)
        }
        val toolbarTitle : TextView=findViewById(R.id.toolbarTitle)
        toolbarTitle.text="مشرف"
        //////////////////////////////////////////////////////////////////////////////////////////// Top view
        val branchtopview = findViewById<TextView>(R.id.branch)
        val nametopview = findViewById<TextView>(R.id.supervisorname)
        branchtopview.text =  "فرع : $branch"
        nametopview.text = username
        /////////////////////////////////////// if supervisor

        val spinnerSection = findViewById<Spinner>(R.id.spinnerSection)
        val scrollView = findViewById<ScrollView>(R.id.scrollview)
        // Build the section spinner list
        // Update the section spinner with the branch
         val schoolData = Gson().fromJson(all_table_supervisor, JR_SchoolData::class.java)
        val sectionsList = mutableListOf<String>()
        sectionsList.add("جميع الحلقات")
        for ((branch_name, branch_node) in schoolData.branches) {
            for ((sectionName, _) in branch_node.sections) {
                    sectionsList.add(sectionName)
            }
        }
        val sectionAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, sectionsList)
        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSection.adapter = sectionAdapter

        // Spinner listener
        spinnerSection.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                val selectedSection = parentView?.getItemAtPosition(position).toString()
                for ((branch_name, branch_node) in schoolData.branches) {
                    val linearContainer = LinearLayout(applicationContext)
                    linearContainer.layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    linearContainer.orientation = LinearLayout.VERTICAL
                    for ((sectionName, section) in branch_node.sections) {
                        if (((selectedSection == sectionName) || (selectedSection == "جميع الحلقات"))) {
                            for (student_item in section?.students ?: emptyList()) {
                                // Filter students based on the selected branch and section
                                    val color_text = R.color.color4
                                    val cardView = CardView(this@MainActivity)
                                    val card_layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    card_layoutParams.gravity = Gravity.CENTER
                                    card_layoutParams.setMargins(
                                        16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 10.dpToPx())
                                    cardView.layoutParams = card_layoutParams
                                    cardView.cardElevation = 4.dpToPx().toFloat()
                                    cardView.setCardBackgroundColor(
                                        ContextCompat.getColor(
                                            this@MainActivity, android.R.color.white))
                                    // horizontal bar
                                    val horizontalBar = View(this@MainActivity)
                                    val horizontalBarParams = LinearLayout.LayoutParams(
                                        2.dpToPx(), LinearLayout.LayoutParams.MATCH_PARENT)
                                    horizontalBar.setBackgroundColor(
                                        ContextCompat.getColor(this@MainActivity, color_text))
                                    horizontalBar.layoutParams = horizontalBarParams
                                    cardView.addView(horizontalBar)
                                    // Student Name
                                    val Name_textView = TextView(this@MainActivity)
                                    val name_text_layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    )
                                    name_text_layoutParams.setMargins(
                                        8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                                    name_text_layoutParams.gravity = Gravity.CENTER
                                    Name_textView.layoutParams = name_text_layoutParams
                                    Name_textView.gravity = Gravity.CENTER
                                    Name_textView.text = student_item?.info?.name
                                    Name_textView.layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
                                    Name_textView.setTextColor(
                                        ContextCompat.getColor(
                                            this@MainActivity, android.R.color.black)
                                    )
                                    Name_textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                                    Name_textView.setTypeface(null, Typeface.BOLD)
                                    cardView.addView(Name_textView)
                                    // Student Section
                                    val Section_textView = TextView(this@MainActivity)
                                    val section_text_layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT)
                                    section_text_layoutParams.setMargins(
                                        8.dpToPx(), 50.dpToPx(), 8.dpToPx(), 8.dpToPx())
                                    section_text_layoutParams.gravity = Gravity.CENTER
                                    Section_textView.layoutParams = section_text_layoutParams
                                    Section_textView.gravity = Gravity.CENTER
                                    Section_textView.text = "$sectionName - $branch_name"
                                    Section_textView.setTextColor(
                                        ContextCompat.getColor(this@MainActivity,
                                            android.R.color.black))
                                    cardView.addView(Section_textView)
                                    // Vertical bar
                                    val VerticalBar = View(this@MainActivity)
                                    val VerticalBarParams = LinearLayout.LayoutParams(
                                        2.dpToPx(), 30.dpToPx())
                                    VerticalBarParams.gravity = Gravity.CENTER_VERTICAL
                                    VerticalBarParams.setMargins(8.dpToPx(), 8.dpToPx(),
                                        8.dpToPx(), 8.dpToPx())
                                    VerticalBar.setBackgroundColor(
                                        ContextCompat.getColor(this@MainActivity, R.color.black))
                                    VerticalBar.layoutParams = VerticalBarParams
                                    // Left TextView
                                    val leftTextView = TextView(this@MainActivity)
                                    val leftTextParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    leftTextParams.gravity = Gravity.END or Gravity.CENTER_VERTICAL
                                    leftTextView.layoutParams = leftTextParams
                                    leftTextView.setTextColor(
                                        ContextCompat.getColor(this@MainActivity,
                                            color_text)
                                    )
                                    leftTextView.setTextSize(
                                        TypedValue.COMPLEX_UNIT_SP, 18f)
                                    leftTextView.setTypeface(null, Typeface.BOLD)
                                    leftTextView.setOnClickListener {
                                        if(student_item!=null) {
                                            JR_get_grade_history(student_item.grade_history){grade_history ->
                                                JR_get_attendance_History(student_item.attendance){attendanceHistory->
                                                    val student = Student(
                                                        id = section?.students?.indexOf(student_item)?:0,
                                                        name = student_item.info?.name.toString(),
                                                        gender = student_item.info?.gender.toString(),
                                                        birthday = student_item.info?.birthday?.replace("-","/").toString(),
                                                        section = sectionName,
                                                        branch = branch_name,
                                                        teacherName =  (section?.teachers?.get(0)?.name) ?: "Unknown",
                                                        gradeLevel = student_item.grade_vector?: emptyList(),
                                                        gradeHistory =grade_history,
                                                        attendanceHistory = attendanceHistory
                                                    )
                                                    to_Student_Attendence_layout(username, "supervisor", student, all_table_supervisor, GlobalstudentspinnerIndex)
                                                }
                                            }
                                        }
                                    }
                                    leftTextView.text = "عرض الحضور"
                                    // Right TextView
                                    val rightTextView = TextView(this@MainActivity)
                                    val rightTextParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.WRAP_CONTENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    rightTextParams.gravity = Gravity.START or Gravity.CENTER_VERTICAL
                                    rightTextView.layoutParams = rightTextParams
                                    rightTextView.setTextColor(
                                        ContextCompat.getColor(this@MainActivity, color_text))
                                    rightTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                                    rightTextView.setTypeface(null, Typeface.BOLD)
                                    rightTextView.setOnClickListener {
                                        if(student_item!=null) {
                                            JR_get_grade_history(student_item.grade_history){grade_history ->
                                                val student = Student(
                                                    id = section?.students?.indexOf(student_item)?:0,
                                                    name = student_item.info?.name.toString(),
                                                    gender = student_item.info?.gender.toString(),
                                                    birthday = student_item.info?.birthday?.replace("-","/").toString(),
                                                    section = sectionName,
                                                    branch = branch_name,
                                                    teacherName =  (section?.teachers?.get(0)?.name) ?: "Unknown",
                                                    gradeLevel = student_item.grade_vector?: emptyList(),
                                                    gradeHistory =grade_history,
                                                    attendanceHistory = student_item.attendance?.map { (date, status) ->
                                                        AttendanceItem(date, "", status)
                                                    }?.toMutableList() ?: mutableListOf()
                                                )
                                                to_Student_Grade_layout(username, "supervisor", student, all_table_supervisor, 0)
                                            }
                                        }
                                    }
                                    rightTextView.text = "عرض الحفظ"
                                    // Create a horizontal LinearLayout for the text views
                                    val textContainer = LinearLayout(this@MainActivity)
                                    textContainer.orientation = LinearLayout.HORIZONTAL
                                    val textContainerParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    textContainerParams.setMargins(
                                        8.dpToPx(), 80.dpToPx(), 8.dpToPx(), 8.dpToPx())
                                    textContainer.layoutParams = textContainerParams
                                    textContainer.gravity = Gravity.CENTER_HORIZONTAL
                                    textContainer.addView(leftTextView)
                                    textContainer.addView(VerticalBar)
                                    textContainer.addView(rightTextView)
                                    cardView.addView(textContainer)
                                    linearContainer.addView(cardView)
                            }
                        }
                    }
                        scrollView.removeAllViews()
                        scrollView.addView(linearContainer)

                }
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
    }

    private fun to_Supervisor_Monitoring_Sections(username: String, branch: String,
                                                  all_table_supervisor: String) {
        setContentView(R.layout.supervisor_monitoring_sections_layout)
        currentLayout = "supervisor_monitoring_sections_layout"
        currentUser = username
        currentUserStatus = "supervisor"
        userBranch = branch
        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            to_Supervisor_Layout(username, userBranch)
        }
        val toolbarTitle : TextView=findViewById(R.id.toolbarTitle)
        toolbarTitle.text="مشرف"
        //////////////////////////////////////////////////////////////////////////////////////////// Top view
        val branchtopview = findViewById<TextView>(R.id.branch)
        val nametopview = findViewById<TextView>(R.id.supervisorname)
        branchtopview.text = "فرع : $branch"
        nametopview.text = username
        ///////////////////////////////////////////////////////////////////////////////////////////// Fill
        val scrollView = findViewById<ScrollView>(R.id.scrollview_in_section)

        val linearContainer = LinearLayout(applicationContext)
        linearContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        linearContainer.orientation = LinearLayout.VERTICAL
        val schoolData = Gson().fromJson(all_table_supervisor, JR_SchoolData::class.java)
        for ((branch_name, branch_node) in schoolData.branches) {
                for ((sectionName, section) in branch_node.sections) {
                    val color_text = R.color.color4
                    val cardView = CardView(this@MainActivity)
                    val card_layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    card_layoutParams.gravity = Gravity.CENTER
                    card_layoutParams.setMargins(16.dpToPx(), 12.dpToPx(), 16.dpToPx(), 10.dpToPx())
                    cardView.layoutParams = card_layoutParams
                    cardView.cardElevation = 4.dpToPx().toFloat()
                    cardView.setCardBackgroundColor(
                        ContextCompat.getColor(this@MainActivity, android.R.color.white)
                    )
                    // Section Name
                    val Name_textView = TextView(this@MainActivity)
                    val name_text_layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    name_text_layoutParams.setMargins(8.dpToPx(), 8.dpToPx(), 8.dpToPx(), 8.dpToPx())
                    name_text_layoutParams.gravity = Gravity.CENTER
                    Name_textView.layoutParams = name_text_layoutParams
                    Name_textView.gravity = Gravity.CENTER
                    Name_textView.text = sectionName
                    Name_textView.layoutDirection = ViewGroup.LAYOUT_DIRECTION_RTL
                    Name_textView.setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
                    Name_textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22f)
                    Name_textView.setTypeface(null, Typeface.BOLD)
                    cardView.addView(Name_textView)
                    // Create a RelativeLayout for the text views
                    val relativeLayout = RelativeLayout(this@MainActivity)
                    val relativeParam = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    relativeParam.setMargins(8.dpToPx(), 50.dpToPx(), 8.dpToPx(), 8.dpToPx())
                    relativeLayout.layoutParams = relativeParam
                    // Left TextView (teachername)
                    val teacherNameTextView = TextView(this@MainActivity)
                    val teacherNameParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_START)
                        setMargins(0, 0, 0, 0)
                    }
                    teacherNameTextView.layoutParams = teacherNameParams
                    teacherNameTextView.text = "الأستاذ(ة): ${section?.teachers?.get(0)?.name}"
                    teacherNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    teacherNameTextView.setTypeface(null, Typeface.BOLD)
                    teacherNameTextView.maxWidth = 140.dpToPx()
                    relativeLayout.addView(teacherNameTextView)

                    // Right TextView (number of students)
                    val sectionTextView = TextView(this@MainActivity)
                    val sectionParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        addRule(RelativeLayout.ALIGN_PARENT_END)
                        setMargins(0, 0, 0, 0)
                    }
                    sectionTextView.layoutParams = sectionParams
                    sectionTextView.text = "عدد الطلاب: ${section?.students?.size}"
                    sectionTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                    sectionTextView.setTypeface(null, Typeface.BOLD)
                    sectionTextView.maxWidth = 140.dpToPx()
                    relativeLayout.addView(sectionTextView)

                    // Add the RelativeLayout to the CardView
                    cardView.addView(relativeLayout)

                    // vertival bar
                    val vertivalBar = View(this@MainActivity)
                    val vertivalBarParams = LinearLayout.LayoutParams(
                        2.dpToPx(),
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    vertivalBarParams.gravity = Gravity.START
                    vertivalBar.layoutDirection = View.LAYOUT_DIRECTION_RTL
                    vertivalBar.setBackgroundColor(
                        ContextCompat.getColor(this@MainActivity, color_text)
                    )
                    vertivalBar.layoutParams = vertivalBarParams
                    cardView.addView(vertivalBar)
                    cardView.setOnClickListener {
                        JR_get_students_list_SECTION(section, sectionName, branch_name,
                            section?.teachers?.get(0)?.name.toString()){sectionstudents->
                            val teacheritem = Teacher(
                                teacherID = "1",
                                teacherName = section?.teachers?.get(0)?.name.toString(),
                                section = sectionName,
                                branch = branch_name,
                                teacherGender = section?.teachers?.get(0)?.gender.toString(),
                                students = sectionstudents
                            )
                            to_Section_Layout(username, "supervisor", teacheritem)
                        }
                    }
                    linearContainer.addView(cardView)
                }
                scrollView.removeAllViews()
                scrollView.addView(linearContainer)
            }
        scrollView.removeAllViews()
        scrollView.addView(linearContainer)


    }

    private fun to_Supervisor_Toplist_Layout( username: String, branch : String,
                                              all_table_supervisor: String){
        setContentView(R.layout.supervisor_toplist_layout)
        currentLayout = "supervisor_toplist_layout"
        currentUser = username
        currentBranch = branch
        currentUserStatus = "supervisor"
        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
             showOptionsMenu(view, username)
        }
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            to_Supervisor_Layout(username, userBranch)
        }
        val toolbarTitle : TextView=findViewById(R.id.toolbarTitle)
        toolbarTitle.text="مشرف"
        //////////////////////////////////////////////////////////////////////////////////////////// Top view
        val branchtopview = findViewById<TextView>(R.id.branch)
        val nametopview = findViewById<TextView>(R.id.supervisorname)
        branchtopview.text = "فرع : $branch"
        nametopview.text = username
        //////////////////////////////////////////////////////////////////////  Fill table
        val spinnerYears = findViewById<Spinner>(R.id.years_spinner)
        var selectedYear: String? = null
        val spinnerMonths = findViewById<Spinner>(R.id.months_spinner)
        var selectedMonth: String? = null

        // SET By default the current year and month
        // Array of years and months from your string resources
        val yearsArray = resources.getStringArray(R.array.years)
        val monthsArray = resources.getStringArray(R.array.months)

        // Create an ArrayAdapter for the years Spinner
        val adapterYears = ArrayAdapter(this, android.R.layout.simple_spinner_item, yearsArray)
        adapterYears.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYears.adapter = adapterYears

        // Create an ArrayAdapter for the months Spinner
        val adapterMonths = ArrayAdapter(this, android.R.layout.simple_spinner_item, monthsArray)
        adapterMonths.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonths.adapter = adapterMonths

        // Get the current year and month
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH) // 0-based (January = 0)

        // Find the index of the current year in the yearsArray
        val indexOfCurrentYear = yearsArray.indexOf(currentYear.toString())

        // Find the index of the current month (increment by 1 to align with the string array)
        val indexOfCurrentMonth = currentMonth // No need for adjustment as array is 0-based

        // Set the current year as the selected item in the Spinner
        if (indexOfCurrentYear >= 0) {
            spinnerYears.setSelection(indexOfCurrentYear)
        }

        // Set the current month as the selected item in the Spinner
        if (indexOfCurrentMonth >= 0) {
            spinnerMonths.setSelection(indexOfCurrentMonth)
        }
        ////////////////////////////////////////////////////////////

        /////////// Add table first time

        val scrollView = findViewById<ScrollView>(R.id.scrollview)
        val horizontalScrollView = HorizontalScrollView(this@MainActivity)
        horizontalScrollView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val tableLayout = TableLayout(this@MainActivity)
        tableLayout.removeAllViews()
        //title
        supervisor_toplist_addTitles(tableLayout)
        // elements
        spinnerYears.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                selectedYear = parentView?.getItemAtPosition(position).toString()
                selectedMonth=spinnerMonths.selectedItem.toString()
                tableLayout.removeAllViews()
                supervisor_toplist_addTitles(tableLayout)
                lifecycleScope.launch {
                    try {
                        val schoolData = Gson().fromJson(all_table_supervisor, JR_SchoolData::class.java)
                        var all_student_list = ArrayList<Student>()
                        for ((branch_name, branch_node) in schoolData.branches) {
                                for ((sectionName, section) in branch_node.sections) {
                                    for (student_item in section?.students ?: emptyList()) {
                                        if (student_item != null) {
                                            JR_get_grade_history(student_item.grade_history) { grade_history ->
                                                val student = Student(
                                                    id = section?.students?.indexOf(student_item) ?: 0,
                                                    name = student_item.info?.name.toString(),
                                                    gender = "",
                                                    birthday =  student_item.info?.birthday?.replace("-","/").toString(),
                                                    section = sectionName,
                                                    branch = branch_name,
                                                    teacherName = (section?.teachers?.get(0)?.name)
                                                        ?: "Unknown",
                                                    gradeLevel = student_item.grade_vector
                                                        ?: emptyList(),
                                                    gradeHistory = grade_history,
                                                    attendanceHistory = mutableListOf()
                                                )
                                                all_student_list.add(student)
                                            }
                                        }
                                    }
                                }
                        }
                        val sortedStudents = all_student_list.map { student ->
                            var score = 0
                            JR_get_toplist_info(student, selectedYear ?: "كل السنوات", selectedMonth ?: "كل الأشهر") {studentScore ->
                                score = studentScore
                            }
                            Pair(student, score)
                        }.sortedByDescending { it.second }
                        var Id = 1
                        for ((sortedStudent, score) in sortedStudents) {
                            supervisor_topilist_Table(Id, sortedStudent, score, tableLayout)
                            Id += 1
                            if (Id==51){
                                break
                            }
                        }
                    } catch (e: Exception) {
                        showToast(e.message.toString())
                    }
                }
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }
        spinnerMonths.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                selectedMonth = parentView?.getItemAtPosition(position).toString()
                tableLayout.removeAllViews()
                supervisor_toplist_addTitles(tableLayout)
                lifecycleScope.launch {
                    try {
                        val schoolData = Gson().fromJson(all_table_supervisor, JR_SchoolData::class.java)
                        var all_student_list = ArrayList<Student>()
                        for ((branch_name, branch_node) in schoolData.branches) {
                                for ((sectionName, section) in branch_node.sections) {
                                    for (student_item in section?.students ?: emptyList()) {
                                        if (student_item != null) {
                                            JR_get_grade_history(student_item.grade_history) { grade_history ->
                                                val student = Student(
                                                    id = section?.students?.indexOf(student_item)
                                                        ?: 0,
                                                    name = student_item.info?.name.toString(),
                                                    gender = "",
                                                    birthday =  student_item.info?.birthday?.replace("-","/").toString(),
                                                    section = sectionName,
                                                    branch = branch_name,
                                                    teacherName = (section?.teachers?.get(0)?.name)
                                                        ?: "Unknown",
                                                    gradeLevel = emptyList(),
                                                    gradeHistory = grade_history,
                                                    attendanceHistory = mutableListOf()
                                                )
                                                all_student_list.add(student)
                                            }
                                        }
                                    }
                                }

                        }
                        val sortedStudents = all_student_list.map { student ->
                            // Calculate the score for each student for the selected year
                            var score = 0
                            JR_get_toplist_info(student, selectedYear ?: "كل السنوات",
                                selectedMonth ?: "كل الأشهر") {studentScore ->
                                score = studentScore
                            }
                            Pair(student, score)
                        }.sortedByDescending { it.second }
                        var Id = 1
                        for ((sortedStudent, score) in sortedStudents) {
                            supervisor_topilist_Table(Id, sortedStudent, score, tableLayout
                            )
                            Id += 1
                            if (Id==51){
                                break
                            }
                        }
                    } catch (e: Exception) {
                        showToast(e.message.toString())
                    }
                }
            }
            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        horizontalScrollView.post {
                horizontalScrollView.scrollTo(2000, 0)
            }
        scrollView.removeAllViews()
        horizontalScrollView.addView(tableLayout)
        scrollView.addView(horizontalScrollView)
    }
    fun supervisor_toplist_addTitles(tableLayout : TableLayout) {
        val row = TableRow(this@MainActivity)
        val cardView = CardView(this@MainActivity)
        val innerLayout = LinearLayout(this@MainActivity)
        var titles: Array<String>
        titles = arrayOf(" ", "الاسم", "الحفظ", "الحلقة", "الأستاذ")
        val ID_size  = 100
        val name_size = 400
        val section_size = 300
        val Teacher_name_size = 300
        val score_size = 300
        var titlessizes: IntArray
        // Titles
        titlessizes = intArrayOf(ID_size, name_size, score_size, section_size, Teacher_name_size)

        for (i in titles.indices) {
            val textView = TextView(this@MainActivity)
            innerLayout.gravity = Gravity.END
            textView.text = titles[i]
            textView.setTextColor(resources.getColor(R.color.bottom_white))
            textView.setBackgroundColor(resources.getColor(R.color.teal_700))

            textView.setTypeface(null, Typeface.BOLD)
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.layoutParams = LinearLayout.LayoutParams(
                titlessizes[i],
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            textView.textDirection = View.TEXT_DIRECTION_RTL
            innerLayout.addView(textView, 0)
        }

        cardView.addView(innerLayout)
        row.addView(cardView)
        tableLayout.addView(row)

        // Add underline after the titles
        val titleRowUnderline = View(this@MainActivity)
        titleRowUnderline.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            2
        )
        titleRowUnderline.setBackgroundColor(Color.BLACK)
        tableLayout.addView(titleRowUnderline)
    }
    fun supervisor_topilist_Table(id: Int, student: Student, score : Int,
                                  tableLayout : TableLayout) {
        // Create a TableRow for each student
        val row = TableRow(this@MainActivity)
        val cardView = CardView(this@MainActivity)
        val cardViewParams = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            120
        )
        cardView.layoutParams = cardViewParams
        val innerLayout = LinearLayout(this@MainActivity)
        // Create TextViews for student data
        val columnsinformation = arrayOf(id.toString(), student.name, pages_to_jiz2(score), student.section,student.teacherName)
        val ID_size  = 100
        val name_size = 400
        val section_size = 300
        val Teacher_name_size = 300
        val score_size = 300
        val titlessizes = intArrayOf(ID_size, name_size, score_size, section_size, Teacher_name_size)

        for (i in columnsinformation.indices) {
            val textView = TextView(this@MainActivity)
            textView.text = columnsinformation[i].toString()
            textView.ellipsize = TextUtils.TruncateAt.END
            textView.layoutParams = LinearLayout.LayoutParams(
                titlessizes[i],
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            textView.textDirection = View.TEXT_DIRECTION_RTL // arabic direction
            textView.gravity = Gravity.CENTER_VERTICAL
            innerLayout.addView(textView, 0)
        }

        cardView.addView(innerLayout)
        row.addView(cardView)
        tableLayout.addView(row)

        // Add underline after each row
        val rowUnderline = View(this@MainActivity)
        rowUnderline.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            2
        )
        rowUnderline.setBackgroundColor(Color.BLACK)
        tableLayout.addView(rowUnderline)
    }

//////////////////////////////////////////////////////////////////////////////////////////////////// Teacher
    fun readLASTUPDATEData_Teacher(): String {
    val sharedPreferences = getSharedPreferences("teacher_last_update", Context.MODE_PRIVATE)
    val lastUpdate = sharedPreferences.getString("last_update", null)
    return lastUpdate.toString()
}
    fun save_all_table_Teacher(Teacher_branch : String, Teacher_section : String,
                               callback: (Boolean) -> Unit) {
        val stdQueryFire = database.getReference("branches")
        stdQueryFire.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val gson = Gson()
                val branchesMap = mutableMapOf<String, JR_Branch>()
                for (branchSnapshot in dataSnapshot.children) {
                    val branchName = branchSnapshot.key.toString()
                    if (Teacher_branch == branchName){
                        val sectionMap = mutableMapOf<String, JR_Section>()
                        for (sectionSnapshot in branchSnapshot.child("sections").children) {
                            val sectionName = sectionSnapshot.key.toString()
                            if (sectionName == Teacher_section){
                                val studentsList = mutableListOf<JR_Student?>()
                                val teachersList = mutableListOf<JR_Teacher?>()
                                for (studentSnapshot in sectionSnapshot.child("students").children) {
                                        val studentData = studentSnapshot.getValue(JR_Student::class.java)
                                        val gradeHistoryMap = mutableMapOf<String, JR_grade_history_item>()
                                        for (grade_historySnapshot in studentSnapshot.child("grade_history").children) {
                                            val date = grade_historySnapshot.key.toString()
                                            val grade_history_item = mutableMapOf<String, Int>()
                                            for (assignmentSnapshot in grade_historySnapshot.children) {
                                                val assignmentNumber = assignmentSnapshot.key.toString()
                                                val grade = assignmentSnapshot.getValue(Int::class.java)
                                                    ?: 0
                                                grade_history_item[assignmentNumber] = grade
                                            }
                                            gradeHistoryMap[date] =
                                                JR_grade_history_item(grade_history_item)
                                        }

                                        studentData?.grade_history =
                                            JR_grade_history(gradeHistoryMap)
                                        studentsList.add(studentData)
                                    }
                                for (teacherSnapshot in sectionSnapshot.child("teachers").children) {
                                        val teacherData = teacherSnapshot.getValue(JR_Teacher::class.java)
                                        teachersList.add(teacherData)
                                    }
                                val section = JR_Section(studentsList, teachersList)
                                sectionMap[sectionName] = section
                            }
                        }
                        val supervisorsList = mutableListOf<JR_Supervisor?>()
                        for (supervisorSnapshot in branchSnapshot.child("supervisors").children) {
                            val supervisorData = supervisorSnapshot.getValue(JR_Supervisor::class.java)
                            supervisorsList.add(supervisorData)
                        }
                        val branch = JR_Branch(sectionMap, supervisorsList)
                        branchesMap[branchName] = branch
                    }
                }

                lifecycleScope.launch {
                    try {
                        val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                        val schoolData = JR_SchoolData(branchesMap)
                        val jsonData = gson.toJson(schoolData)
                        val branchesEntity = branchesEntity(1, jsonData)
                        branchesDao.insertbranches(branchesEntity)
                        // Save date
                        val currentTime = System.currentTimeMillis()
                        val formattedDateTime =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                                Date(currentTime)
                            )
                        saveLASTUPDATEData_Teacher(formattedDateTime)
                        callback(true)
                    } catch (e: Exception) {
                        showToast(e.message.toString())
                        callback(false)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }
    fun saveLASTUPDATEData_Teacher(last_update: String) {
        val sharedPreferences = getSharedPreferences("teacher_last_update", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("last_update", last_update)
        editor.apply()
    }
    fun clear_Teacher_data() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@MainActivity)
                database.branchesDao().removeAllBranches()
            } catch (e: Exception) {
                showToast("Failed to remove all branches: ${e.message}")
            }
        }
    }
    fun JR_findStudentByName(studentName: String,
                             schoolData: JR_SchoolData): Triple<JR_Student?, String?, String?>? {
        for ((branchName, branchNode) in schoolData.branches) {
            for ((sectionName, sectionNode) in branchNode.sections) {
                sectionNode?.students?.forEach { student ->
                    if (student?.info?.name == studentName) {
                        return Triple(student, sectionName, branchName)
                    }
                }
            }
        }
        return null
    }

    private fun to_Teacher_Layout(username: String, branch: String, studentspinnerIndex : Int) {
        setContentView(R.layout.teacher_layout)
        currentLayout = "teacher_layout"
        currentUser = username
        currentUserStatus = "teacher"
        currentBranch = branch
        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val toolbarTitle : TextView=findViewById(R.id.toolbarTitle)
        toolbarTitle.text="أستاذ"
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        //////////////////////////////////////////Variables
        val teacherNameTopView: TextView = findViewById(R.id.teachername)
        val teacherBranchTopView: TextView = findViewById(R.id.branch)
        val teacherSectionTopView: TextView = findViewById(R.id.section)
        val imageViewInfo = findViewById<ImageView>(R.id.imageViewInfo)
        val spinnerStudents: Spinner = findViewById(R.id.teacherstudentlistSpinner)
        val set_attendance: RelativeLayout = findViewById(R.id.set_attendance)
        val section_btn = findViewById<RelativeLayout>(R.id.section_btn)
        val show_Grade: RelativeLayout = findViewById(R.id.student_showgrade)
        val show_attendance: RelativeLayout = findViewById(R.id.show_attendance)
        teacherNameTopView.text = username
        teacherBranchTopView.text = branch

        AppDatabase.getDatabase(this@MainActivity)
        val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
        lifecycleScope.launch {
            try {
                val teacher_all_table = branchesDao.getbranchesTable(1)
                ////// no pre data   FETCH
                if (teacher_all_table.isNullOrBlank() || teacher_all_table == null) {
                    JR_fetch_teacher_info_TEACHER_NAME (username, branch) {section, gender ->
                        teacherSectionTopView.text = section
                        teacher_section = section
                        // refresh btn
                        val update_btn = findViewById<Button>(R.id.updateBtn)
                        update_btn.setOnClickListener {
                            save_all_table_Teacher(currentBranch, teacher_section) { _ ->
                                lifecycleScope.launch {
                                    try {
                                        AppDatabase.getDatabase(this@MainActivity)
                                        val branchesDao =
                                            AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                        all_table_teacher = branchesDao.getbranchesTable(1).toString()
                                        showToast("تم تحديث البيانات")
                                        to_Teacher_Layout(username, currentBranch, 0)
                                    } catch (e: Exception) {
                                        showToast(e.message.toString())
                                    }
                                }
                            }
                        }

                        if (gender == "أنثى") {
                            imageViewInfo.setImageResource(R.drawable.girlicon)
                        }
                        else {
                            imageViewInfo.setImageResource(R.drawable.boyicon)
                        }
                        save_all_table_Teacher(branch, section) { _ ->
                            showToast("تم تحديث البيانات")
                            lifecycleScope.launch {
                                try {
                                    AppDatabase.getDatabase(this@MainActivity)
                                    val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                    all_table_teacher= branchesDao.getbranchesTable(1).toString()

                                    // Build the teacher spinner list
                                    val schoolData = Gson().fromJson(all_table_teacher, JR_SchoolData::class.java)
                                    val studentNamesList = mutableListOf<String>()
                                    schoolData.branches[branch]?.sections?.get(section)?.students?.forEach { student ->
                                        student?.let {
                                            it.info?.name?.let { name ->
                                                studentNamesList.add(name)
                                            }
                                        }
                                    }
                                    val sectionAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, studentNamesList)
                                    sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                    spinnerStudents.adapter = sectionAdapter
                                    spinnerStudents.setSelection(studentspinnerIndex)

                                    // to section
                                    section_btn.setOnClickListener {
                                        val filteredStudents = schoolData.branches[userBranch]?.sections?.get(teacher_section)?.students
                                        val students = mutableListOf<Student>()

                                        filteredStudents?.forEach { jrStudent ->
                                            JR_get_grade_history(jrStudent?.grade_history) { grade_history ->
                                                JR_get_attendance_History(jrStudent?.attendance) { attendanceHistory ->
                                                    jrStudent?.let {
                                                        val student = Student(
                                                            id = 0,
                                                            name = it.info?.name ?: "",
                                                            gender = it.info?.gender ?: "",
                                                            birthday = it.info?.birthday ?: "",
                                                            section = teacher_section,
                                                            branch = userBranch,
                                                            teacherName = username,
                                                            gradeLevel = it.grade_vector ?: emptyList(),
                                                            gradeHistory = grade_history ?: emptyList(),
                                                            attendanceHistory = attendanceHistory ?: mutableListOf()
                                                        )
                                                        students.add(student)
                                                    }
                                                }
                                            }
                                        }

                                        val teacheritem = Teacher(
                                            teacherID = "1",
                                            teacherName = username,
                                            section = teacher_section,
                                            branch = userBranch,
                                            teacherGender = gender,
                                            students = students
                                        )
                                        to_Section_Layout(username, "teacher", teacheritem)
                                    }
                                    // to student grade
                                    show_Grade.setOnClickListener {
                                        val selectedStudent = spinnerStudents.selectedItem?.toString()
                                        val result = JR_findStudentByName(selectedStudent.toString(), schoolData)
                                        if (result!=null)
                                        {

                                            val (student_item, sectionName, branchName) = result
                                                JR_get_grade_history(student_item?.grade_history){grade_history ->
                                                    val student = Student(
                                                        id =  0,
                                                        name = student_item?.info?.name.toString(),
                                                        gender = student_item?.info?.gender.toString(),
                                                        birthday = student_item?.info?.birthday?.replace("-","/").toString(),
                                                        section = sectionName?:"",
                                                        branch = branchName?:"",
                                                        teacherName =  username,
                                                        gradeLevel = student_item?.grade_vector?: emptyList(),
                                                        gradeHistory =grade_history,
                                                        attendanceHistory = mutableListOf()
                                                    )
                                                    GlobalstudentspinnerIndex =spinnerStudents.selectedItemPosition
                                                    to_Student_Grade_layout(username, "teacher", student, "", GlobalstudentspinnerIndex)
                                                }
                                        }
                                    }
                                    //  to attendance layouts
                                    show_attendance.setOnClickListener {
                                        val selectedStudent = spinnerStudents.selectedItem?.toString()
                                        GlobalstudentspinnerIndex =spinnerStudents.selectedItemPosition
                                        val result = JR_findStudentByName(selectedStudent.toString(), schoolData)
                                        if (result!=null)
                                        {
                                            val (student_item, sectionName, branchName) = result
                                                JR_get_attendance_History(student_item?.attendance) { attendanceHistory ->
                                                    val student = Student(
                                                        id = 0,
                                                        name = student_item?.info?.name.toString(),
                                                        gender = student_item?.info?.gender.toString(),
                                                        birthday = student_item?.info?.birthday?.replace(
                                                            "-",
                                                            "/"
                                                        ).toString(),
                                                        section = sectionName ?: "",
                                                        branch = branchName ?: "",
                                                        teacherName = username,
                                                        gradeLevel = student_item?.grade_vector
                                                            ?: emptyList(),
                                                        gradeHistory = emptyList(),
                                                        attendanceHistory = attendanceHistory
                                                    )
                                                    to_Student_Attendence_layout(
                                                        username,
                                                        "teacher",
                                                        student,
                                                        "",
                                                        GlobalstudentspinnerIndex
                                                    )
                                                }
                                        }
                                    }
                                } catch (e: Exception) {
                                    showToast(e.message.toString())
                                }
                            }
                        }
                    }
                }
                ////// with pre data   GET
                else{
                    AppDatabase.getDatabase(this@MainActivity)
                    val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                    all_table_teacher= branchesDao.getbranchesTable(1).toString()
                    // Build the teacher spinner list
                    val schoolData = Gson().fromJson(all_table_teacher, JR_SchoolData::class.java)
                    currentBranch = schoolData.branches.keys.first()
                    val section =  schoolData.branches[currentBranch]?.sections?.keys?.first()
                    teacher_section = section.toString()
                    teacherSectionTopView.text = section
                    // Build the teacher spinner list
                    val studentNamesList = mutableListOf<String>()
                    schoolData.branches[currentBranch]?.sections?.get(section)?.students?.forEach { student ->
                        student?.let {
                            it.info?.name?.let { name ->
                                studentNamesList.add(name)
                            }
                        }
                    }
                    val sectionAdapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_item, studentNamesList)
                    sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinnerStudents.adapter = sectionAdapter
                    spinnerStudents.setSelection(studentspinnerIndex)


                    val last_update_data = readLASTUPDATEData_Teacher()
                    val last_update_textview = findViewById<TextView>(R.id.last_update)
                    if (last_update_data != null) {
                        val lastUpdateDate_stored = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(last_update_data)
                        val formattedTimeDifference_stored_current =formatTimeDifference(System.currentTimeMillis() - lastUpdateDate_stored.time)
                        last_update_textview.text = formattedTimeDifference_stored_current
                    }
                    // refresh btn
                    val update_btn = findViewById<Button>(R.id.updateBtn)
                    update_btn.setOnClickListener {
                        save_all_table_Teacher(currentBranch, teacher_section) { _ ->
                            lifecycleScope.launch {
                                try {
                                    AppDatabase.getDatabase(this@MainActivity)
                                    val branchesDao =
                                        AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                    all_table_teacher = branchesDao.getbranchesTable(1).toString()
                                    showToast("تم تحديث البيانات")
                                    to_Teacher_Layout(username, currentBranch, 0)
                                } catch (e: Exception) {
                                    showToast(e.message.toString())
                                }
                            }
                        }
                    }
                    // Basic information
                    val gender = schoolData.branches.get(branch)
                        ?.sections?.get(section)
                        ?.teachers?.firstOrNull()
                        ?.gender
                    if (gender == "أنثى") {
                        imageViewInfo.setImageResource(R.drawable.girlicon)
                    }
                    else {
                        imageViewInfo.setImageResource(R.drawable.boyicon)
                    }
                    // to section
                    section_btn.setOnClickListener {
                        val filteredStudents = schoolData.branches[userBranch]?.sections?.get(teacher_section)?.students
                        val students = mutableListOf<Student>()

                        filteredStudents?.forEach { jrStudent ->
                            JR_get_grade_history(jrStudent?.grade_history) { grade_history ->
                                JR_get_attendance_History(jrStudent?.attendance) { attendanceHistory ->
                                    jrStudent?.let {
                                        val student = Student(
                                            id = 0,
                                            name = it.info?.name ?: "",
                                            gender = it.info?.gender ?: "",
                                            birthday = it.info?.birthday ?: "",
                                            section = teacher_section,
                                            branch = userBranch,
                                            teacherName = username,
                                            gradeLevel = it.grade_vector ?: emptyList(),
                                            gradeHistory = grade_history ?: emptyList(),
                                            attendanceHistory = attendanceHistory ?: mutableListOf()
                                        )
                                        students.add(student)
                                    }
                                }
                            }
                        }

                        val teacheritem = Teacher(
                            teacherID = "1",
                            teacherName = username,
                            section = teacher_section,
                            branch = userBranch,
                            teacherGender = "",
                            students = students
                        )
                        to_Section_Layout(username, "teacher", teacheritem)
                    }
                    // to student grade
                    show_Grade.setOnClickListener {
                        val selectedStudent = spinnerStudents.selectedItem?.toString()
                        val result = JR_findStudentByName(selectedStudent.toString(), schoolData)
                        if (result!=null)
                        {
                            val (student_item, sectionName, branchName) = result
                            JR_get_grade_history(student_item?.grade_history){grade_history ->
                                val student = Student(
                                    id =  0,
                                    name = student_item?.info?.name.toString(),
                                    gender = student_item?.info?.gender.toString(),
                                    birthday = student_item?.info?.birthday?.replace("-","/").toString(),
                                    section = sectionName?:"",
                                    branch = branchName?:"",
                                    teacherName =  username,
                                    gradeLevel = student_item?.grade_vector?: emptyList(),
                                    gradeHistory =grade_history,
                                    attendanceHistory = mutableListOf()
                                )
                                GlobalstudentspinnerIndex =spinnerStudents.selectedItemPosition
                                to_Student_Grade_layout(username, "teacher", student, "", GlobalstudentspinnerIndex)

                            }
                        }
                    }
                    //  to attendance layouts
                    show_attendance.setOnClickListener {
                        val selectedStudent = spinnerStudents.selectedItem?.toString()
                        GlobalstudentspinnerIndex =spinnerStudents.selectedItemPosition

                        val result = JR_findStudentByName(selectedStudent.toString(), schoolData)
                        if (result!=null)
                        {
                            val (student_item, sectionName, branchName) = result
                            JR_get_attendance_History(student_item?.attendance) { attendanceHistory ->
                                val student = Student(
                                    id =  0,
                                    name = student_item?.info?.name.toString(),
                                    gender = student_item?.info?.gender.toString(),
                                    birthday = student_item?.info?.birthday?.replace("-","/").toString(),
                                    section = sectionName?:"",
                                    branch = branchName?:"",
                                    teacherName =  username,
                                    gradeLevel = student_item?.grade_vector?: emptyList(),
                                    gradeHistory = emptyList(),
                                    attendanceHistory = attendanceHistory
                                )
                                to_Student_Attendence_layout(username, "teacher", student, "", GlobalstudentspinnerIndex)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                showToast(e.message.toString())
            }
        }
        // ONLINE Only
        // Set attendance
        var dialog: AlertDialog? = null
        set_attendance.setOnClickListener {
            // Fetch the list of student names from the spinner
            val studentsNames = mutableListOf<String>()
            val spinnerAdapter = spinnerStudents.adapter as? ArrayAdapter<*>

            spinnerAdapter?.let {
                for (i in 0 until it.count) {
                    studentsNames.add(it.getItem(i).toString())
                }
            }

            // Boolean array to track which students are checked
            val checkedItems = BooleanArray(studentsNames.size)

            // Create the main layout for the dialog (vertical orientation)
            val mainLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(32, 32, 32, 32) // Set padding around the layout
            }



            // Create a ScrollView to wrap the list of checkboxes (limit height)
            val scrollView = ScrollView(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    800 // Limit the height of the ScrollView to 500 pixels
                )
            }

            // Create a vertical LinearLayout for holding the checkboxes inside the ScrollView
            val studentsLayout = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
            }

            // Dynamically add checkboxes for each student to the studentsLayout
            studentsNames.forEachIndexed { index, studentName ->
                val checkBox = CheckBox(this@MainActivity).apply {
                    text = studentName
                    isChecked = checkedItems[index]
                    setOnCheckedChangeListener { _, isChecked ->
                        checkedItems[index] = isChecked // Update the checkedItems array
                    }
                }
                studentsLayout.addView(checkBox) // Add each checkbox to the studentsLayout
            }

            // Add the studentsLayout to the ScrollView and then add the ScrollView to the main layout
            scrollView.addView(studentsLayout)
            mainLayout.addView(scrollView)

            // Create the AlertDialog with the title and mainLayout as its view
            val alertDialogBuilder = AlertDialog.Builder(this@MainActivity)
            alertDialogBuilder.setTitle("تسجيل الحضور")
            alertDialogBuilder.setView(mainLayout) // Set the main layout containing the date picker and the student list

            // Set up the positive and negative buttons for the dialog
            // Create an EditText for the date picker (non-editable, only selectable)
            val dateEditText = EditText(this@MainActivity).apply {
                hint = "أختيار التاريخ"
                isFocusable = false
                setOnClickListener {
                    showDatePickerDialog(this)
                }
            }
            mainLayout.addView(dateEditText) // Add the date picker to the layout
            alertDialogBuilder.setPositiveButton("إرسال") { _, _ -> dialog?.dismiss() }
            alertDialogBuilder.setNegativeButton("إلغاء") { _, _ -> dialog?.dismiss() }

            // Create and show the dialog
            dialog = alertDialogBuilder.create()

            // OnShowListener for handling the submission logic when the positive button is clicked
            dialog?.setOnShowListener {
                val sendButton = dialog?.getButton(AlertDialog.BUTTON_POSITIVE)
                sendButton?.setOnClickListener {
                    val selectedDate = dateEditText.text.toString()
                    if (isValidDate(selectedDate)) {
                        var successMessageShown = false // Track if success message is shown

                        studentsNames.forEachIndexed { index, studentName ->
                            val studentTableRef = database.getReference("branches")
                                .child(userBranch)
                                .child("sections")
                                .child(teacher_section)
                                .child("students")

                            val query = studentTableRef.orderByChild("info/name").equalTo(studentName)
                            query.addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    for (snapshot in dataSnapshot.children) {
                                        val studentId = snapshot.key
                                        val attendanceRecordRef = database.getReference("branches")
                                            .child(userBranch)
                                            .child("sections")
                                            .child(teacher_section)
                                            .child("students")
                                            .child(studentId!!)
                                            .child("attendance")
                                            .child(selectedDate.replace('/', '-'))

                                        // Mark attendance as 1 if checked, 0 if not
                                        val status = if (checkedItems[index]) 1 else 0
                                        attendanceRecordRef.setValue(status)
                                    }

                                    // Show success message only once
                                    if (!successMessageShown) {
                                        showToast("تم تسجيل الحضور بنجاح")
                                        save_all_table_Teacher(userBranch, teacher_section) {
                                            to_Teacher_Layout(username, userBranch, 0)
                                        }
                                        successMessageShown = true
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    showToast("حدث خطأ في الاتصال بقاعدة البيانات: ${databaseError.message}")
                                }
                            })
                        }

                        dialog?.dismiss() // Dismiss the dialog after submission
                    } else {
                        showToast("يرجى إدخال تاريخ صالح للحضور/الغياب")
                    }
                }
            }

            dialog?.show() // Show the dialog
        }



























        // Send agenda
        val sendagenda: RelativeLayout = findViewById(R.id.send_agenda)
        sendagenda.setOnClickListener {
            val selectedStudent: String? = spinnerStudents.selectedItem?.toString()
            if (selectedStudent != null) {
                // Create an AlertDialog
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("أدخل الواجب للطالب $selectedStudent")

                // Create EditText
                val input = EditText(this)
                alertDialogBuilder.setView(input)

                // Set positive button (Send)
                alertDialogBuilder.setPositiveButton("إرسال") { _, _ ->
                    val AgendaText = input.text.toString()
                    val studenttableRef1 = database.getReference("branches").child(userBranch).child("sections")
                        .child(teacher_section).child("students")
                    val query_agenda = studenttableRef1.orderByChild("info/name").equalTo(selectedStudent)
                    query_agenda.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                val studentID = snapshot.key
                                val agendaRef = database.getReference("branches")
                                    .child(userBranch)
                                    .child("sections")
                                    .child(teacher_section)
                                    .child("students")
                                    .child(studentID!!)
                                    .child("messages")
                                    .child("agenda")
                                agendaRef.setValue(AgendaText)
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                    showToast("تم إرسال الواجب بنجاح")
                }

                // Set negative button (Cancel)
                alertDialogBuilder.setNegativeButton("إلغاء") { dialog, _ ->
                    dialog.dismiss()
                }

                // Show the AlertDialog
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            } else {
                showToast("No student selected")
            }
        }
        // Send Note
        val sendNote: RelativeLayout = findViewById(R.id.send_note)
        sendNote.setOnClickListener {
            val selectedStudent: String? = spinnerStudents.selectedItem?.toString()
            if (selectedStudent != null) {
                // Create an AlertDialog
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("أدخل الملاحظة للطالب $selectedStudent")

                // Create EditText
                val input = EditText(this)
                alertDialogBuilder.setView(input)

                // Set positive button (Send)
                alertDialogBuilder.setPositiveButton("إرسال") { _, _ ->
                    val AgendaText = input.text.toString()
                    val studenttableRef1 = database.getReference("branches").child(userBranch).child("sections")
                        .child(teacher_section).child("students")
                    val query_agenda = studenttableRef1.orderByChild("info/name").equalTo(selectedStudent)
                    query_agenda.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (snapshot in dataSnapshot.children) {
                                val studentID = snapshot.key
                                val agendaRef = database.getReference("branches")
                                    .child(userBranch)
                                    .child("sections")
                                    .child(teacher_section)
                                    .child("students")
                                    .child(studentID!!)
                                    .child("messages")
                                    .child("note")
                                agendaRef.setValue(AgendaText)
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {}
                    })
                    showToast("تم إرسال الملاحظة بنجاح")
                }

                // Set negative button (Cancel)
                alertDialogBuilder.setNegativeButton("إلغاء") { dialog, _ ->
                    dialog.dismiss()
                }

                // Show the AlertDialog
                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            } else {
                showToast("No student selected")
            }
        }
}

    fun JR_fetch_teacher_info_TEACHER_NAME(teacher_name : String, branch : String,
                                           callback : (String, String)-> Unit){
        val branchesQuery = database.getReference("branches").orderByKey().equalTo(branch)
        branchesQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(branchNode: DataSnapshot) {
                val sectionsNode = branchNode.child(branch).child("sections")
                for (section_i in sectionsNode.children) {
                    val teachersNode = section_i.child("teachers")
                    for (teacherSnapshot in teachersNode.children) {
                        val Fire_name = teacherSnapshot.child("name").getValue(String::class.java)
                        if (Fire_name == teacher_name){
                            val gender = teacherSnapshot.child("gender").getValue(String::class.java)
                            callback(section_i.key.toString(), gender.toString())
                        }
                    }
                }

            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }


    // update grade data for 1 student
    private fun dialogGrade(callback: (newGrade: Int, date: String) -> Unit) {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(16, 16, 16, 16)
        val linearLayout = LinearLayout(this)
        val gradeTextView = TextView(this)
        gradeTextView.text = "0"
        gradeTextView.gravity = Gravity.CENTER
        gradeTextView.textSize = 24f
        val increaseButton = Button(this)
        increaseButton.text = "+"
        increaseButton.setTypeface(null, Typeface.BOLD)
        increaseButton.setBackgroundResource(R.drawable.add_icon)
        increaseButton.setOnClickListener {
            val currentGrade = gradeTextView.text.toString().toInt()
            if (currentGrade < 20) {
                gradeTextView.text = (currentGrade + 1).toString()
            }
        }
        val decreaseButton = Button(this)
        decreaseButton.text = "-"
        decreaseButton.setTypeface(null, Typeface.BOLD)
        decreaseButton.setBackgroundResource(R.drawable.minous_icon)
        decreaseButton.setOnClickListener {
            val currentGrade = gradeTextView.text.toString().toInt()
            if (currentGrade > -20) {
                gradeTextView.text = (currentGrade - 1).toString()
            }
        }
        // Set size and margin for the "+" button
        val increaseButtonParams = LinearLayout.LayoutParams(resources.getDimensionPixelSize(R.dimen.button_size),resources.getDimensionPixelSize(R.dimen.button_size))
        increaseButtonParams.setMargins(25, 0, 8, 0) // Adjust the margin as needed
        increaseButton.layoutParams = increaseButtonParams
        // Set size and margin for the "-" button
        val decreaseButtonParams = LinearLayout.LayoutParams(resources.getDimensionPixelSize(R.dimen.button_size),resources.getDimensionPixelSize(R.dimen.button_size))
        decreaseButtonParams.setMargins(8, 0, 25, 0) // Adjust the margin as needed
        decreaseButton.layoutParams = decreaseButtonParams
        linearLayout.addView(decreaseButton)
        linearLayout.addView(gradeTextView)
        linearLayout.addView(increaseButton)
        linearLayout.gravity=Gravity.CENTER
        layout.addView(linearLayout)

        // Edit text for grade history
        val dateText = EditText(this)
        // Set text direction to right-to-left (RTL)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            dateText.textDirection = View.TEXT_DIRECTION_RTL
        }
        dateText.hint = "تاريخ الحفظ"
        // Set margins for the EditText
        val editTextParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        editTextParams.setMargins(24, 16, 24, 16) // Adjust the margins as needed (left, top, right, bottom)
        dateText.layoutParams = editTextParams
        layout.addView(dateText)
        layout.gravity = Gravity.CENTER
        dateText.isFocusable = false // Disable keyboard input
        dateText.setOnClickListener {
            showDatePickerDialog(dateText)
        }
        val alertDialog = AlertDialog.Builder(this)
            .setTitle("تعديل - عدد الصفحات")
            .setView(layout)
            .setPositiveButton("إضافة") { _, _ ->
                val newGrade = gradeTextView.text.toString().toIntOrNull()
                //////////////////////////////////////////////////////////////////////////////////// set grade update
                if (newGrade != 0) {
                    if (dateText.text.isNotEmpty()) {
                        callback(newGrade?:0, dateText.text.toString())
                    }
                    else {
                        showToast("الرجاء إدخال تاريخ الحفظ")
                    }
                }
                else {
                    showToast("الرجاء إدخال حفظ جديد")
                }
            }
            .setNegativeButton("إلغاء") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
        alertDialog.show()
    }

    private fun teacherChangeGrade(username : String, student : Student, item: Int,
                                   newGrade: Int, date: String,
                                   callback: (done_error : String, student : Student) -> Unit) {

        val gradeTable = database.getReference("branches").child(student.branch)
            .child("sections").child(student.section).child("students")
            .orderByChild("info/name").equalTo(student.name)
            gradeTable.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshot.children.forEach { studentSnapshot ->
                        val gradeVectorSnapshot = studentSnapshot.child("grade_vector")
                        val gradeItem = gradeVectorSnapshot.value as ArrayList<Int>
                        if (isValidDate(date)) {
                             if (gradeItem != null) {
                             gradeItem[item] = gradeItem[item] + newGrade*5
                                if(gradeItem[item] >= 0 && gradeItem[item] <= 100) {
                                    /////////////////////////////////////////////////////////////////////////////////////  Add grade history
                                    getOldGradeHistoryDay(student, date, item+1) { oldHistoryDay ->
                                        val formattedDate = date.replace('/', '-')
                                        val newTotal : Long
                                        if (oldHistoryDay != null){
                                            newTotal = newGrade.toLong() + oldHistoryDay.toLong()
                                        }
                                        else
                                        {
                                            newTotal = newGrade.toLong()
                                        }
                                        if (newTotal>=0)
                                        {
                                            // set grade vector
                                            val gradeVectorReference = database.getReference("branches").child(student.branch)
                                                .child("sections").child(student.section).child("students")
                                                .child(studentSnapshot.key!!).child("grade_vector")
                                            gradeVectorReference.setValue(gradeItem)
                                            // set grade history
                                            val gradeHistoryReference = database.getReference("branches").child(student.branch)
                                                .child("sections").child(student.section).child("students")
                                                .child(studentSnapshot.key!!).child("grade_history").child(formattedDate)
                                            gradeHistoryReference.child((item + 1).toString()).setValue(newTotal)
                                            // Return to home teacher
                                            save_all_table_Teacher(currentBranch, teacher_section) { done ->
                                                if (done) {
                                                    lifecycleScope.launch {
                                                        try {
                                                            AppDatabase.getDatabase(this@MainActivity)
                                                            val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                                            all_table_teacher = branchesDao.getbranchesTable(1).toString()
                                                            // to_Teacher_Layout(username, currentBranch)
                                                            val schoolData = Gson().fromJson(all_table_teacher, JR_SchoolData::class.java)
                                                            val result = JR_findStudentByName(student.name, schoolData)
                                                            if (result!=null)
                                                            {
                                                                val (student_item, sectionName, branchName) = result
                                                                var updated_student = Student(
                                                                    id = 0,
                                                                    name = student_item?.info?.name.toString(),
                                                                    gender = student_item?.info?.gender.toString(),
                                                                    birthday = student_item?.info?.birthday?.replace("-", "/").toString(),
                                                                    section = sectionName ?: "",
                                                                    branch = branchName ?: "",
                                                                    teacherName = username,
                                                                    gradeLevel = gradeItem,
                                                                    gradeHistory = emptyList(),
                                                                    attendanceHistory = mutableListOf()
                                                                )
                                                                fetch_grade_History_STUDENT(updated_student) { gradeHistory ->
                                                                    updated_student.gradeHistory = gradeHistory
                                                                    to_Student_Grade_layout(
                                                                        username,
                                                                        "teacher",
                                                                        updated_student,
                                                                        "",
                                                                        0
                                                                    )
                                                                }
                                                            }
                                                        } catch (e: Exception) {
                                                            showToast(e.message.toString())
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        else
                                        {
                                            showToast("خطأ : الحفظ لا يمكن أن يكون سالبا")
                                        }
                                    }
                                    showToast("تمت إضافة الحفظ بنجاح")
                                }
                                else{
                                    showToast("يوجد خطأ : الحفظ يجب أن يكون بين 0 و 20 صفحة")
                                    callback("error", student)
                                }
                             }
                        }else {
                            showToast("الرجاء احتيار تاريخ صحيح")
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    callback("error", student)
                }
            })
    }

    fun getOldGradeHistoryDay(student: Student, date: String, item: Int,
                              callback: (Int?) -> Unit) {
        val formattedDate=date.replace('/', '-')
        val gradeHistoryTable = database.getReference("branches").child(student.branch)
            .child("sections").child(student.section).child("students")
            .orderByChild("info/name").equalTo(student.name)

        // Using addListenerForSingleValueEvent to get the data once
        gradeHistoryTable.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var value: Int? = null
                snapshot.children.forEach { studentSnapshot ->
                    val gradeHistoryDay = studentSnapshot.child("grade_history").child(formattedDate).child(item.toString()).getValue(Int::class.java)
                    if (gradeHistoryDay != null) {
                        value = gradeHistoryDay
                        return@forEach
                    }
                }
                callback(value)
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle the error if needed
                callback(null)
            }
        })
    }

    fun fetch_grade_History_STUDENT(student: Student,
                                    callback: (MutableList<Triple<Int, Int, Int>>) -> Unit) {
        val gradeTable = database.getReference("branches").child(student.branch)
            .child("sections").child(student.section).child("students")
            .orderByChild("info/name").equalTo(student.name)

        gradeTable.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val resultList = mutableListOf<Triple<Int, Int, Int>>()
                    for (year in 2025 downTo 2021) {
                        for (month in 12 downTo 0) {
                            fetch_Student_Month_Progress(year, month, student) { monthProgressCount ->
                                if (monthProgressCount > 0) {
                                    resultList.add(Triple(year, month, monthProgressCount))
                                }
                                if (year == 2021 && month == 0) {
                                    callback(resultList)
                                }
                            }
                        }
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })




    }
    fun fetch_Student_Month_Progress(year: Int, month: Int, student : Student,
                                     callback: (Int) -> Unit) {
        var monthProgressCount = 0
        val studentReference = database.getReference("branches").child(student.branch)
            .child("sections").child(student.section).child("students").orderByChild("info/name").equalTo(student.name)
        studentReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (studentSnapshot in dataSnapshot.children) {
                    val gradeHistory = studentSnapshot.child("grade_history")
                    for (gradeSnapshot in gradeHistory.children) {
                        val date = gradeSnapshot.key
                        val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
                        val dateObj: Date = dateFormat.parse(date.toString()) ?: continue
                        val studentYear = dateObj.year + 1900
                        val studentMonth = dateObj.month + 1
                        if (year == studentYear && month == studentMonth) {
                            // Sum up the progress for the month
                            for (entrySnapshot in gradeSnapshot.children) {
                                val progressValue = entrySnapshot.getValue(Int::class.java) ?: 0
                                monthProgressCount += progressValue
                            }
                        }
                    }
                }
                callback(monthProgressCount)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(0)
            }
        })
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////   Student
    fun clear_Student_data() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(this@MainActivity)
                database.branchesDao().removeAllBranches()
            } catch (e: Exception) {
                showToast("Failed to remove all branches: ${e.message}")
            }
        }
    }
    fun saveLASTUPDATEData_Student(last_update: String) {
        val sharedPreferences = getSharedPreferences("student_last_update", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("last_update", last_update)
        editor.apply()
    }
    fun readLASTUPDATEData_Student(): String {
        val sharedPreferences = getSharedPreferences("student_last_update", Context.MODE_PRIVATE)
        val lastUpdate = sharedPreferences.getString("last_update", null)
        return lastUpdate.toString()
    }
    private fun to_Student_Layout(username: String, branch: String) {
        currentLayout = "student_layout"
        currentUser = username
        currentUserStatus = "student"
        currentBranch = branch
        setContentView(R.layout.student_layout)

        /////////////////////////// Menu
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
            // Variables

        lifecycleScope.launch {
            try {
                val student_howmework_msg: TextView = findViewById(R.id.studenthowmeworkmsg)
                val student_note_msg: TextView = findViewById(R.id.studentnotemsg)
                val studentNameTextview: TextView = findViewById(R.id.studentnametopview)
                val studentSectionTextview: TextView = findViewById(R.id.studentsectiontopview)
                val studentBranchYTextview: TextView = findViewById(R.id.studentbranchtopview)
                val imageView: ImageView = findViewById(R.id.imageViewInfo)
                val studentageTextview: TextView = findViewById(R.id.studentagetopview)
                val studentteachertopview: TextView = findViewById(R.id.studentteachertopview)
                val showgradebtn: CardView = findViewById(R.id.student_showgrade)
                val student_showattendance: CardView = findViewById(R.id.student_showattendance)
                studentNameTextview.text = username
                studentBranchYTextview.text =  "فرع : ${currentBranch}"

                ///  Select if fetch or read from local

                AppDatabase.getDatabase(this@MainActivity)
                val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                val student_all_table = branchesDao.getbranchesTable(1)
                if (student_all_table==null){
                    JRfetchStudentInfoByName (username, branch) { section, gender, teacherName, birthday ->
                        val birthdayDate = dateFormat.parse(birthday)
                        val calendar = Calendar.getInstance()
                        val currentDate = calendar.time
                        val age = getAge(birthdayDate?:currentDate, currentDate)
                        val year = when {
                            (age < 11 || age >= 100) -> "سنوات"
                            (age in 11..99) -> "سنة"
                            else -> "سنوات"
                        }
                        studentageTextview.text = "العمر: $age $year"
                        studentteachertopview.text = "الأستاذ(ة): ${teacherName}"
                        studentSectionTextview.text = section
                        student_section = section
                        if (gender == "أنثى") {
                            imageView.setImageResource(R.drawable.girlicon)
                        }
                        else {
                            imageView.setImageResource(R.drawable.boyicon)
                        }
                        // refresh btn
                        val update_btn = findViewById<Button>(R.id.updateBtn)
                        update_btn.setOnClickListener {
                            save_all_table_Student(currentBranch, student_section, username) { _ ->
                                lifecycleScope.launch {
                                    try {
                                        AppDatabase.getDatabase(this@MainActivity)
                                        val branchesDao =
                                            AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                        all_table_student = branchesDao.getbranchesTable(1).toString()
                                        showToast("تم تحديث البيانات")
                                        to_Student_Layout(username, currentBranch)
                                    } catch (e: Exception) {
                                        showToast(e.message.toString())
                                    }
                                }
                            }
                        }
                        save_all_table_Student(branch, section, username) { _ ->
                            showToast("تم تحديث البيانات")
                            lifecycleScope.launch {
                                try {
                                    AppDatabase.getDatabase(this@MainActivity)
                                    val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                    all_table_student= branchesDao.getbranchesTable(1).toString()
                                    val schoolData = Gson().fromJson(all_table_student, JR_SchoolData::class.java)
                                    val message = schoolData.branches.get(branch)?.sections?.get(section)?.students?.firstOrNull()?.messages
                                    val agendaMessage = message?.get("agenda") ?: "لا يوجد واجبات"
                                    val noteMessage = message?.get("note") ?: "لا يوجد ملاحظات"
                                    student_howmework_msg.text = agendaMessage
                                    student_note_msg.text = noteMessage
                                    // to student grade
                                    showgradebtn.setOnClickListener {
                                        val result = JR_findStudentByName(username, schoolData)
                                        if (result!=null)
                                        {
                                            val (student_item, sectionName, branchName) = result
                                            JR_get_grade_history(student_item?.grade_history){grade_history ->
                                                val student = Student(
                                                    id =  0,
                                                    name = student_item?.info?.name.toString(),
                                                    gender =gender,
                                                    birthday = student_item?.info?.birthday?.replace("-","/").toString(),
                                                    section = sectionName?:"",
                                                    branch = branchName?:"",
                                                    teacherName =  teacherName,
                                                    gradeLevel = student_item?.grade_vector?: emptyList(),
                                                    gradeHistory =grade_history,
                                                    attendanceHistory = mutableListOf()
                                                )
                                                to_Student_Grade_layout(username, "student", student, "", 0)
                                            }
                                        }
                                    }

                                    //  to attendance layouts
                                    student_showattendance.setOnClickListener {
                                        val result = JR_findStudentByName(username, schoolData)
                                        if (result!=null)
                                        {
                                            val (student_item, sectionName, branchName) = result
                                            JR_get_attendance_History(student_item?.attendance) { attendanceHistory ->
                                                val student = Student(
                                                    id = 0,
                                                    name = student_item?.info?.name.toString(),
                                                    gender = gender,
                                                    birthday = student_item?.info?.birthday?.replace(
                                                        "-",
                                                        "/"
                                                    ).toString(),
                                                    section = sectionName ?: "",
                                                    branch = branchName ?: "",
                                                    teacherName = teacherName,
                                                    gradeLevel = student_item?.grade_vector
                                                        ?: emptyList(),
                                                    gradeHistory = emptyList(),
                                                    attendanceHistory = attendanceHistory
                                                )
                                                to_Student_Attendence_layout(
                                                    username,
                                                    "student",
                                                    student,
                                                    "",
                                                    GlobalstudentspinnerIndex
                                                )
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    showToast(e.message.toString())
                                }
                            }
                        }
                    }

                }
                else{
                    AppDatabase.getDatabase(this@MainActivity)
                    val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                    all_table_student= branchesDao.getbranchesTable(1).toString()
                    val schoolData = Gson().fromJson(all_table_student, JR_SchoolData::class.java)
                    currentBranch = schoolData.branches.keys.first()
                    val section =  schoolData.branches[currentBranch]?.sections?.keys?.first()
                    teacher_section = section.toString()
                    val gender = schoolData.branches.get(branch)?.sections?.get(section)?.students?.firstOrNull()?.info?.gender
                    if (gender == "أنثى") {
                        imageView.setImageResource(R.drawable.girlicon)
                    }
                    else {
                        imageView.setImageResource(R.drawable.boyicon)
                    }
                    val teacherName = schoolData.branches.get(branch)?.sections?.get(section)?.teachers?.firstOrNull()?.name
                    studentteachertopview.text = "الأستاذ(ة): ${teacherName}"
                    studentSectionTextview.text = section
                    val birthday = schoolData.branches.get(branch)?.sections?.get(section)?.students?.firstOrNull()?.info?.birthday
                    val birthdayDate = dateFormat.parse(birthday)
                    val calendar = Calendar.getInstance()
                    val currentDate = calendar.time
                    val age = getAge(birthdayDate?:currentDate, currentDate)
                    val year = when {
                        (age < 11 || age >= 100) -> "سنوات"
                        (age in 11..99) -> "سنة"
                        else -> "سنوات"
                    }
                    studentageTextview.text = "العمر: $age $year"
                    studentteachertopview.text = "الأستاذ(ة): ${teacherName}"
                    val message = schoolData.branches.get(branch)?.sections?.get(section)?.students?.firstOrNull()?.messages
                    val agendaMessage = message?.get("agenda") ?: "لا يوجد واجبات"
                    val noteMessage = message?.get("note") ?: "لا يوجد ملاحظات"
                    student_howmework_msg.text = agendaMessage
                    student_note_msg.text = noteMessage
                    // refresh btn
                    val update_btn = findViewById<Button>(R.id.updateBtn)
                    update_btn.setOnClickListener {
                        save_all_table_Student(branch, section.toString(), username) { _ ->
                            lifecycleScope.launch {
                                try {
                                    AppDatabase.getDatabase(this@MainActivity)
                                    val branchesDao =
                                        AppDatabase.getDatabase(this@MainActivity).branchesDao()
                                    all_table_student = branchesDao.getbranchesTable(1).toString()
                                    showToast("تم تحديث البيانات")
                                    to_Student_Layout(username, currentBranch)
                                } catch (e: Exception) {
                                    showToast(e.message.toString())
                                }
                            }
                        }
                    }
                    // to student grade
                    showgradebtn.setOnClickListener {
                        val result = JR_findStudentByName(username, schoolData)
                        if (result!=null)
                        {
                            val (student_item, sectionName, branchName) = result
                            JR_get_grade_history(student_item?.grade_history){grade_history ->
                                val student = Student(
                                    id =  0,
                                    name = student_item?.info?.name.toString(),
                                    gender = gender?:"",
                                    birthday = student_item?.info?.birthday?.replace("-","/").toString(),
                                    section = sectionName?:"",
                                    branch = branchName?:"",
                                    teacherName =  teacherName.toString(),
                                    gradeLevel = student_item?.grade_vector?: emptyList(),
                                    gradeHistory =grade_history,
                                    attendanceHistory = mutableListOf()
                                )
                                to_Student_Grade_layout(username, "student", student, "", 0)
                            }
                        }
                    }

                    //  to attendance layouts
                    student_showattendance.setOnClickListener {
                        val result = JR_findStudentByName(username, schoolData)
                        if (result!=null)
                        {
                            val (student_item, sectionName, branchName) = result
                            JR_get_attendance_History(student_item?.attendance) { attendanceHistory ->
                                val student = Student(
                                    id = 0,
                                    name = student_item?.info?.name.toString(),
                                    gender = gender?:"",
                                    birthday = student_item?.info?.birthday?.replace(
                                        "-",
                                        "/"
                                    ).toString(),
                                    section = sectionName ?: "",
                                    branch = branchName ?: "",
                                    teacherName = teacherName.toString(),
                                    gradeLevel = student_item?.grade_vector
                                        ?: emptyList(),
                                    gradeHistory = emptyList(),
                                    attendanceHistory = attendanceHistory
                                )
                                to_Student_Attendence_layout(
                                    username,
                                    "student",
                                    student,
                                    "",
                                    GlobalstudentspinnerIndex
                                )
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                showToast(e.message.toString())
            }
        }
    }

    fun JRfetchStudentInfoByName(studentName: String, branch: String,
                                 callback: (String, String, String, String) -> Unit) {
        val branchesQuery = database.getReference("branches").orderByKey().equalTo(branch)
        branchesQuery.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(branchNode: DataSnapshot) {
                val sectionsNode = branchNode.child(branch).child("sections")
                sectionsNode.children.forEach { sectionSnapshot ->
                    val studentsNode = sectionSnapshot.child("students")
                    studentsNode.children.forEach { studentSnapshot ->
                        val fireStudentName = studentSnapshot.child("info").child("name").getValue(String::class.java)
                        if (fireStudentName == studentName) {
                            val sectionName = sectionSnapshot.key.toString()
                            val gender = studentSnapshot.child("info").child("gender").getValue(String::class.java)
                            val teacherName = sectionsNode.child(sectionName).child("teachers").children.first().child("name").getValue(String::class.java)
                            val birthday = studentSnapshot.child("info").child("birthday").getValue(String::class.java)
                            callback(sectionName, gender ?: "", teacherName ?: "", birthday ?: "")
                            return
                        }
                    }
                }
                callback("", "", "", "")
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
    fun save_all_table_Student(Student_branch : String, Student_section : String,
                               StudentName : String, callback: (Boolean) -> Unit) {
        val stdQueryFire = database.getReference("branches")
        stdQueryFire.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                lifecycleScope.launch {
                    try {
                        val gson = Gson()
                        val branchesMap = mutableMapOf<String, JR_Branch>()
                        for (branchSnapshot in dataSnapshot.children) {
                            val branchName = branchSnapshot.key.toString()
                            if (Student_branch == branchName){
                                val sectionMap = mutableMapOf<String, JR_Section>()
                                for (sectionSnapshot in branchSnapshot.child("sections").children) {
                                    val sectionName = sectionSnapshot.key.toString()
                                    if (sectionName == Student_section){
                                        val studentsList = mutableListOf<JR_Student?>()
                                        val teachersList = mutableListOf<JR_Teacher?>()
                                        for (studentSnapshot in sectionSnapshot.child("students").children) {
                                            val fetchedStudentName = studentSnapshot.child("info/name").getValue(String::class.java)
                                            if(fetchedStudentName == StudentName){
                                            val studentData = studentSnapshot.getValue(JR_Student::class.java)
                                            val gradeHistoryMap = mutableMapOf<String, JR_grade_history_item>()
                                            for (grade_historySnapshot in studentSnapshot.child("grade_history").children) {
                                                val date = grade_historySnapshot.key.toString()
                                                val grade_history_item = mutableMapOf<String, Int>()
                                                for (assignmentSnapshot in grade_historySnapshot.children) {
                                                    val assignmentNumber = assignmentSnapshot.key.toString()
                                                    val grade = assignmentSnapshot.getValue(Int::class.java)
                                                        ?: 0
                                                    grade_history_item[assignmentNumber] = grade
                                                }
                                                gradeHistoryMap[date] =
                                                    JR_grade_history_item(grade_history_item)
                                            }

                                            studentData?.grade_history =JR_grade_history(gradeHistoryMap)
                                            studentsList.add(studentData)
                                        }
                                        }
                                        for (teacherSnapshot in sectionSnapshot.child("teachers").children) {
                                            val teacherData = teacherSnapshot.getValue(JR_Teacher::class.java)
                                            teachersList.add(teacherData)
                                        }
                                        val section = JR_Section(studentsList, teachersList)
                                        sectionMap[sectionName] = section
                                    }
                                }
                                branchesMap[branchName] = JR_Branch(sectionMap, mutableListOf())
                            }
                        }
                        val branchesDao = AppDatabase.getDatabase(this@MainActivity).branchesDao()
                        val schoolData = JR_SchoolData(branchesMap)
                        val jsonData = gson.toJson(schoolData)
                        val branchesEntity = branchesEntity(1, jsonData)
                        branchesDao.insertbranches(branchesEntity)
                        // Save date
                        val currentTime = System.currentTimeMillis()
                        val formattedDateTime =
                            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                                Date(currentTime)
                            )
                        saveLASTUPDATEData_Student(formattedDateTime)
                        callback(true)
                    } catch (e: Exception) {
                        showToast(e.message.toString())
                        callback(false)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////   If ADMIN
    private fun switchToAdminShowLayout(username: String) {
        setContentView(R.layout.admin_show_layout)
        currentLayout = "admin_show_layout"
        currentUser = username
        currentUserStatus = "admin"
        ///////////////////// Menu
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }

        // Add the radio btns
        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)
        fun addRadioButton(text: String) {
            val radioButton = RadioButton(this)
            radioButton.text = text
            val layoutParams = RadioGroup.LayoutParams(
                RadioGroup.LayoutParams.MATCH_PARENT,
                RadioGroup.LayoutParams.WRAP_CONTENT
            )
            layoutParams.setMargins(0, 0, 16.dpToPx(), 0)  // Set the right margin to 16dp
            radioButton.layoutParams = layoutParams
            radioGroup.addView(radioButton)
        }
        addRadioButton("الطلاب")
        addRadioButton("الأساتذة")
        addRadioButton("المشرفون")
        addRadioButton("المدراء")
        addRadioButton("مسؤول البيانات")
        // Set up a listener for the RadioGroup
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            val selectedRadioButton = findViewById<RadioButton>(checkedId)
            val selectedUser = selectedRadioButton?.text.toString()
            val selectedBranch: Spinner = findViewById(R.id.spinnerBranch)
            val defaultBranchValue = selectedBranch.selectedItem.toString()
            updateAdminshowTableUSERS(selectedUser, defaultBranchValue)

            if (selectedUser =="مسؤول البيانات" || selectedUser =="المدراء" || selectedUser =="المشرفون")
            {
                selectedBranch.visibility=View.GONE
            }
            else
            {
                selectedBranch.visibility=View.VISIBLE
            }
            selectedBranch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parentView: AdapterView<*>?,
                    selectedItemView: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedBranchValue = selectedBranch.selectedItem.toString()
                    updateAdminshowTableUSERS(selectedUser, selectedBranchValue)
                }
                override fun onNothingSelected(parentView: AdapterView<*>?) {}
            }
        }

        ///////////// Modify btn
        val adminmodifyButton: Button = findViewById(R.id.adminmodifyBtn)
        adminmodifyButton.setOnClickListener {
            switchToAdminModifyLayout(username)
        }

    }
    private fun updateAdminshowTableUSERS(selectedUser: String, selectedBranch : String) {
        when (selectedUser) {
            "الطلاب" -> adminShowStudents(selectedBranch)
            "الأساتذة" -> adminShowTeachers(selectedBranch)
            "المشرفون" -> adminShowSupervisors()
            "المدراء" -> adminShowAdminsDirectors("directors")
            "مسؤول البيانات" -> adminShowAdminsDirectors("admin")
            else -> selectedUser
        }
    }
    fun  adminShowStudents(selectedBranch : String){
        val scroll: ScrollView = findViewById(R.id.scrollView)
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val horizontalScrollView = HorizontalScrollView(this@MainActivity)
        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(16, 0, 16, 0)
        horizontalScrollView.layoutParams = params
        val tableLayout = TableLayout(this@MainActivity)
        val TableParam = TableLayout.LayoutParams(
            TableLayout.LayoutParams.WRAP_CONTENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableLayout.layoutParams = TableParam
        val studentTable = database.getReference("branches")
        studentTable.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tableLayout.removeAllViews() // Clear existing views
                //////////////////////////////////////////////////////////////////////////////////// Add titles
                val row = TableRow(this@MainActivity)
                val cardView = CardView(this@MainActivity)
                val innerLayout = LinearLayout(this@MainActivity)
                var titles: Array<String>
                var titlessizes: IntArray
                val ID_size = 150
                val name_size = 400
                val section_size = 300
                val Phone_size = 300
                val Gender_size = 200
                val Birth_size = 250
                val Pass_size = 250
                titles = arrayOf(
                    "الرقم",
                    "الاسم",
                    "الحلقة",
                    "الهاتف",
                    "ذكر/أنثى",
                    "تاريخ الميلاد",
                    "كلمة المرور"
                )
                titlessizes = intArrayOf(
                    ID_size,
                    name_size,
                    section_size,
                    Phone_size,
                    Gender_size,
                    Birth_size,
                    Pass_size
                )
                for (i in titles.indices) {
                    val textView = TextView(this@MainActivity)
                    innerLayout.gravity = Gravity.END
                    textView.text = titles[i]
                    textView.setTypeface(null, Typeface.BOLD)
                    textView.ellipsize = TextUtils.TruncateAt.END
                    textView.layoutParams = LinearLayout.LayoutParams(
                        titlessizes[i],
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    textView.setTextColor(resources.getColor(R.color.bottom_white))
                    textView.setBackgroundColor(resources.getColor(R.color.teal_700))
                    textView.textDirection = View.TEXT_DIRECTION_RTL
                    innerLayout.addView(textView, 0)
                }
                cardView.addView(innerLayout)
                row.addView(cardView)
                tableLayout.addView(row)

                // Add underline after each row
                val rowUnderline = View(this@MainActivity)
                rowUnderline.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    2
                )
                rowUnderline.setBackgroundColor(Color.BLACK)
                tableLayout.addView(rowUnderline)

                ///////////////////////////////////////////////////////////////////////////////////// Add data rows
                var Id = 1
                for (branchesSnapshot in snapshot.children) {
                    val BranchName = branchesSnapshot.key
                    if (BranchName == selectedBranch) {
                        for (Section in branchesSnapshot.child("sections").children) {
                            val SectionName = Section.key
                            for (student_i in Section.child("students").children){
                                val studentName = student_i.child("info").child("name").getValue(String::class.java)
                                val studentPhone = student_i.child("info").child("phone").getValue(String::class.java)
                                val studentGender = student_i.child("info").child("gender").getValue(String::class.java)
                                val studentBirthday = student_i.child("info").child("birthday").getValue(String::class.java)
                                val studentPassword = student_i.child("info").child("password").getValue(String::class.java)
                                val row = TableRow(this@MainActivity)
                                val cardView = CardView(this@MainActivity)
                                val innerLayout = LinearLayout(this@MainActivity)
                                // ID
                                val idTextView = TextView(this@MainActivity)
                                idTextView.text = "$Id"
                                Id += 1
                                idTextView.layoutParams = LinearLayout.LayoutParams(
                                    ID_size,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                idTextView.textDirection =
                                    View.TEXT_DIRECTION_RTL // arabic direction
                                innerLayout.addView(idTextView, 0)
                                val columnsinformation = arrayOf(
                                    studentName,
                                    SectionName,
                                    studentPhone,
                                    studentGender,
                                    studentBirthday,
                                    studentPassword
                                )
                                for (i in columnsinformation.indices) {
                                    val textView = TextView(this@MainActivity)
                                    textView.text = columnsinformation[i].toString()
                                    textView.ellipsize = TextUtils.TruncateAt.END
                                    textView.layoutParams = LinearLayout.LayoutParams(
                                        titlessizes[i + 1],
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    textView.textDirection =
                                        View.TEXT_DIRECTION_RTL // arabic direction
                                    innerLayout.addView(textView, 0)
                                }
                                cardView.addView(innerLayout)
                                row.addView(cardView)
                                tableLayout.addView(row)

                                // Add underline after each row
                                val rowUnderline = View(this@MainActivity)
                                rowUnderline.layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    2
                                )
                                rowUnderline.setBackgroundColor(Color.BLACK)
                                tableLayout.addView(rowUnderline)
                            }
                        }
                    }
                }
                // to arabic direction
                horizontalScrollView.addView(tableLayout)
                horizontalScrollView.post {
                    horizontalScrollView.scrollTo(2000, 0)
                }
                // Update the ScrollView with the new content
                scroll.removeAllViews()
                scroll.addView(horizontalScrollView)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    fun  adminShowTeachers(selectedBranch: String){
        val scroll: ScrollView = findViewById(R.id.scrollView)
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val horizontalScrollView = HorizontalScrollView(this@MainActivity)
        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(16, 0, 16, 0)
        horizontalScrollView.layoutParams = params
        val tableLayout = TableLayout(this@MainActivity)
        val TableParam = TableLayout.LayoutParams(
            TableLayout.LayoutParams.WRAP_CONTENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableLayout.layoutParams = TableParam
        val studentTable = database.getReference("branches")
        studentTable.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tableLayout.removeAllViews() // Clear existing views
                //////////////////////////////////////////////////////////////////////////////////// Add titles
                val row = TableRow(this@MainActivity)
                val cardView = CardView(this@MainActivity)
                val innerLayout = LinearLayout(this@MainActivity)
                var titles: Array<String>
                var titlessizes: IntArray
                val ID_size = 150
                val name_size = 400
                val section_size = 300
                val Phone_size = 300
                val Gender_size = 200
                val Pass_size = 250
                titles = arrayOf(
                    "الرقم",
                    "الاسم",
                    "الحلقة",
                    "الهاتف",
                    "ذكر/أنثى",
                    "كلمة المرور"
                )
                titlessizes = intArrayOf(
                    ID_size,
                    name_size,
                    section_size,
                    Phone_size,
                    Gender_size,
                    Pass_size
                )
                for (i in titles.indices) {
                    val textView = TextView(this@MainActivity)
                    innerLayout.gravity = Gravity.END
                    textView.text = titles[i]
                    textView.setTypeface(null, Typeface.BOLD)
                    textView.ellipsize = TextUtils.TruncateAt.END
                    textView.layoutParams = LinearLayout.LayoutParams(
                        titlessizes[i],
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    textView.setTextColor(resources.getColor(R.color.bottom_white))
                    textView.setBackgroundColor(resources.getColor(R.color.teal_700))
                    textView.textDirection = View.TEXT_DIRECTION_RTL
                    innerLayout.addView(textView, 0)
                }
                cardView.addView(innerLayout)
                row.addView(cardView)
                tableLayout.addView(row)

                // Add underline after each row
                val rowUnderline = View(this@MainActivity)
                rowUnderline.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    2
                )
                rowUnderline.setBackgroundColor(Color.BLACK)
                tableLayout.addView(rowUnderline)

                ///////////////////////////////////////////////////////////////////////////////////// Add data rows
                var Id = 1
                for (branchesSnapshot in snapshot.children) {
                    val BranchName = branchesSnapshot.key
                    if (BranchName == selectedBranch) {
                        for (Section in branchesSnapshot.child("sections").children) {
                            val SectionName = Section.key
                            for (teacher_i in Section.child("teachers").children){
                                val teacherName = teacher_i.child("name").getValue(String::class.java)
                                val teacherPhone = teacher_i.child("phone").getValue(String::class.java)
                                val teacherGender = teacher_i.child("gender").getValue(String::class.java)
                                val teacherPassword = teacher_i.child("password").getValue(String::class.java)
                                val row = TableRow(this@MainActivity)
                                val cardView = CardView(this@MainActivity)
                                val innerLayout = LinearLayout(this@MainActivity)
                                // ID
                                val idTextView = TextView(this@MainActivity)
                                idTextView.text = "$Id"
                                Id += 1
                                idTextView.layoutParams = LinearLayout.LayoutParams(
                                    ID_size,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                idTextView.textDirection =
                                    View.TEXT_DIRECTION_RTL // arabic direction
                                innerLayout.addView(idTextView, 0)
                                val columnsinformation = arrayOf(
                                    teacherName,
                                    SectionName,
                                    teacherPhone,
                                    teacherGender,
                                    teacherPassword
                                )
                                for (i in columnsinformation.indices) {
                                    val textView = TextView(this@MainActivity)
                                    textView.text = columnsinformation[i].toString()
                                    textView.ellipsize = TextUtils.TruncateAt.END
                                    textView.layoutParams = LinearLayout.LayoutParams(
                                        titlessizes[i + 1],
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    textView.textDirection =
                                        View.TEXT_DIRECTION_RTL // arabic direction
                                    innerLayout.addView(textView, 0)
                                }
                                cardView.addView(innerLayout)
                                row.addView(cardView)
                                tableLayout.addView(row)

                                // Add underline after each row
                                val rowUnderline = View(this@MainActivity)
                                rowUnderline.layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    2
                                )
                                rowUnderline.setBackgroundColor(Color.BLACK)
                                tableLayout.addView(rowUnderline)
                            }
                        }
                    }
                }
                // to arabic direction
                horizontalScrollView.addView(tableLayout)
                horizontalScrollView.post {
                    horizontalScrollView.scrollTo(2000, 0)
                }
                // Update the ScrollView with the new content
                scroll.removeAllViews()
                scroll.addView(horizontalScrollView)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    fun  adminShowSupervisors(){
        val scroll: ScrollView = findViewById(R.id.scrollView)
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val horizontalScrollView = HorizontalScrollView(this@MainActivity)
        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(16, 0, 16, 0)
        horizontalScrollView.layoutParams = params
        val tableLayout = TableLayout(this@MainActivity)
        val TableParam = TableLayout.LayoutParams(
            TableLayout.LayoutParams.WRAP_CONTENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        tableLayout.layoutParams = TableParam
        val studentTable = database.getReference("branches")
        studentTable.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tableLayout.removeAllViews() // Clear existing views
                //////////////////////////////////////////////////////////////////////////////////// Add titles
                val row = TableRow(this@MainActivity)
                val cardView = CardView(this@MainActivity)
                val innerLayout = LinearLayout(this@MainActivity)
                var titles: Array<String>
                var titlessizes: IntArray
                val ID_size = 150
                val name_size = 400
                val branch_size = 400
                val Phone_size = 300
                val Pass_size = 250
                titles = arrayOf(
                    "الرقم",
                    "الاسم",
                    "الفرع",
                    "الهاتف",
                    "كلمة المرور"
                )
                titlessizes = intArrayOf(
                    ID_size,
                    name_size,
                    branch_size,
                    Phone_size,
                    Pass_size
                )
                for (i in titles.indices) {
                    val textView = TextView(this@MainActivity)
                    innerLayout.gravity = Gravity.END
                    textView.text = titles[i]
                    textView.setTypeface(null, Typeface.BOLD)
                    textView.ellipsize = TextUtils.TruncateAt.END
                    textView.layoutParams = LinearLayout.LayoutParams(
                        titlessizes[i],
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    textView.setTextColor(resources.getColor(R.color.bottom_white))
                    textView.setBackgroundColor(resources.getColor(R.color.teal_700))
                    textView.textDirection = View.TEXT_DIRECTION_RTL
                    innerLayout.addView(textView, 0)
                }
                cardView.addView(innerLayout)
                row.addView(cardView)
                tableLayout.addView(row)

                // Add underline after each row
                val rowUnderline = View(this@MainActivity)
                rowUnderline.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    2
                )
                rowUnderline.setBackgroundColor(Color.BLACK)
                tableLayout.addView(rowUnderline)

                ///////////////////////////////////////////////////////////////////////////////////// Add data rows
                var Id = 1
                for (branchesSnapshot in snapshot.children) {
                    val BranchName = branchesSnapshot.key
                        for (supervisor_i in branchesSnapshot.child("supervisors").children) {
                            val supervisorName = supervisor_i.child("name").getValue(String::class.java)
                            val supervisorPassword = supervisor_i.child("password").getValue(String::class.java)
                            val supervisorPhone = supervisor_i.child("phone").getValue(String::class.java)
                                val row = TableRow(this@MainActivity)
                                val cardView = CardView(this@MainActivity)
                                val innerLayout = LinearLayout(this@MainActivity)
                                // ID
                                val idTextView = TextView(this@MainActivity)
                                idTextView.text = "$Id"
                                Id += 1
                                idTextView.layoutParams = LinearLayout.LayoutParams(
                                    ID_size,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                idTextView.textDirection =
                                    View.TEXT_DIRECTION_RTL // arabic direction
                                innerLayout.addView(idTextView, 0)
                                val columnsinformation = arrayOf(
                                    supervisorName,
                                    BranchName,
                                    supervisorPhone,
                                    supervisorPassword
                                )
                                for (i in columnsinformation.indices) {
                                    val textView = TextView(this@MainActivity)
                                    textView.text = columnsinformation[i].toString()
                                    textView.ellipsize = TextUtils.TruncateAt.END
                                    textView.layoutParams = LinearLayout.LayoutParams(
                                        titlessizes[i + 1],
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    )
                                    textView.textDirection =
                                        View.TEXT_DIRECTION_RTL // arabic direction
                                    innerLayout.addView(textView, 0)
                                }
                                cardView.addView(innerLayout)
                                row.addView(cardView)
                                tableLayout.addView(row)

                                // Add underline after each row
                                val rowUnderline = View(this@MainActivity)
                                rowUnderline.layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    2
                                )
                                rowUnderline.setBackgroundColor(Color.BLACK)
                                tableLayout.addView(rowUnderline)

                        }

                }
                // to arabic direction
                horizontalScrollView.addView(tableLayout)
                horizontalScrollView.post {
                    horizontalScrollView.scrollTo(2000, 0)
                }
                // Update the ScrollView with the new content
                scroll.removeAllViews()
                scroll.addView(horizontalScrollView)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }
    fun  adminShowAdminsDirectors(adminOrdirector: String){
        val scroll: ScrollView = findViewById(R.id.scrollView)
        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val horizontalScrollView = HorizontalScrollView(this@MainActivity)
        horizontalScrollView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val tableLayout = TableLayout(this@MainActivity)
        val TableParam = TableLayout.LayoutParams(
            TableLayout.LayoutParams.WRAP_CONTENT,
            TableLayout.LayoutParams.WRAP_CONTENT
        )
        TableParam.leftMargin = 5
        TableParam.rightMargin = 5
        tableLayout.layoutParams = TableParam
        val adminTable = database.getReference(adminOrdirector)
        adminTable.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                tableLayout.removeAllViews() // Clear existing views
                //////////////////////////////////////////////////////////////////////////////////// Add titles
                val row = TableRow(this@MainActivity)
                val cardView = CardView(this@MainActivity)
                val innerLayout = LinearLayout(this@MainActivity)
                var titles: Array<String>
                var titlessizes: IntArray
                val ID_size = 150
                val name_size = 400
                val Phone_size = 300
                val Pass_size = 250

                titles = arrayOf("الرقم", "الاسم", "الهاتف", "كلمة المرور")
                titlessizes = intArrayOf(ID_size, name_size + 70, Phone_size + 70, Pass_size)

                for (i in titles.indices) {
                    val textView = TextView(this@MainActivity)
                    innerLayout.gravity = Gravity.END
                    textView.text = titles[i]
                    textView.setTypeface(null, Typeface.BOLD)
                    textView.ellipsize = TextUtils.TruncateAt.END
                    textView.layoutParams = LinearLayout.LayoutParams(
                        titlessizes[i],
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    textView.setTextColor(resources.getColor(R.color.bottom_white))
                    textView.setBackgroundColor(resources.getColor(R.color.teal_700))
                    textView.textDirection = View.TEXT_DIRECTION_RTL
                    innerLayout.addView(textView, 0)
                }
                cardView.addView(innerLayout)
                row.addView(cardView)
                tableLayout.addView(row)

                // Add underline after each row
                val rowUnderline = View(this@MainActivity)
                rowUnderline.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    2
                )
                rowUnderline.setBackgroundColor(Color.BLACK)
                tableLayout.addView(rowUnderline)
                ///////////////////////////////////////////////////////////////////////////////////// Add data rows
                var Id = 1
                for (userSnapshot in snapshot.children) {
                    val Data = userSnapshot.getValue() as? Map<*, *>?
                    if (Data != null) {
                        val row = TableRow(this@MainActivity)
                        val cardView = CardView(this@MainActivity)
                        val innerLayout = LinearLayout(this@MainActivity)
                        // ID
                        val idTextView = TextView(this@MainActivity)
                        idTextView.text = "$Id"
                        Id+=1
                        idTextView.layoutParams = LinearLayout.LayoutParams(
                            ID_size,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        idTextView.textDirection =
                            View.TEXT_DIRECTION_RTL // arabic direction
                        innerLayout.addView(idTextView, 0)
                        val columnsinformation = arrayOf(Data["name"], Data["phone"], Data["password"])
                        for (i in columnsinformation.indices) {
                            val textView = TextView(this@MainActivity)
                            textView.text = columnsinformation[i].toString()
                            textView.ellipsize = TextUtils.TruncateAt.END
                            textView.layoutParams = LinearLayout.LayoutParams(
                                titlessizes[i + 1],
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )
                            textView.textDirection =
                                View.TEXT_DIRECTION_RTL // arabic direction
                            innerLayout.addView(textView, 0)
                        }
                        cardView.addView(innerLayout)
                        row.addView(cardView)
                        tableLayout.addView(row)
                            // Add underline after each row
                        val rowUnderline = View(this@MainActivity)
                        rowUnderline.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            2)
                        rowUnderline.setBackgroundColor(Color.BLACK)
                        tableLayout.addView(rowUnderline)
                    }
                }
                // to arabic direction
                horizontalScrollView.addView(tableLayout)
                horizontalScrollView.post {
                    horizontalScrollView.scrollTo(2000, 0)
                }
                // Update the ScrollView with the new content
                scroll.removeAllViews()
                scroll.addView(horizontalScrollView)
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun switchToAdminModifyLayout(username: String) {
        setContentView(R.layout.admin_modify_layout)
        currentLayout = "admin_modify_layout"
        currentUser = username
        currentUserStatus = "admin"
        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        val backButton = findViewById<ImageView>(R.id.back)
        backButton.setOnClickListener { view ->
            switchToAdminShowLayout(username)
        }
        // Find the user type Spinner
        val adminmodifytypeSpinner: Spinner = findViewById(R.id.adminmodifytypeSpinner)
        // Initialize the spinner with user types
        ArrayAdapter.createFromResource(
            this,
            R.array.user_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            adminmodifytypeSpinner.adapter = adapter
        }
        // Set up listener for spinner item selection
        adminmodifytypeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                newStatus = User_Type_Spinner_handleTypeSelection(position)
                setVisibilityForEditTextFields(View.VISIBLE)
                changevisibilityEdittext(newStatus)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val sectionspinner: Spinner = findViewById(R.id.spinnerSection)
        val branchSpinner: Spinner = findViewById(R.id.spinnerBranch)
        // Initialize the branch spinner with user types
        ArrayAdapter.createFromResource(
            this,
            R.array.branches,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            branchSpinner.adapter = adapter
        }
        // Set up listener for branch spinner -> available sections
        branchSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Get the selected branch
                val selectedBranch = parent.getItemAtPosition(position).toString()
                val teachersTable = database.getReference("branches")
                val query = teachersTable.orderByKey().equalTo(selectedBranch)
                query.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val sections = mutableListOf<String>()
                        for (snapshot in dataSnapshot.children) {
                            val sectionsSnapshot = snapshot.child("sections")
                            for (sectionSnapshot in sectionsSnapshot.children) {
                                val section = sectionSnapshot.key
                                section?.let {
                                    sections.add(it)
                                }
                            }
                        }
                        // Update the section spinner with the retrieved sections
                        val sectionAdapter = ArrayAdapter<String>(
                            applicationContext,
                            android.R.layout.simple_spinner_item,
                            sections
                        )
                        sectionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        sectionspinner.adapter = sectionAdapter
                    }
                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        ///////////////////////////////////////////////////////////////////////////////////////////// add and delete  users
        // Get entry Data
        val editTextName: EditText = findViewById(R.id.editTextName)
        editTextBirthday = findViewById(R.id.editTextBirthday)
        val spinnerBranch: Spinner = findViewById(R.id.spinnerBranch)
        val editTextSection: EditText = findViewById(R.id.editTextSection)
        val switchGender: Switch = findViewById(R.id.switchGender)
        val editTextPhone: EditText = findViewById(R.id.editTextPhone)
        val GenderOptions = arrayOf("Male", "Female")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, GenderOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        /// Switch colors
        switchGender.isChecked = true
        switchGender.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorMale))
        switchGender.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorMale))
        selectedGender = "ذكر"
        switchGender.setOnCheckedChangeListener { _, isChecked ->
            // Handle the toggle event
            if (isChecked) {
                // Male is selected
                switchGender.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorMale))
                switchGender.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorMale))
                selectedGender = "ذكر"
            } else {
                // Female is selected
                switchGender.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorFemale))
                switchGender.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorFemale))
                selectedGender = "أنثى"
            }
        }
        // Add new user btn
        editTextBirthday.isFocusable = false // Disable keyboard input
        editTextBirthday.setOnClickListener {
            showDatePickerDialog(editTextBirthday)
        }
        val addnewuserBtn: Button = findViewById(R.id.addnewuserBtn)
        addnewuserBtn.setOnClickListener {
            var error_text = ""
            // Get values from EditText fields
            // Name
            var nameOK = true
            val newname = editTextName.text.toString()
            if (newname == "") {
                error_text+= "- الرجاء إدخال اسم المستخدم\n"
                nameOK = false
            }
            // branch
            val newbranch = spinnerBranch.selectedItem.toString()
            // Section
            var sectionOK = true
            var newsection = ""
            if (newStatus == "teacher") {
                val editTextSection: EditText = findViewById(R.id.editTextSection)
                newsection = editTextSection.text.toString()
                if (newsection == "") {
                    error_text+="- الرجاء إدخال اسم الحلقة\n"
                    sectionOK = false
                }
            }
            else {
                val sectionSpinner: Spinner = findViewById(R.id.spinnerSection)
                if (sectionSpinner.selectedItem != null) {
                    newsection = sectionSpinner.selectedItem.toString()
                } else {
                    if ((newStatus == "student")) {
                        error_text += "- الرجاء إدخال الأستاذ قبل الطالب\n"
                        sectionOK = false
                    }
                }
            }
            // Phone
            val newphone : String
            if (editTextPhone.text.isNotEmpty()){
                newphone= editTextPhone.text.toString()
            }
            else{
                newphone= "0"
            }
            // Gender
            var genderOK = true
            val newgender = selectedGender
            if ((newgender == "")&&((newStatus=="student")||(newStatus=="teacher") )){
                error_text+="- الرجاء إختيار ذكر/أنثى\n"
                genderOK = false
            }
            // Birthday
            var birthdayOK = true
            if (newStatus=="student") {
                if (isValidDate(editTextBirthday.text.toString())) {
                    birthdayOK = true
                } else {
                    error_text += "- الرجاء احتيار تاريخ ميلاد صحيح\n"
                    birthdayOK = false
                }
            }
            val errorText = findViewById<TextView>(R.id.error_text)
            errorText.text = error_text
            // Now add if all ok
            if (((newStatus == "student") && nameOK && birthdayOK && sectionOK && genderOK ) ||
                ((newStatus == "teacher") && nameOK && sectionOK && genderOK )||
                (((newStatus == "supervisor")||(newStatus == "admin")||(newStatus == "director"))&& nameOK)
                ) {
                // check if newname already exist in the newstatus table
                checkIfNameExiststoADD(newname, newStatus, newbranch) { nameExists ->
                    if (nameExists) {
                        showToast("المستخدم $newname موجود بالفعل")
                    }
                    else {
                        when (newStatus) {
                            "student" -> addnewStudent(newname, editTextBirthday.text.toString(), selectedGender, newbranch, newsection, newphone)
                            "teacher" -> addnewTeacher(newname, selectedGender, newbranch, newsection, newphone)
                            "supervisor" -> addnewSupervisor(newname, newbranch, newphone)
                            "director" -> addnewDirector(newname, newphone)
                            "admin" -> addnewAdmin(newname, newphone)
                        }
                        editTextName.setText("")
                        editTextSection.setText("")
                        editTextPhone.setText("")
                        switchGender.isChecked = true
                    }
                }
            }
        }

        ///////////// Delete user btn
        val deleteuserBtn: Button = findViewById(R.id.deleteuserBtn)
        deleteuserBtn.setOnClickListener {
            val newname = editTextName.text.toString()
            // connnect to the database
            // check if newname already exist in the newstatus table
            val newbranch = spinnerBranch.selectedItem.toString()
            checkIfNameExiststoADD(newname, newStatus, newbranch) { nameExists ->
                if (nameExists) {
                    showConfirmationDialog(newname) {
                        removeItemFromTable(newStatus, newname, newbranch)
                        showToast("تم مسح $newname بنجاح")
                        editTextName.setText("")
                        editTextSection.setText("")
                        editTextPhone.setText("")
                        switchGender.isChecked = true
                    }
                } else {
                    showToast("$newname غير موجود")
                }
            }
        }
    }

    private fun setVisibilityForEditTextFields(visibility: Int) {
        val editTextName: EditText = findViewById(R.id.editTextName)
        editTextBirthday = findViewById(R.id.editTextBirthday)
        val spinnerBranch: CardView = findViewById(R.id.spinnerBranchCard)
        val title_Branch: TextView = findViewById(R.id.branch_title)
        val editTextSection: EditText = findViewById(R.id.editTextSection)
        val title_Section: TextView = findViewById(R.id.section_title)
        val switchGender: Switch = findViewById(R.id.switchGender)
        val MaleGender: TextView = findViewById(R.id.textMale)
        val FemaleGender: TextView = findViewById(R.id.textFemale)
        val spinnerSection: CardView = findViewById(R.id.spinnerSectionCard)

        spinnerSection.visibility = visibility
        editTextName.visibility = visibility
        editTextBirthday.visibility = visibility
        spinnerBranch.visibility = visibility
        title_Branch.visibility = visibility
        title_Section.visibility = visibility
        editTextSection.visibility = visibility
        switchGender.visibility = visibility
        MaleGender.visibility = visibility
        FemaleGender.visibility = visibility
    }
    private fun changevisibilityEdittext(newStatus: String) {
        editTextBirthday = findViewById(R.id.editTextBirthday)
        val spinnerBranch: CardView = findViewById(R.id.spinnerBranchCard)

        val switchGender: Switch = findViewById(R.id.switchGender)
        val MaleGender: TextView = findViewById(R.id.textMale)
        val FemaleGender: TextView = findViewById(R.id.textFemale)

        val spinnerSection: CardView = findViewById(R.id.spinnerSectionCard)
        val editTextSection: EditText = findViewById(R.id.editTextSection)
        val title_Section: TextView = findViewById(R.id.section_title)
        val title_Branch: TextView = findViewById(R.id.branch_title)

        when (newStatus)
        {
            "student" -> {  // Student
                editTextSection.visibility = View.GONE
            }
            "teacher" -> {  // Teacher
                editTextBirthday.visibility = View.GONE
                spinnerSection.visibility = View.GONE
                title_Section.visibility = View.GONE
            }
            "supervisor" -> {  // Supervisor
                editTextSection.visibility = View.GONE
                editTextBirthday.visibility = View.GONE
                switchGender.visibility = View.GONE
                MaleGender.visibility = View.GONE
                FemaleGender.visibility = View.GONE
                title_Section.visibility = View.GONE
                spinnerSection.visibility = View.GONE
            }
            "director", "admin"-> {  // Director
                editTextSection.visibility = View.GONE
                spinnerBranch.visibility = View.GONE
                editTextBirthday.visibility = View.GONE
                switchGender.visibility = View.GONE
                MaleGender.visibility = View.GONE
                FemaleGender.visibility = View.GONE
                title_Section.visibility = View.GONE
                title_Branch.visibility = View.GONE
                spinnerSection.visibility = View.GONE

            }

        }
    }
    // Search user functions
    private fun checkIfNameExiststoADD(newname: String,
                                       newstatus: String, newbranch: String, callback: (Boolean) -> Unit) {
        when (newstatus) {
            "teacher" -> adminSearchTeacherbyNAME(newname,  newbranch, callback)
            "student" -> adminSearchStudentbyNAME(newname, newbranch, callback)
            "director" -> adminSearchDirectorbyNAME(newname, callback)
            "supervisor" -> adminSearchSupervisorbyNAME(newname,  newbranch, callback)
            "admin" -> adminSearchAdminbyNAME(newname, callback)
        }
    }
    fun adminSearchStudentbyNAME(newname : String, newbranch : String, callback: (Boolean) -> Unit){
        val sectionSpinner: Spinner = findViewById(R.id.spinnerSection)
        val newsection = sectionSpinner.selectedItem.toString()
        val query = database.getReference("branches")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var found = false
                for (branchSnapshot in dataSnapshot.children) {
                    if (branchSnapshot.key == newbranch){
                        for (sectionSnapshot in branchSnapshot.child("sections").children) {
                            if(newsection == sectionSnapshot.key) {
                                for (studentSnapshot in sectionSnapshot.child("students").children) {
                                    val studentName = studentSnapshot.child("info/name")
                                        .getValue(String::class.java)
                                    if (studentName == newname) {
                                        found = true
                                        break
                                    }
                                }
                                if (found) break
                            }
                        }
                        if (found) break
                    }
                }
                callback(found)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }
    fun adminSearchTeacherbyNAME(newname : String, newbranch : String, callback: (Boolean) -> Unit){
        val query = database.getReference("branches")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var found = false
                for (branchSnapshot in dataSnapshot.children) {
                    if (branchSnapshot.key == newbranch){
                        for (sectionSnapshot in branchSnapshot.child("sections").children) {
                            for (studentSnapshot in sectionSnapshot.child("teachers").children) {
                                val studentName =
                                    studentSnapshot.child("name").getValue(String::class.java)
                                if (studentName == newname) {
                                    found = true
                                    break
                                }
                            }
                            if (found) break
                        }
                        if (found) break
                    }
                }
                callback(found)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }

    fun adminSearchSupervisorbyNAME(newname : String,  newbranch : String, callback: (Boolean) -> Unit){
        val query = database.getReference("branches")
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var found = false
                for (branchSnapshot in dataSnapshot.children) {
                    if (branchSnapshot.key == newbranch){
                        for (sectionSnapshot in branchSnapshot.child("supervisors").children) {
                                val studentName =
                                    sectionSnapshot.child("name").getValue(String::class.java)
                                if (studentName == newname) {
                                    found = true
                                    break
                                }

                            if (found) break
                        }
                        if (found) break
                    }
                }
                callback(found)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }
    fun adminSearchAdminbyNAME(newname : String, callback: (Boolean) -> Unit){
        val query = database.getReference("admin").orderByChild("name").equalTo(newname)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.exists())
            }
            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }
    fun adminSearchDirectorbyNAME(newname : String, callback: (Boolean) -> Unit){
        val query = database.getReference("directors").orderByChild("name").equalTo(newname)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                callback(dataSnapshot.exists())
            }
            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }
    // Add user functions
    fun addnewAdmin(newname: String, newphone : String){
        val user = director_Users(newname, newname, newphone)
        val directorRef = database.getReference("admin")
        // Retrieve the current maximum ID and increment it
        directorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var maxId = 0
                for (snapshot in dataSnapshot.children) {
                    val id = snapshot.key?.toIntOrNull() ?: continue
                    if (id > maxId) {
                        maxId = id
                    }
                }
                // Increment the maximum ID
                val newId = maxId + 1
                // Set the new director with the incremented ID
                val id = newId.toString()
                directorRef.child(id).setValue(user)
                    .addOnSuccessListener {
                        showToast("تمت إضافة $newname بنجاح")
                    }
                    .addOnFailureListener { exception ->
                        showToast("حدث خطأ أثناء إضافة $newname: $exception")
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showToast("حدث خطأ أثناء جلب البيانات")
            }
        })
    }
    fun addnewDirector(newname: String, newphone : String){
        val user = director_Users(newname, newname, newphone)
        val directorRef = database.getReference("directors")
        // Retrieve the current maximum ID and increment it
        directorRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var maxId = 0
                for (snapshot in dataSnapshot.children) {
                    val id = snapshot.key?.toIntOrNull() ?: continue
                    if (id > maxId) {
                        maxId = id
                    }
                }
                // Increment the maximum ID
                val newId = maxId + 1
                // Set the new director with the incremented ID
                val id = newId.toString()
                directorRef.child(id).setValue(user)
                    .addOnSuccessListener {
                        showToast("تمت إضافة $newname بنجاح")
                    }
                    .addOnFailureListener { exception ->
                        showToast("حدث خطأ أثناء إضافة $newname: $exception")
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                showToast("حدث خطأ أثناء جلب البيانات")
            }
        })
    }
    fun addnewStudent(newname: String, birthday: String, gender: String, branch: String, section: String, newphone: String) {
        val branchesRef = database.getReference("branches")
        branchesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var maxId = 0
                for (branchSnapshot in dataSnapshot.children) {
                    for (sectionSnapshot in branchSnapshot.child("sections").children) {
                        for (studentSnapshot in sectionSnapshot.child("students").children) {
                            val id = studentSnapshot.key?.toIntOrNull() ?: continue
                            if (id > maxId) {
                                maxId = id
                            }
                        }
                    }
                }
                val newId = maxId + 1
                val id = newId.toString()
                val studentInfo = JR_StudentInfo(birthday, gender, newname, newname, newphone)
                branchesRef.child(branch).child("sections").child(section).child("students").child(id)
                    .child("info").setValue(studentInfo)
                    .addOnSuccessListener {
                        // Add empty grade for students
                        val gradeRef = branchesRef.child(branch).child("sections").child(section).child("students").child(id)
                            .child("grade_vector")
                        val gradeItems = List(30) { 0 }
                        gradeRef.setValue(gradeItems)
                            .addOnSuccessListener {
                                showToast("تمت إضافة $newname بنجاح")
                            }
                            .addOnFailureListener { exception ->
                                showToast("حدث خطأ أثناء إضافة الدرجات لـ $newname: $exception")
                            }
                    }
                    .addOnFailureListener { exception ->
                        showToast("حدث خطأ أثناء إضافة $newname: $exception")
                    }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                showToast("حدث خطأ أثناء جلب البيانات")
            }
        })
    }
    fun addnewTeacher(newname: String, gender: String, branch: String, section: String, newphone: String) {
        val branchesRef = database.getReference("branches")
        branchesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var maxId = 0
                for (branchSnapshot in dataSnapshot.children) {
                    for (sectionSnapshot in branchSnapshot.child("sections").children) {
                        for (studentSnapshot in sectionSnapshot.child("teachers").children) {
                            val id = studentSnapshot.key?.toIntOrNull() ?: continue
                            if (id > maxId) {
                                maxId = id
                            }
                        }
                    }
                }
                val newId = maxId + 1
                val id = newId.toString()
                val teacherInfo = JR_Teacher( newname, newname, gender, newphone)
                branchesRef.child(branch).child("sections").child(section).child("teachers").child(id)
                   .setValue(teacherInfo).addOnSuccessListener {
                        showToast("تمت إضافة $newname بنجاح")
                    }
                    .addOnFailureListener { exception ->
                        showToast("حدث خطأ أثناء إضافة $newname: $exception")
                    }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                showToast("حدث خطأ أثناء جلب البيانات")
            }
        })
    }

    fun addnewSupervisor(newname: String, branch: String, newphone: String) {
        val branchesRef = database.getReference("branches")
        branchesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var maxId = 0
                for (branchSnapshot in dataSnapshot.children) {
                    for (sectionSnapshot in branchSnapshot.child("supervisors").children) {
                        val id = sectionSnapshot.key?.toIntOrNull() ?: continue
                        if (id > maxId) {
                            maxId = id
                        }
                    }
                }
                val newId = maxId + 1
                val id = newId.toString()
                val supervisorInfo = JR_Supervisor( newname, newname, newphone)
                branchesRef.child(branch).child("supervisors").child(id)
                    .setValue(supervisorInfo).addOnSuccessListener {
                        showToast("تمت إضافة $newname بنجاح")
                    }
                    .addOnFailureListener { exception ->
                        showToast("حدث خطأ أثناء إضافة $newname: $exception")
                    }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                showToast("حدث خطأ أثناء جلب البيانات")
            }
        })
    }
    // Delete user functions
    private fun showConfirmationDialog(name: String, onConfirm: () -> Unit) {
        val alertDialogBuilder = AlertDialog.Builder(this)

        // Create a custom TextView for the title with RTL layout direction
        val titleTextView = TextView(this)
        titleTextView.text = "تأكيد"
        titleTextView.textDirection = View.TEXT_DIRECTION_RTL
        titleTextView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        titleTextView.textSize = 24f // Set the desired text size
        titleTextView.setTypeface(null, Typeface.BOLD)
        titleTextView.setPadding(24, 24, 24, 24) // Add padding
        titleTextView.setTextColor(Color.BLACK) // Change text color

        alertDialogBuilder.setCustomTitle(titleTextView)

        // Create a TextView with RTL layout direction for the message
        val messageTextView = TextView(this)
        messageTextView.text = "هل أنت متأكد من مسح $name ؟ "
        messageTextView.textSize = 16f
        messageTextView.setPadding(24, 24, 24, 24) // Add padding

        messageTextView.setTypeface(null, Typeface.BOLD)
        messageTextView.textDirection = View.TEXT_DIRECTION_RTL
        messageTextView.layoutDirection = View.LAYOUT_DIRECTION_RTL

        alertDialogBuilder.setView(messageTextView)

        alertDialogBuilder.setPositiveButton("نعم") { _, _ ->
            onConfirm.invoke()
        }
        alertDialogBuilder.setNegativeButton("كلا") { _, _ ->
        }

        val alertDialog: AlertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    fun removeItemFromTable(newstatus : String, newname: String, branch : String) {
        val sectionSpinner: Spinner = findViewById(R.id.spinnerSection)
        val section = sectionSpinner.selectedItem.toString()
        when(newstatus){
            "student" -> removeStudent(newname, branch, section)
            "teacher" -> removeTeacher(newname, branch)
            "supervisor" -> removeSupervisor(newname, branch)
            "director" -> removeDirector(newname)
            "admin" -> removeAdmin(newname)
        }
    }
    fun removeStudent(name: String, branch: String, section: String) {
        database.getReference("branches").child(branch).child("sections").child(section)
            .child("students").orderByChild("info/name").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (studentSnapshot in dataSnapshot.children) {
                            studentSnapshot.ref.removeValue()
                        }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
    }


    fun removeTeacher(name : String, branch: String){
        val sectionEDIT = findViewById<EditText>(R.id.editTextSection)
        val section = sectionEDIT.text.toString()
        if (section != "") {
            database.getReference("branches").child(branch).child("sections").child(section)
                .child("teachers").orderByChild("name").equalTo(name)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (studentSnapshot in dataSnapshot.children) {
                            studentSnapshot.ref.removeValue()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // Handle onCancelled if needed
                    }
                })
        }
        else{
            showToast("الرجاء ادخال اسم الحلقة")
        }
    }
    fun removeSupervisor(name : String, branch: String){
        database.getReference("branches").child(branch).child("supervisors").orderByChild("name").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (studentSnapshot in dataSnapshot.children) {
                        studentSnapshot.ref.removeValue()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle onCancelled if needed
                }
            })
    }
    fun removeDirector(name : String){
        database.getReference("directors").orderByChild("name").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
    }
    fun removeAdmin(name : String){
        database.getReference("admin").orderByChild("name").equalTo(name)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
    }

    //////////////////////////////////////////////////////////////////////////////////////////////// Back and Logout
    fun showLogoutDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("تسجيل الخروج")
        alertDialogBuilder.setMessage("هل تريد فعلا تسجيل الخروج؟")
        alertDialogBuilder.setPositiveButton("نعم") { _, _ ->
            handler.removeCallbacksAndMessages(null)
            all_table_data =""
            all_table_supervisor =""
            all_table_teacher = ""
            teacher_section = ""
            all_table_student = ""
            student_section = ""
            currentLayout = "main"
            currentUser = ""
            currentUserStatus = "ghest"
            currentBranch =""
            grade_Level_or_History = ""
            clearaccount_data()
            clear_Student_data()
            clear_Teacher_data()
            clear_Supervisor_data()
            clear_Director_data()
            to_Login_Layout() // Finish the activity or navigate to the login screen
        }
        alertDialogBuilder.setNegativeButton("كلا") { dialog, _ ->
            dialog.dismiss()
        }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }
    override fun onBackPressed() {
        when (currentLayout) {
            "main" -> finish()
            "weare", "contact", "explain" -> to_Login_Layout()
            "director_layout", "student_layout", "supervisor_layout", "teacher_layout",
            "admin_show_layout" -> showLogoutDialog()
            "supervisor_statistics_layout", "supervisor_monitoring_students_layout",
            "supervisor_monitoring_sections_layout", "supervisor_toplist_layout"
            -> to_Supervisor_Layout(currentUser, userBranch)

            "director_statistics_layout", "director_monitoring_students_layout",
            "director_toplist_layout" ,"director_monitoring_sections_layout"
            -> to_Director_Layout(currentUser)

            "admin_modify_layout" -> switchToAdminShowLayout(currentUser)
            "section_layout" -> {
                when (currentUserStatus) {
                    "student", "teacher", "supervisor", "director"
                    -> switchToRelevantLayout_SECTION(currentUser)
                }
            }
            "change_password_layout" ->  {
                when (currentUserStatus) {
                    "student", "teacher", "supervisor", "director", "admin"
                    -> switchToRelevantLayout_MAIN(currentUser)
                }
            }
            "show_student_grade_layout", "show_student_attendance_layout" -> {
                when (currentUserStatus) {
                    "student", "teacher", "supervisor", "director"
                    -> switchToRelevantLayout_GRADE_ATTENDANCE(currentUser)
                }
            }
        }
    }
    private fun switchToRelevantLayout_GRADE_ATTENDANCE(currentUser: String) {
        when (currentUserStatus) {
            "student" -> to_Student_Layout(currentUser, userBranch)
            "teacher" -> to_Teacher_Layout(currentUser, userBranch, GlobalstudentspinnerIndex)
            "supervisor" -> to_Supervisor_Monitoring_Students(currentUser, currentBranch, all_table_supervisor)
            "director" -> to_Director_Monitoring_Students(currentUser, all_table_data)
        }
    }
    private fun switchToRelevantLayout_SECTION(currentUser: String) {
        when (currentUserStatus) {
            "teacher" -> to_Teacher_Layout(currentUser, userBranch, GlobalstudentspinnerIndex)
            "supervisor" -> to_Supervisor_Monitoring_Sections(currentUser, currentBranch, all_table_supervisor)
            "director" -> to_Director_Monitoring_Sections(currentUser, all_table_data)
        }
    }
    private fun switchToRelevantLayout_MAIN(currentUser: String) {
        when (currentUserStatus) {
            "student" -> to_Student_Layout(currentUser, userBranch)
            "teacher" -> to_Teacher_Layout(currentUser, userBranch, GlobalstudentspinnerIndex)
            "supervisor" -> to_Supervisor_Layout(currentUser, userBranch)
            "director" -> to_Director_Layout(currentUser)
            "admin" -> switchToAdminShowLayout(currentUser)

        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////// Common basic functions
    fun formatTimeDifference(timeDifference: Long): String {
        val seconds = timeDifference / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        return when {
            days > 0 -> " آخر تحديث : منذ $days يوم "
            hours > 0 -> " آخر تحديث : منذ ${(hours % 24)} ساعة "
            minutes > 0 ->  " آخر تحديث : منذ $minutes دقائق "
            (hours < 1) && (minutes < 1) && (seconds < 5) -> " آخر تحديث : الآن"
            else ->  " آخر تحديث : منذ $seconds ثوان "
        }
    }
    private fun showToast(message: String) {
        val toast = Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT)
        toast.show()
    }

    private fun clearaccount_data() {
        val account_data = getSharedPreferences("account_login_data", Context.MODE_PRIVATE)
        val editor = account_data.edit()
        editor.remove("username")
        editor.remove("password")
        editor.remove("userType")
        editor.clear()
        editor.apply()
    }
    fun Int.dpToPx(): Int {
        val scale = resources.displayMetrics.density
        return (this * scale + 0.5f).toInt()
    }
    fun pages_to_jiz2(number_of_pages: Int): String {
        val result = number_of_pages / 20
        val remainder = number_of_pages % 20
        var unit = when {
            result == 0 -> ""
            result == 1 -> "جزء"
            result == 2 -> "جزئين"
            result < 11 -> "أجزاء"
            else -> "جزء"
        }
        val prefix = if ((remainder < 11)&&(remainder >2)) "صفحات" else if (remainder == 0) "" else if (remainder == 2) "صفحتين" else "صفحة"
        val conjunction = if (remainder > 0) "و" else ""

        val resultString = if (result == 0) "" else "$result $unit $conjunction"

        return if (remainder == 0) resultString else "$resultString $remainder $prefix"
    }
    fun getAge(birthDate: Date, currentDate: Date): Int {
        val birthCalendar = Calendar.getInstance()
        birthCalendar.time = birthDate

        val currentCalendar = Calendar.getInstance()
        currentCalendar.time = currentDate

        var age = currentCalendar.get(Calendar.YEAR) - birthCalendar.get(Calendar.YEAR)

        if (currentCalendar.get(Calendar.DAY_OF_YEAR) < birthCalendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        return age
    }
    private fun showDatePickerDialog(dateEditText: EditText) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        Locale.setDefault(Locale("ar"))

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                dateEditText.setText(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
        Locale.setDefault(Locale.getDefault())
    }
    private fun isValidDate(date: String): Boolean {

        // Define the expected date format using a regular expression
        val dateFormat = Regex("\\d{1,2}/\\d{1,2}/\\d{4}")

        // Check if the entered date matches the expected format
        if (!date.matches(dateFormat)) {
            return false
        }

        // Parse the entered date string into a Date object
        val enteredDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(date)

        // Get the current date
        val currentDate = Calendar.getInstance().time

        // Compare the entered date with the current date
        return enteredDate?.before(currentDate) == true
    }
    fun getArabicMonthName(month: Int): String {
        return when (month) {
            1 -> "كانون الثاني"
            2 -> "شباط"
            3 -> "آذار"
            4 -> "نيسان"
            5 -> "أيار"
            6 -> "حزيران"
            7 -> "تموز"
            8 -> "آب"
            9 -> "أيلول"
            10 -> "تشرين الأول"
            11 -> "تشرين الثاني"
            12 -> "كانون الأول"
            else -> ""
        }
    }
    private fun calculateProgressBarWidth(monthProgressCount: Int, maxScore: Int): Int {

        // Assuming total width is the screen width (you can adjust this accordingly)
        val totalWidth = resources.displayMetrics.widthPixels

        // Subtracting 200dp for TextView1 width
        val availableWidth = totalWidth - 500

        // Scaling monthProgressCount to fill the available space
        val progressBarWidth = (availableWidth * monthProgressCount)/ maxScore

        return progressBarWidth
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////   Menu : change password
    private fun showOptionsMenu(view: View, username: String) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.inflate(R.menu.options_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.change_pass -> {
                    to_change_password_Layout(username, currentUserStatus)
                    true
                }
                R.id.logout_menu -> {
                    showLogoutDialog()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
    private fun to_change_password_Layout(username : String, status: String){
        setContentView(R.layout.change_password_layout)
        currentLayout = "change_password_layout"
        currentUserStatus = status
        currentUser = username
        //////////////////////////////////////////////////////////////////////////////////////////// Menu
        val toolbarTitle = findViewById<TextView>(R.id.toolbarTitle)
        toolbarTitle.text = when (status) {
            "student" -> "طالب"
            "teacher" -> "أستاذ"
            "supervisor" -> "مشرف"
            "director" -> "مدير"
            "admin" -> "مسؤول البيانات"
            else -> "Unknown Status"
        }
        val optionsMenuButton = findViewById<ImageView>(R.id.optionsMenuButton)
        optionsMenuButton.setOnClickListener { view ->
            showOptionsMenu(view, username)
        }
        val back = findViewById<ImageView>(R.id.back)
        back.setOnClickListener { view ->
            when (status) {
                "teacher" -> to_Teacher_Layout(username, userBranch, 0)
                "supervisor" -> to_Supervisor_Layout(username, userBranch)
                "director" -> to_Director_Layout(username)
                "student" -> to_Student_Layout(username, userBranch)
                "admin" -> switchToAdminShowLayout(username)

            }
        }
        ////////////////////////////////////////////////////////// Change the password
        val oldPass = findViewById<EditText>(R.id.old_pass)
        val newPass = findViewById<EditText>(R.id.newpass)
        val confirmPass = findViewById<EditText>(R.id.confirmnewpass)
        val changeBtn = findViewById<Button>(R.id.change_pass_btn)
        val errorText = findViewById<TextView>(R.id.error_text)
        var allowChange_old = true
        var allowChange_confirm = true
        var allowChange_length = true
        changeBtn.setOnClickListener {
            // Step 1: Verify if the old password is correct
            get_oldpass_USERNAME(username){ FetchedoldPass ->
                if (oldPass.text.toString() != FetchedoldPass) {
                    errorText.text = "كلمة المرور القديمة غير صحيحة"
                    allowChange_old = false
                }
                else{allowChange_old = true}
                // Step 2: Verify if the new password and confirm password match
                val newPassword = newPass.text.toString()
                val confirmPassword = confirmPass.text.toString()
                if (newPassword != confirmPassword) {
                    errorText.text = "كلمة المرور و التأكيد لا يتطابقان"
                    allowChange_confirm = false
                }
                else{allowChange_confirm = true}
                // Step 3: Test if the length of the new password is more than 3
                if (newPassword.length <= 3) {
                    errorText.text = "كلمة المرور يجب أن تكون اكبر من 4 أحرف/أرقام"
                    allowChange_length = false
                }
                else{allowChange_length = true}

                // If all checks pass, show a dialog to change the password
                // You can implement your logic here to actually change the password

                // For example, you can show an AlertDialog
                if (allowChange_old && allowChange_confirm && allowChange_length) {
                    errorText.text = ""
                    val alertDialog = AlertDialog.Builder(this)
                    alertDialog.setTitle("تغيير كلمة المرور")
                    alertDialog.setMessage("لقد تم بنجاح تغيير كلمة المرور")
                    change_password_operation(username, newPassword){done ->
                        if(done){
                            alertDialog.setPositiveButton("موافق") { dialog, _ ->
                                dialog.dismiss()
                                to_Login_Layout()
                            }
                            alertDialog.show()
                        }
                    }

                }

            }

        }

    }

    private fun change_password_operation(username: String, newPassword: String,
                                          callback: (Boolean) -> Unit) {
        val databaseRef = when (currentUserStatus) {
            "director" -> database.getReference("directors")
            "teacher" -> database.getReference("branches")
            "student" -> database.getReference("branches")
            "admin" -> database.getReference("admin")
            "supervisor" -> database.getReference("branches")
            else -> null // Handle other cases or provide a default behavior
        }

        databaseRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var passwordChanged = false
                when (currentUserStatus) {
                    "director" -> {
                        for (snapshot in dataSnapshot.children) {
                            val directorName = snapshot.child("name").value.toString()
                            if (directorName == username) {
                                snapshot.ref.child("password").setValue(newPassword)
                                passwordChanged = true
                                break
                            }
                        }
                    }
                    "teacher" -> {
                        for (branchSnapshot in dataSnapshot.children) {
                            for (sectionSnapshot in branchSnapshot.child("sections").children) {
                                for (teacherSnapshot in sectionSnapshot.child("teachers").children) {
                                    val teacherUsername = teacherSnapshot.child("name").value.toString()
                                    if (teacherUsername == username) {
                                        teacherSnapshot.ref.child("password").setValue(newPassword)
                                        passwordChanged = true
                                        break
                                    }
                                }
                                if (passwordChanged) break
                            }
                            if (passwordChanged) break
                        }
                    }
                    "student" -> {
                        for (branchSnapshot in dataSnapshot.children) {
                            for (sectionSnapshot in branchSnapshot.child("sections").children) {
                                for (studentSnapshot in sectionSnapshot.child("students").children) {
                                    val studentName = studentSnapshot.child("info").child("name").value.toString()
                                    if (studentName == username) {
                                        studentSnapshot.ref.child("info").child("password").setValue(newPassword)
                                        passwordChanged = true
                                        break
                                    }
                                }
                                if (passwordChanged) break
                            }
                            if (passwordChanged) break
                        }
                    }
                    "admin" -> {
                        for (snapshot in dataSnapshot.children) {
                            val adminName = snapshot.child("name").value.toString()
                            if (adminName == username) {
                                snapshot.ref.child("password").setValue(newPassword)
                                passwordChanged = true
                                break
                            }
                        }
                    }
                    "supervisor" -> {
                        for (branchSnapshot in dataSnapshot.children) {
                            for (supervisorSnapshot in branchSnapshot.child("supervisors").children) {
                                val supervisorUsername = supervisorSnapshot.child("name").value.toString()
                                if (supervisorUsername == username) {
                                    supervisorSnapshot.ref.child("password").setValue(newPassword)
                                    passwordChanged = true
                                    break
                                }
                            }
                            if (passwordChanged) break
                        }
                    }

                    else -> {
                        // Handle other cases or provide a default behavior
                    }
                }
                callback(passwordChanged)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                callback(false)
            }
        })
    }

    private fun get_oldpass_USERNAME(username: String, callback: (String) -> Unit) {
        when (currentUserStatus) {
            "admin" -> {
                val directorsRef = database.getReference("admin")
                directorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var password = ""
                        for (directorSnapshot in dataSnapshot.children) {
                            val directorName = directorSnapshot.child("name").value.toString()
                            if (directorName == username) {
                                password = directorSnapshot.child("password").value.toString()
                                callback(password)
                                return
                            }
                        }
                        // Username not found
                        callback(password)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        callback("")
                    }
                })
            }
            "director" -> {
                val directorsRef = database.getReference("directors")
                directorsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var password = ""
                        for (directorSnapshot in dataSnapshot.children) {
                            val directorName = directorSnapshot.child("name").value.toString()
                            if (directorName == username) {
                                password = directorSnapshot.child("password").value.toString()
                                callback(password)
                                return
                            }
                        }
                        // Username not found
                        callback(password)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        callback("")
                    }
                })
            }
            "supervisor" -> {
                val branchesRef = database.getReference("branches")
                branchesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var password = ""
                        for (branchSnapshot in dataSnapshot.children) {
                            for (supervisorSnapshot in branchSnapshot.child("supervisors").children) {
                                val supervisorUsername = supervisorSnapshot.child("name").value.toString()
                                if (supervisorUsername == username) {
                                    password = supervisorSnapshot.child("password").value.toString()
                                    callback(password)
                                    return
                                }
                            }
                        }
                        // Username not found
                        callback(password)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        callback("")
                    }
                })
            }
            "teacher" -> {
                val branchesRef = database.getReference("branches")
                branchesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var password = ""
                        for (branchSnapshot in dataSnapshot.children) {
                            for (sectionSnapshot in branchSnapshot.child("sections").children) {
                                for (teacherSnapshot in sectionSnapshot.child("teachers").children) {
                                    val teacherUsername = teacherSnapshot.child("name").value.toString()
                                    if (teacherUsername == username) {
                                        password = teacherSnapshot.child("password").value.toString()
                                        callback(password)
                                        return
                                    }
                                }
                            }
                        }
                        // Username not found
                        callback(password)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        callback("")
                    }
                })
            }
            "student" -> {
                val branchesRef = database.getReference("branches")
                branchesRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var password = ""
                        for (branchSnapshot in dataSnapshot.children) {
                            for (sectionSnapshot in branchSnapshot.child("sections").children) {
                                for (studentSnapshot in sectionSnapshot.child("students").children) {
                                    val studentName = studentSnapshot.child("info").child("name").value.toString()
                                    if (studentName == username) {
                                        password = studentSnapshot.child("info").child("password").value.toString()
                                        callback(password)
                                        return
                                    }
                                }
                            }
                        }
                        // Username not found
                        callback(password)
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        callback("")
                    }
                })
            }
            else -> {
                // Handle other cases or provide a default behavior
                callback("")
            }
        }
    }
}

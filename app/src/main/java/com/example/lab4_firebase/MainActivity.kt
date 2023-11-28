package com.example.lab4_firebase

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.example.lab4_firebase.ui.theme.Lab4_firebaseTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

// data class of Task which will be push to the firebase
data class Task(
    val priority: String = "",
    val date: String = "",
    val taskName: String = "",
    val taskDescription: String = ""
) {
    // Default no-argument constructor required for Firebase deserialization
    constructor() : this("", "", "", "")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        setContent {
            Lab4_firebaseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    //
                    val task1 = Task(
                        priority = "Normal",
                        date = "2023-11-27", // Set your desired date format
                        taskName = "First Task",
                        taskDescription = "Something1"
                    )
                    val task2 = Task(
                        priority = "High",
                        date = "2023-11-27", // Set your desired date format
                        taskName = "Second Task",
                        taskDescription = "Something2"
                    )
                    val task3 = Task(
                        priority = "Low",
                        date = "2023-11-27", // Set your desired date format
                        taskName = "Third Task",
                        taskDescription = "Something3"
                    )

                    Greeting(task1,task2,task3)
                }
            }
        }
    }
}

@Composable
fun Greeting(task1:Task,task2:Task,task3: Task, modifier: Modifier = Modifier) {

    writeTaskToDatabase(task1)
    writeTaskToDatabase(task2)
    writeTaskToDatabase(task3)
    readTaskNames()
}

fun writeTaskToDatabase(task: Task) {
    // Get a reference to the database
    val database= Firebase.database
    val myRef= database.getReference("tasks")

    // Push the task to the database
    val taskReference = myRef.push()
    taskReference.setValue(task)
}
@Composable
fun readTaskNames() {
    val database = Firebase.database
    val tasksRef = database.getReference("tasks")

    // Save task names to list
    val taskNames = remember {mutableListOf<String>()}
    val taskPriorities = remember {mutableListOf<String>()}

    // Save task names to list
    val taskNamesState = remember { mutableStateOf<List<String>>(emptyList()) }
    val taskPrioritiesState = remember { mutableStateOf<List<String>>(emptyList()) }


    // Composable function to watch the changes in database
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                taskNames.clear()
                taskPriorities.clear()
                for (postSnapshot in snapshot.children) {
                    val task = postSnapshot.getValue(Task::class.java)
                    task?.let { taskNames.add(it.taskName) }
                    task?.let { taskPriorities.add(it.priority) }


                }
                // Update data from firebase to val
                taskNamesState.value = taskNames
                taskPrioritiesState.value = taskPriorities

            }

            override fun onCancelled(error: DatabaseError) {
                // Handle errors
            }

        }

        // Add the ValueEventListener to the database
        tasksRef.addValueEventListener(listener)

        // Remove the event listener
        onDispose {
            tasksRef.removeEventListener(listener)
        }

    }
    if (taskNamesState.value.isNotEmpty() && taskPrioritiesState.value.isNotEmpty()) {
        Column {
            // Send to app screen
            Text(text = "Dane z Firebase: ")
            Text(text = "Nazwy zadań: ${taskNames.first()},${taskNames[2]}, ${taskNames[3]} ")
            Text(text = "Ważność: ${taskPriorities.first()},${taskPriorities[2]}, ${taskPriorities[3]} ")

            // Send to logcat
            Log.d("TaskName", "Task Name is: ${taskNames.first()}")
            Log.d("TaskName", "Task Name is: ${taskNames[2]}")
            Log.d("TaskName", "Task Name is: ${taskNames[3]}")
        }
    }
    // Error handler
    else {
        Text(text = "No data from Firebase")
    }

}


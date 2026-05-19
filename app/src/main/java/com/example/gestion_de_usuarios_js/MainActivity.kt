package com.example.gestion_de_usuarios_js

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {

    private lateinit var dbHelper: DatabasOpenHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        dbHelper = DatabasOpenHelper(this)
        setContent {
            var showSplash by remember { mutableStateOf(true) }

            if (showSplash) {
                SplashScreen {
                    showSplash = false
                }
            } else {
                addUser()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun addUser() {
        var name by remember { mutableStateOf("") }
        var lastname by remember { mutableStateOf("") }
        var age by remember { mutableStateOf("") }

        // Estado para el género seleccionado
        var gender by remember { mutableStateOf("") }
        val genderOptions = listOf("Male", "Female", "Other")
        var expanded by remember { mutableStateOf(false) }

        var phone by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }

        var users by remember { mutableStateOf(dbHelper.getAllUsers()) }

        var editingUserId by remember { mutableStateOf<Int?>(null) }

        val context = LocalContext.current

        Column(modifier = Modifier.padding(50.dp)) {
            TextField(value = name, onValueChange = { name = it }, label = { Text("Name") })
            TextField(value = lastname, onValueChange = { lastname = it }, label = { Text("Lastname") })
            TextField(value = age, onValueChange = { age = it }, label = { Text("Age") })

            // Menú desplegable para seleccionar el género
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                modifier = Modifier.width(280.dp),
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Expand Menu"
                        )
                    }
                }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                genderOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            gender = selectionOption
                            expanded = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            TextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
            TextField(value = email, onValueChange = { email = it }, label = { Text("Email") })
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                if (editingUserId == null) {
                    // Insertar usuario
                    if (dbHelper.insertUser(name, lastname, age.toIntOrNull() ?: 0, gender, phone, email)) {
                        Toast.makeText(context, "Usuario insertado correctamente", Toast.LENGTH_LONG).show()
                        users = dbHelper.getAllUsers()
                    } else {
                        Toast.makeText(context, "Error al insertar el usuario", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Actualizar usuario
                    if (dbHelper.updateUser(editingUserId!!, name, lastname, age.toIntOrNull() ?: 0, gender, phone, email)) {
                        Toast.makeText(context, "Usuario actualizado correctamente", Toast.LENGTH_LONG).show()
                        users = dbHelper.getAllUsers()
                        editingUserId = null
                    } else {
                        Toast.makeText(context, "Error al actualizar el usuario", Toast.LENGTH_LONG).show()
                    }
                }
            }) {
                Text(text = if (editingUserId == null) "Insert User" else "Update User")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de usuarios
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(users) { user ->
                    UserRow(
                        user = user,
                        onDelete = {
                            if (dbHelper.deleteUser(user["id"] as Int)) {
                                users = dbHelper.getAllUsers()
                                Toast.makeText(context, "Usuario eliminado correctamente", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Error al eliminar el usuario", Toast.LENGTH_LONG).show()
                            }
                        },
                        onEdit = {
                            editingUserId = user["id"] as Int
                            name = user["name"] as String
                            lastname = user["lastname"] as String
                            age = (user["age"] as Int).toString()
                            gender = user["gender"] as String
                            phone = user["phone"] as String
                            email = user["email"] as String
                        }
                    )
                }
            }
        }
    }

    @Composable
    fun SplashScreen(onTimeout: () -> Unit) {
        // Temporizador de 3 segundos
        LaunchedEffect(Unit) {
            delay(3000)
            onTimeout()
        }

        // Diseño del splash
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Splash Logo",
                    modifier = Modifier.size(150.dp)
                )
            }
        }
    }

    @Composable
    fun UserRow(user: Map<String, Any>, onDelete: () -> Unit, onEdit: () -> Unit) {
        Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
            Text(text = "Name: ${user["name"]}")
            Text(text = "Last Name: ${user["lastname"]}")
            Text(text = "Age: ${user["age"]}")
            Text(text = "Gender: ${user["gender"]}")
            Text(text = "Phone: ${user["phone"]}")
            Text(text = "Email: ${user["email"]}")
            Row {
                Button(onClick = onEdit) {
                    Text(text = "Edit")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDelete) {
                    Text(text = "Delete")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
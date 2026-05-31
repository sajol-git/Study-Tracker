package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.CardBg
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SyncDialog(onDismiss: () -> Unit, viewModel: MainViewModel) {
    val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    var currentUser by remember { mutableStateOf(auth?.currentUser) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CardBg,
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("ক্লাউড সিঙ্ক", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(16.dp))

                if (auth == null) {
                    Text("ফায়ারবেস কনফিগার করা হয়নি। Settings থেকে API Keys দিন।", color = Color.Red, fontSize = 14.sp)
                } else if (currentUser != null) {
                    Text("লগ ইন করা আছে: ${currentUser?.email}", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (loading) {
                        CircularProgressIndicator(color = AccentBlue)
                    } else {
                        Button(
                            onClick = {
                                loading = true
                                viewModel.syncToCloud(auth.uid!!) { success ->
                                    loading = false
                                    message = if (success) "সিঙ্ক সফল হয়েছে!" else "সিঙ্ক ব্যর্থ হয়েছে"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ক্লাউডে সেভ করুন", color = Color.White)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                loading = true
                                viewModel.syncFromCloud(auth.uid!!) { success ->
                                    loading = false
                                    message = if (success) "ক্লাউড থেকে ডাউনলোড সফল!" else "ডাউনলোড ব্যর্থ"
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("ক্লাউড থেকে রিড স্টোর করুন", color = Color.White)
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedButton(
                            onClick = {
                                auth.signOut()
                                currentUser = null
                                message = "লগ আউট হয়েছে"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("লগ আউট", color = Color.White)
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("ইমেইল", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("পাসওয়ার্ড", color = TextSecondary) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    if (loading) {
                        CircularProgressIndicator(color = AccentBlue)
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (email.isNotEmpty() && password.isNotEmpty()) {
                                        loading = true
                                        auth.signInWithEmailAndPassword(email, password)
                                            .addOnCompleteListener { task ->
                                                loading = false
                                                if (task.isSuccessful) {
                                                    currentUser = auth.currentUser
                                                    message = "লগ ইন সফল"
                                                } else {
                                                    message = "লগ ইন ব্যর্থ: ${task.exception?.message}"
                                                }
                                            }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("লগ ইন", color = Color.White)
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    if (email.isNotEmpty() && password.isNotEmpty()) {
                                        loading = true
                                        auth.createUserWithEmailAndPassword(email, password)
                                            .addOnCompleteListener { task ->
                                                loading = false
                                                if (task.isSuccessful) {
                                                    currentUser = auth.currentUser
                                                    message = "অ্যাকাউন্ট তৈরি সফল"
                                                } else {
                                                    message = "ব্যর্থ: ${task.exception?.message}"
                                                }
                                            }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("রেজিস্টার", color = AccentBlue)
                            }
                        }
                    }
                }
                
                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(message, color = AccentBlue, fontSize = 12.sp)
                }
            }
        }
    }
}

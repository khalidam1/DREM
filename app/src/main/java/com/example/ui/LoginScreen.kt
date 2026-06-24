package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.SupabaseNetwork
import com.example.model.supabase.SupabaseUser
import com.example.ui.theme.Crimson
import com.example.ui.theme.DarkGray
import com.example.ui.theme.LightGrayInfo
import com.example.ui.theme.PureBlack
import com.example.ui.theme.WhiteText
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onBack: () -> Unit) {
    var isLoginMode by remember { mutableStateOf(true) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "إغلاق",
                    tint = WhiteText
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = if (isLoginMode) "تسجيل الدخول" else "إنشاء حساب",
                color = WhiteText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = if (isLoginMode) "مرحباً بك مجدداً في دراما بوكس" else "انضم إلى دراما بوكس الآن",
                color = LightGrayInfo,
                fontSize = 14.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("الاسم المستعار", color = LightGrayInfo) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Crimson,
                    unfocusedBorderColor = DarkGray,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText,
                    cursorColor = Crimson
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("كلمة المرور", color = LightGrayInfo) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val description = if (passwordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description, tint = LightGrayInfo)
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Crimson,
                    unfocusedBorderColor = DarkGray,
                    focusedTextColor = WhiteText,
                    unfocusedTextColor = WhiteText,
                    cursorColor = Crimson
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (!isLoginMode) {
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("تأكيد كلمة المرور", color = LightGrayInfo) },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (confirmPasswordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور"
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = description, tint = LightGrayInfo)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Crimson,
                        unfocusedBorderColor = DarkGray,
                        focusedTextColor = WhiteText,
                        unfocusedTextColor = WhiteText,
                        cursorColor = Crimson
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    if (username.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "الرجاء إدخال البيانات", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (!isLoginMode && password != confirmPassword) {
                        Toast.makeText(context, "كلمات المرور غير متطابقة", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isLoading = true
                    coroutineScope.launch {
                        try {
                            val digest = java.security.MessageDigest.getInstance("SHA-256")
                            val hashBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
                            val hashedPassword = hashBytes.joinToString("") { "%02x".format(it) }

                            if (isLoginMode) {
                                val users = SupabaseNetwork.client.postgrest["users"]
                                    .select {
                                        filter {
                                            eq("username", username)
                                            eq("password_hash", hashedPassword)
                                        }
                                    }.decodeList<SupabaseUser>()
                                if (users.isNotEmpty()) {
                                    val user = users.first()
                                    val authManager = com.example.model.AuthManager(context)
                                    authManager.login(username, user.isAdmin)
                                    Toast.makeText(context, "تم تسجيل الدخول بنجاح", Toast.LENGTH_SHORT).show()
                                    onBack()
                                } else {
                                    Toast.makeText(context, "بيانات الدخول خاطئة", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val existing = SupabaseNetwork.client.postgrest["users"]
                                    .select { filter { eq("username", username) } }
                                    .decodeList<SupabaseUser>()
                                if (existing.isNotEmpty()) {
                                    Toast.makeText(context, "الاسم مستخدم مسبقاً", Toast.LENGTH_SHORT).show()
                                } else {
                                    SupabaseNetwork.client.postgrest["users"]
                                        .insert(SupabaseUser(username = username, passwordHash = hashedPassword))
                                    val authManager = com.example.model.AuthManager(context)
                                    authManager.login(username, false)
                                    Toast.makeText(context, "تم إنشاء الحساب بنجاح", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "حدث خطأ في الاتصال", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = WhiteText, modifier = Modifier.size(24.dp))
                } else {
                    Text(if (isLoginMode) "تسجيل الدخول" else "إنشاء حساب", color = WhiteText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            TextButton(
                onClick = { isLoginMode = !isLoginMode },
                enabled = !isLoading
            ) {
                Text(
                    text = if (isLoginMode) "ليس لديك حساب؟ إنشاء حساب جديد" else "لديك حساب بالفعل؟ تسجيل الدخول",
                    color = LightGrayInfo,
                    fontSize = 14.sp
                )
            }
        }
    }
}

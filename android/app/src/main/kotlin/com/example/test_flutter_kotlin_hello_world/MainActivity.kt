package com.example.test_flutter_kotlin_hello_world

import android.content.Intent
import android.os.Bundle
import io.flutter.embedding.android.FlutterActivity

class MainActivity : FlutterActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Flutter を起動せず Kotlin 画面に遷移
        val intent = Intent(this, HelloWorldActivity::class.java)
        startActivity(intent)
        finish()
    }
}

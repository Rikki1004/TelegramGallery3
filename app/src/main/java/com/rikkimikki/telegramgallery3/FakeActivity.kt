package com.rikkimikki.telegramgallery3

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class FakeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fake)
        setResult(Activity.RESULT_OK)
        finish()
    }
}
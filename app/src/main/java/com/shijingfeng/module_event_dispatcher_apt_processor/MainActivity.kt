package com.shijingfeng.module_event_dispatcher_apt_processor

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.shijingfeng.module_event_dispatcher.data.annotations.ModuleEventReceiver

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
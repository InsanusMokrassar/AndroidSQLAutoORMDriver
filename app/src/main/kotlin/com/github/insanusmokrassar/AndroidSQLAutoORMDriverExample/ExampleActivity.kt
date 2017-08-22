package com.github.insanusmokrassar.AndroidSQLAutoORMDriverExample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import com.github.insanusmokrassar.AndroidSQLAutoORMDriver.prepareConfig
import com.github.insanusmokrassar.AutoORM.core.createDatabasesPool

class ExampleActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        try {
        createDatabasesPool(prepareConfig(
                "config.json",
                this
        ))
        } catch (e: Exception) {

        }
    }
}
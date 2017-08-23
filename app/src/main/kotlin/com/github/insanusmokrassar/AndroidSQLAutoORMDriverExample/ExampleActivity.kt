package com.github.insanusmokrassar.AndroidSQLAutoORMDriverExample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.github.insanusmokrassar.AndroidSQLAutoORMDriver.prepareConfig
import com.github.insanusmokrassar.AutoORM.core.createDatabasesPool

class ExampleActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        try {
            val pool = createDatabasesPool(
                    prepareConfig(
                            "config.json",
                            this
                    )
            )
            val connection = pool["Example"]!!.getConnection()
            try {
                val operations = connection.getTable(Operations::class, Model::class)
                operations.insert(
                        object: Model {
                            override val id: Int? = null
                            override val name: String = "Example"
                        }
                )
                operations.getById(0)
            } catch (e: Exception) {
                Toast.makeText(this, "Error: $e", Toast.LENGTH_LONG).show()
                Log.e(javaClass.simpleName, "Error", e)
            } finally {
                connection.free()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: $e", Toast.LENGTH_LONG).show()
            Log.e(javaClass.simpleName, "Error", e)
        }
    }
}
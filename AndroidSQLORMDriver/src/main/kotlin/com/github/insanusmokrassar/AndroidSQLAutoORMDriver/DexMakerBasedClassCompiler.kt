package com.github.insanusmokrassar.AndroidSQLAutoORMDriver

import android.content.Context
import com.android.dx.DexMaker
import com.android.dx.TypeId
import com.github.insanusmokrassar.AutoORM.core.compilers.ClassCompiler
import com.github.insanusmokrassar.AutoORM.core.compilers.DefaultClassCompiler
import com.github.insanusmokrassar.AutoORM.core.isInterface
import java.io.File
import java.lang.reflect.Modifier
import kotlin.reflect.KClass

val classesDirName = "classes"
val classesNamesSuffix = ".java"
val dexMakerCacheFileName = "dexMakerCache"

class DexMakerBasedClassCompiler(private val context: Context): ClassCompiler {
    private var parentClassLoader = javaClass.classLoader!!
    override fun <T: Any> compile(className: String, classCode: String, source: KClass<in T>): KClass<T> {
        val cacheDir = context.cacheDir
        val classesDir = File(cacheDir.absolutePath, classesDirName)
        if (!classesDir.exists()) {
            classesDir.mkdirs()
        }
        val classFile = File(classesDir, "$className$classesNamesSuffix")
        if (classFile.exists()) {
            classFile.delete()
        }
        classFile.createNewFile()
        val os = classFile.outputStream()
        os.write(classCode.toByteArray())
        os.flush()
        os.close()
        val dexMaker = DexMaker()

        if (source.isInterface()) {
            dexMaker.declare(
                    TypeId.get<T>("L$className"),
                    classFile.absolutePath,
                    Modifier.PUBLIC,
                    null,
                    TypeId.get(source.java)
            )
        } else {
            dexMaker.declare(
                    TypeId.get<T>("L$className"),
                    classFile.absolutePath,
                    Modifier.PUBLIC,
                    TypeId.get(source.java)
            )
        }
        parentClassLoader = dexMaker.generateAndLoad(parentClassLoader, File(cacheDir, dexMakerCacheFileName))
        return parentClassLoader.loadClass(className).kotlin as KClass<T>
    }
}
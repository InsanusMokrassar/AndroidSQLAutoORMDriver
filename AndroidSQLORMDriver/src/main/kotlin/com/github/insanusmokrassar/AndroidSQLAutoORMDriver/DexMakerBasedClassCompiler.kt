package com.github.insanusmokrassar.AndroidSQLAutoORMDriver

import android.content.Context
import com.github.insanusmokrassar.AutoORM.core.compilers.ClassCompiler
import com.github.insanusmokrassar.AutoORM.core.compilers.DefaultClassCompiler
import com.github.insanusmokrassar.AutoORM.core.isInterface
import com.google.dexmaker.DexMaker
import com.google.dexmaker.TypeId
import dalvik.system.DexClassLoader
import java.io.*
import java.util.jar.Attributes
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import kotlin.reflect.KClass
import java.util.jar.Manifest


val classesDirName = "classes"
val classesNamesSuffix = ".java"
val cacheFileName = "classesCache.jar"

class DexMakerBasedClassCompiler(private val context: Context): ClassCompiler {
    override fun <T: Any> compile(className: String, classCode: String): KClass<T> {
        val name = className.split(".").last()
        val classPath = className.replace(".", "/").dropLast(name.length)
        val cacheDir = context.cacheDir
        val classesDir = File(cacheDir.absolutePath, "$classesDirName/")
        if (!classesDir.exists()) {
            classesDir.mkdirs()
        }
        val currentClassDir = File(classesDir.absolutePath, classPath)
        if (!currentClassDir.exists()) {
            currentClassDir.mkdirs()
        }
        val classFile = File(currentClassDir, "${name}$classesNamesSuffix")
        if (classFile.exists()) {
            classFile.delete()
        }
        classFile.createNewFile()
        val os = classFile.outputStream()
        os.write(classCode.toByteArray())
        os.flush()
        os.close()
        val manifest = Manifest()
        manifest.mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0")
        val targetJarFile = File(cacheDir.absolutePath, cacheFileName)
        if (targetJarFile.exists()) {
            targetJarFile.delete()
        }
        targetJarFile.createNewFile()
        val target = JarOutputStream(FileOutputStream(targetJarFile), manifest)
        target.add(classesDir)
        val loader = DexClassLoader(targetJarFile.absolutePath, cacheDir.absolutePath, null, javaClass.classLoader)
        return loader.loadClass(className).kotlin as KClass<T>
    }
}

@Throws(IOException::class)
private fun JarOutputStream.add(source: File) {
    var bufferedInputStream: BufferedInputStream? = null
    try {
        if (source.isDirectory) {
            var name = source.path.replace("\\", "/")
            if (!name.isEmpty()) {
                if (!name.endsWith("/"))
                    name += "/"
                val entry = JarEntry(name)
                entry.setTime(source.lastModified())
                putNextEntry(entry)
                closeEntry()
            }
            for (nestedFile in source.listFiles())
                add(nestedFile)
            return
        }

        val entry = JarEntry(source.path.replace("\\", "/"))
        entry.time = source.lastModified()
        putNextEntry(entry)
        bufferedInputStream = BufferedInputStream(FileInputStream(source))

        val buffer = ByteArray(1024)
        while (true) {
            val count = bufferedInputStream.read(buffer)
            if (count == -1)
                break
            write(buffer, 0, count)
        }
        closeEntry()
    } finally {
        if (bufferedInputStream != null)
            bufferedInputStream.close()
    }
}

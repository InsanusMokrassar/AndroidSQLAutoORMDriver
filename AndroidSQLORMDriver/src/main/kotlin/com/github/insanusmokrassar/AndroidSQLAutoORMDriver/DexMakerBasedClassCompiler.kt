package com.github.insanusmokrassar.AndroidSQLAutoORMDriver

import com.android.dx.DexMaker
import com.github.insanusmokrassar.AutoORM.core.compilers.ClassCompiler
import com.github.insanusmokrassar.AutoORM.core.compilers.DefaultClassCompiler
import kotlin.reflect.KClass

private val dexMaker = DexMaker()
class DexMakerBasedClassCompiler: ClassCompiler {
    override fun compile(className: String, classCode: String): KClass<*> {
        return DefaultClassCompiler().compile(className, classCode)
    }
}
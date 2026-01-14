package com.knightboost.lancet.plugin.asm

import com.didiglobal.booster.transform.Supervisor
import com.didiglobal.booster.transform.asm.asClassNode
import com.didiglobal.booster.transform.util.Collector
import org.objectweb.asm.tree.ClassNode

interface ClassSupervisor {
    fun collect2(name: String, classNode: ClassNode)
}

class CompositeClassSupervisor(private val collectors: Iterable<ClassSupervisor>) :
    Supervisor {

    override fun accept(name: String): Boolean = name.endsWith(".class")

    override fun collect(name: String, data: () -> ByteArray) {
        collect2(name, data().asClassNode())
    }

    fun collect2(name: String, classNode: ClassNode) {
        collectors.forEach { it.collect2(name, classNode) }
    }


}
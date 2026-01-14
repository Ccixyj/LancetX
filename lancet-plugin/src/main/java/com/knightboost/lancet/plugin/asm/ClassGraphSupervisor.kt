package com.knightboost.lancet.plugin.asm

import com.knightboost.lancet.internal.graph.ClassEntity
import com.knightboost.lancet.internal.graph.FieldEntity
import com.knightboost.lancet.internal.graph.GraphBuilder
import com.knightboost.lancet.internal.graph.MethodEntity
import org.objectweb.asm.tree.ClassNode

class ClassGraphSupervisor(private val graphBuilder: GraphBuilder) : ClassSupervisor {
    override fun collect2(name: String, classNode: ClassNode) {
        val methods = classNode.methods.map {
            MethodEntity(
                it.access,
                classNode.name,
                it.name,
                it.desc,
                it.exceptions.toTypedArray()
            )
        }
        val fields = classNode.fields.map {
            FieldEntity(it.access, classNode.name, it.name, it.desc, it.signature)
        }
        val ce = ClassEntity(
            classNode.access,
            classNode.name,
            classNode.superName,
            classNode.interfaces.toList(),
            fields,
            methods,
            true
        )
        graphBuilder.add(ce)
    }
}
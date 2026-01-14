package com.knightboost.lancet.plugin.asm

import com.knightboost.lancet.api.annotations.Weaver
import com.knightboost.lancet.internal.parser.WeaverClassesParser
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

class WeaverSupervisor(private val classesParser: WeaverClassesParser) : ClassSupervisor {
    override fun collect2(name: String, classNode: ClassNode) {
        val annotations: MutableList<AnnotationNode>? = classNode.visibleAnnotations
        if (annotations != null) {
            for (annotationNode in annotations) {
                if (annotationNode.desc == Type.getDescriptor(Weaver::class.java)) {
                    //this is weave class
                    classesParser.addWeaverClass(classNode)
                }
            }
        }
    }

}
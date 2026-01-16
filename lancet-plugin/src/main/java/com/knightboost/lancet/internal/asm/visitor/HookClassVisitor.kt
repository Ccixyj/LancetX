package com.knightboost.lancet.internal.asm.visitor

import com.knightboost.lancet.internal.entity.TransformInfo
import com.knightboost.lancet.internal.log.WeaverLog
import com.knightboost.lancet.internal.util.TypeUtils
import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class HookClassVisitor(
    private val transformInfo: TransformInfo,
    private val originalClassVisitor: ClassVisitor?
) : BaseWeaveClassVisitor() {
    private var isWeaveClass = false

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<String?>?
    ) {
        transformer.methodChain!!.init(name, originalClassVisitor)
        transformer.className = name
        transformer.superName = superName
        if (transformInfo.isWeaverClass(name)) {
            isWeaveClass = true
            skipWeaveVisitor()
            //这个WeaveClass ，不对weaveClass做任何字节码插桩操作
            //并且修改 类，只能直接继承 Object
        }

        super.visit(version, access, name, signature, superName, interfaces)
    }

    private var skipped = false

    private fun skipWeaveVisitor() {
        if (skipped) return
        this.cv = transformer.tail!!
            .getNext()

        skipped = true
    }


    override fun visitAnnotation(descriptor: String?, visible: Boolean): AnnotationVisitor? {
        return super.visitAnnotation(descriptor, visible)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        desc: String?,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor? {
        if (isWeaveClass) {
            return super.visitMethod(
                TypeUtils.resetAccessScope(access, Opcodes.ACC_PUBLIC),
                name, desc, signature, exceptions
            )
        } else {
            return super.visitMethod(access, name, desc, signature, exceptions)
        }
    }

    override fun visitEnd() {
        super.visitEnd()
        //头结点的end最后执行
        transformer.visitEnd()
        WeaverLog.d("Visitor:HookClass ${transformer.className} end")
    }
}

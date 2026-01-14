package com.knightboost.lancet.internal.asm.visitor

import com.knightboost.lancet.internal.entity.TransformInfo
import com.knightboost.lancet.internal.log.WeaverLog
import org.objectweb.asm.MethodVisitor

class TryCatchClassVisitor(transformInfo: TransformInfo) : BaseWeaveClassVisitor() {
    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<String?>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor? {
        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }

    override fun visitEnd() {
        super.visitEnd()
//        WeaverLog.i("Visitor:TryCatch ${transformer.className} end")
    }
}

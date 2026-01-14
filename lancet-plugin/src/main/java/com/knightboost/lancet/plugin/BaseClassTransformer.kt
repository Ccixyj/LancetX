package com.knightboost.lancet.plugin

import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.didiglobal.booster.transform.asm.asClassNode
import com.knightboost.lancet.internal.log.WeaverLog
import com.knightboost.lancet.internal.verify.AsmVerifier
import com.knightboost.lancet.internal.verify.AsmVerifyClassVisitor
import com.knightboost.lancet.internal.visitor.ClassVisitorChain
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode

open class BaseClassTransformer : ClassTransformer {

    override fun onPreTransform(context: TransformContext) {
        super.onPreTransform(context)
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        val node =  try {
            val cw = ClassWriter(0)

            // 1. 预校验
            val chain = ClassVisitorChain()

            if (needVerify()) {
                chain.connect(AsmVerifyClassVisitor())
            }

            // 2. Handler 处理 ClassVisitor 链路
            transform2(context, klass, chain)
            chain.classWriter(cw)

            // 3. 创建 ClassReader 并使用 EXPAND_FRAMES 解析
            val classBytes = getClassBytes(klass) // 需要将 ClassNode 转回字节数组
            // 关键修改：使用 ClassReader 和 EXPAND_FRAMES
            val cr = ClassReader(classBytes)
            val raw = chain.accept(cr, ClassReader.EXPAND_FRAMES)
            // 4. 获取转换后的 ClassNode
            val fnode = raw.asClassNode()
            if (needVerify()) {
                AsmVerifier.verify(fnode)
            }
            // 6. 返回修改后的 ClassNode（必须返回，Booster 以此作为最终结果）
            // 6. 核心步骤：在 Chain 处理末尾，生成内部类（关键调用）
            transformEnd(fnode)
            fnode
        } catch (e: Exception) {
            WeaverLog.e("transform error", e)
            klass
        }
        return node
    }

    private fun getClassBytes(klass: ClassNode): ByteArray {
        // 将 ClassNode 转回字节数组
        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS)
        klass.accept(cw)
        return cw.toByteArray()
    }

    protected open fun transformEnd(node: ClassNode) {

    }

    protected open fun transform2(
        context: TransformContext,
        klass: ClassNode,
        chain: ClassVisitorChain
    ) {

    }

    protected open fun needVerify() = false

    override fun onPostTransform(context: TransformContext) {
        super.onPostTransform(context)
    }

    public open fun onBeforeTransform() {
    }

    open fun callOutputComplete(
        context: TransformContext,
        jos: JarArchiveOutputStream,
    ) {
    }

}
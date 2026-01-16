package com.knightboost.lancet.internal.asm.visitor

import com.didiglobal.booster.transform.TransformContext
import com.knightboost.lancet.internal.graph.Graph
import com.knightboost.lancet.internal.log.WeaverLog
import com.knightboost.lancet.internal.visitor.BaseClassVisitor
import com.knightboost.lancet.internal.visitor.ClassVisitorChain
import com.knightboost.lancet.plugin.LancetContext
import org.apache.commons.compress.archivers.jar.JarArchiveEntry
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.util.concurrent.ConcurrentHashMap

class WeaveTransformer(
    private val context: LancetContext,
    private val transformContext: TransformContext
) {
    // simple name of innerClass
    val mInnerClassWriter: MutableMap<String, ClassWriter> = ConcurrentHashMap()

    /**
     * 当前类的类名
     */
    @JvmField
    var className: String? = null

    @JvmField
    var superName: String? = null


    private var chain: ClassVisitorChain? = null

    @JvmField
    var methodChain: MethodChain? = null

    fun initVisitorChain(visitorChain: ClassVisitorChain, graph: Graph?) {
        this.chain = visitorChain
        methodChain = MethodChain(graph)

        //        OriginalClassVisitor originalClassVisitor = new OriginalClassVisitor();
        val classVisitor = BaseWeaveClassVisitor()
        val transformInfo = context.getTransformInfo()
        connect(HookClassVisitor(transformInfo, classVisitor))
        //
        connect(ChangeClassExtendVisitor(transformInfo))
        //会生成新函数的Visitor
        connect(InsertClassVisitor(transformInfo.insertInfo))
        connect(ProxyClassVisitor(transformInfo.proxyInfo))
        connect(classVisitor)
        //不会生成新函数的Visitor
        connect(ReplaceClassVisitor(transformInfo))
        connect(ReplaceNewClassVisitor(transformInfo))

        connect(TryCatchClassVisitor(transformInfo))

        //        this.originalClassVisitor = originalClassVisitor;
    }


    val tail: BaseClassVisitor?
        get() = chain!!.getTail()


    private fun connect(visitor: BaseWeaveClassVisitor) {
        chain!!.connect(visitor)
        visitor.transformer = this
    }

    fun getInnerClassVisitor(classSimpleName: String): ClassVisitor {
        return mInnerClassWriter.getOrPut(classSimpleName) {
            val w = ClassWriter(ClassWriter.COMPUTE_MAXS)
            initForWriter(w, classSimpleName)
            w
        }
    }


    fun getCanonicalName(simpleName: String?): String {
        return className + "$" + simpleName
    }

    private fun initForWriter(visitor: ClassVisitor, classSimpleName: String?) {
        visitor.visit(
            Opcodes.V1_7,
            Opcodes.ACC_SUPER,
            getCanonicalName(classSimpleName),
            null,
            "java/lang/Object",
            null
        )
        val mv = visitor.visitMethod(Opcodes.ACC_PRIVATE, "<init>", "()V", null, null)
        mv.visitCode()
        mv.visitVarInsn(Opcodes.ALOAD, 0)
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
        mv.visitInsn(Opcodes.RETURN)
        mv.visitMaxs(1, 1)
        mv.visitEnd()
    }

    fun visitEnd(): Boolean {
        for (className in mInnerClassWriter.keys) {
            this.tail!!.visitInnerClass(
                getCanonicalName(className),
                this.className,
                className,
                Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC
            )
            return true
        }
        return false
    }

    fun needWriteInnerClass(): Boolean {
        return !mInnerClassWriter.isEmpty()
    }

    fun generateInnerClasses(creator: JarArchiveOutputStream) {
//        for (String className : mInnerClassWriter.keySet()) {
//            getTail().visitInnerClass(getCanonicalName(className), this.className, className, Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC);
//        }
        WeaverLog.i("transForm:innerClass $mInnerClassWriter")
        if (!mInnerClassWriter.isEmpty()) {
            mInnerClassWriter.forEach { (s, v) ->
                val name = "$className$$s.class"
                WeaverLog.i("transForm:innerClass $name")
                creator.addBinaryFile(name, v.toByteArray())
            }
        }
    }

    private fun JarArchiveOutputStream.addBinaryFile(
        fileName: String,
        data: ByteArray
    ) {
        try {
            val entry = JarArchiveEntry(fileName)
            entry.method = JarArchiveEntry.DEFLATED
            entry.size = data.size.toLong()
            entry.time = System.currentTimeMillis()

            this.putArchiveEntry(entry)
            this.write(data)
            this.closeArchiveEntry()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        const val AID_INNER_CLASS_NAME: String = "_boostWeave"
    }
}

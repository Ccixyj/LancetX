package com.knightboost.lancet.internal.util

import com.didiglobal.booster.transform.asm.asClassNode
import com.knightboost.lancet.internal.verify.AsmVerifier
import com.knightboost.lancet.internal.visitor.BaseClassVisitor
import com.knightboost.lancet.internal.visitor.ClassVisitorChain
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InnerClassNode
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream


// 核心常量（ASM 字节码格式，类名规范：外部类$内部类）
private const val OUTER_CLASS_NAME = "Hello" // 外部类名（Hello）
private const val MEMBER_INNER_CLASS_NAME = "Hello\$Inner" // 成员内部类（Hello.Inner）
private const val STATIC_INNER_CLASS_NAME = "Hello\$StaticInner" // 静态内部类（Hello.StaticInner）
private const val ANONYMOUS_INNER_CLASS_NAME = "Hello$1" // 匿名内部类（Hello$1）
private const val MEMBER_INNER_SIMPLE_NAME = "Inner" // 成员内部类简单名
private const val STATIC_INNER_SIMPLE_NAME = "StaticInner" // 静态内部类简单名

class BitsetTest {
    @Test
    fun test() {
        val bitset = Bitset()
        bitset.setInitializer(object : Bitset.Initializer {
            override fun initialize(bitset: Bitset) {
                val len: Int = ACCESS.length
                bitset.tryAdd("access$001", len)
                bitset.tryAdd("access$011", len)
                bitset.tryAdd("access$1111", len)
                bitset.tryAdd("access$11111", len)
            }
        })
        for (i in 0..11114) {
            val index = bitset.consume()
        }
    }


    @Test
    fun testInnerClass() {
        println("readHello")
        val h = getClassBytecodeByClassLoader(Hello::class.java) ?: return
        println(h.available())

//        // 步骤 2：创建各类内部类的 ClassNode（独立创建，字节码层面独立）
//        val memberInnerClassNode = createMemberInnerClassNode()
//        val staticInnerClassNode = createStaticInnerClassNode()
//        val anonymousInnerClassNode = createAnonymousInnerClassNode()

//        // 步骤 3：核心操作 - 向外部类 ClassNode 关联所有内部类（填充 innerClasses 集合）
//        outerClassNode.associateInnerClass(
//            innerClassName = MEMBER_INNER_CLASS_NAME,
//            innerSimpleClassName = MEMBER_INNER_SIMPLE_NAME,
//            innerAccess = Opcodes.ACC_PUBLIC
//        )
//        outerClassNode.associateInnerClass(
//            innerClassName = STATIC_INNER_CLASS_NAME,
//            innerSimpleClassName = STATIC_INNER_SIMPLE_NAME,
//            innerAccess = Opcodes.ACC_PUBLIC or Opcodes.ACC_STATIC
//        )
//        outerClassNode.associateInnerClass(
//            innerClassName = ANONYMOUS_INNER_CLASS_NAME,
//            innerSimpleClassName = null, // 匿名内部类无简单名，传 null
//            innerAccess = Opcodes.ACC_SUPER
//        )

        // 步骤 4：验证结果 - 打印外部类关联的内部类信息
        val a = ByteArrayOutputStream()
        h.copyTo(a)
        val at = AT().apply {
            cs.connect(BaseClassVisitor())
            cs.connect(ICW(Hello::class.java.name))
        }
        a.toByteArray().rewriteToClassFile("build/hello2.class", listOf(at))
    }

    /**
     * 创建外部类 Hello 的 ClassNode
     */
//    private fun createOuterClassNode(): ClassNode {
//        return ClassNode(Opcodes.ASM9).apply {
//            // 配置外部类核心信息
//            name = OUTER_CLASS_NAME
//            access = Opcodes.ACC_PUBLIC or Opcodes.ACC_SUPER
//            superName = "java/lang/Object"
//            // 初始化 innerClasses 集合（Kotlin 空安全，避免后续操作空指针）
//            innerClasses = arrayListOf()
//        }
//    }


    /**
     * 扩展函数：给外部类 ClassNode 添加内部类关联（核心逻辑，贴合 Kotlin 语法）
     * @param innerClassName ASM 格式的内部类全限定名（如 Hello$Inner）
     * @param innerSimpleClassName 内部类简单名（如 Inner，匿名内部类传 null）
     * @param innerAccess 内部类访问标志（如 ACC_PUBLIC | ACC_INNER_CLASS）
     */
    private fun ClassNode.associateInnerClass(
        innerClassName: String,
        innerSimpleClassName: String?,
        innerAccess: Int
    ) {
        // 1. 创建 InnerClassNode，封装内部类关联元数据
        val innerClassNode = InnerClassNode(
            innerClassName,// 内部类全限定名（ASM 格式）
            this@associateInnerClass.name, // 外部类全限定名（获取扩展函数的接收者：外部类 ClassNode）
            innerSimpleClassName, // 内部类简单名（匿名内部类为 null）
            innerAccess
        )// 内部类访问标志

        // 2. 核心操作：将 InnerClassNode 添加到外部类的 innerClasses 集合
        // Kotlin 空安全：确保 innerClasses 不为 null（已在 createOuterClassNode 初始化）
        this.innerClasses?.add(innerClassNode)
    }

    /**
     * 验证：打印外部类关联的所有内部类信息
     */
    private fun printOuterClassInnerClasses(outerClassNode: ClassNode) {
        println("===== 外部类 ${outerClassNode.name} 关联的内部类 =====")
        val innerClasses = outerClassNode.innerClasses ?: run {
            println("无关联内部类")
            return
        }

        if (innerClasses.isEmpty()) {
            println("无关联内部类")
            return
        }

        innerClasses.forEach { innerClassNode ->
            println("\n内部类全限定名：${innerClassNode.name}")
            println("外部类全限定名：${innerClassNode.outerName}")
            println("内部类简单名：${innerClassNode.innerName ?: "无（匿名内部类）"}")
            println("内部类访问标志：${parseAccessFlags(innerClassNode.access)}")
        }
    }

    /**
     * 辅助方法：解析访问标志为可读字符串
     */
    private fun parseAccessFlags(access: Int): String {
        val flagBuilder = StringBuilder()
        if ((access and Opcodes.ACC_PUBLIC) != 0) flagBuilder.append("public ")
        if ((access and Opcodes.ACC_PRIVATE) != 0) flagBuilder.append("private ")
        if ((access and Opcodes.ACC_PROTECTED) != 0) flagBuilder.append("protected ")
        if ((access and Opcodes.ACC_STATIC) != 0) flagBuilder.append("static ")
//        if ((access and Opcodes.ACC_INNER_CLASS) != 0) flagBuilder.append("innerClass")
        return flagBuilder.toString().trim()
    }


    private fun getClassBytecodeByClassLoader(clazz: Class<*>): InputStream? {
        val className = clazz.getName().replace('.', '/')
        val classResourcePath = className + ".class" // 注意：此处无前置斜杠（与 Class.getResourceAsStream 区别）

        // 方式 1：使用当前类的类加载器
        val classLoader = clazz.getClassLoader()


        // 方式 2：使用线程上下文类加载器（更灵活，支持复杂类加载场景）
        // ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader.getResourceAsStream(classResourcePath)
    }

    /**
     * 核心扩展函数：将 ClassNode 重新写入生成 .class 字节码文件
     * @param filePath 输出文件路径（如 ./generated_classes/Hello.class）
     */
    private fun ByteArray.rewriteToClassFile(filePath: String, s: List<Transformer>) {
        // 步骤 1：创建 ClassWriter，指定自动计算帧和最大变量
        val cw = ClassWriter(ClassWriter.COMPUTE_MAXS).also { writer ->
            s.fold(ClassNode().also { klass ->
                ClassReader(this).accept(klass, 0)
            }) { klass, acc ->
                println("$klass , $acc")
                acc(klass)
            }.accept(writer)
        }

        // 步骤 3：获取生成的字节码数组
        val classBytes = cw.toByteArray()

        // 步骤 4：将字节码数组写入 .class 文件
        File(filePath).apply {
            // 确保父目录存在（内部类可能存在多级目录）
            parentFile?.mkdirs()
            writeBytes(classBytes)
        }
    }


    companion object {
        private const val ACCESS = "access$"
    }
}

typealias Transformer = (ClassNode) -> ClassNode

class AT : Transformer {

    val cs = ClassVisitorChain()

    override fun invoke(p1: ClassNode): ClassNode {
        cs.classWriter(ClassWriter(0))
        (cs.tail as ICW).generate()
        val d = cs.accept(p1)
        val n= d.asClassNode()
        AsmVerifier.verify(n)
        return n
    }


}

class ICW(val classname: String) : BaseClassVisitor() {

    val w = hashMapOf<String, ClassWriter>()
    init {
        val cw = ClassWriter(0)
        initForWriter(cw, "__boost")
        w[classname] = cw
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


    fun generate() {
        for ((cn , cw) in w) {
            this.visitInnerClass(
                getCanonicalName(cn),
                classname,
                cn,
                Opcodes.ACC_PRIVATE or Opcodes.ACC_STATIC
            )
        }
    }
    fun getCanonicalName(simpleName: String?): String {
        return classname + "$" + simpleName
    }
}
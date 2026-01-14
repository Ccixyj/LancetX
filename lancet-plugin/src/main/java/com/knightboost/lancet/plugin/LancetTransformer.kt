package com.knightboost.lancet.plugin

import com.didiglobal.booster.transform.TransformContext
import com.knightboost.lancet.internal.asm.visitor.WeaveTransformer
import com.knightboost.lancet.internal.graph.GraphBuilder
import com.knightboost.lancet.internal.log.WeaverLog
import com.knightboost.lancet.internal.parser.WeaverClassesParser
import com.knightboost.lancet.internal.visitor.ClassVisitorChain
import com.knightboost.lancet.plugin.asm.ClassGraphSupervisor
import com.knightboost.lancet.plugin.asm.CompositeClassSupervisor
import com.knightboost.lancet.plugin.asm.WeaverSupervisor
import org.apache.commons.compress.archivers.jar.JarArchiveOutputStream
import org.objectweb.asm.tree.ClassNode

class LancetTransformer : BaseClassTransformer() {


    private lateinit var lc: LancetContext
    private lateinit var graphBuilder: GraphBuilder

    private lateinit var parser: WeaverClassesParser
    private val history: MutableMap<String, WeaveTransformer> = mutableMapOf()

    override fun onPreTransform(context: TransformContext) {
        super.onPreTransform(context)
        lc = context.getProperty("lancetx.context", null) ?: error("")
        parser = WeaverClassesParser(lc)
        graphBuilder = GraphBuilder()
        context.registerCollector(
            CompositeClassSupervisor(
                listOf(
                    ClassGraphSupervisor(graphBuilder),
                    WeaverSupervisor(parser),
                )
            )
        )
    }

    override fun onBeforeTransform() {
        super.onBeforeTransform()
        parser.parse(graphBuilder.build())
        WeaverLog.i("weaver graph build success")
        WeaverLog.i("weaver graph:${parser.graph}")
        WeaverLog.i("weaver transform :${lc.transformInfo}")
    }

    override fun transform2(context: TransformContext, klass: ClassNode, chain: ClassVisitorChain) {
        super.transform2(context, klass, chain)
        val w = WeaveTransformer(lc, context)
        history[klass.name] = w
        w.initVisitorChain(chain, parser.graph)
//        weaveTransformer.generateInnerClasses()
    }

    override fun transformEnd(node: ClassNode) {
        super.transformEnd(node)
        history[node.name]?.let { b ->
            if (!b.needWriteInnerClass()) {
                history.remove(node.name)
            }
        }
    }

    override fun needVerify(): Boolean {
        return lc.extension.debug
    }

    override fun onPostTransform(context: TransformContext) {
        super.onPostTransform(context)
    }

    override fun callOutputComplete(
        context: TransformContext,
        jos: JarArchiveOutputStream
    ) {
        super.callOutputComplete(context, jos)
        WeaverLog.i("callOutputComplete writeInnerClass ${history.size}")
        history.forEach {
            it.value.generateInnerClasses(jos)

        }
    }
}

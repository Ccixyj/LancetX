package com.knightboost.lancet.plugin

import com.knightboost.lancet.internal.log.WeaverLog
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.model.ObjectFactory

open class LancetExtension(factory: ObjectFactory) {

    var enable = false
    var debug = false
    val weaveGroup: NamedDomainObjectContainer<WeaveGroup?> =
        factory.domainObjectContainer(WeaveGroup::class.java)


    fun weaveGroup(action: Action<in NamedDomainObjectContainer<WeaveGroup?>?>) {
        action.execute(weaveGroup)
    }

    fun findWeaveGroup(group: String): WeaveGroup? {
        return weaveGroup.findByName(group)
    }

    fun isWeaveGroupEnable(group: String?): Boolean {
        if (group == null) { // 没有使用@WeaveGroup 注解，则功能开关 依赖于 全局的插件开关
            return enable
        }

        val weaveGroup = findWeaveGroup(group)
        if (weaveGroup == null) {
            // 如果 配置了@Group 注解，但是 没有配置对应的 extension,则默认将该功能关闭
            // todo  ，允许在 注解上配置默认的功能开关
            WeaverLog.i("未发现 weaver group " + group + " 的gradle 配置 ,因此功能默认关闭")
            return false
        }
        return weaveGroup.isEnable
    }

}
package com.knightboost.lancet.internal.graph.cache

import com.knightboost.lancet.internal.graph.ClassEntity
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by yangzhiqian on 2020-03-04<br/>
 */
internal class RamClassesCacheStorage : ClassCacheStorage {
    private val caches = ConcurrentHashMap<String, List<ClassEntity>>()
//            if (BooleanProperty.ENABLE_GRADLE_DAEMON_IGNORE_CLASSLOADER_SINGLETON.value()) {
//                GradleDaemonIgnoreClassLoaderSingletonManager.computeIfAbsent<ConcurrentHashMap<String, List<ClassEntity>>>(this, this.javaClass.name) {
//                    ConcurrentHashMap()
//                }
//            } else {
//                ConcurrentHashMap()
//            }

    fun clear() {
        caches.clear()
    }

    fun clearCache(f: File) {
        caches.remove(f.absolutePath)
    }

    override fun loadCache(t: File?): List<ClassEntity>? {
        if (t == null) {
            return null
        }
        return caches.remove(t.absolutePath)?.apply {
            println("Load ByteX Classes Cache(${size}) Success[RAM]:" + t.absolutePath)
        }
    }

    override fun saveCache(t: File?, d: List<ClassEntity>): Boolean {
        if (t == null) {
            return false
        }
        println("Save ByteX Classes Cache(${d.size}) Success[RAM]:" + t.absolutePath)
        caches[t.absolutePath] = d
        return true
    }
}
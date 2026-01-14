package com.knightboost.lancet.internal.graph.cache;

import com.knightboost.lancet.internal.graph.ClassEntity
import com.knightboost.lancet.internal.graph.Node


/**
 * Created by yangzhiqian on 2020-7-13<br/>
 */
internal interface CacheStorage<T, D> {

    fun loadCache(t: T?): D?

    fun saveCache(t: T?, d: D): Boolean
}

package com.knightboost.lancet.internal.graph.cache

import com.knightboost.lancet.internal.graph.ClassEntity
import java.io.File

/**
 * Created by yangzhiqian on 2020/7/13<br/>
 */
internal interface ClassCacheStorage : CacheStorage<File, List<ClassEntity>>
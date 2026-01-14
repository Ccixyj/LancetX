package com.knightboost.lancet.internal.graph.cache

import com.knightboost.lancet.internal.graph.Node
import java.io.File

/**
 * Created by yangzhiqian on 2020/9/3<br/>
 */
internal interface NodeCacheStorage :CacheStorage<File,Map<String,Node>>
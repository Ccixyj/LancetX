package com.knightboost.lancet.internal.util;

import java.util.Iterator;
import java.util.Map;

public class CollectionUtils {
    private CollectionUtils() { }


    public static void reverse(Map source, Map target) {
        for (Iterator it = source.keySet().iterator(); it.hasNext();) {
            Object key = it.next();
            target.put(source.get(key), key);
        }
    }

}

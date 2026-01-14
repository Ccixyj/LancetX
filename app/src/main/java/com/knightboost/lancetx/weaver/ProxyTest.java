package com.knightboost.lancetx.weaver;

import com.knightboost.lancet.api.Origin;
import com.knightboost.lancet.api.Scope;
import com.knightboost.lancet.api.annotations.Group;
import com.knightboost.lancet.api.annotations.NameRegex;
import com.knightboost.lancet.api.annotations.Proxy;
import com.knightboost.lancet.api.annotations.TargetClass;
import com.knightboost.lancet.api.annotations.TargetMethod;
import com.knightboost.lancet.api.annotations.Weaver;

@Weaver
@Group("proxyTest")
public class ProxyTest {

    @Proxy()
    @TargetClass(value = "android.util.Log", scope = Scope.SELF)
    @TargetMethod(methodName = "i")
    @NameRegex(".*knightboost.*")
    public static int replaceLogI(String tag, String msg) {
        msg = msg + "lancet";
        return (int) Origin.call();
    }

    @Proxy()
    @TargetClass(value = "android.util.Log", scope = Scope.SELF)
    @TargetMethod(methodName = "w")
    @NameRegex(".*knightboost.*")
    public static int replaceLogW(String tag, String msg) {
        msg = msg + "hook by [bytex]";
        return (int) Origin.call();
    }

}

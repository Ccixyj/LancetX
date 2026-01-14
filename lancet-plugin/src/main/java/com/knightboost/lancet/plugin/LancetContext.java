package com.knightboost.lancet.plugin;

import com.android.build.gradle.AppExtension;
import com.knightboost.lancet.internal.entity.TransformInfo;

import org.gradle.api.Project;

import java.util.HashMap;
import java.util.Map;

public class LancetContext {


    final Project project;
    final AppExtension android;

    final LancetExtension extension;

    // 类名 -> 所属组别  的映射关系
    private final Map<String, String> weaverClassOfGroupMap = new HashMap<>();

    private final TransformInfo transformInfo = new TransformInfo();


    public LancetContext(Project project,
                         AppExtension android,
                         LancetExtension extension) {
        this.project = project;
        this.android = android;
        this.extension = extension;

    }


    public void registerGroupWeaverClass(String weaverClass, String group) {
        weaverClassOfGroupMap.put(weaverClass, group);
    }

    /**
     * 判断 weaver 功能是否开启
     * @param weaverClass
     * @return
     */
    public boolean isWeaveEnable(String weaverClass) {
        String group = weaverClassOfGroupMap.get(weaverClass);
        return extension.isWeaveGroupEnable(group);
    }

    public TransformInfo getTransformInfo() {
        return transformInfo;
    }

}

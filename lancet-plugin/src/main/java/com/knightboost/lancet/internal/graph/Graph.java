package com.knightboost.lancet.internal.graph;


import com.knightboost.lancet.internal.graph.ClassEntity;
import com.knightboost.lancet.internal.graph.ClassNode;
import com.knightboost.lancet.internal.graph.FieldEntity;
import com.knightboost.lancet.internal.graph.InterfaceNode;
import com.knightboost.lancet.internal.graph.MethodEntity;
import com.knightboost.lancet.internal.graph.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * Class dependency graph.
 */
public class Graph {


    protected Map<String, com.knightboost.lancet.internal.graph.Node> nodeMap;

    Graph(Map<String, com.knightboost.lancet.internal.graph.Node> nodesMap) {
        this.nodeMap = nodesMap;
    }

    public boolean inherit(String child, String parent) {
        com.knightboost.lancet.internal.graph.Node childNode = nodeMap.get(child);
        com.knightboost.lancet.internal.graph.Node parentNode = nodeMap.get(parent);
        return childNode != null && childNode.inheritFrom(parentNode);
    }


    public com.knightboost.lancet.internal.graph.Node get(String className) {
        return nodeMap.get(className);
    }


    /**
     * Returns the common super type of the two given types.
     *
     * @param type1 the internal name of a class.
     * @param type2 the internal name of another class.
     * @return the internal name of the common super class of the two given
     * classes.
     */
    public String getCommonSuperClass(String type1, String type2) {
        if (type1.equals(type2)) {
            return type1;
        }
        com.knightboost.lancet.internal.graph.Node node = nodeMap.get(type1);
        if (node == null) {
            throw new TypeNotPresentException(type1, null);
        }
        com.knightboost.lancet.internal.graph.Node node2 = nodeMap.get(type2);
        if (node2 == null) {
            throw new TypeNotPresentException(type2, null);
        }
        if (node.isAssignableFrom(node2)) {
            return type1;
        }

        if (node2.isAssignableFrom(node)) {
            return type2;
        }
        if (node instanceof com.knightboost.lancet.internal.graph.InterfaceNode || node2 instanceof com.knightboost.lancet.internal.graph.InterfaceNode) {
            return "java/lang/Object";
        } else {
            do {
                node = node.parent;
            } while (!node.isAssignableFrom(node2));
            return node.entity.name.replace('.', '/');
        }
    }

    public List<ClassNode> implementsOf(String interfaceName) {
        com.knightboost.lancet.internal.graph.Node node = nodeMap.get(interfaceName);
        if (node == null) {
            return Collections.emptyList();
        } else if (!(node instanceof com.knightboost.lancet.internal.graph.InterfaceNode)) {
            throw new IllegalArgumentException(interfaceName + " is not a interface");
        }
        com.knightboost.lancet.internal.graph.InterfaceNode realNode = ( com.knightboost.lancet.internal.graph.InterfaceNode) node;
        return realNode.implementedClasses;
    }

    public boolean implementOf(String child, String interfaceName) {
        com.knightboost.lancet.internal.graph.Node node = nodeMap.get(interfaceName);
        if (node == null) {
            return false;
        } else if (!(node instanceof com.knightboost.lancet.internal.graph.InterfaceNode)) {
            return false;
        }
        com.knightboost.lancet.internal.graph.InterfaceNode interfaceNode = ( com.knightboost.lancet.internal.graph.InterfaceNode) node;
        final boolean[] found = {false};
        traverseChildren(interfaceNode, n -> {
            if (child.equals(n.entity.name)) {
                found[0] = true;
                return true;
            }
            return false;
        });
        return found[0];
    }

    public List<ClassNode> childrenOf(String className) {
        com.knightboost.lancet.internal.graph.Node node = nodeMap.get(className);
        if (node == null) {
            return Collections.emptyList();
        } else if (!(node instanceof ClassNode)) {
            throw new IllegalArgumentException(className + " is not a interface");
        }
        ClassNode classNode = (ClassNode) node;
        List<ClassNode> children = new ArrayList<>();
        traverseAllChild(classNode, children::add);
        return children;
    }

    public boolean instanceofClass(String className, String targetClassName) throws ClassNotFoundException {
        com.knightboost.lancet.internal.graph.Node child = get(className);
        if (child == null) {
            throw new ClassNotFoundException(String.format("class %s not found!", className));
        }
        com.knightboost.lancet.internal.graph.Node parent = get(targetClassName);
        if (parent == null) {
            throw new ClassNotFoundException(String.format("class %s not found!", targetClassName));
        }
        return child.inheritFrom(parent);
    }

    // TODO: 2019/3/13 should be optimized
    public void traverseAllChild(ClassNode classNode, Consumer<ClassNode> visitor) {
        // bfs
        Queue<ClassNode> handleQ = new LinkedList<>();
        classNode.children.forEach(handleQ::offer);
        while (!handleQ.isEmpty()) {
            ClassNode n = handleQ.poll();
            visitor.accept(n);
            n.children.forEach(handleQ::offer);
        }
    }

    public void traverseChildren(ClassNode classNode, Function<ClassNode, Boolean> visitor) {
        // bfs
        Queue<ClassNode> handleQ = new LinkedList<>();
        classNode.children.forEach(handleQ::offer);
        while (!handleQ.isEmpty()) {
            ClassNode n = handleQ.poll();
            if (visitor != null && visitor.apply(n)) {
                break;
            }
            n.children.forEach(handleQ::offer);
        }
    }

    public void traverseChildren( com.knightboost.lancet.internal.graph.InterfaceNode interfaceNode, Function< com.knightboost.lancet.internal.graph.Node, Boolean> visitor) {
        // bfs
        Queue< com.knightboost.lancet.internal.graph.Node> handleQ = new LinkedList<>();
        interfaceNode.children.forEach(handleQ::offer);
        interfaceNode.implementedClasses.forEach(handleQ::offer);
        while (!handleQ.isEmpty()) {
            com.knightboost.lancet.internal.graph.Node n = handleQ.poll();
            if (visitor != null && visitor.apply(n)) {
                break;
            }
            if (n instanceof com.knightboost.lancet.internal.graph.InterfaceNode) {
                (( com.knightboost.lancet.internal.graph.InterfaceNode) n).children.forEach(handleQ::offer);
                (( com.knightboost.lancet.internal.graph.InterfaceNode) n).implementedClasses.forEach(handleQ::offer);
            } else if (n instanceof ClassNode) {
                ((ClassNode) n).children.forEach(handleQ::offer);
            }
        }
    }

    public void backtrackToParent(ClassNode classNode, Function<ClassNode, Boolean> visitor) {
        while (classNode != null) {
            if (visitor != null && visitor.apply(classNode)) {
                break;
            }
            classNode = classNode.parent;
        }
    }

    /**
     * judge the method whether it overrides from superior or interfaces.
     */
    public boolean overrideFromSuper(String className, String methodName, String desc) {
        com.knightboost.lancet.internal.graph.Node classNode = get(className);
        if (classNode == null) throw new RuntimeException("No such method : " + methodName);
        if (isMethodFromInterface(methodName, desc, classNode)) return true;
        while (classNode.parent != null) {
            ClassEntity parent = classNode.parent.entity;
            if (parent.methods.stream().anyMatch(m -> m.name().equals(methodName) && m.desc().equals(desc))) {
                return true;
            }
            if (isMethodFromInterface(methodName, desc, classNode)) return true;
            classNode = classNode.parent;
        }
        return false;
    }

    private boolean isMethodFromInterface(String methodName, String desc, com.knightboost.lancet.internal.graph.Node classNode) {
        Queue< com.knightboost.lancet.internal.graph.InterfaceNode> handleQ = new LinkedList<>();
        classNode.interfaces.forEach(handleQ::offer);
        while (!handleQ.isEmpty()) {
            InterfaceNode n = handleQ.poll();
            if (n.entity.methods.stream().anyMatch(m -> m.name().equals(methodName) && m.desc().equals(desc))) {
                return true;
            }
            n.interfaces.forEach(handleQ::offer);
        }
        return false;
    }

    /**
     * Judge the method whether it is overrided by children.
     */
    public boolean overridedBySubclass(String className, String methodName, String desc) {
        ClassNode classNode = (ClassNode) get(className);
        if (classNode == null) throw new RuntimeException("No such method : " + methodName);
        AtomicBoolean found = new AtomicBoolean(false);
        traverseChildren(classNode, child -> {
            ClassEntity childEntity = child.entity;
            for (MethodEntity m : childEntity.methods) {
                if (m.name().equals(methodName) && m.desc().equals(desc)) {
                    found.set(true);
                    return true;
                }
            }
            if (isMethodFromInterface(methodName, desc, child)) {
                found.set(true);
                return true;
            }
            return false;
        });
        return found.get();
    }

    public FieldEntity confirmOriginField(String owner, String name, String desc) {
        com.knightboost.lancet.internal.graph.Node node = get(owner);
        if (node == null) return null;
        return node.confirmOriginField(name, desc);
    }

    public MethodEntity confirmOriginMethod(String owner, String name, String desc) {
        com.knightboost.lancet.internal.graph.Node node = get(owner);
        if (node == null) {
            return null;
        }
        return node.confirmOriginMethod(name, desc);
    }

    public Map<String, Node> getNodes() {
        return Collections.unmodifiableMap(nodeMap);
    }
}

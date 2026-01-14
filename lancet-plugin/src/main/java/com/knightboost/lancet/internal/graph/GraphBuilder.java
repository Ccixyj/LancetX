package com.knightboost.lancet.internal.graph;

import org.objectweb.asm.Opcodes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GraphBuilder {
    // Key is class name. value is class node.
    protected Map<String, com.knightboost.lancet.internal.graph.Node> nodeMap = new ConcurrentHashMap<>(2 >> 16);
    private volatile com.knightboost.lancet.internal.graph.Graph graph;

    //useless
//    public GraphBuilder(TransformContext context) {
//
//    }

    public GraphBuilder() {

    }

    public boolean isCacheValid() {
        return false;
    }

    public void add(ClassEntity entity) {
        add(entity, false);
    }

    // thread safe
    public void add(ClassEntity entity, boolean fromCache) {
        final com.knightboost.lancet.internal.graph.Node current = getOrPutEmpty((entity.access & Opcodes.ACC_INTERFACE) != 0, entity.name);
        if (!current.defined.compareAndSet(false, true)) {
            if (fromCache) {
                //先正式添加后面再添加cache，防止cache覆盖了新的数据，此处return
                return;
            }
            if (!entity.fromAndroid && !isCacheValid()) {
                String msg = String.format("We found duplicate %s class files in the project.", current.entity.name);
                throw new IllegalArgumentException(msg);
//                if (BooleanProperty.ENABLE_DUPLICATE_CLASS_CHECK.value() && !"module-info".equals(current.entity.name)) {
//                    throw new DuplicateClassException(msg);
//                } else {
//                    LevelLog.sDefaultLogger.e(msg);
//                }
            }
        }

        ClassNode superNode = null;
        List< com.knightboost.lancet.internal.graph.InterfaceNode> interfaceNodes = Collections.emptyList();
        if (entity.superName != null) {
            com.knightboost.lancet.internal.graph.Node node = getOrPutEmpty(false, entity.superName);
            if (node instanceof ClassNode) {
                superNode = (ClassNode) node;
                // all interfaces extends java.lang.Object
                // make java.lang.Object subclasses purely
                if (current instanceof ClassNode) {
                    synchronized (superNode) {
                        if (superNode.children == Collections.EMPTY_LIST) {
                            superNode.children = new LinkedList<>();
                        }
                        superNode.children.add((ClassNode) current);
                    }
                }
            } else {
                throw new RuntimeException(String.format("%s is not a class. Maybe there are duplicate class files in the project.", entity.superName));
            }
        }
        if (entity.interfaces.size() > 0) {
            interfaceNodes = entity.interfaces.stream()
                    .map(i -> {
                        com.knightboost.lancet.internal.graph.Node node = getOrPutEmpty(true, i);
                        if (node instanceof com.knightboost.lancet.internal.graph.InterfaceNode) {
                            final com.knightboost.lancet.internal.graph.InterfaceNode interfaceNode = ( com.knightboost.lancet.internal.graph.InterfaceNode) node;
                            synchronized (interfaceNode) {
                                if (current instanceof com.knightboost.lancet.internal.graph.InterfaceNode) {
                                    if (interfaceNode.children == Collections.EMPTY_LIST) {
                                        interfaceNode.children = new LinkedList<>();
                                    }
                                    interfaceNode.children.add(( com.knightboost.lancet.internal.graph.InterfaceNode) current);
                                } else if (current instanceof ClassNode) {
                                    if (interfaceNode.implementedClasses == Collections.EMPTY_LIST) {
                                        interfaceNode.implementedClasses = new LinkedList<>();
                                    }
                                    interfaceNode.implementedClasses.add((ClassNode) current);
                                }
                            }
                            return ( com.knightboost.lancet.internal.graph.InterfaceNode) node;
                        } else {
                            throw new RuntimeException(String.format("%s is not a interface. Maybe there are duplicate class files in the project.", i));
                        }
                    })
                    .collect(Collectors.toList());
        }
        current.entity = entity;
        current.parent = superNode;
        current.interfaces = interfaceNodes;
    }

    // find node by name, if node is not exist then create and add it.
    private Node getOrPutEmpty(boolean isInterface, String className) {
        return nodeMap.computeIfAbsent(className, n -> isInterface ?
                new InterfaceNode(n) :
                new ClassNode(n));
    }


    public Graph build() {
        if (graph == null) {
            synchronized (this) {
                if (graph == null) {
                    graph = new EditableGraph(nodeMap);
                }
            }
        }
        return graph;
    }
}

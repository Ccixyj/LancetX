package com.knightboost.lancet.internal.graph;


import  com.knightboost.lancet.internal.graph.Node;

import java.util.function.Consumer;

public interface NodeVisitor {
    void forEach(Consumer<Node> node);

}

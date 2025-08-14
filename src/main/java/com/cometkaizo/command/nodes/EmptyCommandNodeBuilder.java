package com.cometkaizo.command.nodes;

public class EmptyCommandNodeBuilder extends SoftCommandNodeBuilder {

    @Override
    protected EmptyCommandNode build() {
        return new EmptyCommandNode(this);
    }
}

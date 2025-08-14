package com.cometkaizo.command.nodes;

public class RootCommandNodeBuilder extends CommandNodeBuilder {

    public RootCommandNodeBuilder() {
        // root command nodes always have a level of -1
        level = -1;
    }

    @Override
    protected RootCommandNode build() {
        return new RootCommandNode(this);
    }
}

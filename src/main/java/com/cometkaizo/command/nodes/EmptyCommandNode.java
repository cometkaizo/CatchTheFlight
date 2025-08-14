package com.cometkaizo.command.nodes;

class EmptyCommandNode extends SoftCommandNode {

    protected EmptyCommandNode(EmptyCommandNodeBuilder builder) {
        super(builder);
    }

    @Override
    protected boolean accepts() {
        return true;
    }

    @Override
    public String toString() {
        return "EmptyCommandNode{" +
                "level=" + level +
                '}';
    }
}

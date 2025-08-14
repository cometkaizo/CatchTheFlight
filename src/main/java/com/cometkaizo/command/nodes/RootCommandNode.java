package com.cometkaizo.command.nodes;

class RootCommandNode extends CommandNode {

    public RootCommandNode(RootCommandNodeBuilder builder) {
        super(builder);
    }


    @Override
    protected boolean accepts(String arg) {
        return true;
    }

    @Override
    protected void executeFunctionality() {

    }
}

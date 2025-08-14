package com.cometkaizo.command.nodes;

/**
 * Represents classes that do not need an argument to determine its functionality
 */
abstract class SoftCommandNode extends CommandNode {

    protected SoftCommandNode(SoftCommandNodeBuilder builder) {
        super(builder);
    }

    @Override
    protected final boolean accepts(String arg) {
        return accepts();
    }

    protected abstract boolean accepts();

    @Override
    protected final void executeFunctionality() {

    }
}

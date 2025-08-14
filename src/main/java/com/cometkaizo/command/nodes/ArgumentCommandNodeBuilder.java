package com.cometkaizo.command.nodes;

import com.cometkaizo.command.arguments.Argument;

public class ArgumentCommandNodeBuilder extends CommandNodeBuilder {

    protected final Argument argument;

    public ArgumentCommandNodeBuilder(Argument argument) {
        this.argument = argument;
    }

    @Override
    protected ArgumentCommandNode build() {
        return new ArgumentCommandNode(this);
    }

    @Override
    public String toString() {
        return "ArgumentCommandNodeBuilder{" +
                "argument=" + argument +
                '}';
    }
}

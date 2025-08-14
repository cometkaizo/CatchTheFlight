package com.cometkaizo.command.nodes;

public class LiteralCommandNodeBuilder extends CommandNodeBuilder {

    protected final String literal;

    public LiteralCommandNodeBuilder(String literal) {
        this.literal = literal;
    }

    @Override
    protected LiteralCommandNode build() {
        return new LiteralCommandNode(this);
    }

    @Override
    public String toString() {
        return "LiteralCommandNodeBuilder{" +
                "literal='" + literal + '\'' +
                '}';
    }
}

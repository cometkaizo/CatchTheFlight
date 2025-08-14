package com.cometkaizo.command.nodes;

import java.util.function.Supplier;

class ConditionalCommandNode extends SoftCommandNode {

    protected final String name;
    protected final Supplier<Boolean> condition;

    public ConditionalCommandNode(ConditionalCommandNodeBuilder builder) {
        super(builder);
        this.condition = builder.condition;
        this.name = builder.name;
    }

    @Override
    protected boolean accepts() {
        return condition.get();
    }

    @Override
    public String toString() {
        return "ConditionalCommandNode{" +
                "name='" + name + '\'' +
                ", condition=" + condition +
                '}';
    }

    @Override
    public String toPrettyString() {
        return name;
    }
}

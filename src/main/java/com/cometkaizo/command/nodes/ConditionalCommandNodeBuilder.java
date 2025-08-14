package com.cometkaizo.command.nodes;

import java.util.function.Supplier;

public class ConditionalCommandNodeBuilder extends SoftCommandNodeBuilder {

    protected final Supplier<Boolean> condition;
    protected final String name;

    public ConditionalCommandNodeBuilder(Supplier<Boolean> condition) {
        this(condition, "CONDITIONAL");
    }

    public ConditionalCommandNodeBuilder(Supplier<Boolean> condition, String name) {
        this.condition = condition;
        this.name = name;
    }

    @Override
    protected ConditionalCommandNode build() {
        return new ConditionalCommandNode(this);
    }
}

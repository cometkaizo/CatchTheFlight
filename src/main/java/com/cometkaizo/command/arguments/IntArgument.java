package com.cometkaizo.command.arguments;

import java.util.function.Predicate;

public class IntArgument extends Argument {


    public IntArgument(String name) {
        super(name);
    }
    public IntArgument(String name, Predicate<Object> requirement) {
        super(name, requirement);
    }

    @Override
    public boolean accepts(String s) {
        return s.matches("[-+]?\\d+") && requirement.test(Integer.parseInt(s));
    }

    @Override
    public Integer translate(String string) throws IllegalArgumentException {
        if (!accepts(string)) throw new IllegalArgumentException();
        return Integer.parseInt(string);
    }

    @Override
    public String toString() {
        return "IntArgument{" +
                "name='" + name + '\'' +
                ", requirement=" + requirement +
                '}';
    }
}

package com.cometkaizo.command;

import com.cometkaizo.command.nodes.Command;
import com.cometkaizo.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CommandGroup {

    private final List<List<String>> names;
    private final List<Command> commands;
    private final List<Supplier<Command>> commandSuppliers;

    @SafeVarargs
    public CommandGroup(Supplier<Command>... commands) {
        this.commandSuppliers = List.of(commands);
        this.commands = new ArrayList<>(commands.length);
        this.names = new ArrayList<>(commands.length);

        for (Supplier<Command> commandSupplier : commands) {
            Command command = commandSupplier.get();
            this.commands.add(command);
            this.names.add(command.getNames());
        }

        assert this.commands.size() == this.names.size();
    }
    @SafeVarargs
    public CommandGroup(Function<Command, List<String>> nameGenerator, Supplier<Command>... commands) {
        this.commandSuppliers = List.of(commands);
        this.commands = new ArrayList<>(commands.length);
        this.names = new ArrayList<>(commands.length);

        for (Supplier<Command> commandSupplier : commands) {
            Command command = commandSupplier.get();
            this.commands.add(command);
            this.names.add(nameGenerator.apply(command));
        }

        assert this.commands.size() == this.names.size();
    }

    public Object execute(String input) {
        validateInput(input);

        // getting inputted command information
        String[] parts = getInputParts(input);
        String commandName = parts[0];
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        // getting index of correct command based on input command name
        int commandIndex = getValidCommandIndexOrThrow(commandName);

        // getting, running, and resetting the correct command
        Command command = commands.get(commandIndex);
        resetCommand(commandIndex);
        return command.execute(args);
    }

    private void resetCommand(int commandIndex) {
        commands.set(commandIndex, commandSuppliers.get(commandIndex).get());
    }

    private int getValidCommandIndexOrThrow(String commandName) {
        int commandIndex = CollectionUtils.indexOf(names, names -> names.contains(commandName));
        if (commandIndex == -1) throw new UnknownCommandException(commandName, names);
        return commandIndex;
    }

    private static String[] getInputParts(String input) {
        return input.trim().split(" ");
    }

    private static void validateInput(String input) {
        Objects.requireNonNull(input, "Command cannot be null");
        if (input.isBlank()) throw new UnknownCommandException("Command cannot be blank");
    }

    @Override
    public String toString() {
        return "CommandGroup{" +
                commands.stream().map(c -> "\t" + c).collect(Collectors.joining(",\n", "\n", "\n")) +
                '}';
    }
}

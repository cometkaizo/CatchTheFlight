package com.cometkaizo.command.nodes;

import com.cometkaizo.command.CommandSyntaxException;
import com.cometkaizo.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A CommandNode represents a single action in a command (e.g. getting user input, translating user input, executing code, etc).
 * Specific functionality is specified by subclasses.
 *
 * @see Command
 * @see RootCommandNode
 * @see LiteralCommandNode
 * @see ArgumentCommandNode
 * @see SoftCommandNode
 * @see ConditionalCommandNode
 */
abstract class CommandNode {

    protected final int level;
    protected final List<CommandNode> subNodes;
    protected final List<Runnable> tasks;
    protected Command context;

    protected CommandNode(CommandNodeBuilder builder) {
        this.subNodes = buildSubNodes(builder);
        this.tasks = List.copyOf(builder.tasks);
        this.level = builder.level;
    }
    private static List<CommandNode> buildSubNodes(CommandNodeBuilder builder) {
        return CollectionUtils.map(builder.subNodes, CommandNodeBuilder::build);
    }


    /**
     * Executes this node's tasks, and potentially one of its sub-nodes depending on arguments provided by {@code context}.
     * If no sub-nodes can be executed or there are insufficient arguments, a CommandSyntaxException is thrown.
     * If multiple sub-nodes can be executed, it will execute the first one that accepts the next argument.
     * @param context the context to run this command in
     * @throws CommandSyntaxException If there are insufficient arguments, or an argument could not be parsed by any sub-nodes.
     */
    final void execute(Command context) throws CommandSyntaxException {
        this.context = context;

        executeFunctionality();

        for (Runnable task : tasks)
            task.run();

        if (hasSubNodes())
            executeSubNodes();
    }

    private void executeSubNodes() throws CommandSyntaxException {
        if (!hasSubNodes()) return;

        if (hasNextArg())
            executeSubNodesWithNextArg();
        else {
            if (requiresNextArg()) {
                throw notEnoughArgsException();
            } else executeNoArgSubNode();
        }
    }


    protected abstract boolean accepts(String arg);

    protected abstract void executeFunctionality();

    private CommandSyntaxException notEnoughArgsException() {
        String formattedArgs = Arrays.stream(context.args)
                .map(Objects::toString)
                .collect(Collectors.joining(" "));
        String formattedSubNodes = subNodes.stream()
                .map(CommandNode::toPrettyString)
                .collect(Collectors.joining("\n or "));

        return new CommandSyntaxException(
                "Unexpected end of arguments: \n    " +
                        formattedArgs + "\n    " +
                        " ".repeat(formattedArgs.length()) + "^\n" +
                        "required: \n    " +
                        formattedSubNodes
        );
    }

    public String toPrettyString() {
        return getClass().getSimpleName().replaceAll("(?<=.)CommandNode$", "").toUpperCase();
    }

    private void executeSubNodesWithNextArg() throws CommandSyntaxException {

        for (CommandNode subNode : subNodes) {
            if (subNode.accepts(nextArg())) {
                subNode.execute(context);
                return;
            }
        }

        throw wrongArgumentTypeException();
    }

    private CommandSyntaxException wrongArgumentTypeException() {
        String formattedSubNodes = subNodes.stream()
                .map(CommandNode::toPrettyString)
                .collect(Collectors.joining("\n or "));

        return new CommandSyntaxException("Unexpected argument '" + nextArg() + "'; required: \n    " + formattedSubNodes);
    }

    private void executeNoArgSubNode() {
        CommandNode noArgSubNode = getNoArgSubNode();

        if (noArgSubNode == null) throw new NoSuchElementException();

        noArgSubNode.execute(context);
    }

    private boolean requiresNextArg() {
        return subNodes.stream().allMatch(CommandNode::acceptsArguments);
    }

    private CommandNode getNoArgSubNode() {
        return subNodes.stream()
                .filter(node -> !node.acceptsArguments())
                .findFirst().orElse(null);
    }

    public final boolean acceptsArguments() {
        return !(this instanceof SoftCommandNode);
    }

    private String nextArg() {
        return context.args[level + 1];
    }

    private boolean hasNextArg() {
        return context.args.length - 1 > level;
    }

    public boolean hasSubNodes() {
        return subNodes.size() > 0;
    }

}

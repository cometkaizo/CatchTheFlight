package com.cometkaizo.command.nodes;

import com.cometkaizo.command.CommandSyntaxException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Command class and its subclasses aim to map series of user inputted strings to specific actions.
 * Subclasses define what types of arguments are to be expected, and what to do with them.
 * The {@link RootCommandNode} {@code root} field is the root of a node tree which specifies the functionality of any command.
 * Subclasses should edit {@code root} by using {@link CommandNodeBuilder#then(CommandNodeBuilder)}, {@link CommandNodeBuilder#split(CommandNodeBuilder...)}, etc
 *
 * @see CommandNode
 */
public abstract class Command {

    protected RootCommandNodeBuilder rootNode = new RootCommandNodeBuilder();
    protected Object result = null;
    protected boolean success = true;
    String[] args;

    protected Map<String, Object> parsedArgs = new HashMap<>(0);

    public boolean getSuccess() {
        return success;
    }
    public Map<String, Object> getParsedArguments() {
        return parsedArgs;
    }

    protected void log(String message) {
        System.out.println(message);
    }

    protected void err(String message) {
        System.err.println(message);
    }

    public abstract List<String> getNames();

    /**
     * Executes this command and returns the result if one exists. Otherwise, returns null.
     * @param args The arguments to give this command. This should not contain the name of the command as the first element.
     * @return The result of this command, one exists. Otherwise, returns null.
     * @throws CommandSyntaxException Thrown if there are too little arguments, or an argument does not fit the syntax of the command.
     */
    public final Object execute(String[] args) throws CommandSyntaxException {
        this.args = args;
        rootNode.build().execute(this);
        return result;
    }

}

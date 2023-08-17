package org.aresclient.ares.api.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import net.meshmc.mesh.loader.Mod;

import java.util.Collection;
import java.util.Collections;

public class Command {
    public interface IContext {
        void print(String message);
        void error(String message);
    }

    @Mod.Interface
    private static Command.IContext CONTEXT;

    private static final CommandDispatcher<IContext> DISPATCHER = new CommandDispatcher<>();

    private final CommandNode<IContext> node;

    public Command(CommandNode<IContext> node) {
        this.node = node;
    }

    public CommandNode<IContext> getNode() {
        return node;
    }

    public Collection<String> getUsages(IContext context) {
        return getUsages(context, node);
    }

    public static void execute(String command) {
        try {
            DISPATCHER.execute(command, CONTEXT);
        } catch(CommandSyntaxException e) {
            CONTEXT.error(e.getLocalizedMessage());
        }
    }

    public static CommandNode<IContext> register(LiteralArgumentBuilder<IContext> builder) {
        return DISPATCHER.register(builder);
    }

    public static Collection<String> getUsages(IContext context, CommandNode<IContext> node) {
        return DISPATCHER.getSmartUsage(node, context).values();
    }

    public static CommandNode<IContext> getCommand(Collection<String> path) {
        return DISPATCHER.findNode(path);
    }

    public static CommandNode<IContext> getCommand(String name) {
        return getCommand(Collections.singletonList(name));
    }
}

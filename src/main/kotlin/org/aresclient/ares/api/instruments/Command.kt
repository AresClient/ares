package org.aresclient.ares.api.instruments

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.CommandNode
import com.mojang.brigadier.tree.LiteralCommandNode

abstract class Command(val name: String, vararg val aliases: String = arrayOf()) {
    interface IContext {
        fun print(message: String)
        fun error(message: String)
        fun clear()
    }

    companion object {
        private val DISPATCHER: CommandDispatcher<IContext?> = CommandDispatcher()

        fun getUsages(context: IContext, node: CommandNode<IContext?>): Collection<String> {
            return DISPATCHER.getSmartUsage(node, context).values
        }

        fun execute(context: IContext, command: String) {
            try {
                DISPATCHER.execute(command, context)
            } catch(e: CommandSyntaxException) {
                context.error(e.localizedMessage)
            }
        }
    }

    private val node: LiteralCommandNode<IContext?> = DISPATCHER.register(literal<IContext>(name).builder())

    protected abstract fun LiteralArgumentBuilder<IContext>.builder(): LiteralArgumentBuilder<IContext?>

    init {
        for(alias in aliases) {
            DISPATCHER.register(literal<IContext>(alias).fixedRedirect(node))
        }
    }

    fun getNode(): CommandNode<IContext?> {
        return node
    }

    fun getUsages(context: IContext): Collection<String> {
        return getUsages(context, node)
    }

    // Brigadier's redirect is broken for commands with no arguments, and has been for many years. See:
    // https://github.com/Mojang/brigadier/issues/46
    // https://github.com/PaperMC/Velocity/blob/8abc9c80a69158ebae0121fda78b55c865c0abad/proxy/src/main/java/com/velocitypowered/proxy/util/BrigadierUtils.java#L38
    private fun <T> LiteralArgumentBuilder<T>.fixedRedirect(target: LiteralCommandNode<T>): LiteralArgumentBuilder<T> {
        val builder = requires(target.requirement).forward(target.redirect, target.redirectModifier, target.isFork).executes(target.command)
        target.children.forEach { builder.then(it) }
        return builder
    }
}

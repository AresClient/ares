package org.aresclient.ares.api.instruments

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.CommandNode
import net.minecraft.client.gui.DrawContext

open class Command(private val node: CommandNode<IContext?>) {
	interface IContext {
		fun print(message: String)
		fun error(message: String)
	}

	companion object {
		private val DISPATCHER: CommandDispatcher<IContext?> = CommandDispatcher()

		fun register(builder: LiteralArgumentBuilder<IContext?>?): CommandNode<IContext?> {
			return DISPATCHER.register(builder)
		}

		fun getUsages(context: IContext, node: CommandNode<IContext?>): Collection<String> {
			return DISPATCHER.getSmartUsage(node, context).values
		}

		fun getCommand(path: Collection<String>): CommandNode<IContext?> {
			return DISPATCHER.findNode(path)
		}

		fun getCommand(name: String): CommandNode<IContext?> {
			return getCommand(listOf(name))
		}

		fun execute(context: IContext, command: String) {
			try {
				DISPATCHER.execute(command, context)
			} catch(e: CommandSyntaxException) {
				context.error(e.localizedMessage)
			}
		}
	}

	fun getNode(): CommandNode<IContext?> {
		return node
	}

	fun getUsages(context: IContext): Collection<String> {
		return getUsages(context, node)
	}
}


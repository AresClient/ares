package org.aresclient.ares.api.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.tree.CommandNode

open class Command(private val node:CommandNode<IContext?>?) {
	interface IContext {
		fun print(message:String?)
		fun error(message:String?)
	}

	companion object {
		@JvmStatic private val CONTEXT:IContext? = null
		@JvmStatic private val DISPATCHER:CommandDispatcher<IContext?> = CommandDispatcher()

		fun register(builder:LiteralArgumentBuilder<IContext?>?): CommandNode<IContext?> {
			return DISPATCHER.register(builder)
		}

		fun getUsages(context:IContext?, node:CommandNode<IContext?>?): Collection<String> {
			return DISPATCHER.getSmartUsage(node, context).values
		}

		fun getCommand(path:Collection<String?>?):CommandNode<IContext?> {
			return DISPATCHER.findNode(path)
		}

		fun getCommand(name:String):CommandNode<IContext?> {
			return getCommand(listOf(name))
		}
	}

	fun getNode():CommandNode<IContext?>? {
		return node
	}

	fun getUsages(context:IContext?):Collection<String> {
		return getUsages(context, node)
	}

	fun execute(command:String?) {
		try {
			DISPATCHER.execute(command, CONTEXT)
		} catch (e:CommandSyntaxException) {
			CONTEXT!!.error(e.localizedMessage)
		}
	}
}


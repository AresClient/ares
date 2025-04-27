package org.aresclient.ares.impl.instrument.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder.literal
import org.aresclient.ares.api.instruments.Command

object SetCommand: Command(register(
    literal<IContext?>("set")
        .then()
))
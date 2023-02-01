package org.aresclient.ares.impl

import net.minecraft.client.network.ClientPlayerEntity

class ClientPlayerEntityWrapper(clientPlayerEntity: ClientPlayerEntity): PlayerEntityWrapper(clientPlayerEntity), org.aresclient.ares.api.ClientPlayerEntity {
}
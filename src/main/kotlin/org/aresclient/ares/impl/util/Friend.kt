package org.aresclient.ares.impl.util

import com.mojang.authlib.GameProfile
import java.util.UUID

class Friend(val uuid: UUID, var name: String) {
    constructor(profile: GameProfile): this(profile.id, profile.name)

    fun matches(profile: GameProfile): Boolean {
        return if(uuid == profile.id) {
            name = profile.name
            true
        } else false
    }
}

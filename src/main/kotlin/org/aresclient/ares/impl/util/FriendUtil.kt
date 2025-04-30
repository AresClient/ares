package org.aresclient.ares.impl.util

import com.google.gson.Gson
import com.mojang.authlib.GameProfile
import com.mojang.util.UndashedUuid
import org.aresclient.ares.api.Wrapper
import java.io.File
import java.net.URI
import java.net.URLEncoder

object FriendUtil: Wrapper {
    private val file = File("ares/friends.csv")
    private val gson = Gson()
    private val friends = read()

    private fun read(): MutableList<Friend> {
        file.parentFile.mkdirs()
        if(!file.exists()) return mutableListOf()
        return file.readLines().mapNotNull {
            val split = it.split(",")
            if(split.size != 2) return@mapNotNull null
            try {
                Friend(UndashedUuid.fromStringLenient(split[0]), split[1])
            } catch(e: Exception) {
                null
            }
        }.toMutableList()
    }

    fun save() {
        file.parentFile.mkdirs()
        file.bufferedWriter().use { out ->
            friends.forEach { friend ->
                out.write(UndashedUuid.toString(friend.uuid))
                out.write(",")
                out.write(friend.name)
                out.newLine()
            }
        }
    }

    private data class ProfileLookupResponse(val id: String, val name: String)

    fun getProfileByName(name: String): GameProfile? {
        val profile = MC.networkHandler?.playerList?.find { it.profile.name == name }?.profile
        if(profile != null) return profile

        try {
            val res = gson.fromJson(URI("https://api.mojang.com/users/profiles/minecraft/${URLEncoder.encode(name, "UTF-8")}").toURL().openConnection().also {
                it.addRequestProperty("User-Agent", "Mozilla/4.76")
            }.getInputStream().reader(), ProfileLookupResponse::class.java)
            return GameProfile(UndashedUuid.fromString(res.id), res.name)
        } catch(e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun addFriendByName(name: String): Boolean {
        addFriend(getProfileByName(name) ?: return false)
        return true
    }

    fun addFriend(profile: GameProfile) {
        friends.add(Friend(profile))
    }

    fun removeFriend(friend: Friend) {
        friends.remove(friend)
    }

    fun removeFriend(profile: GameProfile) {
        friends.removeIf { profile.id == it.uuid }
    }

    fun removeFriendByName(name: String) {
        friends.remove(friends.find { it.name == name } ?: return)
    }

    fun isFriend(profile: GameProfile): Boolean = friends.any { it.matches(profile) }

    fun isFriendByName(name: String): Boolean = friends.any { it.name == name }

    fun getFriends() = friends
}

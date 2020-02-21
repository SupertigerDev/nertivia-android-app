package com.supertiger.nertivia.cache

import com.supertiger.nertivia.models.Server
import com.supertiger.nertivia.models.ServerMember


var servers: MutableMap<String?, Server> = mutableMapOf()

var serverChannelIDs: MutableMap<String?, List<String?>> = mutableMapOf()

var serverMembers: MutableList<ServerMember> = mutableListOf()

fun selectedServerMembers (): List<ServerMember> {
    return serverMembers.filter {
        it.server_id == selectedServerID
    }
}
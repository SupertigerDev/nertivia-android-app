package com.supertiger.nertivia.cache

import com.supertiger.nertivia.models.Server


var servers: MutableMap<String?, Server> = mutableMapOf()

var serverChannelIDs: MutableMap<String?, List<String?>> = mutableMapOf()
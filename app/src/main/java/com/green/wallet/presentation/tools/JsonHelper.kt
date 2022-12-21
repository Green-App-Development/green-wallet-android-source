package com.green.wallet.presentation.tools

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ValueNode
import java.util.*
import javax.inject.Inject


class JsonHelper @Inject constructor() {
    fun getFlattenedHashmapFromJsonForLocalization(
        currentPath: String,
        jsonNode: JsonNode,
        map: HashMap<String, String>
    ): HashMap<String, String> {
        when {
            jsonNode.isObject -> {
                val objectNode = jsonNode as ObjectNode
                val iterator = objectNode.fields()
                val pathPrefix = if (currentPath.isEmpty()) "" else (currentPath + "_")
                while (iterator.hasNext()) {
                    val entry = iterator.next()
                    getFlattenedHashmapFromJsonForLocalization(
                        entry.key,
                        entry.value,
                        map
                    )
                }
            }
            jsonNode.isValueNode -> {
                val valueNode = jsonNode as ValueNode
                map[currentPath.trim()] = valueNode.asText()
            }
        }
        return map
    }
}

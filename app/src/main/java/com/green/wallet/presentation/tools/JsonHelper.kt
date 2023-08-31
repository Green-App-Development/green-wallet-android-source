package com.green.wallet.presentation.tools

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.ValueNode
import org.json.JSONObject
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

     inline fun parseJsonTibetTokenAssetIDPairID(
        str: String,
        callBack: (String, String) -> Unit
    ) {
        val withBracket = str.trim()
        val s = withBracket.substring(1, withBracket.length - 1)
        var c = 0
        val builder = StringBuilder()
        var at = 0
        while (at < s.length) {
            if (s[at] == '{') {
                builder.append('{')
                c++
                at++
            } else if (s[at] == '}') {
                builder.append('}')
                c--
                at++
                if (c == 0) {
                    val json = JSONObject(builder.toString())
                    val assetID = json["asset_id"].toString()
                    val pairID = json["pair_id"].toString()
                    callBack(assetID, pairID)
                    builder.clear()
                    at++
                }
            } else {
                builder.append(s[at])
                at++
            }
        }
    }


}

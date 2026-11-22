package com.chiu.guitarroad.data.local

import androidx.room.TypeConverter
import com.chiu.guitarroad.data.model.NodeTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Room TypeConverters：List<String> / List<Int> / List<NodeTool> 用
 * kotlinx-serialization 编码为 JSON 字符串存 TEXT。
 *
 * 说明：契约第五节的领域模型 NodeTool 本身不带 @Serializable，因此这里用
 * 一个内部 @Serializable 替身做编解码；解码容忍未知字段（assets 里
 * metronome 渐进加速配置的 "progressive" 键不在领域模型中，解码时忽略，
 * 但原始 toolsJson 字符串仍完整保留在 path_node 表中）。
 */
class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    @Serializable
    private data class NodeToolJson(
        val type: String,
        val target: String? = null,
        val bpm: Int? = null,
        val trigger: String? = null,
    )

    @TypeConverter
    fun fromStringList(value: List<String>): String =
        json.encodeToString(ListSerializer(String.serializer()), value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        if (value.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(String.serializer()), value)
        }.getOrDefault(emptyList())
    }

    @TypeConverter
    fun fromIntList(value: List<Int>): String =
        json.encodeToString(ListSerializer(Int.serializer()), value)

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        if (value.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(Int.serializer()), value)
        }.getOrDefault(emptyList())
    }

    @TypeConverter
    fun fromNodeToolList(value: List<NodeTool>): String {
        val surrogate = value.map { NodeToolJson(it.type, it.target, it.bpm, it.trigger) }
        return json.encodeToString(ListSerializer(NodeToolJson.serializer()), surrogate)
    }

    @TypeConverter
    fun toNodeToolList(value: String): List<NodeTool> {
        if (value.isBlank()) return emptyList()
        return runCatching {
            json.decodeFromString(ListSerializer(NodeToolJson.serializer()), value)
                .map { NodeTool(it.type, it.target, it.bpm, it.trigger ?: "auto") }
        }.getOrDefault(emptyList())
    }
}

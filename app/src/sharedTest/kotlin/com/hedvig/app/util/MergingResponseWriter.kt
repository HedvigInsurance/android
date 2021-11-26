package com.hedvig.app.util

import com.apollographql.apollo.api.ResponseField
import com.apollographql.apollo.api.ScalarType
import com.apollographql.apollo.api.ScalarTypeAdapters
import com.apollographql.apollo.api.internal.ResponseFieldMarshaller
import com.apollographql.apollo.api.internal.ResponseWriter
import com.apollographql.apollo.api.internal.json.JsonWriter
import com.apollographql.apollo.api.internal.json.Utils
import com.apollographql.apollo.api.internal.json.use
import okio.Buffer
import okio.IOException

/*

Copyright (c) 2016-2020 Apollo Graph, Inc. (Formerly Meteor Development Group, Inc.)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

*/
class MergingResponseWriter(
    private val scalarTypeAdapters: ScalarTypeAdapters,
) : ResponseWriter {
    internal val data: MutableMap<String, Any?> = LinkedHashMap()

    @Throws(IOException::class)
    fun toJson(indent: String?): String {
        return Buffer().apply {
            JsonWriter.of(this).use { jsonWriter ->
                jsonWriter.indent = indent
                jsonWriter.beginObject()
                jsonWriter.name("data")
                Utils.writeToJson(data, jsonWriter)
                jsonWriter.endObject()
            }
        }.readUtf8()
    }

    override fun writeString(field: ResponseField, value: String?) {
        data[field.responseName] = value
    }

    override fun writeInt(field: ResponseField, value: Int?) {
        data[field.responseName] = value
    }

    override fun writeLong(field: ResponseField, value: Long?) {
        data[field.responseName] = value
    }

    override fun writeDouble(field: ResponseField, value: Double?) {
        data[field.responseName] = value
    }

    override fun writeBoolean(field: ResponseField, value: Boolean?) {
        data[field.responseName] = value
    }

    override fun writeCustom(field: ResponseField.CustomTypeField, value: Any?) {
        if (value == null) {
            data[field.responseName] = null
        } else {
            val typeAdapter = scalarTypeAdapters.adapterFor<Any>(field.scalarType)
            val customTypeValue = typeAdapter.encode(value)
            data[field.responseName] = customTypeValue.value
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun writeObject(field: ResponseField, marshaller: ResponseFieldMarshaller?) {
        if (marshaller == null) {
            data[field.responseName] = null
        } else {
            val objectResponseWriter = MergingResponseWriter(scalarTypeAdapters)
            marshaller.marshal(objectResponseWriter)
            if (data.containsKey(field.responseName)) {
                val curr = data[field.responseName] as MutableMap<String, Any?>
                data[field.responseName] = curr + objectResponseWriter.data
            } else {
                data[field.responseName] = objectResponseWriter.data
            }
        }
    }

    override fun writeFragment(marshaller: ResponseFieldMarshaller?) {
        marshaller?.marshal(this)
    }

    override fun <T> writeList(field: ResponseField, values: List<T>?, listWriter: ResponseWriter.ListWriter<T>) {
        if (values == null) {
            data[field.responseName] = null
        } else {
            val listItemWriter = CustomListItemWriter(scalarTypeAdapters)
            listWriter.write(values, listItemWriter)
            data[field.responseName] = listItemWriter.data
        }
    }

    private class CustomListItemWriter(private val scalarTypeAdapters: ScalarTypeAdapters) :
        ResponseWriter.ListItemWriter {
        val data = ArrayList<Any?>()

        override fun writeString(value: String?) {
            data.add(value)
        }

        override fun writeInt(value: Int?) {
            data.add(value)
        }

        override fun writeLong(value: Long?) {
            data.add(value)
        }

        override fun writeDouble(value: Double?) {
            data.add(value)
        }

        override fun writeBoolean(value: Boolean?) {
            data.add(value)
        }

        override fun writeCustom(scalarType: ScalarType, value: Any?) {
            if (value == null) {
                data.add(null)
            } else {
                val typeAdapter = scalarTypeAdapters.adapterFor<Any>(scalarType)
                val customTypeValue = typeAdapter.encode(value)
                data.add(customTypeValue.value)
            }
        }

        override fun writeObject(marshaller: ResponseFieldMarshaller?) {
            if (marshaller == null) {
                data.add(null)
            } else {
                val objectResponseWriter = MergingResponseWriter(scalarTypeAdapters)
                marshaller.marshal(objectResponseWriter)
                data.add(objectResponseWriter.data)
            }
        }

        override fun <T> writeList(items: List<T>?, listWriter: ResponseWriter.ListWriter<T>) {
            if (items == null) {
                data.add(null)
            } else {
                val listItemWriter = CustomListItemWriter(scalarTypeAdapters)
                listWriter.write(items, listItemWriter)
                data.add(listItemWriter.data)
            }
        }
    }
}

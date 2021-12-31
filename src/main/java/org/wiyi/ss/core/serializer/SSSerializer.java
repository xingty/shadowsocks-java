package org.wiyi.ss.core.serializer;

import org.wiyi.ss.core.exception.SSSerializationException;

/**
 * Basic interface serialization and deserialization of Objects to byte arrays (binary data).
 */
public interface SSSerializer<T> {
    /**
     * Serialize the given object to binary data.
     *
     * @param data object to serialize.
     * @return the equivalent binary data.
     */
    byte[] serialize(T data) throws SSSerializationException;

    /**
     * Deserialize an object from the given binary data.
     *
     * @param bytes object binary representation
     * @return the equivalent object instance.
     */
    T deserialize(byte[] bytes) throws SSSerializationException;
}

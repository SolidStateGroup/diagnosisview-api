package com.tyler.gson.immutable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

abstract class BaseMapDeserializer<E> implements JsonDeserializer<E> {

    protected abstract E buildFrom(final Map<?, ?> map);

    public E deserialize(
            final JsonElement json,
            final Type type,
            final JsonDeserializationContext context) throws JsonParseException {
        final Type[] typeArguments = ((ParameterizedType) type).getActualTypeArguments();

        final Type parameterizedType = Types.hashmapOf(typeArguments[0], typeArguments[1]).getType();

        final Map<?, ?> map = context.deserialize(json, parameterizedType);

        return buildFrom(map);
    }
}

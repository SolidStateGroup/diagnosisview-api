package com.tyler.gson.immutable;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class ImmutableMapDeserializer extends BaseMapDeserializer<ImmutableMap<?, ?>> {

    @Override
    protected ImmutableMap<?, ?> buildFrom(final Map<?, ?> map) {
        return ImmutableMap.copyOf(map);
    }

}
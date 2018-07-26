package com.tyler.gson.immutable;

import com.google.common.collect.ImmutableSortedSet;

import java.util.Collection;

public class ImmutableSortedSetDeserializer extends BaseCollectionDeserializer<ImmutableSortedSet<?>> {

    @Override
    protected ImmutableSortedSet<?> buildFrom(final Collection<?> collection) {
        return ImmutableSortedSet.copyOf(collection);
    }

}
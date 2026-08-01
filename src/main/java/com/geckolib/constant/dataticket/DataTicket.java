package com.geckolib.constant.dataticket;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.reflect.TypeToken;

/** Typed identity key for addon-provided animation data. */
public class DataTicket<D> {

    private static final Map<Identity, DataTicket<?>> IDENTITY_CACHE = new ConcurrentHashMap<>();

    private final String id;
    private final Type dataType;

    protected DataTicket(String id, Type dataType) {
        this.id = id;
        this.dataType = dataType;
    }

    public static <D> DataTicket<D> create(String id, Class<? extends D> objectType) {
        return create(id, TypeToken.of(objectType));
    }

    @SuppressWarnings("unchecked")
    public static <D> DataTicket<D> create(String id, TypeToken<? extends D> token) {
        Identity identity = new Identity(id, token.getType());

        return (DataTicket<D>) IDENTITY_CACHE
            .computeIfAbsent(identity, ignored -> new DataTicket<>(id, token.getType()));
    }

    public String id() {
        return this.id;
    }

    public Type dataType() {
        return this.dataType;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DataTicket)) return false;

        DataTicket<?> other = (DataTicket<?>) obj;

        return this.id.equals(other.id) && this.dataType.equals(other.dataType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.dataType);
    }

    @Override
    public String toString() {
        return "DataTicket{" + this.id + ": " + this.dataType.getTypeName() + "}";
    }

    private static final class Identity {

        private final String id;
        private final Type type;

        private Identity(String id, Type type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Identity)) return false;

            Identity other = (Identity) obj;

            return this.id.equals(other.id) && this.type.equals(other.type);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id, this.type);
        }
    }
}

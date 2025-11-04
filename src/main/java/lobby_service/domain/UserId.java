package lobby_service.domain;

import common.ddd.ValueObject;

import java.util.Objects;

public record UserId(String id) implements ValueObject {

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        UserId userId = (UserId) object;
        return Objects.equals(id, userId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}

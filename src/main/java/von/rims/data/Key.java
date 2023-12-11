package von.rims.data;

import java.util.Objects;

public class Key {
    private String value;
    private int index;

    public Key() {
    }

    public Key(String value, int index) {
        this.value = value;
        this.index = index;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Key key = (Key) o;
        return index == key.index && Objects.equals(value, key.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, index);
    }
}

package CCAlgorithm.base;

import java.util.Objects;

public class ArrayStructure {
    private  String nodeRepresentation; // Unique ID for the store node
    private  String array;              // Base array
    private  String index;              // Store index
    private  String value;              // Stored value

    public ArrayStructure(String nodeRepresentation, String array, String index, String value) {
        this.nodeRepresentation = nodeRepresentation;
        this.array = array;
        this.index = index;
        this.value = value;
    }

    public String getNodeRepresentation() {
        return nodeRepresentation;
    }

    public String getArray() {
        return array;
    }

    public String getIndex() {
        return index;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArrayStructure that = (ArrayStructure) o;
        return Objects.equals(nodeRepresentation, that.nodeRepresentation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeRepresentation);
    }

    @Override
    public String toString() {
        return "ArrayStructure{" +
                "nodeRepresentation='" + nodeRepresentation + '\'' +
                ", array='" + array + '\'' +
                ", index='" + index + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}

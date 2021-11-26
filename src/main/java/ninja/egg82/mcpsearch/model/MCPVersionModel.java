package ninja.egg82.mcpsearch.model;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MCPVersionModel implements Serializable {
    private List<@NotNull Long> snapshot = new ArrayList<>();

    private List<@NotNull Short> stable = new ArrayList<>();

    public MCPVersionModel() { }

    public @NotNull List<@NotNull Long> getSnapshot() { return snapshot; }

    public void setSnapshot(@NotNull List<@NotNull Long> snapshot) { this.snapshot = snapshot; }

    public @NotNull List<Short> getStable() { return stable; }

    public void setStable(@NotNull List<@NotNull Short> stable) { this.stable = stable; }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MCPVersionModel)) {
            return false;
        }
        MCPVersionModel that = (MCPVersionModel) o;
        return snapshot.equals(that.snapshot) && stable.equals(that.stable);
    }

    @Override
    public int hashCode() { return Objects.hash(snapshot, stable); }

    @Override
    public String toString() {
        return "MCPVersionModel{" +
                "snapshot=" + snapshot +
                ", stable=" + stable +
                '}';
    }
}

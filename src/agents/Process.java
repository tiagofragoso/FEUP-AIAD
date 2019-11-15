package agents;

import java.io.Serializable;
import java.util.Objects;

public class Process implements Serializable {
    private String code;

    public Process(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }

    @Override
    public String toString() {
        return this.getCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof Process)) {
            return false;
        }

        Process p = (Process) obj;

        return this.code.equals(p.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
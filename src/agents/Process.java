package agents;

public class Process {
    private String code;

    public Process(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
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
}
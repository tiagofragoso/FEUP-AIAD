public class Task {

    private Process process;
    private int duration;
    private int start;
    private int end;

    public Task(Process process, int duration) {
        this.process = process;
        this.duration = duration;
    }

    public Task(String process, int duration, int start, int end) {
        this.process = new Process(process);
        this.duration = duration;
        this.start = start;
        this.end = end;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Process getProcess() {
        return process;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public void setEnd(int end) {
        this.end = end;
    }
    


    
}
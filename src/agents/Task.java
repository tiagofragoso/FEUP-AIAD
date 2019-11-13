package agents;

public class Task {

    private Process process;
    private int duration;
    private int startTime;
    private int endTime;

    public Task(Process process, int duration) {
        this.process = process;
        this.duration = duration;
    }

    public Task(Process process, int duration, int startTime, int endTime) {
        this.process = process;
        this.duration = duration;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public int getEndTime() {
        return endTime;
    }

    public void setEndTime(int endTime) {
        this.endTime = endTime;
    }

    public Process getProcess() {
        return process;
    }


}
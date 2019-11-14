package utils;

public class Point {

    public int x;
    public int y;

    Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int distanceTo(Point point) {
        return Math.abs(this.x - point.x) + Math.abs(this.y - point.y);
    }
}

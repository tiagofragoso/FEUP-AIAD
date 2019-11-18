package utils;

import java.io.PrintWriter;
import java.util.ArrayList;

public class Table {
    private String[] headers;
    private ArrayList<Object[]> rows = new ArrayList<>();
    private int COLUMN_SIZE = 30;
    private boolean striped = false;
    private PrintWriter out;

    public Table(PrintWriter out, String[] headers) {
        this.headers = headers;
        this.out = out;
    }

    public Table(PrintWriter out, String[] headers, int columnSize) {
        this.headers = headers;
        this.COLUMN_SIZE = columnSize;
        this.out = out;
    }

    public void addRow(Object[] row) {
        this.rows.add(row);
    }

    public void setStriped(boolean striped) {
        this.striped = striped;
    }

    public void print() {
        this.printHeader();
        this.printRows();
    }

    private void printHeader() {
        for (int i = 0; i < headers.length * COLUMN_SIZE; i++) out.print("-");
        out.println();

        for (String header : headers) {
            out.printf("%-" + COLUMN_SIZE + "s", header);
        }
        out.println();

        for (int i = 0; i < headers.length * COLUMN_SIZE; i++) out.print("-");
        out.println();
    }

    private void printRows() {
        for (int i = 0; i < rows.size(); i++) {
            Object[] row = rows.get(i);
            for (Object o : row) {
                out.printf("%-" + COLUMN_SIZE + "s", o.toString());
            }
            out.println();
            if (striped || i == rows.size() - 1) {
                for (int j = 0; j < headers.length * COLUMN_SIZE; j++) out.print("-");
                out.println();
            }

        }
    }
}

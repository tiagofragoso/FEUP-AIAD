package utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class FileWriter {
    private PrintWriter out;

    public FileWriter(String fileName) throws IOException {
        File f = new File(fileName);
        f.createNewFile();
        out = new PrintWriter(fileName);
    }

    PrintWriter out() {
        return out;
    }
}

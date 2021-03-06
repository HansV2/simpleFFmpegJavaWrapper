package de.hans;

import com.sun.javafx.PlatformUtil;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class FfprobeConsoleWrapperService {

    private static final String PATH_ENCLOSURE = System.getProperty("os.name").toLowerCase().contains("win") ? "\"" : "";

    private List<String> parameters;
    private ProcessBuilder processBuilder;

    public FfprobeConsoleWrapperService() {
        init();
    }

    private void init() {
        ProcessBuilder.Redirect redirected;
        processBuilder = new ProcessBuilder();
        parameters = new ArrayList<String>();
        if(System.getProperty("os.name").toLowerCase().contains("win")){
            parameters.add("cmd");
            parameters.add("/c");
        }

        parameters.add("ffprobe");
    }

    public long getContainerLengthInMilliseconds(File video) throws IOException, InterruptedException, ParseException {
        Process start = getVideoContainerLengthInSecondsAsync(video);
        InputStream is = start.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        if (start.waitFor() != 0) {
            throw new RuntimeException("Something went wrong while getting video length.");
        }

        StringBuilder output = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        String time = output.toString();
        return getAsMilliseconds(time);
    }

    private int getAsMilliseconds(String time) {
        String[] split = time.split("\\.");
        int seconds = Integer.parseInt(split[0]);
        return seconds * 1000;
    }

    private Process getVideoContainerLengthInSecondsAsync(File video) throws IOException {
        parameters.add("-v");
        parameters.add("error");
        parameters.add("-show_entries");
        parameters.add("format=duration");
        parameters.add("-of");
        parameters.add("default=noprint_wrappers=1:nokey=1");
        parameters.add(PATH_ENCLOSURE + video.getAbsolutePath() + PATH_ENCLOSURE);

        processBuilder.command(parameters);

        Process start = processBuilder.start();
        init();
        return start;
    }
}

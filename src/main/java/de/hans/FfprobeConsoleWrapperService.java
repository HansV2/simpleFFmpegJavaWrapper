package de.hans;

import java.io.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class FfprobeConsoleWrapperService {

    private List<String> parameters;
    private ProcessBuilder processBuilder;

    public FfprobeConsoleWrapperService() {
        init();
    }

    private void init() {
        ProcessBuilder.Redirect redirected;
        processBuilder = new ProcessBuilder();
        parameters = new ArrayList<String>();
        parameters.add("cmd");
        parameters.add("/c");
        parameters.add("ffprobe");
    }

    public long getVideoContainerLengthInMilliseconds(File video) throws IOException, InterruptedException, ParseException {
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
        int milliseconds = 0;
        if(split[1].length() > 0){
            milliseconds =Integer.parseInt(split[1].replaceAll("0+$", ""));
        }
        return seconds * 1000 + milliseconds;
    }

    private Process getVideoContainerLengthInSecondsAsync(File video) throws IOException {
        parameters.add("-v");
        parameters.add("error");
        parameters.add("-show_entries");
        parameters.add("format=duration");
        parameters.add("-of");
        parameters.add("default=noprint_wrappers=1:nokey=1");
        parameters.add("\"" + video.getAbsolutePath() + "\"");

        processBuilder.command(parameters);

        Process start = processBuilder.start();
        init();
        return start;
    }
}

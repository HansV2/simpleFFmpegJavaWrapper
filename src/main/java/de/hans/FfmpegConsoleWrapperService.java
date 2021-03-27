package de.hans;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FfmpegConsoleWrapperService {

    private List<String> parameters;
    private ProcessBuilder processBuilder;
    private String resultVideoParam;
    private String pathToOutputDir;

    public FfmpegConsoleWrapperService(String pathToOutputDir) {
        this.pathToOutputDir = pathToOutputDir;
        init();
    }

    private void init() {
        processBuilder = new ProcessBuilder().inheritIO();
        parameters = new ArrayList<String>();
        parameters.add("cmd");
        parameters.add("/c");
        parameters.add("ffmpeg");
    }

    public File rotateVideo90Degree(File video, String resultingVideoName) throws IOException, InterruptedException {
        File resultingFile = new File(pathToOutputDir + "/" + resultingVideoName + ".ts");
        resultVideoParam = "\"" + resultingFile.getAbsolutePath() + "\"";
        Process start = rotateVideo90Degree(video);
        if (start.waitFor() != 0 || !isValidFile(resultingFile)) {
            throw new RuntimeException("Something went wrong while rotating video.");
        }
        return resultingFile;
    }

    private Process rotateVideo90Degree(File video) throws IOException {
        parameters.add("-i");
        parameters.add("\"" + video.getAbsolutePath() + "\"");
        parameters.add("-vf");
        parameters.add("\"transpose=1\"");
        parameters.add(resultVideoParam);

        processBuilder.command(parameters);

        Process start = processBuilder.start();
        init();
        return start;
    }

    public File rescaleVideo(File video, File resultingFile, int width, int height) throws IOException, InterruptedException {
        resultVideoParam = "\"" + resultingFile.getAbsolutePath() + "\"";
        Process start = reScaleVideoAsync(video, width, height);
        if (start.waitFor() != 0 || !isValidFile(resultingFile)) {
            throw new RuntimeException("Something went wrong while rotating video.");
        }
        return resultingFile;
    }

    private Process reScaleVideoAsync(File video, int width, int height) throws IOException {
        parameters.add("-i");
        parameters.add("\"" + video.getAbsolutePath() + "\"");
        parameters.add("-vf");
        parameters.add("scale=w=" + width + ":h=" + height + ":force_original_aspect_ratio=decrease,pad=" + width + ":" + height + ":-1:-1:color=black");
        parameters.add(resultVideoParam);

        processBuilder.command(parameters);

        Process start = processBuilder.start();
        init();
        return start;
    }

    public File reEncodeTo1080p(File video, String resultingVideoName) throws IOException, InterruptedException {
        File resultingFile = new File(pathToOutputDir + "/" + resultingVideoName + ".ts");
        resultVideoParam = "\"" + resultingFile.getAbsolutePath() + "\"";
        Process start = reEncodeTo1080pAsync(video);
        if (start.waitFor() != 0 || !isValidFile(resultingFile)) {
            throw new RuntimeException("Something went wrong while re-encoding video to 1080p.");
        }
        return resultingFile;
    }

    private Process reEncodeTo1080pAsync(File video) throws IOException {
        parameters.add("-i");
        parameters.add("\"" + video.getAbsolutePath() + "\"");
        parameters.add("-vf");
        parameters.add("scale=-1:1080");
        parameters.add("-c:v");
        parameters.add("libx264");
        parameters.add("-crf");
        parameters.add("18");
        parameters.add("-preset");
        parameters.add("veryslow");
        parameters.add("-c:a");
        parameters.add("copy");
        parameters.add(resultVideoParam);

        processBuilder.command(parameters);

        Process start = processBuilder.start();
        init();
        return start;
    }

    public File mergeImageWithAudio(File image, File audio, String resultingVideoName, int width, int height) throws IOException, InterruptedException {
        File resultingFile = new File(pathToOutputDir + "/" + resultingVideoName + ".ts");
        resultVideoParam = "\"" + resultingFile.getAbsolutePath() + "\"";
        Process start = mergeImageWithAudioAsync(image, audio, width, height);
        if (start.waitFor() != 0 || !isValidFile(resultingFile)) {
            throw new RuntimeException("Something went wrong while merging audio with image.");
        }
        return resultingFile;
    }

    private Process mergeImageWithAudioAsync(File image, File audio, int width, int height) throws IOException {
        parameters.add("-loop");
        parameters.add("1");
        parameters.add("-y");
        parameters.add("-r");
        parameters.add("1");
        parameters.add("-i");
        parameters.add("\"" + image.getAbsolutePath() + "\"");
        parameters.add("-i");
        parameters.add("\"" + audio.getAbsolutePath() + "\"");
        parameters.add("-c:a");
        parameters.add("copy");
        parameters.add("-shortest");
        parameters.add("-vf");
        parameters.add("scale=w=" + width + ":h=" + height + ":force_original_aspect_ratio=decrease,pad=" + width + ":" + height + ":-1:-1:color=black");
        parameters.add("-f");
        parameters.add("mpegts");
        parameters.add(resultVideoParam);

        processBuilder.command(parameters);

        Process start = processBuilder.start();
        init();
        return start;
    }

    public File concatVideos(List<File> files, String resultingVideoName) throws IOException, InterruptedException {
        File resultingFile = new File(pathToOutputDir + "/" + resultingVideoName + ".mp4");
        resultVideoParam = "\"" + resultingFile.getAbsolutePath() + "\"";
        Process start = concatVideosAsync(files);
        if (start.waitFor() != 0 || !isValidFile(resultingFile)) {
            throw new RuntimeException("Something went wrong while concattenating videos.");
        }
        return resultingFile;
    }

    private Process concatVideosAsync(List<File> files) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();

        parameters.add("-i");

        stringBuffer.append("\"concat:");
        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            stringBuffer.append(file.getAbsolutePath());
            if (i != files.size() - 1) {
                stringBuffer.append("|");
            }

        }
        stringBuffer.append("\"");
        parameters.add(stringBuffer.toString());
        parameters.add("-c");
        parameters.add("copy");
        parameters.add(resultVideoParam);

        processBuilder.command(parameters);

        Process start = processBuilder.start();
        init();
        return start;
    }

    private boolean isValidFile(File resultingFile) throws IOException {
        boolean validFile = resultingFile.exists() && Files.size(resultingFile.toPath()) > 0;
        return validFile;
    }
}

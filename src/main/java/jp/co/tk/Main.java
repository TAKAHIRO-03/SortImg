package jp.co.tk;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    private final static String INPUT_DIR = "./resources/ImageData_1";
    private final static String OUTPUT_DIR = "./out";

    public static void main(String[] args) {

        log.info("Started SortImg. target dir is \"" + INPUT_DIR + "\".");

        final var imgFiles = getImgFiles();
        if (imgFiles == null || imgFiles.isEmpty()) {
            log.warn("Dir is not found or dir is empty.");
            return;
        }

        createOutputDir();

        final var repMinMax = getRepMinMax(imgFiles);

        sortImgFile(repMinMax, imgFiles);

        log.info("Finished SortImg. Please check your \"" + OUTPUT_DIR + "\"");
    }

    private static List<String> getImgFiles() {
        final var imgFiles = new File(INPUT_DIR);
        if (!imgFiles.exists()) {
            return Collections.emptyList();
        }

        final var removedPath =
                Arrays.stream(imgFiles.listFiles())
                        .filter(fileOrDir -> fileOrDir.isFile())
                        .map(x -> x.getName().replace(INPUT_DIR, ""))
                        .collect(Collectors.toList());

        return removedPath;
    }

    private static void createOutputDir() {
        final var outPutDir = new File(OUTPUT_DIR);
        if ( ! outPutDir.exists()) {
            outPutDir.mkdir();
        }
    }

    private static int[] getRepMinMax(List<String> imgFiles) {
        final var imgFileNums =
                imgFiles.stream()
                        .map(incl_AndExt -> (incl_AndExt.replaceFirst("_[^\\\\d]+", "").replace(".jpg", "")))
                        .distinct()
                        .filter(num -> StringUtils.isNumeric(num))
                        .map(x -> Integer.valueOf(x))
                        .collect(Collectors.toList());
        final var min = imgFileNums.stream().min(Comparator.naturalOrder()).get();
        final var max = imgFileNums.stream().max(Comparator.naturalOrder()).get();

        return new int[]{min, max};
    }

    private static void sortImgFile(int[] repMinMax, List<String> imgFiles) {
        for (int i = repMinMax[0], max = repMinMax[1]; i <= max; i++) {
            final var countDirStr = String.valueOf(i);
            final var sortedImgFiles = imgFiles.stream().filter(x -> x.contains(countDirStr)).collect(Collectors.toList());
            if( ! sortedImgFiles.isEmpty()) {
                final var outPutDir = new File(OUTPUT_DIR + "\\" + countDirStr);
                if( ! outPutDir.exists()){
                    outPutDir.mkdir();
                }
                sortedImgFiles.stream().forEach(x -> {
                    final var depPath = Paths.get(INPUT_DIR + "\\" + x);
                    final var dstPath = Paths.get(outPutDir.getPath() + "\\" + x);
                    try {
                        Files.move(depPath, dstPath);
                    } catch (IOException e) {
                        log.error("Caused error moving files. \\d departure path=" + depPath + " destination path=" + dstPath, e);
                    }
                });
            }
        }
    }

}

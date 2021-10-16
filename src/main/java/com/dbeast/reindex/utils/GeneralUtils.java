package com.dbeast.reindex.utils;

import com.dbeast.reindex.project_settings.ProjectPOJO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.utils.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GeneralUtils {
    private static final Logger logger = LogManager.getLogger();

    private final static ObjectMapper mapper = new ObjectMapper();
    private final ClassLoader classLoader = getClass().getClassLoader();


    public static String generateNewUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String convertLongToDateString(final long longDate,
                                                 final SimpleDateFormat dateFormat) {
        Date date = new Date(longDate);
        return dateFormat.format(date);
    }

    public static boolean createFolder(final String folderAbsolutePath) {
        final Path folder = Paths.get(folderAbsolutePath);
        if (!Files.exists(folder)) {
            try {
                Files.createDirectories(folder);
                logger.info("Folder: " + folderAbsolutePath + " successfully created");
                return true;
            } catch (IOException e) {
                logger.error("There is the error in creating folder: " +
                        folderAbsolutePath + "\n" +
                        e);
                e.printStackTrace();
                return false;
            }
        } else {
            return true;
        }
    }

    public static boolean createFile(final String fileAbsolutePath,
                                     final Object fileContextObject) {
        File file = new File(fileAbsolutePath);
        try {
            if (fileContextObject instanceof ProjectPOJO) {
                ProjectPOJO project = ((ProjectPOJO) fileContextObject).clone();
                project.getConnectionSettings().getSource().setPassword(null);
                project.getConnectionSettings().getDestination().setPassword(null);
                mapper.writeValue(file, project);
            } else {
                mapper.writeValue(file, fileContextObject);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("File: " + fileAbsolutePath + " successfully created");
            }
            return true;
        } catch (IOException e) {
            logger.error("There is the error in creating file: " +
                    fileAbsolutePath + "\n" +
                    e);
            e.printStackTrace();
            return false;
        }
    }

    public static <T> List<T> readFilesFromFolderAndSerializeToObject(final String folderAbsolutePath,
                                                                      final Class<T> serializationClass) throws IOException {
        final Path sourceFolder = Paths.get(folderAbsolutePath);
        List<T> resultList = new ArrayList<>();
        Files.walk(sourceFolder)
                .filter(Files::isRegularFile)
                .forEach(path -> {
                    try {
                        logger.info("Read file: " + path);
                        String fileContent = readFileLineByLine(path);
                        resultList.add(mapper.readValue(fileContent, serializationClass));

                    } catch (IOException e) {
                        logger.error("There is an error in reading file: " +
                                path +
                                e);
                    }
                });
        return resultList;
    }

    public static void deleteDirectoryStream(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    public static boolean deleteFolderThatContainsSpecifiedFiles(final String folderAbsolutePath,
                                                                 final String containedFile) {
        final Path sourceFolder = Paths.get(folderAbsolutePath);
        try {
            Files.walk(sourceFolder)
                    .filter(file -> Files.isDirectory(file) && !file.equals(sourceFolder))
                    .forEach(folder -> {
                        try {
                            List<Path> projectsFiles = Files.walk(folder)
                                    .filter(file -> file.toString().contains(containedFile))
                                    .collect(Collectors.toList());
                            if (projectsFiles.size() == 0) {
                                logger.debug("The folder: " + folder + " doesn't contain the settings filed and will be deleted");
                                deleteDirectoryStream(folder);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            return true;
        } catch (IOException e) {
            logger.error("There is an error in reading file: " +
                    sourceFolder + "\n" +
                    e);
            return false;
        }
    }

    public static List<Path> readFilesFromFolderPathOneInnerFolder(final String folderAbsolutePath) {
        final Path sourceFolder = Paths.get(folderAbsolutePath);
        List<Path> resultList = new ArrayList<>();

        try {
            Files.walk(sourceFolder)
                    .filter(Files::isRegularFile)
                    .forEach(innerPath -> {
                        logger.debug("Read file: " + innerPath);
                        resultList.add(innerPath);

                    });
        } catch (IOException e) {
            logger.error("There is the error in reading file: " +
                    sourceFolder +
                    e);
            e.printStackTrace();
        }
        return resultList;
    }

    public static <T> T readFileFromFileAndSerializeToObject(final Path file,
                                                             final Class<T> serializationClass) {
        try {
//            logger.info("Read file: " + file);
            String fileContent = readFileLineByLine(file);
            return mapper.readValue(fileContent, serializationClass);

        } catch (IOException e) {
            logger.error("There is the error in reading file: " +
                    file +
                    e);
            return null;
        }
    }

    public static boolean deleteFile(final String fileAbsolutePath) {
        boolean[] isDeleted = new boolean[1];
        isDeleted[0] = true;
        try (Stream<Path> walk = Files.walk(Paths.get(fileAbsolutePath))) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(file -> {
                        try {
                            Files.delete(file);
                        } catch (IOException e) {
                            logger.error("Can't delete Folder: " + fileAbsolutePath + " Exception: " + e);
                            e.printStackTrace();
                            isDeleted[0] = false;
                        }
                    });
            if (isDeleted[0]) {
                logger.info("Folder: " + fileAbsolutePath + " Successfully deleted");
            }
            return isDeleted[0];
        } catch (IOException e) {
            logger.error("Can't delete Folder: " + fileAbsolutePath + " Exception: " + e);
            e.printStackTrace();
            return false;
        }

    }

    public String readFileFromResourcesToString(final String fileName) {
        String result = "";
        try {
            result = IOUtils.toString(classLoader.getResourceAsStream(fileName));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String readFileLineByLine(final Path filePath) {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(filePath, StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }
}

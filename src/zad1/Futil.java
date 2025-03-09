package zad1;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Futil {
    // Variable that allows the output file (if exists) to be truncated ONLY during the first writeToFile method invocation
    private static boolean existingFileTruncated = false;

    public static void processDir(String dirName, String resultFileName) {
        FileVisitor<Path> fileVisitor = new FileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                System.out.println("Entering directory: " + dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("Entering file: " + file);
                if (file.toString().endsWith(".txt")) {
                    byte[] byteArray = readFile(file.toString());
                    writeToFile(resultFileName, byteArray);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                System.out.println("File visit failed: " + file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(Paths.get(dirName), fileVisitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] readFile(String fileName) throws IOException {
        try (FileInputStream fis = new FileInputStream(fileName)) {
            FileChannel fc = fis.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) fc.size());
            fc.read(byteBuffer);
            fc.close();

            byteBuffer.flip();
            byte[] byteArray = new byte[byteBuffer.remaining()];
            byteBuffer.get(byteArray);

            // Convert from Cp1250 to UTF-8 encoding
            byteArray = new String(byteArray, Charset.forName("windows-1250"))
                    .getBytes(StandardCharsets.UTF_8);

            return byteArray;
        }
    }

    private static void writeToFile(String fileName, byte[] byteArray) throws IOException {
        // StandardOpenOptions kept in a Set to add TRUNCATE_EXISTING option ONLY during the first writeToFile method invocation
        Set<StandardOpenOption> StandardOpenOptions = new HashSet<>(Arrays.asList(
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        ));
        if (!existingFileTruncated) {
            StandardOpenOptions.add(StandardOpenOption.TRUNCATE_EXISTING);
            StandardOpenOptions.remove(StandardOpenOption.APPEND); // APPEND + TRUNCATE_EXISTING not allowed
            existingFileTruncated = true;
        }

        try (FileChannel fc = FileChannel.open(Paths.get(fileName), StandardOpenOptions)) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);
            while (byteBuffer.hasRemaining()) {
                fc.write(byteBuffer);
            }
        }
    }
}




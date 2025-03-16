package zad1;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
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
        FileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                System.out.println("Entering directory: " + dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("Entering file: " + file);
                if (file.toString().endsWith(".txt")) {
                    CharBuffer charBuffer = readFile(file.toString());
                    writeToFile(resultFileName, charBuffer);
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

    private static CharBuffer readFile(String fileName) throws IOException {
        try (FileChannel fc = FileChannel.open(Paths.get(fileName), StandardOpenOption.READ)) {
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) fc.size());
            fc.read(byteBuffer);
            fc.close();

            byteBuffer.flip();
            
            // Decode from Cp1250
            return Charset
                    .forName("windows-1250")
                    .decode(byteBuffer);
        }
    }

    private static void writeToFile(String fileName, CharBuffer charBuffer) throws IOException {
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

        Charset charset = StandardCharsets.UTF_8;
        ByteBuffer byteBuffer = charset.encode(charBuffer);

        try (FileChannel fc = FileChannel.open(Paths.get(fileName), StandardOpenOptions)) {
            while (byteBuffer.hasRemaining()) {
                fc.write(byteBuffer);
            }
        }
    }
}




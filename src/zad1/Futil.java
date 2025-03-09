package zad1;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Futil {
    public static void processDir(String dirName, String resultFileName) {
        File outputFile = new File(resultFileName);

        // Create output file if it doesn't exist
        if (!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("There was an error while creating the output file");
            }
        } else { // Clear contents of existing file
            try {
                new PrintWriter(resultFileName).close();
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        FileVisitor<Path> fileVisitor = new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("Entering directory: " + dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println("Entering file: " + file);
                if (file.toString().endsWith(".txt")) {
                    FileInputStream fileInputStream = new FileInputStream(file.toString());
                    FileChannel fileInputChannel = fileInputStream.getChannel();
                    ByteBuffer inputByteBuffer = ByteBuffer.allocate((int) fileInputChannel.size());
                    fileInputChannel.read(inputByteBuffer);
                    fileInputChannel.close();

                    inputByteBuffer.flip();
                    byte[] byteArray = new byte[inputByteBuffer.remaining()];
                    inputByteBuffer.get(byteArray);

                    FileOutputStream fileOutputStream = new FileOutputStream(file.toString());
                    FileChannel fileOutputChannel = fileOutputStream.getChannel();
                    ByteBuffer outputByteBuffer = ByteBuffer.wrap(byteArray);
                    fileOutputChannel.write(outputByteBuffer);
                    fileOutputChannel.close();
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                System.out.println("File visiting failed: " + file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                System.out.println("Exiting directory: " + dir);
                return FileVisitResult.CONTINUE;
            }
        };
        try {
            Files.walkFileTree(Paths.get(dirName), fileVisitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



// NOTES
// Files visited correctly, not sure if possible to read bytes only or have to convert, because no data is outputted to resultFile
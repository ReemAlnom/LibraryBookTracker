import java.io.*;
import java.util.*;

public class LibraryBookTracker {

    public static void main(String[] args) {

        try {

            if (args.length < 2) {
                throw new InsufficientArgumentsException(
                        "You must provide catalog file and operation argument."
                );
            }

            String fileName = args[0];
            String operation = args[1];

            if (!fileName.endsWith(".txt")) {
                throw new InvalidFileNameException(
                        "Catalog file must end with .txt"
                );
            }

            File catalogFile = new File(fileName);

            // 🔹 Shared list between threads
            List<Book> sharedBooks = new ArrayList<>();

            // ==============================
            // Thread 1: File Reader
            // ==============================
            FileReaderThread fileTask =
                    new FileReaderThread(fileName, sharedBooks);

            Thread fileThread = new Thread(fileTask);

            fileThread.start();
            fileThread.join();   // WAIT until reading finishes

            // ==============================
            // Thread 2: Operation Analyzer
            // ==============================
            OperationAnalyzerThread opTask =
                    new OperationAnalyzerThread(operation,
                            sharedBooks,
                            catalogFile);

            Thread opThread = new Thread(opTask);

            opThread.start();
            opThread.join();    // WAIT until operation finishes

            // ==============================
            // Final statistics
            // ==============================
            System.out.println("Valid records: " + sharedBooks.size());
            System.out.println("Thank you for using the Library Book Tracker.");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
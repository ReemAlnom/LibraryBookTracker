import java.io.*;
import java.util.*;

public class FileReaderThread implements Runnable {

    private String fileName;
    private List<Book> sharedBooks;

    public FileReaderThread(String fileName, List<Book> sharedBooks) {
        this.fileName = fileName;
        this.sharedBooks = sharedBooks;
    }

    @Override
    public void run() {
        try {
            File file = new File(fileName);

            if (!file.exists()) {
                file.createNewFile();
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = reader.readLine()) != null) {
                try {
                    Book book = parseBook(line);
                    sharedBooks.add(book);
                } catch (BookCatalogException e) {
                    System.out.println("Error reading line: " + e.getMessage());
                }
            }

            reader.close();

        } catch (IOException e) {
            System.out.println("File reading error: " + e.getMessage());
        }
    }

    private Book parseBook(String line) throws BookCatalogException {

        String[] parts = line.split(":");

        if (parts.length != 4) {
            throw new MalformedBookEntryException("Invalid format: " + line);
        }

        String title = parts[0].trim();
        String author = parts[1].trim();
        String isbn = parts[2].trim();
        int copies = Integer.parseInt(parts[3].trim());

        if (!isbn.matches("\\d{13}")) {
            throw new InvalidISBNException("Invalid ISBN: " + line);
        }

        return new Book(title, author, isbn, copies);
    }
}
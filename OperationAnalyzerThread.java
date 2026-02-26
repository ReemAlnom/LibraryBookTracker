import java.io.*;
import java.util.*;

public class OperationAnalyzerThread implements Runnable {

    private String operation;
    private List<Book> sharedBooks;
    private File catalogFile;

    public OperationAnalyzerThread(String operation,
                                   List<Book> sharedBooks,
                                   File catalogFile) {
        this.operation = operation;
        this.sharedBooks = sharedBooks;
        this.catalogFile = catalogFile;
    }

    @Override
    public void run() {

        try {

            if (operation.contains(":")) {
                addBook();
            } else if (operation.matches("\\d{13}")) {
                searchByISBN();
            } else {
                searchByTitle();
            }

        } catch (Exception e) {
            System.out.println("Operation error: " + e.getMessage());
        }
    }

    private void addBook() throws Exception {

        String[] parts = operation.split(":");

        String title = parts[0].trim();
        String author = parts[1].trim();
        String isbn = parts[2].trim();
        int copies = Integer.parseInt(parts[3].trim());

        if (!isbn.matches("\\d{13}")) {
            throw new InvalidISBNException("Invalid ISBN");
        }

        Book newBook = new Book(title, author, isbn, copies);
        sharedBooks.add(newBook);

        writeToFile();

        System.out.printf("%-30s %-20s %-15s %5s%n",
                "Title", "Author", "ISBN", "Copies");

        printBook(newBook);
    }

    private void searchByTitle() {

        System.out.printf("%-30s %-20s %-15s %5s%n",
                "Title", "Author", "ISBN", "Copies");

        for (Book book : sharedBooks) {
            if (book.getTitle().toLowerCase()
                    .contains(operation.toLowerCase())) {
                printBook(book);
            }
        }
    }

    private void searchByISBN() throws Exception {

        for (Book book : sharedBooks) {
            if (book.getIsbn().equals(operation)) {

                System.out.printf("%-30s %-20s %-15s %5s%n",
                        "Title", "Author", "ISBN", "Copies");

                printBook(book);
                return;
            }
        }

        System.out.println("No book found with ISBN: " + operation);
    }

    private void writeToFile() throws IOException {

        BufferedWriter writer =
                new BufferedWriter(new FileWriter(catalogFile));

        for (Book book : sharedBooks) {
            writer.write(book.getTitle() + ":" +
                         book.getAuthor() + ":" +
                         book.getIsbn() + ":" +
                         book.getCopies());
            writer.newLine();
        }

        writer.close();
    }

    private void printBook(Book book) {
        System.out.printf("%-30s %-20s %-15s %5d%n",
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCopies());
    }
}
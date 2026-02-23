import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LibraryBookTracker {

    public static void main(String[] args) {

        int validRecords = 0;
        int errors = 0;
        int results = 0;
        String operation = "";

        try {

            if (args.length < 2) {
                throw new InsufficientArgumentsException(
                        "You must provide catalog file and operation argument."
                );
            }

            String fileName = args[0];
            operation = args[1];

            if (!fileName.endsWith(".txt")) {
                throw new InvalidFileNameException(
                        "Catalog file must end with .txt"
                );
            }

            File catalogFile = new File(fileName);

            if (catalogFile.getParentFile() != null) {
                catalogFile.getParentFile().mkdirs();
            }

            if (!catalogFile.exists()) {
                catalogFile.createNewFile();
            }

            List<Book> books = readBooksFromFile(catalogFile);
            validRecords = books.size();

            results = handleOperation(operation, books, catalogFile);

        } catch (BookCatalogException e) {
            errors++;
            System.out.println("Error: " + e.getMessage());
            logError(e.getMessage(), operation);
        } catch (IOException e) {
            errors++;
            System.out.println("File error: " + e.getMessage());
            logError(e.getMessage(), operation);
        } catch (Exception e) {
            errors++;
            System.out.println("Unexpected error: " + e.getMessage());
            logError(e.getMessage(), operation);
        } finally {
            System.out.println("Valid records: " + validRecords);
            System.out.println("Search results: " + results);
            System.out.println("Books added: " + (operation.contains(":") ? 1 : 0));
            System.out.println("Errors: " + errors);
            System.out.println("Thank you for using the Library Book Tracker.");
        }
    }

    private static List<Book> readBooksFromFile(File file) throws IOException {
        List<Book> books = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        while ((line = reader.readLine()) != null) {
            try {
                Book book = parseBook(line);
                books.add(book);
            } catch (BookCatalogException e) {
                logError(e.getMessage(), line);
            }
        }

        reader.close();
        return books;
    }

    private static Book parseBook(String line) throws BookCatalogException {

        String[] parts = line.split(":");

        if (parts.length != 4) {
            throw new MalformedBookEntryException(
                    "Invalid format: " + line
            );
        }

        String title = parts[0].trim();
        String author = parts[1].trim();
        String isbn = parts[2].trim();
        String copiesStr = parts[3].trim();

        if (title.isEmpty() || author.isEmpty()) {
            throw new MalformedBookEntryException(
                    "Empty title or author: " + line
            );
        }

        if (!isbn.matches("\\d{13}")) {
            throw new InvalidISBNException(
                    "Invalid ISBN: " + line
            );
        }

        int copies;
        try {
            copies = Integer.parseInt(copiesStr);
            if (copies <= 0) {
                throw new MalformedBookEntryException(
                        "Copies must be positive: " + line
                );
            }
        } catch (NumberFormatException e) {
            throw new MalformedBookEntryException(
                    "Invalid copies: " + line
            );
        }

        return new Book(title, author, isbn, copies);
    }

    private static int handleOperation(String operation, List<Book> books, File catalogFile)
            throws BookCatalogException, IOException {

        int results = 0;

        if (operation.contains(":")) {
            Book newBook = parseBook(operation);
            books.add(newBook);

            books.sort(Comparator.comparing(Book::getTitle));

            writeBooksToFile(catalogFile, books);

            printBook(newBook);
            return 1;
        }

        if (operation.matches("\\d{13}")) {
            results = searchByISBN(operation, books);
            return results;
        }

        results = searchByTitle(operation, books);
        return results;
    }

    private static int searchByTitle(String keyword, List<Book> books) {
        int count = 0;

        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                printBook(book);
                count++;
            }
        }

        if (count == 0) {
            System.out.println("No books found with title containing: " + keyword);
        }

        return count;
    }

    private static int searchByISBN(String isbn, List<Book> books)
            throws DuplicateISBNException {

        List<Book> results = new ArrayList<>();

        for (Book book : books) {
            if (book.getIsbn().equals(isbn)) {
                results.add(book);
            }
        }

        if (results.size() > 1) {
            throw new DuplicateISBNException("Duplicate ISBN found: " + isbn);
        }

        if (results.isEmpty()) {
            System.out.println("No book found with ISBN: " + isbn);
            return 0;
        }

        printBook(results.get(0));
        return 1;
    }

    private static void writeBooksToFile(File file, List<Book> books)
            throws IOException {

        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        for (Book book : books) {
            writer.write(formatBookLine(book));
            writer.newLine();
        }

        writer.close();
    }

    private static String formatBookLine(Book book) {
        return book.getTitle() + ":" +
               book.getAuthor() + ":" +
               book.getIsbn() + ":" +
               book.getCopies();
    }

    private static void printBook(Book book) {
        System.out.printf("%-30s %-20s %-15s %5d%n",
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getCopies());
    }

    private static void logError(String message, String offending) {
        try {
            File logFile = new File("errors.log");
            BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true));

            String timestamp = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            writer.write("[" + timestamp + "] " + offending + " - " + message);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            System.out.println("Failed to write log.");
        }
    }
}
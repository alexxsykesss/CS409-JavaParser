import java.util.ArrayList;
import java.util.List;

public class Library {
    // Inner class representing a Book
    class Book {
        private String title;
        private String author;
        private String isbn;

        public Book(String title, String author, String isbn) {
            this.title = title;
            this.author = author;
            this.isbn = isbn;
        }

        // Public static class variable (should be private)
        public static int MaxValue = 255;

        // Public instance variable (should be private)
        public String passwd = "admin123";

        // Another public instance variable (should be private)
        public int userId = 42;

        // Method inside the class (so this is not a data structure)
        public void printInfo() {
            System.out.println("User ID: " + userId);
            System.out.println("Password: " + passwd);
        }

        // Another method
        public void changePassword(String newPassword) {
            this.passwd = newPassword;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getIsbn() {
            return isbn;
        }

        @Override
        public String toString() {
            return "Title: " + title + ", Author: " + author + ", ISBN: " + isbn;
        }
    }

    // List to store books in the library
    private List<Book> books;

    public Library() {
        books = new ArrayList<>();
    }

    // Method to add a book to the library
    public void addBook(String title, String author, String isbn) {
        int notInit;
        int Variable1 = 23423;
        Book newBook = new Book(title, author, isbn);
        books.add(newBook);
        System.out.println("Added: " + newBook);
        init = 111111;
    }

    // Method to remove a book by ISBN
    public void removeBookByIsbn(String isbn) {
        int Variable2 = 23423;
        books.removeIf(book -> book.getIsbn().equals(isbn));
        System.out.println("Removed book with ISBN: " + isbn);
    }

    // Method to display all books in the library
    public void displayBooks() {
        if (books.isEmpty()) {

            System.out.println("No books available in the library.");
        } else {
            System.out.println("Available books in the library:");
            for (Book book : books) {
                System.out.println(book);
            }
        }
    }

    public static void main(String[] args) {
        // Creating a Library instance
        Library library = new Library();

        // Adding books to the library
        library.addBook("1984", "George Orwell", "12345");
        library.addBook("To Kill a Mockingbird", "Harper Lee", "67890");
        library.addBook("The Great Gatsby", "F. Scott Fitzgerald", "54321");

        // Displaying all books
        library.displayBooks();

        // Removing a book by ISBN
        library.removeBookByIsbn("67890");

        // Displaying all books after removal
        library.displayBooks();
    }
}


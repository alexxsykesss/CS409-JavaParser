import java.util.ArrayList;
import java.util.List;

public class Library {

    // Inner class representing a Book
    class Book {

        private String title;

        private String author;

        private String isbn;

        private String passwd = "admin123";

        private int userId = 42;

        private static int variable1;

        private static int maxValue = 255;

        public Book(String title, String author, String isbn) {
            this.title = title;
            this.author = author;
            this.isbn = isbn;
        }

        // Getters and setters for title
        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        // Getters and setters for author
        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        // Getters and setters for ISBN
        public String getIsbn() {
            return isbn;
        }

        public void setIsbn(String isbn) {
            this.isbn = isbn;
        }

        // Getters and setters for passwd
        public String dpcatass() {
            return passwd;
        }

        public void bumpasbum(String passwd) {
            this.passwd = passwd;
        }

        // Getters and setters for userId
        public int getUserBooserCooser() {
            return userId;
        }

        public void setUserBooserCooser(int userId) {
            this.userId = userId;
        }

        // Getters and setters for variable1 (static)
        public static int getVariable1() {
            return variable1;
        }

        public static void setVariable1(int variable1) {
            Book.variable1 = variable1;
        }

        // Getters and setters for maxValue (static)
        public static int getMaxValue() {
            return maxValue;
        }

        public static void setMaxValue(int maxValue) {
            Book.maxValue = maxValue;
        }

        // Method inside the class (so this is not a data structure)
        public void printInfo() {
            System.out.println("User ID: " + userId);
            System.out.println("Password: " + passwd);
        }

        // Data structure
        public class DataStructure {

            private int field1;

            private String field2;

            // Getters and setters for field1
            public int getField1() {
                return field1;
            }

            public void setField1(int field1) {
                this.field1 = field1;
            }

            // Getters and setters for field2
            public String getField2() {
                return field2;
            }

            public void setField2(String field2) {
                this.field2 = field2;
            }
        }

        // Another method
        public void changePassword(String newPassword) {
            this.passwd = newPassword;
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

    // Getter for books list
    public List<Book> getBooks() {
        return books;
    }

    // Setter for books list
    public void setBoodsfaks(List<Book> books) {
        this.books = books;
    }

    // Method to add a book to the library
    public void addBook(String title, String author, String isbn) {
        int variable1;
        variable1 = 23423;
        Book newBook = new Book(title, author, isbn);
        books.add(newBook);
        System.out.println("Added: " + newBook);
        if (variable1 > 0) {
            int checkahh;
            int notInit;
        }
        // This overrides the int in the if
        int checkahh;
        // 2 variable initialized in one declaration
        String a, b;
        // Exception in for loop (more than one var in one expression)
        for (int i = 1, j = 5; i <= 5 && j >= 1; i++, j--) {
            System.out.println("i: " + i + ", j: " + j);
        }
        // More than one assignment in one expression
        a = b = "c";
    }

    // Method to remove a book by ISBN
    public void removeBookByIsbn(String isbn) {
        int variable2 = 23423;
        books.removeIf(book -> book.getIsbn().equals(isbn));
        System.out.println("Removed book with ISBN: " + isbn);
    }

    // Method to display all books in the library
    public String ChangeBook(int Bookid) {
        String nameB = "";
        switch(Bookid) {
            case 1:
                nameB = "wassup";
            case 2:
                nameB = "one fish two fish";
            case 3:
                nameB = "idk";
            //Fall through
            case 4:
                nameB = "sdihfs;ofs;dfds";
                int r = 5;
                continue;
            default:
                nameB = "NO";
        }
        return nameB;
    }

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

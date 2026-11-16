import java.io.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DiffTrack {

    static Scanner sc = new Scanner(System.in);

    static String DOCUMENT = "Project.txt";
    static String HISTORY = "versions.dat";
    static String LOG = "changes.log";

    // Store all document versions
    static ArrayList<Version> versions = new ArrayList<>();

    static int nextId = 1;


    // VERSION CLASS
    static class Version implements Serializable {

        private static final long serialVersionUID = 1L;

        int id;
        String author;
        String content;
        String time;

        Version(int id, String author, String content) {

            this.id = id;
            this.author = author;
            this.content = content;

            this.time = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
            );
        }
    }


    // MAIN METHOD
    public static void main(String[] args) {

        loadHistory();

        while (true) {

            System.out.println("\n================================");
            System.out.println("       DIFFTRACK SYSTEM");
            System.out.println("================================");

            System.out.println("1. Create Shared Document");
            System.out.println("2. View Document");
            System.out.println("3. Edit Document");
            System.out.println("4. Compare Changes");
            System.out.println("5. View Version History");
            System.out.println("6. Rollback");
            System.out.println("7. Exit");

            System.out.print("\nEnter choice: ");

            int choice = readInt();

            switch (choice) {

                case 1:
                    createDocument();
                    break;

                case 2:
                    viewDocument();
                    break;

                case 3:
                    editDocument();
                    break;

                case 4:
                    compareChanges();
                    break;

                case 5:
                    viewHistory();
                    break;

                case 6:
                    rollback();
                    break;

                case 7:
                    saveHistory();
                    System.out.println("Thank you!");
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }


    // READ INTEGER
    static int readInt() {

        try {
            return Integer.parseInt(sc.nextLine().trim());

        } catch (NumberFormatException e) {

            return -1;
        }
    }


    // READ MULTILINE CONTENT
    static String readContent() {

        StringBuilder content = new StringBuilder();

        System.out.println("Enter document content.");
        System.out.println("Type END to finish.");

        while (true) {

            String line = sc.nextLine();

            if (line.equalsIgnoreCase("END")) {
                break;
            }

            if (content.length() > 0) {
                content.append("\n");
            }

            content.append(line);
        }

        return content.toString();
    }


    // CREATE SHARED DOCUMENT
    static void createDocument() {

        File file = new File(DOCUMENT);

        if (file.exists()) {

            System.out.println(
                "Document already exists. Use Edit Document instead."
            );

            return;
        }

        System.out.print("Enter developer name: ");

        String author = sc.nextLine().trim();

        if (author.isEmpty()) {
            System.out.println("Author cannot be empty.");
            return;
        }

        String content = readContent();

        writeDocument(content);

        saveVersion(content, author);

        System.out.println("Shared document created successfully!");
    }


    // VIEW SHARED DOCUMENT
    static void viewDocument() {

        try {

            File file = new File(DOCUMENT);

            if (!file.exists()) {

                System.out.println("Document does not exist.");
                return;
            }

            BufferedReader reader =
                new BufferedReader(new FileReader(file));

            System.out.println("\n========== SHARED DOCUMENT ==========");

            String line;

            while ((line = reader.readLine()) != null) {

                System.out.println(line);
            }

            reader.close();

        } catch (IOException e) {

            System.out.println("Error reading document.");
        }
    }


    // EDIT EXISTING DOCUMENT
    static void editDocument() {

        File file = new File(DOCUMENT);

        if (!file.exists()) {

            System.out.println("Create a document first.");
            return;
        }

        String oldContent = readDocument();

        System.out.print("Enter developer name: ");

        String author = sc.nextLine().trim();

        if (author.isEmpty()) {

            System.out.println("Author cannot be empty.");
            return;
        }

        System.out.println("\nCurrent document:");

        System.out.println(oldContent);

        System.out.println("\nEnter the UPDATED document:");

        String newContent = readContent();

        if (oldContent.equals(newContent)) {

            System.out.println("No changes detected.");
            return;
        }

        writeDocument(newContent);

        saveVersion(newContent, author);

        System.out.println("\nDocument updated successfully!");

        compareText(oldContent, newContent);
    }


    // READ DOCUMENT FROM FILE
    static String readDocument() {

        StringBuilder content = new StringBuilder();

        try {

            BufferedReader reader =
                new BufferedReader(new FileReader(DOCUMENT));

            String line;

            while ((line = reader.readLine()) != null) {

                if (content.length() > 0) {
                    content.append("\n");
                }

                content.append(line);
            }

            reader.close();

        } catch (IOException e) {

            System.out.println("Error reading file.");
        }

        return content.toString();
    }


    // WRITE DOCUMENT TO FILE
    static void writeDocument(String content) {

        try {

            FileWriter writer = new FileWriter(DOCUMENT);

            writer.write(content);

            writer.close();

        } catch (IOException e) {

            System.out.println("Error writing document.");
        }
    }


    // SAVE A NEW VERSION
    static void saveVersion(String content, String author) {

        Version v = new Version(
            nextId++,
            author,
            content
        );

        versions.add(v);

        saveHistory();

        saveLog(v);

        System.out.println("Version saved. ID: " + v.id);
    }


    // COMPARE TWO TEXTS USING LCS
    static void compareText(String oldText, String newText) {

        // Normalize Windows and old-style line endings before comparing.
        String[] a = getLines(oldText);
        String[] b = getLines(newText);

        int m = a.length;
        int n = b.length;

        int[][] dp = new int[m + 1][n + 1];

        // Build the LCS table.
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                if (a[i].equals(b[j])) {
                    dp[i][j] = 1 + dp[i + 1][j + 1];
                } else {
                    dp[i][j] = Math.max(dp[i + 1][j], dp[i][j + 1]);
                }
            }
        }

        int i = 0;
        int j = 0;

        int added = 0;
        int removed = 0;
        int modified = 0;
        int unchanged = 0;

        ArrayList<String> deleted = new ArrayList<>();
        ArrayList<String> inserted = new ArrayList<>();

        System.out.println("\n================================");
        System.out.println("         CHANGE REPORT");
        System.out.println("================================");

        while (i < m && j < n) {

            if (a[i].equals(b[j])) {

                // Finish the changed block before displaying the unchanged line.
                int[] result = showChanges(deleted, inserted);
                added += result[0];
                removed += result[1];
                modified += result[2];

                deleted.clear();
                inserted.clear();

                System.out.println("\n[UNCHANGED]");
                System.out.println(a[i]);
                unchanged++;

                i++;
                j++;

            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                deleted.add(a[i]);
                i++;
            } else {
                inserted.add(b[j]);
                j++;
            }
        }

        // Collect any remaining lines after the LCS traversal.
        while (i < m) {
            deleted.add(a[i]);
            i++;
        }

        while (j < n) {
            inserted.add(b[j]);
            j++;
        }

        // Finish the final changed block.
        int[] result = showChanges(deleted, inserted);
        added += result[0];
        removed += result[1];
        modified += result[2];

        System.out.println("\n========== SUMMARY ==========");
        System.out.println("Added Lines: " + added);
        System.out.println("Removed Lines: " + removed);
        System.out.println("Modified Lines: " + modified);
        System.out.println("Unchanged Lines: " + unchanged);
    }

    // Convert text into lines without creating a fake line for an empty document.
    static String[] getLines(String text) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }

        String normalized = text.replace("\r\n", "\n").replace("\r", "\n");
        return normalized.split("\n", -1);
    }

    // Display one group of changes and return {added, removed, modified}.
    static int[] showChanges(
        ArrayList<String> deleted,
        ArrayList<String> inserted
    ) {

        int common = Math.min(deleted.size(), inserted.size());

        int added = 0;
        int removed = 0;
        int modified = 0;

        // Pair one old line with one new line and count it as one modification.
        for (int k = 0; k < common; k++) {
            System.out.println("\n[MODIFIED]");
            System.out.println("OLD: " + deleted.get(k));
            System.out.println("NEW: " + inserted.get(k));
            modified++;
        }

        // Extra old lines are removals.
        for (int k = common; k < deleted.size(); k++) {
            System.out.println("\n[REMOVED]");
            System.out.println("- " + deleted.get(k));
            removed++;
        }

        // Extra new lines are additions.
        for (int k = common; k < inserted.size(); k++) {
            System.out.println("\n[ADDED]");
            System.out.println("+ " + inserted.get(k));
            added++;
        }

        return new int[]{added, removed, modified};
    }


    // COMPARE PREVIOUS AND CURRENT DOCUMENT
    static void compareChanges() {

        if (versions.size() < 2) {

            System.out.println("At least two versions required.");
            return;
        }

        Version old = versions.get(versions.size() - 2);

        Version current = versions.get(versions.size() - 1);

        System.out.println("\nModified by: " + current.author);

        System.out.println("Version: " + old.id +
                           " -> " + current.id);

        compareText(old.content, current.content);
    }


    // DISPLAY VERSION HISTORY
    static void viewHistory() {

        for (Version v : versions) {

            System.out.println("\nVersion ID: " + v.id);

            System.out.println("Author: " + v.author);

            System.out.println("Time: " + v.time);
        }
    }


    // ROLLBACK
    static void rollback() {

        System.out.print("Enter version ID to restore: ");

        int id = readInt();

        Version old = null;

        for (Version v : versions) {

            if (v.id == id) {

                old = v;
                break;
            }
        }

        if (old == null) {

            System.out.println("Version not found.");
            return;
        }

        System.out.print("Enter developer name: ");

        String author = sc.nextLine().trim();

        if (author.isEmpty()) {

            System.out.println("Author cannot be empty.");
            return;
        }

        String currentContent = readDocument();

        writeDocument(old.content);

        saveVersion(old.content, author);

        System.out.println("Document restored successfully!");

        compareText(currentContent, old.content);
    }


    // SAVE VERSION HISTORY
    static void saveHistory() {

        try {

            ObjectOutputStream out =
                new ObjectOutputStream(
                    new FileOutputStream(HISTORY)
                );

            out.writeObject(versions);

            out.close();

        } catch (IOException e) {

            System.out.println("Error saving history.");
        }
    }


    // LOAD VERSION HISTORY
    @SuppressWarnings("unchecked")
    static void loadHistory() {

        File file = new File(HISTORY);

        if (!file.exists()) {
            return;
        }

        try {

            ObjectInputStream in =
                new ObjectInputStream(
                    new FileInputStream(HISTORY)
                );

            versions = (ArrayList<Version>) in.readObject();

            in.close();

            for (Version v : versions) {

                nextId = Math.max(nextId, v.id + 1);
            }

            System.out.println("Previous history loaded.");

        } catch (Exception e) {

            System.out.println("Could not load history: " +
                               e.getMessage());
        }
    }


    // RECORD CHANGES IN LOG FILE
    static void saveLog(Version v) {

        try {

            FileWriter writer = new FileWriter(LOG, true);

            writer.write(
                "Version: " + v.id +
                " | Developer: " + v.author +
                " | Time: " + v.time + "\n"
            );

            writer.close();

        } catch (IOException e) {

            System.out.println("Error writing log.");
        }
    }
}
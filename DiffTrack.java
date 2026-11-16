import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class DiffTrack {

    static class Version implements Serializable {
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

    static final String FILE = "versions.dat";
    static ArrayList<Version> versions = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);
    static int nextId = 1;

    public static void main(String[] args) {
        loadVersions();

        while (true) {
            System.out.println("\n================================");
            System.out.println("   DIFFTRACK - VERSION MANAGER");
            System.out.println("================================");
            System.out.println("1. Create new version");
            System.out.println("2. View all versions");
            System.out.println("3. Compare two versions");
            System.out.println("4. Rollback to an earlier version");
            System.out.println("5. Author with most changed lines");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");

            int choice = readInt();

            switch (choice) {
                case 1: createVersion(); break;
                case 2: viewVersions(); break;
                case 3: compareVersions(); break;
                case 4: rollback(); break;
                case 5: mostChangedAuthor(); break;
                case 6:
                    saveVersions();
                    System.out.println("Versions saved. Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    static int readInt() {
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    static void createVersion() {
        System.out.print("Enter author name: ");
        String author = sc.nextLine().trim();

        if (author.isEmpty()) {
            System.out.println("Author name cannot be empty.");
            return;
        }

        System.out.println("Enter document text.");
        System.out.println("Type END on a separate line to finish:");

        StringBuilder content = new StringBuilder();

        while (true) {
            String line = sc.nextLine();
            if (line.equals("END")) break;
            content.append(line).append("\n");
        }

        Version v = new Version(nextId++, author, content.toString());
        versions.add(v);
        saveVersions();

        System.out.println("Version created successfully. ID: " + v.id);
    }

    static void viewVersions() {
        if (versions.isEmpty()) {
            System.out.println("No versions available.");
            return;
        }

        for (Version v : versions) {
            System.out.println("\nVersion ID: " + v.id);
            System.out.println("Author: " + v.author);
            System.out.println("Time: " + v.time);
            System.out.println("Content:\n" + v.content);
            System.out.println("------------------------------");
        }
    }

    static Version findVersion(int id) {
        for (Version v : versions) {
            if (v.id == id) return v;
        }
        return null;
    }

    static void compareVersions() {
        System.out.print("Enter first version ID: ");
        Version a = findVersion(readInt());

        System.out.print("Enter second version ID: ");
        Version b = findVersion(readInt());

        if (a == null || b == null) {
            System.out.println("Invalid version ID.");
            return;
        }

        String[] x = a.content.split("\n", -1);
        String[] y = b.content.split("\n", -1);

        int m = x.length, n = y.length;
        int[][] dp = new int[m + 1][n + 1];

        // Longest Common Subsequence (LCS)
        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                if (x[i].equals(y[j])) {
                    dp[i][j] = 1 + dp[i + 1][j + 1];
                } else {
                    dp[i][j] = Math.max(
                        dp[i + 1][j], dp[i][j + 1]
                    );
                }
            }
        }

        System.out.println("\n--- Comparing V" + a.id +
                           " with V" + b.id + " ---");

        int i = 0, j = 0;
        while (i < m && j < n) {
            if (x[i].equals(y[j])) {
                System.out.println("  " + x[i]);
                i++;
                j++;
            } else if (dp[i + 1][j] >= dp[i][j + 1]) {
                System.out.println("- " + x[i]);
                i++;
            } else {
                System.out.println("+ " + y[j]);
                j++;
            }
        }

        while (i < m) System.out.println("- " + x[i++]);
        while (j < n) System.out.println("+ " + y[j++]);

        System.out.println("Comparison complete.");
    }

    static void rollback() {
        System.out.print("Enter version ID to restore: ");
        Version old = findVersion(readInt());

        if (old == null) {
            System.out.println("Version not found.");
            return;
        }

        System.out.print("Enter your author name: ");
        String author = sc.nextLine().trim();

        if (author.isEmpty()) {
            System.out.println("Author name cannot be empty.");
            return;
        }

        Version restored = new Version(
            nextId++, author, old.content
        );

        versions.add(restored);
        saveVersions();

        System.out.println("Rollback saved as new version ID: "
                           + restored.id);
    }

    static void mostChangedAuthor() {
        if (versions.size() < 2) {
            System.out.println("Create at least two versions first.");
            return;
        }

        Map<String, Integer> changes = new HashMap<>();

        for (int k = 1; k < versions.size(); k++) {
            Version previous = versions.get(k - 1);
            Version current = versions.get(k);

            int count = countChanges(
                previous.content, current.content
            );

            changes.merge(current.author, count, Integer::sum);
        }

        String bestAuthor = "";
        int maximum = -1;

        for (Map.Entry<String, Integer> entry : changes.entrySet()) {
            if (entry.getValue() > maximum) {
                maximum = entry.getValue();
                bestAuthor = entry.getKey();
            }
        }

        System.out.println("Author with most changed lines: "
                           + bestAuthor);
        System.out.println("Changed lines: " + maximum);
    }

    static int countChanges(String oldText, String newText) {
        String[] a = oldText.split("\n", -1);
        String[] b = newText.split("\n", -1);

        int m = a.length, n = b.length;
        int[][] dp = new int[m + 1][n + 1];

        for (int i = m - 1; i >= 0; i--) {
            for (int j = n - 1; j >= 0; j--) {
                if (a[i].equals(b[j])) {
                    dp[i][j] = 1 + dp[i + 1][j + 1];
                } else {
                    dp[i][j] = Math.max(
                        dp[i + 1][j], dp[i][j + 1]
                    );
                }
            }
        }

        // Deleted lines + added lines
        return (m - dp[0][0]) + (n - dp[0][0]);
    }

    static void saveVersions() {
        try (ObjectOutputStream out =
                 new ObjectOutputStream(new FileOutputStream(FILE))) {
            out.writeObject(versions);
        } catch (IOException e) {
            System.out.println("Error saving versions: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    static void loadVersions() {
        File file = new File(FILE);

        if (!file.exists()) return;

        try (ObjectInputStream in =
                 new ObjectInputStream(new FileInputStream(file))) {
            versions = (ArrayList<Version>) in.readObject();

            for (Version v : versions) {
                nextId = Math.max(nextId, v.id + 1);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Could not load saved versions: "
                               + e.getMessage());
        }
    }
}

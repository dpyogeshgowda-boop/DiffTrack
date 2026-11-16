# CP-096 DiffTrack: Document Versioning and Diff Engine

## Team
Yogesh T U (1AT24CS261), Java track

## How to run
```bash
javac DiffTrack.java
java DiffTrack
```

## Features that work
1. Create a shared document.
2. View the shared document.
3. Edit the shared document and save each update as a new version.
4. Store version ID, developer name, and timestamp.
5. Compare document versions using the Longest Common Subsequence (LCS) algorithm.
6. Identify and display added, removed, modified, and unchanged lines.
7. View version history.
8. Roll back to a previously saved version.
9. Store version history in `versions.dat` and activity details in `changes.log`.

## Sample run
```text
================================
       DIFFTRACK SYSTEM
================================
1. Create Shared Document
2. View Document
3. Edit Document
4. Compare Changes
5. View Version History
6. Rollback
7. Exit

========== SUMMARY ==========
Added Lines: 1
Removed Lines: 0
Modified Lines: 1
Unchanged Lines: 2
```

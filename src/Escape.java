/**
 * Mission 3. Escape: DFS from cell 0 to the last cell (rows * cols - 1) in the maze of Mission 1.
 * Two neighbouring cells are connected when the wall between them is broken.
 * The neighbours are tried in this order: up, right, down, left (cell - cols, cell + 1, cell + cols, cell - 1).
 * The ready method Graphs.open(w, broken) gives you the open neighbours of every cell, in no particular order:
 * to visit them in the required order, go through the four cells above one by one and take those that are in the list.
 * The program must not overflow the stack on a maze of 1000 by 1000 cells.
 */
public class Escape {

    /**
     * path: the cells of the way out, from 0 to the last cell.
     * visited: how many cells are marked as visited at the moment the exit is marked (the start and the exit are counted).
     * deadEnds: how many cells of the maze have exactly one open neighbour.
     */
    public record Result(int[] path, int visited, int deadEnds) {}

    public static Result escape(Input.Walls w, int[] broken) {
        int n = w.cells(), cols = w.cols();
        int exit = n - 1;
        int[][] g = Graphs.open(w, broken);

        // Dead ends: cells with exactly one open door (the start and the exit count too).
        int deadEnds = 0;
        for (int c = 0; c < n; c++) if (g[c].length == 1) deadEnds++;

        // DFS with our own stack instead of recursion, so a 1000 x 1000 maze cannot overflow the call stack.
        boolean[] seen = new boolean[n];
        int[] parent = new int[n];
        int[] next = new int[n];     // next[c] = which direction cell c tries next: 0 up, 1 right, 2 down, 3 left
        int[] stack = new int[n];
        int top = 0, visited = 0;

        stack[top++] = 0;
        seen[0] = true;              // a cell is marked the moment we enter it
        visited = 1;
        parent[0] = -1;

        while (top > 0 && !seen[exit]) {
            int c = stack[top - 1];
            if (next[c] == 4) { top--; continue; }   // all four directions tried: go back (pop)
            int d = next[c]++;
            int to;
            switch (d) {
                case 0 -> to = c - cols >= 0 ? c - cols : -1;                 // up
                case 1 -> to = (c % cols != cols - 1) ? c + 1 : -1;           // right
                case 2 -> to = c + cols < n ? c + cols : -1;                  // down
                default -> to = (c % cols != 0) ? c - 1 : -1;                 // left
            }
            if (to < 0 || seen[to] || !isOpen(g[c], to)) continue;
            seen[to] = true;                         // mark, count, go deeper
            visited++;
            parent[to] = c;
            stack[top++] = to;
        }

        // Rebuild the path from the exit back to the start through parent[].
        int length = 0;
        for (int c = exit; c != -1; c = parent[c]) length++;
        int[] path = new int[length];
        int i = length;
        for (int c = exit; c != -1; c = parent[c]) path[--i] = c;

        return new Result(path, visited, deadEnds);
    }

    /** True if 'to' is in the list of open neighbours (the list has at most 4 cells). */
    private static boolean isOpen(int[] neighbours, int to) {
        for (int x : neighbours) if (x == to) return true;
        return false;
    }
}
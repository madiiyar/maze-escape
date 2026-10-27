/**
 * Missions 1 and 2. Building the maze out of the walls.
 * A wall k joins the cells w.a()[k] and w.b()[k] and costs w.cost()[k]. Indexes start from 0,
 * the line number in walls.txt is the index + 1. The cells are connected by DisjointSet.
 */
public class Maze {

    /**
     * Mission 1. Take the walls in the order of the file. Break a wall when its two cells are in different sets.
     * Returns the indexes of the broken walls, in the order of breaking.
     */
    public static int[] breakWalls(Input.Walls w) {
        DisjointSet dsu = new DisjointSet(w.cells());
        int[] broken = new int[w.cells() - 1];   // a maze of n cells breaks exactly n - 1 walls
        int taken = 0;
        for (int k = 0; k < w.count(); k++) {     // walls in the order of the file
            if (dsu.union(w.a()[k], w.b()[k])) {  // different groups: break the wall
                broken[taken++] = k;
            }                                     // same group: the wall stays
        }
        return java.util.Arrays.copyOf(broken, taken);
    }

    /**
     * Mission 2. Kruskal: the same, but take the walls from the cheapest to the most expensive.
     * Returns the indexes of the broken walls.
     */
    public static int[] cheapestMaze(Input.Walls w) {
        int m = w.count();
        // Sort the walls by cost. Key = cost in the high 32 bits, wall index in the low 32 bits,
        // so one primitive long[] sort gives the order and we still know which wall it was.
        long[] keys = new long[m];
        for (int k = 0; k < m; k++) keys[k] = ((long) w.cost()[k] << 32) | k;
        java.util.Arrays.sort(keys);

        DisjointSet dsu = new DisjointSet(w.cells());
        int[] broken = new int[w.cells() - 1];
        int taken = 0;
        for (long key : keys) {
            int k = (int) key;                       // low 32 bits: the wall index
            if (dsu.union(w.a()[k], w.b()[k])) {     // joins two groups: break it
                broken[taken++] = k;
                if (taken == broken.length) break;   // n - 1 walls broken: the maze is connected
            }
        }
        return java.util.Arrays.copyOf(broken, taken);
    }

    /**
     * Mission 2. Prim with a priority queue. Returns the total cost of the cheapest maze.
     * It must be equal to the total cost of cheapestMaze.
     */
    public static long primCost(Input.Walls w) {
        int n = w.cells(), m = w.count();

        // Adjacency lists in arrays: start[v] .. start[v+1]-1 are the walls touching cell v.
        int[] start = new int[n + 1];
        for (int k = 0; k < m; k++) { start[w.a()[k] + 1]++; start[w.b()[k] + 1]++; }
        for (int v = 0; v < n; v++) start[v + 1] += start[v];
        int[] fill = java.util.Arrays.copyOf(start, n);
        int[] wallAt = new int[2 * m];
        for (int k = 0; k < m; k++) { wallAt[fill[w.a()[k]]++] = k; wallAt[fill[w.b()[k]]++] = k; }

        boolean[] inTree = new boolean[n];
        java.util.PriorityQueue<Long> pq = new java.util.PriorityQueue<>();  // keys: cost << 32 | wall index
        long total = 0;
        int added = 1;
        inTree[0] = true;
        for (int i = start[0]; i < start[1]; i++) pq.add(((long) w.cost()[wallAt[i]] << 32) | wallAt[i]);

        while (!pq.isEmpty() && added < n) {
            long key = pq.poll();                    // the cheapest wall leaving the tree
            int k = (int) key;
            int next = inTree[w.a()[k]] ? w.b()[k] : w.a()[k];   // the end that is not in the tree yet
            if (inTree[next]) continue;              // both ends already inside: skip this wall
            inTree[next] = true;
            added++;
            total += w.cost()[k];
            for (int i = start[next]; i < start[next + 1]; i++) {
                int j = wallAt[i];
                int other = w.a()[j] == next ? w.b()[j] : w.a()[j];
                if (!inTree[other]) pq.add(((long) w.cost()[j] << 32) | j);
            }
        }
        return total;
    }
}
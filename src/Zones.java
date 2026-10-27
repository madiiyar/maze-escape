/**
 * Missions 4 and 5. The slides: a directed graph. Cells are 0 .. n-1, slide i goes from s.from()[i] to s.to()[i]
 * and takes s.time()[i]. There are no slides from a cell to itself, and the exit can always be reached from the start.
 *
 * Ready for you: Graphs.slidesFrom(s)[u] is the list of the slides that leave cell u, and Graphs.slidesInto(s)[v] is the list
 * of the slides that enter cell v (reversed graph). The lists hold slide numbers i; the cell at the other end is s.to()[i]
 * or s.from()[i].
 */
public class Zones {

    /**
     * Mission 4. Kosaraju. Returns zoneOf: zoneOf[cell] = number of the zone, numbers 0 .. zones-1.
     * Number the zones in the order in which the second pass finds them. That order is a topological order
     * of the zone graph: every slide between two zones goes from a smaller number to a larger number.
     *
     * Plan: 1) DFS on the slides, write the cells in the order in which they are finished (use a stack, not recursion);
     * 2) go through the cells from the last finished to the first; if a cell has no zone yet, DFS from it on the reversed
     * slides (slidesInto) and give every cell it reaches the next zone number.
     * Is DAG in Main is 1 when the number of zones equals the number of cells (no cycle), so you do not need a separate check.
     */
    public static int[] kosaraju(Input.Slides s) {
        int n = s.n();
        int[][] out = Graphs.slidesFrom(s);     // slides leaving each cell
        int[][] in = Graphs.slidesInto(s);      // slides entering each cell = the reversed graph

        // Pass 1: DFS on the slides. Write the cells in the order in which they are FINISHED.
        boolean[] seen = new boolean[n];
        int[] finished = new int[n];            // finished[0] is the first cell to finish
        int count = 0;
        int[] stack = new int[n];
        int[] next = new int[n];                // next[u] = which slide of u we try next
        for (int root = 0; root < n; root++) {
            if (seen[root]) continue;
            int top = 0;
            stack[top++] = root;
            seen[root] = true;
            while (top > 0) {
                int u = stack[top - 1];
                if (next[u] < out[u].length) {
                    int v = s.to()[out[u][next[u]++]];
                    if (!seen[v]) { seen[v] = true; stack[top++] = v; }
                } else {
                    finished[count++] = u;       // all slides of u are done: u is finished
                    top--;
                }
            }
        }

        // Pass 2: go through the cells from the LAST finished to the first. If a cell has no zone yet,
        // DFS from it on the REVERSED slides; everything it reaches is one zone.
        int[] zoneOf = new int[n];
        java.util.Arrays.fill(zoneOf, -1);
        int zones = 0;
        for (int idx = n - 1; idx >= 0; idx--) {
            int root = finished[idx];
            if (zoneOf[root] != -1) continue;
            int top = 0;
            stack[top++] = root;
            zoneOf[root] = zones;
            while (top > 0) {
                int u = stack[--top];
                for (int i : in[u]) {
                    int v = s.from()[i];         // the cell the slide comes from
                    if (zoneOf[v] == -1) { zoneOf[v] = zones; stack[top++] = v; }
                }
            }
            zones++;                              // the next zone found gets the next number
        }
        return zoneOf;
    }

    /**
     * Mission 5. The graph of zones: the edge X to Y costs the cheapest slide from X to Y, walking inside a zone is free.
     * Go through the zones in the order of their numbers (no priority queue). Returns two numbers:
     * [0] flow stages: the number of zones on the longest chain of zones;
     * [1] the cost of the cheapest route from the zone of cell 0 to the zone of the last cell.
     */
    public static long[] routes(Input.Slides s, int[] zoneOf, int zoneCount) {
        int m = s.count();

        // Group the slides by the zone they start in (counting sort), so we can go zone by zone.
        int[] start = new int[zoneCount + 1];
        for (int i = 0; i < m; i++) start[zoneOf[s.from()[i]] + 1]++;
        for (int z = 0; z < zoneCount; z++) start[z + 1] += start[z];
        int[] fill = java.util.Arrays.copyOf(start, zoneCount);
        int[] byZone = new int[m];
        for (int i = 0; i < m; i++) byZone[fill[zoneOf[s.from()[i]]]++] = i;

        final long INF = Long.MAX_VALUE;
        long[] chain = new long[zoneCount];      // chain[z] = zones on the longest chain that ends in z
        long[] dist = new long[zoneCount];       // dist[z] = cheapest cost from the zone of the start to z
        java.util.Arrays.fill(chain, 1);
        java.util.Arrays.fill(dist, INF);
        int startZone = zoneOf[0], exitZone = zoneOf[s.n() - 1];
        dist[startZone] = 0;

        // Zone numbers are a topological order: every slide between zones goes from a smaller number to a larger one.
        // So when we reach zone z, nothing can improve it any more: all its incoming edges were already used.
        for (int z = 0; z < zoneCount; z++) {
            for (int j = start[z]; j < start[z + 1]; j++) {
                int i = byZone[j];
                int to = zoneOf[s.to()[i]];
                if (to == z) continue;                          // a slide inside a zone: walking there is free
                chain[to] = Math.max(chain[to], chain[z] + 1);   // longest chain
                if (dist[z] != INF) {                           // cheapest route (only from zones we can reach)
                    dist[to] = Math.min(dist[to], dist[z] + s.time()[i]);
                }
            }
        }

        long stages = 0;
        for (long c : chain) stages = Math.max(stages, c);
        return new long[]{stages, dist[exitZone]};
    }
}
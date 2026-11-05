import java.util.*;

/** Independent small-case oracles. Compile after src and run with -cp out SelfTest. */
public class SelfTest {
    static final Random R = new Random(250671);
    static void check(boolean ok) { if (!ok) throw new AssertionError(); }
    static String dna(int n) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < n; i++) s.append("ACGT".charAt(R.nextInt(4)));
        return s.toString();
    }
    static boolean equal(String a, int i, String b, int j, int k) {
        for (int t = 0; t < k; t++) if (a.charAt(i+t) != b.charAt(j+t)) return false;
        return true;
    }
    static int bruteEdit(String a, String b) {
        if (a.isEmpty()) return b.length();
        if (b.isEmpty()) return a.length();
        return Math.min(bruteEdit(a.substring(1), b) + 1,
                Math.min(bruteEdit(a, b.substring(1)) + 1,
                        bruteEdit(a.substring(1), b.substring(1)) + (a.charAt(0) == b.charAt(0) ? 0 : 1)));
    }
    static int bruteLcs(String a, String b) {
        int best = 0;
        for (int mask = 0; mask < (1 << a.length()); mask++) {
            int j = 0, length = 0;
            boolean valid = true;
            for (int i = 0; i < a.length(); i++) if ((mask & (1 << i)) != 0) {
                while (j < b.length() && a.charAt(i) != b.charAt(j)) j++;
                if (j == b.length()) { valid = false; break; }
                j++; length++;
            }
            if (valid) best = Math.max(best, length);
        }
        return best;
    }
    public static void main(String[] args) {
        for (int t = 0; t < 500; t++) {
            String a = dna(R.nextInt(10)), b = dna(R.nextInt(10));
            check(Strains.lcsLength(a,b) == bruteLcs(a,b));
            if (a.length() <= 4 && b.length() <= 4)
                check(Strains.editDistance(a,b) == bruteEdit(a,b));
            int[] pi = Marker.prefixFunction(b);
            for (int i = 0; i < b.length(); i++) {
                int border = 0;
                for (int len = 1; len <= i; len++)
                    if (equal(b,0,b,i+1-len,len)) border=len;
                check(pi[i] == border);
            }
            List<Integer> expected = new ArrayList<>();
            for (int i = 0; i + b.length() <= a.length(); i++)
                if (equal(a,i,b,0,b.length())) expected.add(i);
            check(Arrays.equals(Marker.findAll(a,b),expected.stream().mapToInt(x->x).toArray()));
            for (int k = 1; k <= 12; k++) {
                int count = 0;
                for (int i = 0; i+k <= a.length(); i++) {
                    for (int j = 0; j+k <= b.length(); j++)
                        if (equal(a,i,b,j,k)) { count++; break; }
                }
                check(Marker.sharedWords(a,b,k) == count);
            }
            int n = 1 + R.nextInt(10);
            int[] parent = new int[n], cost = new int[n], degree = new int[n];
            for (int v = 1; v < n; v++) { parent[v]=R.nextInt(v); degree[v]++; degree[parent[v]]++; }
            int[][] nb = new int[n][];
            for (int v = 0; v < n; v++) { nb[v]=new int[degree[v]]; cost[v]=R.nextInt(10); }
            Arrays.fill(degree,0);
            for (int v = 1; v < n; v++) { nb[v][degree[v]++]=parent[v]; nb[parent[v]][degree[parent[v]]++]=v; }
            for (int mode = 0; mode < 2; mode++) {
                if (mode == 1) Arrays.fill(cost,1);
                int best = Integer.MAX_VALUE;
                for (int mask = 0; mask < (1 << n); mask++) {
                    boolean valid = true; int total = 0;
                    for (int v = 1; v < n; v++)
                        if ((mask & (1 << v)) == 0 && (mask & (1 << parent[v])) == 0) valid = false;
                    if (!valid) continue;
                    for (int v = 0; v < n; v++) if ((mask & (1 << v)) != 0) total += cost[v];
                    best = Math.min(best,total);
                }
                boolean[] tested = Outbreak.cheapestTests(n,nb,cost); int total=0;
                for (int v = 0; v < n; v++) if (tested[v]) total+=cost[v];
                for (int v = 1; v < n; v++) check(tested[v] || tested[parent[v]]);
                check(total==best);
            }
            int diameter=0;
            for (int start=0; start<n; start++) {
                int[] dist=new int[n]; Arrays.fill(dist,-1); dist[start]=0;
                ArrayDeque<Integer> queue=new ArrayDeque<>(); queue.add(start);
                while (!queue.isEmpty()) {
                    int v=queue.remove();
                    diameter=Math.max(diameter,dist[v]);
                    for (int w:nb[v]) if (dist[w]<0) { dist[w]=dist[v]+1; queue.add(w); }
                }
            }
            check(Outbreak.longestChain(n,nb)==diameter);
        }
        check(Arrays.equals(Marker.findAll("AAAA","AA"),new int[]{0,1,2}));
        check(Marker.sharedWords("AAAA","AAA",2)==3);
        check(Strains.editDistance("GATTACA","GCATGCA")==3);
        check(Outbreak.longestChain(0,new int[0][])==0);
        System.out.println("PASS: 500 seeded randomized cases, exhaustive tree covers, subsequence enumeration, naive string oracles and BFS diameter.");
    }
}

import java.util.*;

/**
 * Missions 3 and 4. Find a word in a long text.
 */
public class Marker {

    /**
     * Mission 3a. The prefix function of p. pi[i] = the length of the longest proper prefix of p[0..i] that is
     * also a suffix of p[0..i] ("proper" means shorter than p[0..i] itself).
     * Example: p = "ACGAC" gives [0, 0, 0, 1, 2]. The last number is the "Border length" line.
     */
    public static int[] prefixFunction(String p) {
        int[] pi = new int[p.length()];
        for (int i = 1; i < p.length(); i++) {
            int j = pi[i - 1];
            while (j > 0 && p.charAt(i) != p.charAt(j)) j = pi[j - 1];
            if (p.charAt(i) == p.charAt(j)) j++;
            pi[i] = j;
        }
        return pi;
    }

    /**
     * Mission 3b. KMP. All start positions (counting from 0, in increasing order) where p occurs in text.
     * Overlapping occurrences count: "ACG" occurs in "ACGACG" at 0 and 3, and "AA" in "AAA" at 0 and 1.
     * Use the prefix function: the text pointer never goes back. The number of positions is "Marker hits",
     * the first one is "First hit" (-1 when there is none).
     */
    public static int[] findAll(String text, String p) {
        // The empty pattern matches every boundary, including after the text.
        if (p.isEmpty()) {
            int[] boundaries = new int[text.length() + 1];
            for (int i = 0; i < boundaries.length; i++) boundaries[i] = i;
            return boundaries;
        }
        int[] pi = prefixFunction(p);
        List<Integer> hits = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < text.length(); i++) {
            while (j > 0 && text.charAt(i) != p.charAt(j)) j = pi[j - 1];
            if (text.charAt(i) == p.charAt(j)) j++;
            if (j == p.length()) {
                hits.add(i - p.length() + 1);
                j = pi[j - 1]; // Keep a possible overlapping match.
            }
        }
        int[] result = new int[hits.size()];
        for (int i = 0; i < result.length; i++) result[i] = hits.get(i);
        return result;
    }

    /**
     * Mission 4. Rabin-Karp. The number of positions i in a such that the word of k letters starting at i
     * (a[i..i+k-1]) also occurs somewhere in b. Count positions, not different words. This is the "Shared words" line.
     * The idea: give every window of k letters a number (a rolling hash, updated in O(1) when the window moves by one),
     * look the number up among the windows of b, and when it is found COMPARE THE LETTERS, because two different words
     * can have the same hash.
     * Example: a = "ACGTAC", b = "TTACGT", k = 4: the words of a are ACGT, CGTA, GTAC; only ACGT is in b, so the answer is 1.
     */
    private static long slide(long hash, char outgoing, char incoming,
                              long power, long base, long mod) {
        long remainder = (hash - outgoing * power % mod + mod) % mod;
        return (remainder * base + incoming) % mod;
    }

    public static int sharedWords(String a, String b, int k) {
        if (k <= 0) throw new IllegalArgumentException("Window length must be positive");
        if (a.length() < k || b.length() < k) return 0;
        final long base = 257, mod = 1_000_000_007;
        long power = 1;
        for (int i = 1; i < k; i++) power = power * base % mod;
        Map<Long, List<Integer>> windows = new HashMap<>();
        long hash = 0;
        for (int i = 0; i < k; i++) hash = (hash * base + b.charAt(i)) % mod;
        for (int start = 0; start + k <= b.length(); start++) {
            if (start > 0) hash = slide(hash, b.charAt(start - 1),
                    b.charAt(start + k - 1), power, base, mod);
            windows.computeIfAbsent(hash, ignored -> new ArrayList<>()).add(start);
        }
        hash = 0;
        for (int i = 0; i < k; i++) hash = (hash * base + a.charAt(i)) % mod;
        int count = 0;
        for (int start = 0; start + k <= a.length(); start++) {
            if (start > 0) hash = slide(hash, a.charAt(start - 1),
                    a.charAt(start + k - 1), power, base, mod);
            List<Integer> candidates = windows.get(hash);
            if (candidates == null) continue;
            for (int other : candidates) {
                int j = 0;
                while (j < k && a.charAt(start + j) == b.charAt(other + j)) j++;
                if (j == k) { count++; break; }
            }
        }
        return count;
    }
}

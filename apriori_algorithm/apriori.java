/* THIS CODE IS MY OWN WORK, IT WAS WRITTEN WITHOUT CONSULTING CODE WRITTEN BY OTHER STUDENTS. Wendy Zhu */
/*
 * COLLABORATION STATEMENT:
 * - I referenced the pseudocode in Figure 6.4 of Data Mining: Concepts and Techniques
 * - I referred to Java documentation for Sets, Lists, HashMaps, FileWriter, and Scanner
 * - referenced http://www.philippe-fournier-viger.com/spmf/apriori_longer.pdf for in-depth psuedocode explanation
 * - referenced https://dzone.com/articles/how-to-sort-a-map-by-value-in-java-8 for sorting a hashmap by its values
*/

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class apriori {

  public static void main(String[] args) {
    aprioriAlgorithm("data.csv", 600, "output.txt");
  }

  public static void aprioriAlgorithm(String inputFile, int minSup, String outputFile) {
    List<List<String>> tweets = scanFile(inputFile); // get list of tweets from the data file

    // go through each tweet and store the keywords as a string in a list
    List<String> keywordStrings = new ArrayList<String>();
    for (int i = 1; i < tweets.size(); i++) {
      keywordStrings.add(tweets.get(i).get(7)); // gets the keywords
    }

    List<Set<String>> keywords = new ArrayList<Set<String>>(); // list to store sets of keywords
    // separate each string of keywords into entries in a list
    // and store each list as a set to prevent duplicate items
    for (int i = 0; i < keywordStrings.size(); i++) {
      List<String> list = Arrays.asList(keywordStrings.get(i).split(";"));
      HashSet<String> words = new HashSet<String>(list);
      keywords.add(words);
    }

    // use a map to store each set of keywords and their corresponding support
    HashMap<Set<String>, Integer> supportCounts = new HashMap<>();

    // get the list of frequent 1-itemsets
    List<Set<String>> frequentOneItemsets = getFrequentOneItemsets(keywords, supportCounts, minSup);

    // use a map to store frequent k-itemsets
    // where k is each key and the frequent k-itemsets are the values
    HashMap<Integer, List<Set<String>>> map = new HashMap<>();
    map.put(1, frequentOneItemsets); // store the list of frequent 1-itemsets

    int k = 1;
    // use k-itemsets to find (k+1)-itemsets until no more can be found
    while (!map.get(k).isEmpty()) {
      k++;

      // find frequent k-itemset candidates from the (k-1)-itemset
      List<Set<String>> candidates = getCandidates(map.get(k-1));

      // get the list of subsets from the candidate itemsets
      for (Set<String> keyword : keywords) {
        List<Set<String>> subsets = subset(candidates, keyword);

        // update the support counts of each new itemset in the list of subsets
        for (Set<String> itemset : subsets) {
          supportCounts.put(itemset, supportCounts.getOrDefault(itemset, 0) + 1);
        }
      }

      // update the map with the list of frequent k-itemsets
      map.put(k, getNextItemsets(candidates, supportCounts, minSup, keywords.size()));

    }

    // extract all the frequent itemsets and map them to their support counts
    Map<Set<String>, Integer> temp = extractFrequentItemsets(map, supportCounts);

    // sort the itemsets in descending order of their support counts
    final Map<Set<String>, Integer> result = sortByValue(temp);

    // write the results to the output file
    writeOutput(result, supportCounts, outputFile);

  }

  // find frequent 1-itemsets
  public static List<Set<String>> getFrequentOneItemsets(List<Set<String>> keywords, Map<Set<String>, Integer> supportCounts, int minSup) {
    List<Set<String>> frequentOneItemsets = new ArrayList<Set<String>>();
    HashMap<String, Integer> map = new HashMap<>(); // stores single keywords and their support counts

    // go through the list of keyword sets
    for (Set<String> words : keywords) {
      // for each word, update its support count in the supportCounts map
      for (String word : words) {

        Set<String> set = new HashSet<String>();
        set.add(word);

        if (supportCounts.containsKey(set)) {
          supportCounts.put(set, supportCounts.get(set) + 1);
        } else {
          supportCounts.put(set, 1);
        }

        // store each word in the map along with its support count, updating
        // the support count if needed
        map.put(word, map.getOrDefault(word, 0) + 1);
      }
    }

    // find entries in the map with a support greater than or equal to
    // the minimum support and add them to the list of frequent 1-itemsets
    for (Map.Entry<String, Integer> entry : map.entrySet()) {
      if (entry.getValue() >= minSup) {
        Set<String> set2 = new HashSet<String>();
        set2.add(entry.getKey());
        frequentOneItemsets.add(set2);
      }
    }

    return frequentOneItemsets;
  }

  // gets candidates for frequent k-itemsets by joining each itemset with itself
  public static List<Set<String>> getCandidates(List<Set<String>> itemsets) {
    List<List<String>> list = new ArrayList<>();

    // sort each itemset and store them in a list
    for (int i = 0; i < itemsets.size(); i++) {
      List<String> l = new ArrayList<>(itemsets.get(i));
      Collections.sort(l);
      list.add(l);
    }

    List<Set<String>> candidates = new ArrayList<>();

    // get list of candidates by performing every possible merge
    for (int i = 0; i < list.size(); i++) {
      for (int j = i + 1; j < list.size(); j++) {
        // create the candidate through the merge and add it to the list of candidates
        HashSet<String> candidate = merge(list.get(i), list.get(j));

        if (candidate != null) {
          candidates.add(candidate);
        }
      }
    }

    return candidates;
  }

  // merges itemsets together
  public static HashSet<String> merge(List<String> items1, List<String> items2) {
    int n = items1.size();

    // does not perform the merge if the first (n-1) items are not common
    for (int i = 0; i < n - 1; i++) {
      if (!items1.get(i).equals(items2.get(i))) {
        return null;
      }
    }

    // does not merge if the two lists have all the same elements
    // to prevent duplicates
    if (items1.get(n - 1).equals(items2.get(n - 1))) {
      return null;
    }

    HashSet<String> candidate = new HashSet<>();

    // adds each item from the list(s) to the candidate set except the last item
    for (int i = 0; i < n - 1; i++) {
      candidate.add(items1.get(i));
    }

    // individually add the last items of both lists, since they are different
    candidate.add(items1.get(n-1));
    candidate.add(items2.get(n-1));

    return candidate;
  }

  // generates a list of subsets of the candidate k-itemset
  public static List<Set<String>> subset(List<Set<String>> candidates, Set<String> keyword) {
    List<Set<String>> subsets = new ArrayList<>();

    // adds the subset to the list if all of the words in the set
    // are in the keyword set
    for (Set<String> candidate : candidates) {
      if (keyword.containsAll(candidate)) {
        subsets.add(candidate);
      }
    }

    return subsets;
  }

  // creates a list of all frequent candidate itemsets
  public static List<Set<String>> getNextItemsets(List<Set<String>> candidates, Map<Set<String>, Integer> supportCounts, int minSup, int transactions) {
    List<Set<String>> result = new ArrayList<>();

    // if the support count of the itemset satisfies minSup, then it gets added
    for (Set<String> itemset : candidates) {
      if (supportCounts.containsKey(itemset)) {
        int supportCount = supportCounts.get(itemset);

        if (supportCount >= minSup) {
          result.add(itemset);
        }
      }
    }

    return result;
  }

  // extracts the frequent itemsets from the map of frequent k-itemsets
  // and places them in a new map with their support counts
  public static Map<Set<String>, Integer> extractFrequentItemsets(Map<Integer, List<Set<String>>> map, Map<Set<String>, Integer> supportCounts) {
    Map<Set<String>, Integer> result = new HashMap<>();

    for (List<Set<String>> itemsetList : map.values()) {
      for (Set<String> itemset : itemsetList) {
        result.put(itemset, supportCounts.get(itemset));
      }
    }

    return result;
  }

  // sorts a map in descending order of the values
  public static Map<Set<String>, Integer> sortByValue(final Map<Set<String>, Integer> wordCounts) {

        return wordCounts.entrySet()
                .stream()
                .sorted((Map.Entry.<Set<String>, Integer>comparingByValue().reversed()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

  //------------------------------------------------------------//
  // read and write methods
  //------------------------------------------------------------//

  // scans the data.csv file and stores the data as a list of lists, where
  // each list is a "transaction"
  public static List<List<String>> scanFile(String fileName) {
    List<List<String>> tweets = new ArrayList<>();
    try {
      Scanner sc = new Scanner(new File(fileName));
      while(sc.hasNextLine())
      {
        tweets.add(getRecordFromLine(sc.nextLine()));
      }
      sc.close();
    } catch (FileNotFoundException e) {
      System.out.println("File not found");
    }
    return tweets;
  }

  // for each line (transaction) of the dataset, adds its attributes to a list that will
  // be stored in the list of records
  private static List<String> getRecordFromLine(String line) {
    List<String> tweet = new ArrayList<String>();
    Scanner sc = new Scanner(line);
    sc.useDelimiter(",");
    while (sc.hasNext()) {
      tweet.add(sc.next());
    }
    return tweet;
  }

  // writes the list of frequent itemsets to the output file
  public static void writeOutput(Map<Set<String>, Integer> frequentItemsets, Map<Set<String>, Integer> supportCounts, String outputFile) {
    try {
      FileWriter writer = new FileWriter(outputFile);

      for(Set<String> itemset : frequentItemsets.keySet()) {
        for (String item : itemset) {
          writer.write(item + " ");
        }
        writer.write("(" + frequentItemsets.get(itemset) + ")"); // writes the corresponding support
        writer.write(System.lineSeparator());
      }

      writer.close();
    } catch (IOException e) {
      System.out.println("IOException");
    }
  }

}

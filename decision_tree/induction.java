/* THIS CODE IS MY OWN WORK, IT WAS WRITTEN WITHOUT CONSULTING CODE WRITTEN BY OTHER STUDENTS. Wendy Zhu */
// Collaboration statement: I utilized the pseudocode in the textbook to construct
// the algorithm, and also referred to the textbook for the gini value calculations.

import java.io.*;
import java.util.*;

public class induction {
  public static void main(String[] args) {
    DTNode root = generateDecisionTree("data.csv", "para2_file.txt", "para3_file.txt", "output.txt");

  }

  // creates the output file with class decisions for the test data
  public static void testDecisionTree(DTNode root, String dataset, String test, String output) {
    List<Integer> testIDs = getTrainingIDs(test);
    List<List<String>> testTuples = parseTrainingData(dataset, testIDs);
    List<String> attributes = generateAttributeList(dataset);

    // maps from patient ID to class decision
    Map<Integer, String> idToClass = new HashMap<>();

    for (List<String> tuple : testTuples) {
      DTNode p = root;
      String att = p.attribute;

      // traverse the tree for each tuple
      while (p.children.size() > 0) {
          int a = attributes.indexOf(p.attribute);

          // if attribute is numerical
          if (isDouble(tuple.get(a))) {
            double d = Double.parseDouble(tuple.get(a));
            if (d <= p.numericalSplit) {
              p = p.children.get("le");
              att = p.attribute;
            } else {
              p = p.children.get("gr");

              att = p.attribute;
            }
          } else {
            // categorical attribute
            String val = tuple.get(a);
            p = p.children.get(val);
            att = p.attribute;
          }

      }
      // add the id and corresponding class label to the map
      idToClass.put(Integer.parseInt(tuple.get(0)), att);
    }
    writeOutput(idToClass, output);
  }

  // write results to the output file
  public static void writeOutput(Map<Integer, String> classifiedData, String outputFile) {
    try {
      FileWriter writer = new FileWriter(outputFile);

      for (Map.Entry x : classifiedData.entrySet()) {
        int id = (int)x.getKey();
        String c = (String)x.getValue();
        writer.write(id + " " + c);
        writer.write(System.lineSeparator());
      }
      writer.close();
    } catch (IOException e) {
      System.out.println("IOException");
    }
  }

  public static DTNode generateDecisionTree(String dataset, String training, String test, String output) {
    List<Integer> trainingIDs = getTrainingIDs(training);
    List<List<String>> trainingTuples = parseTrainingData(dataset, trainingIDs);
    List<String> attributes = generateAttributeList(dataset);

    DTNode root = generateDecisionTreeRecursive(trainingTuples, attributes);

    // test the decision tree that was jut generated
    testDecisionTree(root, dataset, test, output);

    return root;
  }

  public static DTNode generateDecisionTreeRecursive(List<List<String>> trainingTuples, List<String> attributes) {
    // gets number of attributes in the training tuples
    int n = trainingTuples.get(0).size();

    // returns a leaf node if all tuples are in the same class
    if (sameClass(trainingTuples)) {
      return new DTNode(trainingTuples.get(0).get(n-1));
    }

    // if there are no more attributes remaining (besides "has heart disease" and id), return a lear node of the
    // majority class
    if (attributes.size() <= 2) {
      return new DTNode(majorityClass(trainingTuples));
    }

    // generate partition using gini index
    DTNode root = gini(trainingTuples, attributes);

    int idx = attributes.indexOf(root.attribute);

    List<String> list = new ArrayList<>(); // list of values for chosen attribute
    for (int k = 0; k < trainingTuples.size(); k++) {
      list.add(trainingTuples.get(k).get(idx));
      trainingTuples.get(k).remove(idx);
    }

    Map<String, DTNode> map = root.children;
    // get the tuples in the current partition
    for (Map.Entry branch : map.entrySet()) {
        String key = (String)branch.getKey();
        List<List<String>> Dj = new ArrayList<>();

        // numerical "<= split point" branch
        if (key.equals("le")) {
          for (int i = 0; i < trainingTuples.size(); i++) {
            if (Double.parseDouble(list.get(i)) <= root.numericalSplit) {
              Dj.add(trainingTuples.get(i));
            }
          }
        // numerical "> split point" branch
        } else if (key.equals("gr")) {
          for (int i = 0; i < trainingTuples.size(); i++) {
            if (Double.parseDouble(list.get(i)) > root.numericalSplit) {
              Dj.add(trainingTuples.get(i));
            }
          }

        } else { // if attribute is categorical
          for (int i = 0; i < trainingTuples.size(); i++) {
            if (list.get(i).equals(key)) {
              Dj.add(trainingTuples.get(i));
            }
          }
        }

        // if no tuples in partition
        if (Dj.size() == 0) {
          DTNode c = new DTNode(majorityClass(trainingTuples));
          root.children.put(key, c);
        } else {
          // recursively generate child node
          List<String> attributesCopy = new ArrayList<>();
          for (int i = 0; i < attributes.size(); i++) {
            if (i != idx) {
              attributesCopy.add(attributes.get(i));
            }
          }
          root.children.put(key, generateDecisionTreeRecursive(Dj, attributesCopy));
        }
    }
    return root;
  }

  public static DTNode gini(List<List<String>> trainingTuples, List<String> attributes) {
    // map to store gini values of each potential splitting attribute
    Map<String, Double> giniValues = new HashMap<>();
    // map to store split points for the numerical attributes
    Map<String, Double> splitPoints = new HashMap<>();

    // calculate gini index for each attribute
    for (int i = 1; i < attributes.size()-1; i++) {

      String copy = trainingTuples.get(0).get(i);

      // evaluate min gini index if attribute is numerical
      if (isDouble(copy)) {
        // list of objects with just their attribute value and their class
        List<List<String>> valuesToClasses = new ArrayList<>();

        // generates the valuesToClasses list
        for (int j = 0; j < trainingTuples.size(); j++) {
          List<String> valToClass = new ArrayList<>();
          valToClass.add(trainingTuples.get(j).get(i));
          valToClass.add(trainingTuples.get(j).get(trainingTuples.get(j).size()-1));
          valuesToClasses.add(valToClass);
        }

        valuesToClasses.sort((o1, o2) -> Double.compare(Double.parseDouble(o1.get(0)), Double.parseDouble(o2.get(0))));

        double min = -1; // find min gini index of all split points
        double minSplitPointD = 0; // find value of split point with min gini index

        // calculate gini index for each possible split point (midpoints of adjacent sorted values)
        for (int k = 0; k < valuesToClasses.size()-1; k++) {
          double val1 = Double.parseDouble(valuesToClasses.get(k).get(0));
          double val2 = Double.parseDouble(valuesToClasses.get(k+1).get(0));
          double mid = (val1 + val2) / 2.0;

          int d1 = 0;
          int d2 = 0;

          int c1d1 = 0; // objects in d1 belonging to "yes" cluster
          int c1d2 = 0; // objects in d2 belonging to "yes cluster"

          int c2d1 = 0; // objects in d1 belonging to "no" cluster
          int c2d2 = 0; // objects in d2 belonging to "no" cluster

          int n = valuesToClasses.get(0).size();

          for (int j = 0; j < valuesToClasses.size(); j++) {
            if (Double.parseDouble(valuesToClasses.get(j).get(0)) <= mid) {
              d1++;
              if (valuesToClasses.get(j).get(n-1).equals("Yes")) {
                c1d1++;
              } else {
                c2d1++;
              }
            } else {
              d2++;
              if (valuesToClasses.get(j).get(n-1).equals("Yes")) {
                c1d2++;
              } else {
                c2d2++;
              }
            }
          }

          double a = (double)d1 / (double)valuesToClasses.size();
          double b = (double)d2 / (double)valuesToClasses.size();

          double giniD1 = 1;
          double giniD2 = 1;

          if (d1 != 0) {
            giniD1 -= (Math.pow((double)c1d1 / (double)d1, 2) + Math.pow((double)c2d1 / (double)d1, 2));
          } else {
            giniD1 = 0;
          }
          if (d2 != 0) {
            giniD2 -= (Math.pow((double)c1d2 / (double)d2, 2) + Math.pow((double)c2d2 / (double)d2, 2));
          } else {
            giniD2 = 0;
          }

          double giniValue = a * giniD1 + b * giniD2;

          if (giniValue > min) {
            min = giniValue;
            minSplitPointD = mid;
          }
        }
        // add split point to map
        splitPoints.put(attributes.get(i), minSplitPointD);
        // store the min gini value
        giniValues.put(attributes.get(i), min);
      } else {
        int s = 0;
        List<String> splits = new ArrayList<>(); // store list of categories
        for (int j = 0; j < trainingTuples.size(); j++) {
          if (!splits.contains(trainingTuples.get(j).get(i))) {
            s++;
            splits.add(trainingTuples.get(j).get(i));
          }
        }

        // creates list of just each data object's attribute value and class assignment
        List<List<String>> valuesToClasses = new ArrayList<>();

        // generates the valuesToClasses list
        for (int j = 0; j < trainingTuples.size(); j++) {
          List<String> valToClass = new ArrayList<>();
          valToClass.add(trainingTuples.get(j).get(i));
          valToClass.add(trainingTuples.get(j).get(trainingTuples.get(j).size()-1));
          valuesToClasses.add(valToClass);
        }

        // calculate gini index
        double giniVal = 0;

        for (int j = 0; j < s; j++) {
          double giniD = 1;
          // double pSum = 0;
          String val = splits.get(j);
          for (int k = 0; k < trainingTuples.size(); k++) {
            int d = 0;
            double p = 0;
            double c1 = 0;
            double c2 = 0;
            if (trainingTuples.get(k).get(i).equals(val)) {
              d++;
              if (trainingTuples.get(k).get(trainingTuples.get(k).size()-1).equals("Yes")) {
                c1++;
              } else {
                c2++;
              }
            }
            p = Math.pow(c1 / d, 2) + Math.pow(c2 / d, 2);
            giniD -= p;
          }
          giniVal += giniD;
        }
        // put the gini value in the map, with its key being the attribute
        giniValues.put(attributes.get(i), giniVal);
      }
    }
    // sort map of gini values in ascending order
    Map<String, Double> giniValuesSorted = sortByValue(giniValues);

    DTNode node = new DTNode();
    // get the attribute with the minimum gini value
    String minGiniAttribute = (String)giniValuesSorted.keySet().toArray()[0];
    // assign the node's attribute to the attribute with the minimum gini value
    node.attribute = minGiniAttribute;

    int x = attributes.indexOf(minGiniAttribute);

    // if attribute with lowest gini index is numerical
    if (splitPoints.containsKey(giniValuesSorted.keySet().toArray()[0])) {
      // create 2 branches based on the split point
      node.numericalSplit = splitPoints.get(giniValuesSorted.keySet().toArray()[0]);
      node.children.put("le", new DTNode());
      node.children.put("gr", new DTNode());
    } else { // if attribute with lowest gini index is categorical
      // put each branch of the node into the map, along with a new node
      if (node.attribute.equals("gender")) {
        node.children.put("M", new DTNode());
        node.children.put("F", new DTNode());
      } else if (node.attribute.equals("chest pain type")) {
        node.children.put("Type 0", new DTNode());
        node.children.put("Type 1", new DTNode());
        node.children.put("Type 2", new DTNode());
        node.children.put("Type 3", new DTNode());
      } else if (node.attribute.equals("fasting blood sugar > 120 mg/dl") || node.attribute.equals("exercise induced angina")) {
        node.children.put("Yes", new DTNode());
        node.children.put("No", new DTNode());
      } else if (node.attribute.equals("resting electrocardiographic results")) {
        node.children.put("hypertrophy of heart", new DTNode());
        node.children.put("myocardial infarction", new DTNode());
        node.children.put("ischemia", new DTNode());
      }
    }
    return node;
  }

  // sort the elements of a map according to the values
  public static Map<String, Double> sortByValue(Map<String, Double> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double>> list =
               new LinkedList<Map.Entry<String, Double>>(hm.entrySet());

        list.sort((o1, o2) -> Double.compare((double)o1.getValue(), (double)o2.getValue()));

        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }

  // returns true if the value in the input string can be parsed to a double
  public static boolean isDouble(String value) {
    boolean d = false;
    try {
      Double.parseDouble(value);
      d = true;
    } catch (NumberFormatException e) {

    }
    return d;
  }

  // returns true if all the tuples are in the same class
  public static boolean sameClass(List<List<String>> tuples) {
    String c = tuples.get(0).get(tuples.get(0).size()-1);
    for (int i = 1; i < tuples.size(); i++) {
      if (!tuples.get(i).get(tuples.get(i).size()-1).equals(c)) {
        return false;
      }
    }
    return true;
  }

  // find the majority class of the remaining training tuples
  public static String majorityClass(List<List<String>> trainingTuples) {
    int y = 0;
    int n = 0;
    for (List<String> tuple : trainingTuples) {
      if (tuple.get(tuple.size()-1).equals("Yes")) {
        y++;
      } else if (tuple.get(tuple.size()-1).equals("No")) {
        n++;
      }
    }
    if (y > n) {
      return "Yes";
    } else {
      return "No";
    }
  }

  // get the list of the attributes describing the data
  public static List<String> generateAttributeList(String dataset) {
    List<String> attributes = new ArrayList<>();
    try {
      Scanner sc = new Scanner(new File(dataset));
      attributes = getRecordFromLine(sc.nextLine());
      sc.close();
    } catch (FileNotFoundException e) {
      System.out.println("File not found");
    }
    return attributes;
  }

  // make an array containing the IDs of patient data in the training set
  public static List<Integer> getTrainingIDs(String training) {
    List<Integer> ids = new ArrayList<>();
    try {
      Scanner sc = new Scanner(new File(training));
      while(sc.hasNextInt())
      {
        ids.add(sc.nextInt());
      }
      sc.close();
    } catch (FileNotFoundException e) {
      System.out.println("File not found");
    }
    return ids;
  }

  // get the list of training data objects into arraylist form
  public static List<List<String>> parseTrainingData(String dataset, List<Integer> trainingIDs) {
    List<List<String>> trainingData = new ArrayList<>();
    int c = 0;
    try {
      Scanner sc = new Scanner(new File(dataset));
      while(sc.hasNextLine())
      {
        List<String> line = getRecordFromLine(sc.nextLine());
        if (c > 0 && trainingIDs.contains(Integer.parseInt(line.get(0)))) {
          trainingData.add(line);
        }
        c++;
      }
      sc.close();
    } catch (FileNotFoundException e) {
      System.out.println("File not found");
    }
    return trainingData;
  }

  // for a single line (transaction) of the dataset, adds its attributes to a list that will
  // be stored in the list of records
  private static List<String> getRecordFromLine(String line) {
    List<String> dataLine = new ArrayList<String>();
    Scanner sc = new Scanner(line);
    sc.useDelimiter(",");
    while (sc.hasNext()) {
      dataLine.add(sc.next());
    }
    return dataLine;
  }
}

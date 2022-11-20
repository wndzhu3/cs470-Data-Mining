/* THIS CODE IS MY OWN WORK, IT WAS WRITTEN WITHOUT CONSULTING CODE WRITTEN BY OTHER STUDENTS. Wendy Zhu */

import java.util.*;

public class DTNode {
  String attribute; // attribute being used to split data

  // if attribute is numerical, the split is a number
  double numericalSplit;

  // map of branching paths to children
  HashMap<String, DTNode> children;

  public DTNode() {
    attribute = null;
    children = new HashMap<>();
  }

  // node with
  public DTNode(String attribute) {
    this.attribute = attribute;
    children = new HashMap<>();
  }

  public DTNode(String attribute, double split) {
    this.attribute = attribute;
    numericalSplit = split;
    children = new HashMap<>();
  }
}

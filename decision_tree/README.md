# Decision Tree Algorithm

## Description and Usage

To create the decision tree and create the output file containing results from the test data, run the generateDecisionTree method in the main method, which returns a decision tree node (DTNode). For example, in the main method you should run DTNode root = generateDecisionTree(parameter 1, parameter 2, parameter 3, parameter 4).

The input parameters are (1) path to the dataset CSV file, (2) path to the list of training data IDs, (3) path to the list of test data IDs, and (4) path to the output file.

I decided to do binary splits for numerical attributes (one split for <= split point, one split for > split point), but multiway splits for categorical attributes (with number of splits = number of distinct attribute values). To split numerical attributes, I sorted the attribute values, then considered the midpoint between each set of adjacent points as a possible split point by calculating the gini index, then chose the point that had the minimum gini value as the split point. For the categorical attributes, I decided to do multiway instead of binary splits because it shortened the decision tree induction process by allowing all different values of a categorical attribute to be tested at one node, instead of having to split up an attribute into multiple nodes (e.g. “chest pain type = Type 1?” then “chest pain type = Type 2?” and so on).

To measure impurity, I chose the gini measure because, as opposed to entropy, it doesn't require calculation with logarithms, which would have been more computationally intensive.  I chose not to use misclassification error either, because the induction algorithm is greedy, so trying to maximize classification accuracy at each step may not end up selecting the accuracy-maximizing classifier overall; therefore, I thought that gini would be the best measure to use for this project.

I created my own custom node to generate the decision tree called DTNode, which contains a String attribute (or class decision if it is a leaf node), a double split point value (for if the splitting attribute is numerical), and a hashmap the maps from the name of the partition leading to it (e.g. “gender = M”) to the child DTNode whose attribute is the next splitting attribute from that path (e.g. “age”).

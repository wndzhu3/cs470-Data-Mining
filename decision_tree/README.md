# Decision Tree Algorithm

## Algorithm and Usage

To create the decision tree and create the output file containing results from the test data, run the generateDecisionTree method in the main method, which returns a decision tree node (DTNode). For example, in the main method you should run DTNode root = generateDecisionTree(parameter 1, parameter 2, parameter 3, parameter 4).

The input parameters are (1) path to the dataset CSV file, (2) path to the list of training data IDs, (3) path to the list of test data IDs, and (4) path to the output file.

I decided to do binary splits for numerical attributes (one split for <= split point, one split for > split point), but multiway splits for categorical attributes (with number of splits = number of distinct attribute values). To split numerical attributes, I sorted the attribute values, then considered the midpoint between each set of adjacent points as a possible split point by calculating the gini index, then chose the point that had the minimum gini value as the split point. For the categorical attributes, I decided to do multiway instead of binary splits because it shortened the decision tree induction process by allowing all different values of a categorical attribute to be tested at one node, instead of having to split up an attribute into multiple nodes (e.g. “chest pain type = Type 1?” then “chest pain type = Type 2?” and so on).

To measure impurity, I chose the gini measure because, as opposed to entropy, it doesn't require calculation with logarithms, which would have been more computationally intensive.  I chose not to use misclassification error either, because the induction algorithm is greedy, so trying to maximize classification accuracy at each step may not end up selecting the accuracy-maximizing classifier overall; therefore, I thought that gini would be the best measure to use for this project.

I created my own custom node to generate the decision tree called DTNode, which contains a String attribute (or class decision if it is a leaf node), a double split point value (for if the splitting attribute is numerical), and a hashmap the maps from the name of the partition leading to it (e.g. “gender = M”) to the child DTNode whose attribute is the next splitting attribute from that path (e.g. “age”).

## Results

I used a 4-fold cross validation method to evaluate my model. I got the following metrics:
- accuracy: 0.713
- precision: 0.749
- recall or sensitivity: 0.731
- F score: 0.733
- specificity: 0.698

Overall, they were all somewhat similar in value, hovering around the 70-75% range, implying that my algorithm doesn't seem to have a large "bias" in the number of false positives it identifies versus false negatives, and vice versa. However, because the specificity value was smallest, it's possible that my model has a tendency to identify false positives. In general, I would prefer my metrics to be higher, since predicting heart disease is an important task with large consequences in the real world. Nonetheless, this assignment helped me understand the importance of building good classification models, as well as the importance of testing these models, because evaluating the quality of my induction tree really helped me understand just how effective (or not effective) it was, and if I were to continue improving it in the future, these numbers would be very useful in helping guide the adjustments I could make to the algorithm.

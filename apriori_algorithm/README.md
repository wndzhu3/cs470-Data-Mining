# Apriori Algorithm

## Description

Using Java, I implemented the apriori algorithm to perform frequent itemset mining on a dataset of tweets from 2014 related to the topic of flu shots. 

The program is executable with 3 parameters: the name of the input dataset file, the threshold of minimum support count, and the name of the output file. The minimum support count is an integer. An itemset is frequent if its support count is greater than or equal to this threshold. 

The `output.txt` file contains all the frequent itemsets together with the minimum support count. For example, “home flu shot (530)” represents an itemset containing items home, flu, shot with a support count of 530. The lines of itemsets are ranked in descending order of their support counts.

## Algorithm

I divided up the algorithm into multiple different methods: 3 methods to scan the input file and write the output file, 1 method to sort the itemsets in descending order of their support counts, and 7 methods for the apriori algorithm itself. I created separate methods to find the set of frequent 1-itemsets, then to generate frequent k-itemset candidates from the (k − 1)-itemsets, to generate subsets from the candidates, and then to prune candidates by extracting the frequent itemsets. I utilized HashMaps to keep track of the sets of frequent k-itemsets and the support counts of each itemset.

For my candidate generation method ( getCandidates() ), I utilized the Fk−1 × Fk−1 method. In this method, I sorted each frequent set of keywords (of length k − 1) in alphabetical order and then merged them together if their first (k − 2) items were the identical. I made sure to check that the sets were not completely identical before merging to avoid duplicates.

## Results

In the set of frequent itemsets generated, "flu", "shot", and "flu shot" were the top keyword groups, confirming that the topic of the tweets in the dataset were related to flu shots. Additionally, variations of "get flu shot" and "getting flu shot" were among the most popular word sets, revealing that the tweets may have frequently described the Twitter users' experiences or thoughts about receiving the flu shot. There were also many groups including "flu" and "shot" along with a description of time, such as "today," "time," "year," and "last." This suggests that many tweets also described when the authors may have received their flu shots or when they intended to receive it. Because "year" was among the frequent keywords, it's probable that a good number of tweets referenced the yearly nature of getting flu shots or the act of getting this year's shot.

A final interesting characteristic of the set of frequent itemsets is the co-occurrence of "sore" and "sick" with "flu shot." These could point to Twitter users' fears of getting the flu and/or facing negative symptoms due to getting the flu shot. For instance, since many people feel soreness in their bodies and sometimes even flu symptoms after receiving the shot, this may be an area of concern that people often talk about. This observation could be useful in an analysis about people's attitudes toward the getting flu shot––including why they decide to get it and feel about the symptoms afterward. This analysis could provide insight into vaccine distribution and general attitudes toward the vaccine.

## Usage

To run the program, execute the following commands:

	`javac apriori.java`
  
	`java apriori`
  
The default minimum support is set to 600. To change the minimum support or edit the names of the input/output file parameters, adjust the call to apriori(inputFile, minSup, outputFile) in the main method of apriori.java. No other environments or packages are needed to run this program.

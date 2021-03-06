# Name Entity Recognition (NER) with Hidden Markov Model

This is the second course project in Cornell CS4740 Natural Language Process course. For this project, we use a hidden markov model to predict the NER of each work in a document. Our final model with HMM reached about 0.75 F1-Score, and our project achieved top 18% in the course's Kaggle competition.

## Run
In root folder of the project, run:
```
javac -d . src/*.java
java HMM [training file path] [testing file path]
```

For Example
```
javac -d . src/*.java
java HMM ./data/train.txt ./data/test.txt
```
Command above assign "./data/train.txt" as training data and "./data/test.txt" as testing data, and finally output the result as a
csv file named "test_result.csv" in the root folder

If you don't specify the [training file path] [testing file path], program runs with default values (trainingPath: "./data/train.txt", testingPath: "./data/test.txt").

The program also owns the function to divide training data into training and validating sets with custom ratio, and the function to 
validate the validation set and output specific precision/recall/F1 scores for each label and overall mean precision/recall/F1 scores, 
but these functions are only used in development phase.





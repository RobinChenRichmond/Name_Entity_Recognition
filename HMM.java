import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;

/**
 * The HMM Model class reads a training file and store all the data into maps and matrix.
 * After trained, it could also test a file by predicting the labels of words in the test file.
 * Finally, it will output the test results as a csv file named "test_result.csv"
 * 
 * @author dongdong Yu, guanyu Chen, yanfei Yu
 *
 */
public class HMM {
	public LabelMap labelMap;
	public String fileName;
	public HashMap<String,Integer> labelIndex;
	public String[] labelList;
	public double[][] labelMatrix;
	
	
	/**
	 * Initialize the constructor with a training file's path
	 * @param fileName path of the file to be trained by the model
	 */
	public HMM(String fileName) {
		this.fileName = fileName;
		
		// Initialize different data structures that help store and fetch training data
		labelMap = new LabelMap();
		labelList = new String[]{"B-PER","I-PER","B-LOC","I-LOC","B-ORG","I-ORG","B-MISC","I-MISC","O"};
		labelIndex = new HashMap<String,Integer>();
		labelIndex.put("B-PER", 0);
		labelIndex.put("I-PER", 1);
		labelIndex.put("B-LOC", 2);
		labelIndex.put("I-LOC", 3);
		labelIndex.put("B-ORG", 4);
		labelIndex.put("I-ORG", 5);
		labelIndex.put("B-MISC", 6);
		labelIndex.put("I-MISC", 7);
		labelIndex.put("O", 8);
		labelIndex.put("startToken", 9);
		labelMatrix = new double[10][11];
		
		readFile();
	}
	
	/**
	 * Read the training file by iterating all it's words, then store the appearance of words into maps
	 * and matrix, and finally compute the probability for later use 
	 */
	public void readFile() {
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileName)));){
			System.out.println("start training: "+fileName);
			String line;
			while ((line = br.readLine()) != null) {
				String[] words = line.split("\t");
				line = br.readLine();
				String[] tags = line.split("\t");
				line = br.readLine();
				String[] labels = line.split("\t");
				
				// use bigram to store the appearance of labels related to other labels into the labelMatrix
				for(int index = 0; index<labels.length; index++){
					if(index==0){
						labelMatrix[labelIndex.get("startToken")][labelIndex.get(labels[index])]++;
						labelMatrix[labelIndex.get("startToken")][10]++;
					} else{
						labelMatrix[labelIndex.get(labels[index-1])][labelIndex.get(labels[index])]++;
						labelMatrix[labelIndex.get(labels[index-1])][10]++;
					}
				}
				
				// pre-processing: lowercase words and combine them with POS tags
				for(int i = 0; i < words.length; i++){
					words[i] = words[i].toLowerCase()+" "+tags[i];
				}
				
				// store the appearance of words related to labels into the labelMap
				for (int i = 0; i < words.length; i ++) {
					labelMap.getLabelDist().get(labels[i]).put(words[i],labelMap.getLabelDist().get(labels[i]).getOrDefault(words[i], 0)+1);
					labelMap.getCountMap().put(labels[i],labelMap.getCountMap().get(labels[i])+1);
				}
			}

			// normalization the labelMatrix, convert the number of appearance into probability
			for(int i = 0; i < 10; i++){
				for(int j = 0; j < 10; j++){
					labelMatrix[i][j] = labelMatrix[i][j]/labelMatrix[i][10];
				}
			}
			System.out.println("finish training");
		}
		catch(Exception e) {
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * This is a helper method that divide the training file into 90% training data and 
	 * 10% validation data. We could set custom ratio between training data and validation data
	 */
	public void createValidationFile() {
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(fileName)));){
			PrintWriter pw1 = new PrintWriter(new File("new_train.txt"));
			PrintWriter pw2 = new PrintWriter(new File("new_validate.txt"));
			System.out.println("start dividing: "+fileName);
			int count = 1;
			String line;
			String line2;
			String line3;
			
			while ((line = br.readLine()) != null) {
				line2 = br.readLine();
				line3 = br.readLine();
				
				// This part will output new_train.txt and new_validate.txt with 90% and 10% of training data
				if(count%10!=0){
					pw1.write(line+"\n");
					pw1.write(line2+"\n");
					pw1.write(line3+"\n");
				} else{
					pw2.write(line+"\n");
					pw2.write(line2+"\n");
					pw2.write(line3+"\n");
				}
				count++;
			}
			pw1.close();
			pw2.close();
			System.out.println("finish dividing");
		}
		catch(Exception e) {
			System.out.print(e.getMessage());
		}
	}	
	
	/**
	 * Test the testing file by applying Viterbi algorithm and then output the result
	 * as a csv file
	 * 
	 * @param testFile path of the file to be tested by the model
	 */
	public void test(String testFile) {
		try (BufferedReader br = new BufferedReader(
				new InputStreamReader(new FileInputStream(testFile)));){
			
			try {
				// initialize the output csv file format
				PrintWriter pw = new PrintWriter(new File("test_result.csv"));
				StringBuilder head = new StringBuilder();
				head.append("Type,Prediction");
				StringBuilder per = new StringBuilder();
				per.append("PER,");
				StringBuilder loc = new StringBuilder();
				loc.append("LOC,");
				StringBuilder org = new StringBuilder();
				org.append("ORG,");
				StringBuilder misc = new StringBuilder();
				misc.append("MISC,");
				HashMap<String, StringBuilder> sbs = new HashMap<>();
				sbs.put("LOC", loc);
				sbs.put("PER", per);
				sbs.put("ORG", org);
				sbs.put("MISC", misc);

				String line;
				System.out.println("start testing: "+testFile);
				while ((line = br.readLine()) != null) {

					String[] words = line.split("\t");
					line = br.readLine();
					String[] tags = line.split("\t");
					line = br.readLine();
					String[] indexes = line.split(" ");
					
					// lowercase words and combine them with tags
					for(int i = 0; i < words.length; i++){
						words[i] = words[i].toLowerCase()+" "+tags[i];
					}

					// Viterbi Algorithm begins:
					double[][] score = new double[10][words.length];
					int[][] bptr = new int[10][words.length];
					
					for(int i = 0; i < 9; i++){
						score[i][0] = labelMatrix[labelIndex.get("startToken")][i]*labelMap.countP(words[0], labelList[i]);
						bptr[i][0] = 0;
					}
					for(int j = 1; j < words.length; j++){
						for(int k = 0; k < 9; k++){
							double max = 0;
							int index = -1;
							for(int l = 0; l < 9; l++){
								double temp = score[l][j-1]*labelMatrix[l][k]*labelMap.countP(words[j], labelList[k]);
								if(temp>=max){
									max = temp;
									index = l;
								}
							}
							score[k][j] = max;
							bptr[k][j] = index;
						}
					}
					
					// find the index of the final highest score row
					double maxScore = 0;
					int finalI = -1;
					for(int p = 0; p < 9; p++){
						if(score[p][words.length-1]>=maxScore){
							maxScore = score[p][words.length-1];
							finalI = p;
						}
					}
					
					// backtrack the matrix and use results[] to store the most possible labels for the each word
					String[] results = new String[words.length];
					results[words.length-1] = labelList[finalI];
					for(int q = words.length-2; q>=0; q--){
						results[q] = labelList[bptr[labelIndex.get(results[q+1])][q+1]];
					}
					
					// formatting the results' labels by removing 'B-' and 'I-'
					for(int m = 0; m < words.length; m++){
						if(!results[m].equals("O")){
							results[m] = results[m].substring(2);
						}
					}
					
					// put final result into HashMap for outputting to the csv
					for(int n = 0; n < results.length; n++){
						if(!results[n].equals("O")){
							String temp = results[n];
							String startIndex = indexes[n];
							while((n+1)<results.length && results[n+1].equals(temp)){
								n=n+1;
							}
							String endIndex = indexes[n];
							sbs.get(temp).append(startIndex+"-"+endIndex+" ");
						}
					}
					
				}
				System.out.println("finish testing");
				pw.write(head.toString() + "\n");
				pw.write(per.toString() + "\n");
				pw.write(loc.toString() + "\n");
				pw.write(org.toString()+ "\n");
				pw.write(misc.toString() + "\n");
				
				
				pw.close();
			}
			catch(Exception e) {
				System.out.println(e.getMessage());
			}
		}
		catch(Exception e) {
			System.out.print(e.getMessage());
		}
	}
	
	/**
	 * Validate a file by comparing the predicted labels with the right answers.
	 * Notice: the validation file should be in the same format as training file, not testing file
	 * Notice: it only print the right and wrong numbers in word level
	 * 
	 * @param validationFile path of the file to be validated by the model
	 */
	public void validate(String validationFile) {
		double precision = 0;
		double recall = 0;
		double F1 = 0;
		String[] labels = new String[]{"PER","LOC","ORG","MISC"};
		System.out.println("start validating: "+validationFile);
		for(int s = 0; s < 4; s++){
			// set current positive label and initialize different categories of prediction results
			String currLabel = labels[s];
			int truePos = 0;
			int trueNeg = 0;
			int falsePos = 0;
			int falseNeg = 0;
			
			try (BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(validationFile)));){
				
				String line;
				while ((line = br.readLine()) != null) {

					String[] words = line.split("\t");
					line = br.readLine();
					String[] tags = line.split("\t");
					line = br.readLine();
					String[] answers = line.split("\t");
					
					// lowercase words and combine them with tags
					for(int i = 0; i < words.length; i++){
						words[i] = words[i].toLowerCase()+" "+tags[i];
					}

					// Viterbi Algorithm begins:
					double[][] score = new double[10][words.length];
					int[][] bptr = new int[10][words.length];

					for(int i = 0; i < 9; i++){
						score[i][0] = labelMatrix[labelIndex.get("startToken")][i]*labelMap.countP(words[0], labelList[i]);
						bptr[i][0] = 0;
					}
					for(int j = 1; j < words.length; j++){
						for(int k = 0; k < 9; k++){
							double max = 0;
							int index = -1;
							for(int l = 0; l < 9; l++){
								double temp = labelMatrix[l][k]*score[l][j-1]*labelMap.countP(words[j], labelList[k]);
								if(temp>=max){
									max = temp;
									index = l;
								}
							}
							score[k][j] = max;
							bptr[k][j] = index;
						}
					}
					double maxScore = 0;
					int finalI = -1;
					for(int p = 0; p < 9; p++){
						if(score[p][words.length-1]>=maxScore){
							maxScore = score[p][words.length-1];
							finalI = p;
						}
					}
					
					// results[] stores the labels for the current line's words
					String[] results = new String[words.length];
					results[words.length-1] = labelList[finalI];
					for(int q = words.length-2; q>=0; q--){
						results[q] = labelList[bptr[labelIndex.get(results[q+1])][q+1]];
					}
					
					for(int index = 0; index < results.length; index++){
						if(!results[index].equals("O")){
							results[index] = results[index].substring(2);
						}
						if(!answers[index].equals("O")){
							answers[index] = answers[index].substring(2);
						}
					}
					
					// Validation: compare the predictions with answers
					for(int i = 0; i < results.length; i++){
						if(results[i].equals(currLabel)){
							if(currLabel.equals(answers[i])){
								truePos++;
							} else{
								falsePos++;
							}
						} else{
							if(!currLabel.equals(answers[i])){
								trueNeg++;
							} else{
								falseNeg++;
							}
						}
					}
				}
				// calculate the precision/recall/F1 score, and print the result
				double tempPrecision = (double)truePos/(truePos+falsePos);
				double tempRecall = (double)truePos/(truePos+falseNeg);
				double tempF1 = tempPrecision*tempRecall*2/(tempPrecision+tempRecall);
				precision+=tempPrecision;
				recall+=tempRecall;
				F1+= tempF1;
				System.out.println("Current Positive Label: "+ currLabel);
				System.out.println("Precision: "+tempPrecision+" Recall: "+tempRecall+" F1 Score: "+tempF1);
			}
			catch(Exception e) {
				System.out.print(e.getMessage());
			}
		}
		// print the overall result for precision/recall/F1 score.
		System.out.println("finish validating");
		System.out.println("Mean Precision: "+precision/4);
		System.out.println("Mean Recall: "+recall/4);
		System.out.println("Mean F1 Score: "+F1/4);
		
	}
	

	public static void main(String[] args) {
		String training = args.length>0?args[0]:"./data/train.txt";
		String testing = args.length>1?args[1]:"./data/test.txt";
		String validate = "./data/new_validate.txt";
		
		HMM model = new HMM(training);
		model.validate(testing);

	}

}

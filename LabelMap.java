import java.util.HashMap;

/**
 * This class store the number of appearance of words labeled in each label.
 * It uses labelDist to show the distribution of number of apperance of different words 
 * for different labels, and it also uses countMap to record the total number of words appeared in each 
 * label
 * 
 * @author Guanyu Chen, Dongdong Yu, Yanfei Yu
 *
 */
public class LabelMap {
	private HashMap<String, HashMap<String,Integer>> labelDist;
	private HashMap<String, Integer> countMap;
	private double kValue = 0.005;
	
	/**
	 * The constructor initialize the labelDist and countMap
	 */
	public LabelMap(){
		labelDist = new HashMap<String, HashMap<String,Integer>>();
		labelDist.put("B-PER", new HashMap<String,Integer>());
		labelDist.put("I-PER", new HashMap<String,Integer>());
		labelDist.put("B-LOC", new HashMap<String,Integer>());
		labelDist.put("I-LOC", new HashMap<String,Integer>());
		labelDist.put("B-ORG", new HashMap<String,Integer>());
		labelDist.put("I-ORG", new HashMap<String,Integer>());
		labelDist.put("B-MISC", new HashMap<String,Integer>());
		labelDist.put("I-MISC", new HashMap<String,Integer>());
		labelDist.put("O", new HashMap<String,Integer>());
		
		countMap = new HashMap<String, Integer>();
		countMap.put("B-PER", 0);
		countMap.put("I-PER", 0);
		countMap.put("B-LOC", 0);
		countMap.put("I-LOC", 0);
		countMap.put("B-ORG", 0);
		countMap.put("I-ORG", 0);
		countMap.put("B-MISC", 0);
		countMap.put("I-MISC", 0);
		countMap.put("O", 0);
	}
	
	/**
	 * get the labelDist
	 * @return labelDist
	 */
	public HashMap<String, HashMap<String,Integer>> getLabelDist(){
		return labelDist;
	}
	
	/**
	 * get the countMap
	 * @return countMap
	 */
	public HashMap<String, Integer> getCountMap(){
		return countMap;
	}
	
	/**
	 * count the probability P(word | label) with given data stored in the maps and also apply
	 * the add-k smoothing
	 * @param word the input word
	 * @param label the input label
	 * @return the probability
	 */
	public double countP(String word, String label){
		if(!labelDist.get(label).containsKey(word)){
			return (double)kValue/(countMap.get(label)+kValue*labelDist.get(label).size());
		}
		return (double)(labelDist.get(label).get(word)+kValue)/(countMap.get(label)+kValue*labelDist.get(label).size());
	}
}

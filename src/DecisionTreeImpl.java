import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Fill in the implementation details of the class DecisionTree using this file. Any methods or
 * secondary classes that you want are fine but we will only interact with those methods in the
 * DecisionTree framework.
 * 
 * You must add code for the 1 member and 4 methods specified below.
 * 
 * See DecisionTree for a description of default methods.
 */
public class DecisionTreeImpl extends DecisionTree {
	private DecTreeNode root;
	//ordered list of class labels
	private List<String> labels; 
	//ordered list of attributes
	private List<String> attributes; 
	//map to ordered discrete values taken by attributes
	private Map<String, List<String>> attributeValues; 

	/**
	 * Answers static questions about decision trees.
	 */
	DecisionTreeImpl() {
		// no code necessary this is void purposefully
	}

	/**
	 * Build a decision tree given only a training set.
	 * 
	 * @param train: the training set
	 */
	DecisionTreeImpl(DataSet train) {
		this.labels = train.labels;
		this.attributes = train.attributes;
		this.attributeValues = train.attributeValues;
		// TODO: add code here
		//Build decision tree following the algo on Figure 18.5 on page 702.
		//May need to write recursive function corresponding to DecisionTreeLearning or BuildTree Function on lecture slide.
		boolean[] used_attributes = new boolean[attributes.size()];
		root = buildTree(train.instances, train.attributes, used_attributes, null, null);
	}

	DecTreeNode buildTree(List<Instance> instances, List<String> attributes, boolean[] used_attributes, List<Instance> parent_instances, String parent_AttributeValue){
		String best_attribute = null;
		int best_attributeIndex = 0;
		//examples are empty
		if(instances.isEmpty()){
			//if there is no examples from the beginning
			if(parent_instances == null) return null;
			else return new DecTreeNode(getPluralityValue(parent_instances), null, parent_AttributeValue, true);
		}

		String majorityVote = getPluralityValue(instances);

		//all examples have the same lebal
		boolean same = true;
		String ist = instances.get(0).label;
		for(Instance is : instances){
			if(!is.label.equals(ist)){
				same = false;
				break;
			}
		}
		if(same) return new DecTreeNode(majorityVote, null, parent_AttributeValue, true);

		//all attributes have been used
		boolean all_used = true;
		for(int i = 0; i < used_attributes.length; i++){
			if(!used_attributes[i]){
				all_used = false;
				break;
			}		
		}
		if(all_used){
			return new DecTreeNode(majorityVote, null, parent_AttributeValue, true);  //return majority-class of examples
		}

		best_attribute = getBestAttribute(instances, used_attributes);
		best_attributeIndex = getAttributeIndex(best_attribute);
		DecTreeNode newNode = new DecTreeNode(majorityVote, best_attribute, parent_AttributeValue, false);

		used_attributes[best_attributeIndex] = true;

		List<Instance> child_instances = null;
		for(String value : attributeValues.get(best_attribute)){
			child_instances = new ArrayList<Instance>();
			for(Instance inst : instances){
				if(inst.attributes.get(best_attributeIndex).equals(value)){
					child_instances.add(inst);
				}
			}
			newNode.addChild(buildTree(child_instances, attributes, used_attributes, instances, value));
		}

		used_attributes[best_attributeIndex] = false;
		return newNode;
	}  

	private String getPluralityValue(List<Instance> instances){
		if(instances.isEmpty()) return null;
		Map<String, Integer> pluralMap = new HashMap<String, Integer>();
		int max = 1;
		for(Instance inst : instances){
			if(!pluralMap.containsKey(inst.label)){
				pluralMap.put(inst.label, 1);
			}else{
				int i = pluralMap.get(inst.label)+1;
				pluralMap.put(inst.label, i);
				max = Math.max(max, i);
			}
		}

		int index = labels.size()-1; 
		for(String label : pluralMap.keySet()){
			if(pluralMap.get(label) == max){
				if(getLabelIndex(label) < index) index = getLabelIndex(label);
			}
		}
		return labels.get(index);
	}

	private String getBestAttribute(List<Instance> instances, boolean[] used_attributes){
		String best_attribute = attributes.get(0);
		double best_infoGain = -1;
		for(String attribute : attributes){
			if(used_attributes[getAttributeIndex(attribute)]) continue;
			if(getInfoGain(attribute, instances) > best_infoGain){
				best_infoGain = getInfoGain(attribute, instances);
				best_attribute = attribute;
			}
		}
		return best_attribute;
	}

	@Override
	public String classify(Instance instance) {
		// TODO: add code here
		List<String> attributeValueList = instance.attributes;

		DecTreeNode curr = root;
		while(!curr.terminal){
			int attributeIndex = getAttributeIndex(curr.attribute);
			for(DecTreeNode child : curr.children){
				if(child.parentAttributeValue.equals(attributeValueList.get(attributeIndex))){
					curr = child;
					break;
				}
			}
		}

		return curr.label;
	}

	@Override
	public void rootInfoGain(DataSet train) {
		this.labels = train.labels;
		this.attributes = train.attributes;
		this.attributeValues = train.attributeValues;
		// TODO: add code here
		// System.out.format("%.5f\n", arg) 
		// Printout order must be same as order that attributes appear in training set's header
		for(String attribute : attributes){
			System.out.format("%s %.5f\n", attribute, getInfoGain(attribute, train.instances));
		}    
	}

	private double getInfoGain(String attribute, List<Instance> instances){
		return getEntropy(instances) - getCEntropy(attribute, instances);	
	}

	private double getEntropy(List<Instance> instances){ 
		Map<String, Integer> mapLabels = new HashMap<String, Integer>();
		int sum_labels = instances.size();
		for(Instance inst : instances){
			String inst_label = inst.label;
			if(!mapLabels.containsKey(inst_label)){
				mapLabels.put(inst_label, 1);
			}else{
				mapLabels.put(inst_label, mapLabels.get(inst_label)+1);
			}
		}

		double entropy = 0;
		for(String label : mapLabels.keySet()){
			double num = ((double)mapLabels.get(label))/sum_labels;
			entropy += (-1)*(num)*(Math.log(num)/Math.log(2));
		}
		return entropy;
	}

	private double getCEntropy(String attribute, List<Instance> instances){
		int attribute_index = getAttributeIndex(attribute);
		int sum_values = instances.size();
		//attribute-value, label, number of these characteristics 
		Map<String, Map<String, Integer>> mapValues = new HashMap<String, Map<String, Integer>>();
		//number of attribute values appeared. It will be saved according to the method provided.
		int[] num_values = new int[attributeValues.get(attribute).size()];
		for(Instance inst :instances){
			String label = inst.label;
			String value = inst.attributes.get(attribute_index);
			num_values[getAttributeValueIndex(attribute, value)]++;

			//if it does not have the attribute-value as key
			if(!mapValues.containsKey(value)){
				Map<String, Integer> mapLabels = new HashMap<String, Integer>();
				mapLabels.put(label, 1);
				mapValues.put(value, mapLabels); 
			}else{
				//If it has attribute-value as key but does not match label
				if(!mapValues.get(value).containsKey(label)){
					mapValues.get(value).put(label, 1);
				}else{
					mapValues.get(value).put(label, mapValues.get(value).get(label)+1);
				}
			}
		}

		double cEntropy = 0;
		for(String value : mapValues.keySet()){
			int num_value = num_values[getAttributeValueIndex(attribute, value)];
			double num = ((double)num_value)/sum_values;
			double calc = 0;
			for(String label : mapValues.get(value).keySet()){
				double num_conditional = ((double)mapValues.get(value).get(label))/num_value;
				calc += (-1)*num_conditional*(Math.log(num_conditional)/Math.log(2));
			}
			cEntropy += num*calc;
		}

		return cEntropy;
	}

	private double getAccuracy(DataSet test){
		List<Instance> testInstances = test.instances;
		int correct = 0;
		for(Instance inst : testInstances){
			if(inst.label.equals(classify(inst))) correct++;
		}
		return (correct*1.0)/testInstances.size();
	}

	@Override
	public void printAccuracy(DataSet test) {
		// TODO: add code here
		System.out.format("Accuracy: %.5f\n", getAccuracy(test));
	}
	/**
	 * Build a decision tree given a training set then prune it using a tuning set.
	 * ONLY for extra credits
	 * @param train: the training set
	 * @param tune: the tuning set
	 */
	DecisionTreeImpl(DataSet train, DataSet tune) {

		this.labels = train.labels;
		this.attributes = train.attributes;
		this.attributeValues = train.attributeValues;
		// TODO: add code here
		// only for extra credits
		if(train.instances.isEmpty()) return;
		boolean[] used_attributes = new boolean[attributes.size()];
		root = buildTree(train.instances, attributes, used_attributes, null, null);
		//no need to prune if root is the leaf
		if(root.terminal) return; 
		//pruning
		DecTreeNode best_node = null;
		Queue<DecTreeNode> q = new LinkedList<DecTreeNode>();
		double best_accuracy = 0.0;
		double prev_bestAccuracy = getAccuracy(tune);
		DecTreeNode curr = null;
		do{
			best_accuracy = 0.0;
			q.add(root);
			while(!q.isEmpty()){
				curr = q.remove();
				curr.terminal = true;
				double curr_accuracy = getAccuracy(tune);
				// take the first node with the max accuracy within one round
				if(curr_accuracy > best_accuracy){ 
					best_accuracy = curr_accuracy;
					best_node = curr;
				}
				curr.terminal = false;
				for(DecTreeNode child : curr.children){
					if(!child.terminal) q.add(child);
				}
			}
			if(best_accuracy >= prev_bestAccuracy){
				best_node.terminal = true;
				prev_bestAccuracy = best_accuracy;
			}
			if(root == best_node) break;
			//no need to continue if accuracy decreases
		}while(best_accuracy >= prev_bestAccuracy); 
	}

	@Override
	/**
	 * Print the decision tree in the specified format
	 */
	public void print() {

		printTreeNode(root, null, 0);
	}

	/**
	 * Prints the subtree of the node with each line prefixed by 4 * k spaces.
	 */
	public void printTreeNode(DecTreeNode p, DecTreeNode parent, int k) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < k; i++) {
			sb.append("    ");
		}
		String value;
		if (parent == null) {
			value = "ROOT";
		} else {
			int attributeValueIndex = this.getAttributeValueIndex(parent.attribute, p.parentAttributeValue);
			value = attributeValues.get(parent.attribute).get(attributeValueIndex);
		}
		sb.append(value);
		if (p.terminal) {
			sb.append(" (" + p.label + ")");
			System.out.println(sb.toString());
		} else {
			sb.append(" {" + p.attribute + "?}");
			System.out.println(sb.toString());
			for (DecTreeNode child : p.children) {
				printTreeNode(child, p, k + 1);
			}
		}
	}

	/**
	 * Helper function to get the index of the label in labels list
	 */
	private int getLabelIndex(String label) {
		for (int i = 0; i < this.labels.size(); i++) {
			if (label.equals(this.labels.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Helper function to get the index of the attribute in attributes list
	 */
	private int getAttributeIndex(String attr) {
		for (int i = 0; i < this.attributes.size(); i++) {
			if (attr.equals(this.attributes.get(i))) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Helper function to get the index of the attributeValue in the list for the attribute key in the attributeValues map
	 */
	private int getAttributeValueIndex(String attr, String value) {
		for (int i = 0; i < attributeValues.get(attr).size(); i++) {
			if (value.equals(attributeValues.get(attr).get(i))) {
				return i;
			}
		}
		return -1;
	}

}

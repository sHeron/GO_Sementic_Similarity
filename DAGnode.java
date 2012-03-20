//TODO BigDecimal not needed really
import java.math.BigDecimal;
import java.util.HashSet;

import java.util.HashSet;

public class DAGnode {
	public String incomingRelationshipType;
	public String nodeValue;
	public String namespace;
	public int storageIndex;
	public DAGnode next;
	public HashSet<Integer> annotations;
	
	DAGnode(String nodeValue, String incomingRelationshipType, String namespace, int storageIndex)
	{
        this.nodeValue = nodeValue;
		this.incomingRelationshipType = incomingRelationshipType;
		this.namespace = namespace;
		this.storageIndex = storageIndex;
		next = null;
		annotations = new HashSet<Integer>();
	}
	
	//The following function is not listed in report as it was scheduled for removal, however I 
	//have been unable to resolve resulting dependency issues from doing so, so it remains
	//despite the fact that it adds no functionality to any calculations or alters any results
	
	//for virtual nodes
	DAGnode(String val, String type, String nspace, int index, BigDecimal transitionProb)
	{
		nodeValue = val;
		incomingRelationshipType = type;
		namespace = nspace;
		storageIndex = index;
		next = null;
		annotations = new HashSet<Integer>();
		//transitionProbability = transitionProb;
	}
}

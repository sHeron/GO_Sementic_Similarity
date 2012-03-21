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
}

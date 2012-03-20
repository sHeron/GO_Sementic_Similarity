import java.math.BigDecimal;

public class linkList {
	
	public DAGnode root;
	
	linkList()
	{
		root = null;
	}
	
	public void addNode(String nodeValue, String incomingRelationshipType, String namespace, int storageIndex)
	{
        if(root!=null)
		{
			DAGnode newNode = new DAGnode(nodeValue, incomingRelationshipType, namespace, storageIndex);
			DAGnode currentNode = root;
			while (currentNode.next!=null)
			{
				currentNode = currentNode.next;
			}
			currentNode.next = newNode;
		}
		else
		{
			root = new DAGnode(nodeValue, incomingRelationshipType, namespace, storageIndex);
		}
	}
	//The following function is not listed in report as it was scheduled for removal, however I 
	//have been unable to resolve resulting dependency issues from doing so, so it remains
	//despite the fact that it adds no functionality to any calculations or alters any results
		
	//for virtual nodes
	public void addNode(String val, String type, String nspace, int index, BigDecimal transitionProb)
	{		
		root = new DAGnode(val, type, nspace, index, transitionProb);//this is a self ref now as thats more useful //index = 0 as this is this nodes primary store (not as a relation)
	}
	
	public void removeNode(String target)
	{
		DAGnode currentNode = root;
		if(currentNode.nodeValue.equals(target) == true)
		{
			if(currentNode.next!=null)
			{
				root = currentNode.next;
			}
			else
			{
				root=null;
			}
		}
		else
		{
			DAGnode previousNode;
			//root isn't the target, as has just been checked
			previousNode = currentNode;
			currentNode = currentNode.next;
			while(true)
			{
				if(currentNode.nodeValue.equals(target)==false)
				{
					previousNode = currentNode;
					if(currentNode.next!=null)
					{
						currentNode = currentNode.next;
					}
					else
					{
						break;
					}
				}
				else
				{
					currentNode = currentNode.next;
					previousNode.next = currentNode;
					break;
				}
			}
		}
	}
	
	public boolean contains(String target)
	{
		DAGnode currentNode = root;
		while (currentNode!=null)
		{
			if(currentNode.nodeValue.equals(target) == true)
			{
				return true;
			}
			currentNode = currentNode.next;
		}
		return false;
	}
}

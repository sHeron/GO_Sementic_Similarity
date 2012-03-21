import java.io.*;
import java.util.HashSet;
import java.lang.Math;
import java.math.BigDecimal;
import Jama.Matrix;

public class DAGstructure {
	
	public linkList[] DAGedges;
	public int noNodes;
	public int noNodesDeletedFromDAG;
	public AnnoNode[] annotationElements;
	public HashSet<DAGnode> GOleaves;
	public HashSet<DAGnode> lcaResultsStore;
	
	DAGstructure(int noNodes, int noAnnotations)
	{
		this.noNodes = noNodes;
		noNodesDeletedFromDAG=0;
		DAGedges = new linkList[noNodes];
		for(int i=0;i<noNodes;i++)
		{
			DAGedges[i] = new linkList(); //initialise
		}
		noNodes--; //array begins at [0]
		annotationElements = new AnnoNode[noAnnotations]; 
		GOleaves  = new HashSet<DAGnode>();
		lcaResultsStore  = new HashSet<DAGnode>();
	}
	
	public void addNode(String nodeValue, String namespace) 
	{
		//create linklist for new node
		boolean thereAlready=false; 
		for(int m=0; m<noNodes; m++)
		{
			if(DAGedges[m].root!=null)
			{
				if(DAGedges[m].root.nodeValue.equals(nodeValue)==true)
				{
					thereAlready=true;
				}
			}
			if(DAGedges[m].root == null && thereAlready==false)
			{
				DAGedges[m].addNode(nodeValue, "root", namespace, m);
				break;
			}
		}
	}
	
	public void addNeighbour(String rootVal, String neighbourVal, String namespace, String incomingRelationshipType)
	{
		//obtaining index
		int index=0;
		boolean found=false;
		for(int i=0; i<noNodes; i++)
		{
			if(DAGedges[i].root!=null)
			{
				if(DAGedges[i].root.nodeValue.equals(neighbourVal)==true)
				{
					index = i;
					found = true;
					break;
				}
			}
		}
		if(found == false)
		{
			addNode(neighbourVal, namespace);
			for(int l=0; l<noNodes; l++)
			{
				if(DAGedges[l].root!=null)
				{
					if(DAGedges[l].root.nodeValue.equals(neighbourVal)==true)
					{
						index = l;
						break;
					}
				}
			}
		}
		//adding neighbour
		for(int j=0; j<noNodes; j++)
		{
			if(DAGedges[j].root!=null)
			{
				if(DAGedges[j].root.nodeValue.equals(rootVal)==true)
				{
					if(DAGedges[j].contains(neighbourVal)==true)
					{
						System.out.println("The nodes: "+rootVal+" "+neighbourVal+" are already linked by a "+incomingRelationshipType+" relationship");
					}
					else
					{
						DAGedges[j].addNode(neighbourVal, incomingRelationshipType, namespace, index);
					}
				}
			}
		}		
	}
	
	public void deleteNode(DAGnode target)
	{
		DAGedges[target.storageIndex].root=null;
	}
	
	public boolean hasNeighbour(DAGnode target)
	{
		for(int i=0;i<noNodes;i++)
		{
			if(DAGedges[i].root!=null)
			{
				if(DAGedges[i].root.equals(target)==true)
				{
					if(DAGedges[i].root.next!=null)
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void propagate()
	{
		System.out.println("Propagating Annotation...");
		propagateAnnotation(getLeaves());
		pruneDAG();
	}
	
	public void pruneDAG()
	{
		System.out.println("Prunning empty/unused nodes from the data structure");
		int deleteCount=0;
		for(int i=0; i<noNodes; i++)
		{
			if(DAGedges[i].root!=null)
			{
				if(DAGedges[i].root.annotations.isEmpty()==true)
				{
					deleteNode(DAGedges[i].root); 
					deleteCount++;
				}
			}
		}
		noNodesDeletedFromDAG = deleteCount;
		System.out.println(deleteCount + " Nodes Removed");
	}
	
	public HashSet<DAGnode> getLeaves()
	{
		HashSet<DAGnode> leaves = new HashSet<DAGnode>();
		for(int f=0;f<noNodes;f++) 
		{
			if(DAGedges[f].root!=null)
				{
					leaves.add(DAGedges[f].root);
				}
		}
		DAGnode currentNode;
		for(int j=0;j<noNodes;j++) 
		{
			if(DAGedges[j].root!=null)
			{
				currentNode = DAGedges[j].root;
				if(currentNode.next!=null) //if node has neighbours
				{
					DAGnode parentAtSomeLevel = currentNode;
					while(parentAtSomeLevel!=null) //while neighbours exist
					{
						parentAtSomeLevel = parentAtSomeLevel.next; //on first pass = current.next
						 //current = first neighbour (first pass), then consecutive neighbours
						if(parentAtSomeLevel!=null)
							{
								if(DAGedges[parentAtSomeLevel.storageIndex].root!=null)
								{
									if(leaves.contains(DAGedges[parentAtSomeLevel.storageIndex].root)==true)//check leaves array for neighbour
									{
										leaves.remove(DAGedges[parentAtSomeLevel.storageIndex].root);
									}
								}
							}
					}
				}
			}
		}
		GOleaves=leaves; //saves doing all this again later
		return leaves;
	}
	
	public void propagateAnnotation(HashSet<DAGnode> nodeList)
	{ 
		HashSet<DAGnode> parents = new HashSet<DAGnode>();
		DAGnode [] nodeListArray = new DAGnode[nodeList.size()];
		nodeListArray=(DAGnode[]) nodeList.toArray(new DAGnode[0]);
		Definitions defs = new Definitions();
		DAGnode currentNode=null;
		
		for(int i=0;i<nodeList.size();i++)
		{
			currentNode = nodeListArray[i];
				if(currentNode.nodeValue.equals(defs.getMFrootValue())==false&&currentNode.nodeValue.equals(defs.getCCrootValue())==false&&currentNode.nodeValue.equals(defs.getBProotValue())==false)
				{
					//copy anno's
					DAGnode parent=null;
					if(currentNode.next!=null)
					{
						parent=currentNode.next;
						//go through all parents
						while(parent!=null)
						{
							int parentIndex = parent.storageIndex;
							DAGedges[parentIndex].root.annotations.addAll(DAGedges[currentNode.storageIndex].root.annotations); //used to be currentIndex not current.storageIndex
							parents.add(DAGedges[parentIndex].root);
							parent=parent.next;
						}
					}
				}
		}
		if(parents.isEmpty()==false)
		{
			propagateAnnotation(parents);
		}
		return;
	}
	
	public Object[] semanticSimilarity(String namespace, int semsimChoice)
	{
		System.out.println("Beginning Sematic Similarity Calculations...");
		
		int ontologyNodes=0;
		int noLeaves=0;
		for(int u=0;u<noNodes;u++)
		{
			if(DAGedges[u].root!=null)
			{
				if(DAGedges[u].root.namespace.equals(namespace)==true)
				{
					ontologyNodes++;
					if(GOleaves.contains(DAGedges[u].root)==true)
					{
						noLeaves++;
					}
				}
			}
		}
		//5 steps:
		//step 1; create the nodelist containing the transition probabilities
		//step 2; create the wtable (by doing the downward random walks)
		//step 3(step4); calculating the random walk contribution which needs step 4; the hsm matrix
		//step 5; calculate the ism matrix
		
		Object[] resultMatrixArray = new Object[4];
		//users choice:
		switch(semsimChoice)
		{
		case 1:{
			Object[] lhsmResults=linHSM(namespace, ontologyNodes);
			resultMatrixArray[0] = new Object();
			resultMatrixArray[0] = lhsmResults[1]; //axis
			resultMatrixArray[1] = new Object();
			resultMatrixArray[1] = lhsmResults[0]; //hsm
			break;
		}
		case 2:{
			Object[] rhsmResults=resnikHSM(namespace, ontologyNodes);
			resultMatrixArray[0] = new Object();
			resultMatrixArray[0] = rhsmResults[1]; //axis
			resultMatrixArray[1] = new Object();
			resultMatrixArray[1] = rhsmResults[0]; //hsm
			break;
		}
		case 3:{
			Object[] jhsmResults=jiangHSM(namespace, ontologyNodes);
			resultMatrixArray[0] = new Object();
			resultMatrixArray[0] = jhsmResults[1]; //axis
			resultMatrixArray[1] = new Object();
			resultMatrixArray[1] = jhsmResults[0]; //hsm
			break;
		}
		case 4:{
			Object[] step4results=step4_HSM(noLeaves, namespace, ontologyNodes, false); 
			Matrix[] fullHSMs = (Matrix[])step4results[0];
			resultMatrixArray[0] = new Object();
			resultMatrixArray[0] = step4results[1]; //axis
			resultMatrixArray[1] = new Object();
			resultMatrixArray[1] = fullHSMs[0]; //lin hsm
			resultMatrixArray[2] = new Object();
			resultMatrixArray[2] = fullHSMs[1]; //resnik hsm
			resultMatrixArray[3] = new Object();
			resultMatrixArray[3] = fullHSMs[2]; //jiang hsm
			break;
		}
		case 5:{
			Matrix[] step3results = step3_RWC(ontologyNodes,step2_DRW(ontologyNodes,step1_Initialisation(ontologyNodes, namespace)), namespace);
			Object[] lHSM = linHSM(namespace, ontologyNodes);
			DAGnode[] axisLabels = (DAGnode[]) lHSM[1];
			Matrix ismLin =linISM(ontologyNodes, step3results, (Matrix)lHSM[0], axisLabels);
			resultMatrixArray[0] = new Object();
			resultMatrixArray[0] = axisLabels;
			resultMatrixArray[1] = new Object();
			resultMatrixArray[1] = ismLin;
			break;
		}
		case 6:{
			Matrix[] step3results = step3_RWC(ontologyNodes,step2_DRW(ontologyNodes,step1_Initialisation(ontologyNodes, namespace)), namespace);
			Object[] rHSM = resnikHSM(namespace, ontologyNodes);
			DAGnode[] axis = (DAGnode[]) rHSM[1];
			Matrix ismResnik = resnikISM(ontologyNodes, step3results, (Matrix)rHSM[0], axis);
			resultMatrixArray[0] = new Object();
			resultMatrixArray[0] = axis;
			resultMatrixArray[1] = new Object();
			resultMatrixArray[1] = ismResnik;
		}
			break;
		case 7:{
			Matrix[] step3results = step3_RWC(ontologyNodes,step2_DRW(ontologyNodes,step1_Initialisation(ontologyNodes, namespace)), namespace);
			Object[] jHSM = jiangHSM(namespace, ontologyNodes);
			DAGnode[] axis = (DAGnode[]) jHSM[1];
			Matrix ismJiang = jiangISM(ontologyNodes, step3results, (Matrix)jHSM[0], axis);
			resultMatrixArray[0] = new Object();
			resultMatrixArray[0] = axis;
			resultMatrixArray[1] = new Object();
			resultMatrixArray[1] = ismJiang;
			break;
		}
		case 8:{
			Matrix[] step3results = step3_RWC(ontologyNodes,step2_DRW(ontologyNodes,step1_Initialisation(ontologyNodes, namespace)), namespace);
			Object[] step4results = step4_HSM(noLeaves, namespace, ontologyNodes, false);
			Matrix[] fullHSMs = (Matrix[]) step4results[0];
			DAGnode[] axis = (DAGnode[]) step4results[1];
			Matrix[] step5Isms=step5_ISM(ontologyNodes, step3results, fullHSMs, axis);
			resultMatrixArray[0] = new Object();
			resultMatrixArray[0] = axis;
			resultMatrixArray[1] = new Object();
			resultMatrixArray[1] = step5Isms[0];//lin
			resultMatrixArray[2] = new Object();
			resultMatrixArray[2] = step5Isms[1];//Resnik
			resultMatrixArray[3] = new Object();
			resultMatrixArray[3] = step5Isms[2];//Jiang
			break;
		}
		}
		return resultMatrixArray;
	}
	
	public BigDecimal[][] step1_Initialisation(int ontologyNodes, String namespace)
	{
		System.out.println("Calculating Transition Probabilities...");
        BigDecimal noAnnotations;
		BigDecimal uniqueAnnotations;
		BigDecimal transitionProb = new BigDecimal(0);
		DAGnode currentNode;
		DAGnode [] children;
		HashSet<Integer> childrenAnnotations = new HashSet<Integer>();
		int totalChildAnnotations=0;
		BigDecimal totalChildAnno;
		
		DAGnode [] nodeIndex = new DAGnode[ontologyNodes];
		int nodeCounter=-1;
		for(int u=0;u<noNodes;u++)
		{
			if(DAGedges[u].root!=null)
			{
				if(DAGedges[u].root.namespace.equals(namespace)==true)
				{
					nodeCounter++;
					nodeIndex[nodeCounter]=DAGedges[u].root;
				}
			}
		}
		
		//P:
		BigDecimal [][] nodeTable = new BigDecimal[ontologyNodes][ontologyNodes];
		
		//P<-0
		for(int m=0;m<ontologyNodes;m++)
		{
			for(int n=0;n<ontologyNodes;n++)
			{
				nodeTable[m][n]=new BigDecimal(0);
			}
		}
				
		//P(c,c)<-1
		for(int i=0;i<ontologyNodes;i++)
			{
				if(GOleaves.contains(nodeIndex[i])==true)
					{
						nodeTable[i][i] = new BigDecimal(1);
					}
			}
		for(int j=0;j<ontologyNodes;j++)
		{
				if(GOleaves.contains(nodeIndex[j])==false)//{}else
					{
						currentNode = nodeIndex[j];
                        noAnnotations = new BigDecimal(nodeIndex[j].annotations.size());
                        children=(DAGnode[]) getChildren(currentNode).toArray(new DAGnode[0]);
						int annoCounter=0;
						for(int k=0;k<children.length;k++)
						{
                        	//find all the annotations on all the children
							childrenAnnotations.addAll(children[k].annotations);
							totalChildAnnotations+=children[k].annotations.size();
						}
						annoCounter = nodeIndex[j].annotations.size()-childrenAnnotations.size();
                        if(annoCounter!=0)
						{
                        	//account for virtual child/uncertainty
							uniqueAnnotations = new BigDecimal(annoCounter);
							transitionProb = uniqueAnnotations.divide(noAnnotations,5,BigDecimal.ROUND_FLOOR);							
						}
						else
						{
							uniqueAnnotations = new BigDecimal(0);
						}
						//transition probability for real nodes now:
						BigDecimal regTransProb = new BigDecimal(1);
						regTransProb = regTransProb.subtract(transitionProb);
						BigDecimal currentChildAnno;
						totalChildAnno=new BigDecimal(totalChildAnnotations);
						int nodeTableIndex=0;
                        for(int l=0;l<children.length;l++)
						{
							currentChildAnno = new BigDecimal(children[l].annotations.size());
							regTransProb = regTransProb.multiply((currentChildAnno.divide(totalChildAnno, 5, BigDecimal.ROUND_FLOOR)));
							for(int k=0;k<nodeIndex.length;k++)
                            {
								if(nodeIndex[k].equals(children[l])==true)
                                {
									nodeTableIndex=k;
                                }
                            }
							nodeTable[j][nodeTableIndex] = regTransProb;
							
						}
					}
		}
        return nodeTable;
	}
	
	public Matrix step2_DRW(int ontologyNodes, BigDecimal[][] nodeTable)
	{	
		System.out.println("Walking Randomly Downward...");
		double [][] wTable = new double[ontologyNodes][ontologyNodes];
		BigDecimal stopThreshold = new BigDecimal(0.001);
		for(int m=0;m<ontologyNodes;m++)
		{
			wTable[m][m]=1; //W starts as an identity matrix
		}
		double[][] transitionNodeTable = new double[ontologyNodes][ontologyNodes];
		for(int p=0;p<ontologyNodes;p++)
		{
			for(int q=0;q<ontologyNodes;q++)
			{
				transitionNodeTable[p][q] = nodeTable[p][q].doubleValue();
			}
		}
		Matrix MnodeTable = new Matrix(transitionNodeTable);
		Matrix MwTable= new Matrix(ontologyNodes,ontologyNodes);
		Matrix MwTableStar = new Matrix(wTable);		
		do
		{
			MwTable = MwTableStar;
			MwTableStar = MnodeTable.times(MwTable);
		} while((MwTableStar.minus(MwTable)).normF() > stopThreshold.doubleValue());
		MwTable = MwTableStar;
		return MwTableStar;
	}
        
    public Matrix[] step3_RWC(int ontologyNodes, Matrix MwTable, String namespace)
    {
    	System.out.println("Calculating Random Walk Contribution...");
    	
    	System.out.println("Nodes Utilised: "+ontologyNodes); 
		int noLeaves=0;
		DAGnode [] nodeIndex = new DAGnode[ontologyNodes]; //stores the nodes, the of which index equals the column/row in the nodetable that represents it
		int nodeCounter=-1;
		for(int u=0;u<noNodes;u++)
		{
			if(DAGedges[u].root!=null)
			{
				if(DAGedges[u].root.namespace.equals(namespace)==true)
				{
					if(DAGedges[u].root.nodeValue.startsWith("virtual")==false) 
					{ 
						nodeCounter++;
						nodeIndex[nodeCounter]=DAGedges[u].root;
						if(GOleaves.contains(DAGedges[u].root)==true) 
						{
							noLeaves++;
						}
					}
				}
			}
		}
		System.out.println("Number of Leaves: "+noLeaves);
		
		double[][] wSubmatrixLN = new double[noLeaves][ontologyNodes]; 
		double[][] wSubmatrixNL = new double[ontologyNodes][noLeaves];
		
		//create the submatrices
		int index=-1;
		for(int r=0;r<ontologyNodes;r++)
		{
			if(GOleaves.contains(nodeIndex[r])==true)
			{
				index++;
				for(int s=0;s<ontologyNodes;s++)
				{
					wSubmatrixLN[index][s] = MwTable.get(r, s);
					wSubmatrixNL[s][index] = MwTable.get(r, s);
				}
			}
		}
		
		Matrix MwSubmatrixLN = new Matrix(wSubmatrixLN);
		wSubmatrixLN = null;
		Matrix MwSubmatrixNL = new Matrix(wSubmatrixNL);
		wSubmatrixNL = null;
		
		//step 4 call
		Object[] step4results = step4_HSM(noLeaves, namespace, ontologyNodes, true);
		Matrix[] step4resultsM = (Matrix[]) step4results[0];
		
		Matrix rwcLin = new Matrix(ontologyNodes,ontologyNodes);
		rwcLin = MwSubmatrixNL.times(step4resultsM[0]);
		rwcLin = rwcLin.times(MwSubmatrixLN);
		Matrix rwcResnik = new Matrix(ontologyNodes,ontologyNodes);
		rwcResnik = MwSubmatrixNL.times(step4resultsM[1]);//(MhsmResnik);
		rwcResnik = rwcResnik.times(MwSubmatrixLN);
		Matrix rwcJiang = new Matrix(ontologyNodes,ontologyNodes);
		rwcJiang = MwSubmatrixNL.times(step4resultsM[2]);
		rwcJiang = rwcJiang.times(MwSubmatrixLN);
		MwSubmatrixNL = null;
		MwSubmatrixLN = null;
		
		Matrix[] step3results = new Matrix[3];
		step3results[0] = rwcLin;
		rwcLin=null;
		step3results[1] = rwcResnik;
		rwcResnik=null;
		step3results[2] = rwcJiang;
		rwcJiang=null;
		
		return step3results;
    }
    
    public Object[] step4_HSM(int noLeaves, String namespace, int ontologyNodes, boolean justLeaves)
    {
    	if(justLeaves==true)
		{
			System.out.println("Calculating HSM Subsets...");
		}
		else
		{
			System.out.println("Calculating HSM's...");
		}
		
		Matrix[] step4resultsM = new Matrix[3];
		Object[] step4results = new Object[2];
		
		double[][] hsmLin;
		double[][] hsmResnik;
		double[][] hsmJiang;
		
    	//create the HSM's
		if(justLeaves==true)
		{
			hsmLin = new double[noLeaves][noLeaves];
			hsmResnik = new double[noLeaves][noLeaves];
			hsmJiang = new double[noLeaves][noLeaves];
			
			BigDecimal[] semSimVals;
			DAGnode[] leaves = new DAGnode[noLeaves];
    	
			int index=0;
			for(int u=0;u<noNodes;u++)
			{
				if(DAGedges[u].root!=null)
				{
					if(DAGedges[u].root.namespace.equals(namespace)==true)
					{
						if(DAGedges[u].root.nodeValue.startsWith("virtual")==false)
						{ 
							if(GOleaves.contains(DAGedges[u].root)==true) 
							{
								leaves[index]=DAGedges[u].root;
								index++;
							}
						}
					}
				}
			}
			
			for(int x=0;x<noLeaves;x++)
			{
				for(int y=0;y<noLeaves;y++)
				{
					semSimVals = resnikLinJiang(leaves[x],leaves[y]);
					hsmLin[x][y] = semSimVals[0].doubleValue();
					hsmResnik[x][y] = semSimVals[1].doubleValue();
					hsmJiang[x][y] = semSimVals[2].doubleValue();
				}	
			}
			step4resultsM[0] = new Matrix(hsmLin);
	    	step4resultsM[1] = new Matrix(hsmResnik);
	    	step4resultsM[2] = new Matrix(hsmJiang);
	    	
		}
		else
		{
			hsmLin = new double[ontologyNodes][ontologyNodes];
			hsmResnik = new double[ontologyNodes][ontologyNodes];
			hsmJiang = new double[ontologyNodes][ontologyNodes];
			
			BigDecimal[] semSimVals;
			DAGnode[] nodeSet = new DAGnode[ontologyNodes];
    	
			int index=0;
			for(int u=0;u<noNodes;u++)
			{
				if(DAGedges[u].root!=null)
				{
					if(DAGedges[u].root.namespace.equals(namespace)==true)
					{
						if(DAGedges[u].root.nodeValue.startsWith("virtual")==false) 
						{ 
								nodeSet[index]=DAGedges[u].root;
								index++;
						}
					}
				}
			}
			
			for(int x=0;x<ontologyNodes;x++)
			{
				for(int y=0;y<ontologyNodes;y++)
				{
					semSimVals = resnikLinJiang(nodeSet[x],nodeSet[y]);
					hsmLin[x][y] = semSimVals[0].doubleValue();
					hsmResnik[x][y] = semSimVals[1].doubleValue();
					hsmJiang[x][y] = semSimVals[2].doubleValue();
				}	
			}
	    	step4resultsM[0] = new Matrix(hsmLin);
	    	step4resultsM[1] = new Matrix(hsmResnik);
	    	step4resultsM[2] = new Matrix(hsmJiang);
	    	
			//print hsm matrices to file
	    	try {
				printMatrix(step4resultsM[0], nodeSet, "HSM_Lin");
				printMatrix(step4resultsM[1], nodeSet, "HSM_Resnik");
				printMatrix(step4resultsM[2], nodeSet, "HSM_Jiang");
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	step4results[1] = new Object();
	    	step4results[1] = nodeSet;
		}
		step4results[0] = new Object();
		step4results[0] = step4resultsM;
	
    	return step4results;
    }
    
    public Matrix[] step5_ISM(int ontologyNodes, Matrix[] step3results, Matrix[] fullHSMs, DAGnode[] axisLabels)
    {
    	System.out.println("Calulating ISM's...");
    	//step3results: 0-2 = rwc of L,R,J.
    	
    	Matrix ismLin = new Matrix(ontologyNodes,ontologyNodes);
    	ismLin = (step3results[0].plus(fullHSMs[0])).times(0.5);
		Matrix ismResnik = new Matrix(ontologyNodes,ontologyNodes);
		ismResnik = (step3results[1].plus(fullHSMs[1])).times(0.5);
		Matrix ismJiang = new Matrix(ontologyNodes,ontologyNodes);
		ismJiang = (step3results[2].plus(fullHSMs[2])).times(0.5);
		
		//print ism matrices to file
    	try {
			printMatrix(ismLin, axisLabels, "ISM_Lin");
			printMatrix(ismResnik, axisLabels, "ISM_Resnik");
			printMatrix(ismJiang, axisLabels, "ISM_Jiang");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
    	Matrix[] step5Isms = new Matrix[3];
    	step5Isms[0]=ismLin;
    	step5Isms[1]=ismResnik;
    	step5Isms[2]=ismJiang;
    	
    	return step5Isms;
    }
        
    public void printMatrix(Matrix outputMatrix, DAGnode[] axisLabels, String filename) throws IOException
    {
    	String currentDirectory = new File("").getAbsolutePath();
    	Writer outputWriter = new OutputStreamWriter(new FileOutputStream(currentDirectory+"/outputs/"+filename+".txt"));
    	//printing
    	outputWriter.write(filename+" matrix");
    	outputWriter.write("\n");
    	outputWriter.write("\n");
    	outputWriter.write("\t");
    	for(int p=0;p<axisLabels.length;p++)
    	{
    		outputWriter.write("\t");
    		outputWriter.write(axisLabels[p].nodeValue);
    	}
    	outputWriter.write("\n");
    	String temporaryWritingVariable, temporaryWritingVariable2;
    	int loopvar=0;
    	for(int i=0; i<outputMatrix.getRowDimension();i++)
    	{
    		outputWriter.write(axisLabels[i].nodeValue);
    		for(int j=0; j<outputMatrix.getColumnDimension();j++)
    		{
    			outputWriter.write("\t");
    			temporaryWritingVariable = Double.toString(outputMatrix.get(i, j));
    			temporaryWritingVariable2="";
    			loopvar=9;
    			if(temporaryWritingVariable.charAt(0)=='-')
    			{
    				loopvar=10;
    			}
    			for(int k=0; k<loopvar;k++)
    			{
    				if(temporaryWritingVariable.length()>k) //some values will be 0.0 or 1.0
    				{
    					temporaryWritingVariable2 += temporaryWritingVariable.charAt(k);
    				}
    			}
    			if(temporaryWritingVariable2.length()<loopvar)
    			{
    				while(temporaryWritingVariable2.length()<loopvar)
    				{
    					temporaryWritingVariable2 += " ";
    				}
    			}
    			outputWriter.write(temporaryWritingVariable2);
    		}
    		outputWriter.write("\n");
    	}
    	outputWriter.close();
    }
	
	public HashSet<DAGnode> getChildren(DAGnode parent)
	{
		HashSet<DAGnode> children = new HashSet<DAGnode>();
		DAGnode currentNode;
		
		for(int j=0;j<noNodes;j++)
		{
			if(DAGedges[j].root!=null)
			{
				if(hasNeighbour(DAGedges[j].root)==true)
				{
					currentNode = DAGedges[j].root.next;
					while(currentNode!=null)
					{
						if(currentNode.nodeValue.equals(parent.nodeValue)==true)
						{
							children.add(DAGedges[j].root);
							break;
						}
						currentNode=currentNode.next;
					}
				}
			}
		}
		return children;
	}
	
	public BigDecimal[] resnikLinJiang(DAGnode node1, DAGnode node2)
	{	
		//get required info
		if(lowestCommonAncestor(node1, node2)==false)
		{
			BigDecimal[] semSimVals = new BigDecimal[3];
			semSimVals[0] = new BigDecimal(0);
			semSimVals[1] = new BigDecimal(0);
			semSimVals[2] = new BigDecimal(0);
			return semSimVals;
		}
		else
		{
			int LCAresult=1000000;
			
			DAGnode [] LCAResults = new DAGnode[lcaResultsStore.size()];
			LCAResults=(DAGnode[]) lcaResultsStore.toArray(new DAGnode[0]);
			if(lcaResultsStore.size()>1)
			{	
				for(int i=0;i<LCAResults.length;i++)
				{
					if(LCAResults[i].annotations.size()<LCAresult)
					{
						LCAresult=LCAResults[i].annotations.size();
					}
				}
			}
			else
			{
				LCAresult=LCAResults[0].annotations.size();
			}
			//get the number of annotations on the same GO tree
			HashSet<Integer> annoNumber = new HashSet<Integer>();
			for(int i=0;i<noNodes;i++)
			{
				if(DAGedges[i].root!=null)
				{	
					if(DAGedges[i].root.namespace.equals(node1.namespace)==true)
					{
						if(DAGedges[i].root.annotations.isEmpty()==false)
						{
							annoNumber.addAll(DAGedges[i].root.annotations);
						}
					}
				}
			}
			Definitions defs = new Definitions();
			if(node1.nodeValue.equals(defs.getMFrootValue())==true||node2.nodeValue.equals(defs.getMFrootValue())==true||node1.nodeValue.equals(defs.getBProotValue())==true||node2.nodeValue.equals(defs.getBProotValue())==true||node1.nodeValue.equals(defs.getCCrootValue())==true||node2.nodeValue.equals(defs.getCCrootValue())==true)
			{
				LCAresult=annoNumber.size(); //if any of the nodes if a root of DAG, then entropy must be all/all
			}
			
			BigDecimal LCAr = new BigDecimal(LCAresult);
			BigDecimal noAnnotations = new BigDecimal(annoNumber.size());
			BigDecimal probability = new BigDecimal(0); 
			probability = LCAr.divide(noAnnotations, 5, BigDecimal.ROUND_FLOOR); 
			BigDecimal node1entropy = new BigDecimal(node1.annotations.size());
			BigDecimal node2entropy = new BigDecimal(node2.annotations.size());
			node1entropy = node1entropy.divide(noAnnotations, 5, BigDecimal.ROUND_FLOOR);
			node2entropy = node2entropy.divide(noAnnotations, 5, BigDecimal.ROUND_FLOOR);
		
			//Jiang 
			double jiangValue = Math.log(probability.doubleValue());
			jiangValue = jiangValue*2; //this is the first step; 2logPmica(c1,c2)
			jiangValue = jiangValue-Math.log(node1entropy.doubleValue())-Math.log(node2entropy.doubleValue()); //step 2; dJiang(c1,c2) now obtained
			//M value = max possible value of dJiang(c1,c2), in principle this will be: 2log(n/n)-log(n/n)-log(1/n) which works out as 0-0-log(1/n)
			//where n= number of annotation nodes in relevant GO tree
			BigDecimal mtemp = new BigDecimal(1);
			mtemp = mtemp.divide(noAnnotations,5,BigDecimal.ROUND_FLOOR);
			BigDecimal mtemp2 = new BigDecimal(annoNumber.size()); 
			mtemp2 = mtemp2.divide(noAnnotations,5,BigDecimal.ROUND_FLOOR);
			double mValue = (2*Math.log(mtemp2.doubleValue())-Math.log(mtemp2.doubleValue())-Math.log(mtemp.doubleValue()));
			jiangValue = (1-jiangValue)/mValue;
		
			//Lin
			double linValueFinal;
			if(LCAresult==annoNumber.size()||(node1entropy.doubleValue()==1&&node2entropy.doubleValue()==1))
			{
				linValueFinal=0;
			}
			else
			{
				if((node1.nodeValue.equals(defs.getBProotValue())==true&&node2.nodeValue.equals(defs.getBProotValue())==true)||(node1.nodeValue.equals(defs.getCCrootValue())==true&&node2.nodeValue.equals(defs.getCCrootValue())==true)||(node1.nodeValue.equals(defs.getMFrootValue())==true&&node2.nodeValue.equals(defs.getMFrootValue())==true))
				{
					linValueFinal=0; //if both nodes are the root, the lin equation will divide by 0, as this result is not important the end result is set to 0
				}
				else
				{
					double linValueTop = Math.log(probability.doubleValue());
					if(linValueTop==0)
					{
						linValueFinal =0;
					}
					else
					{
						linValueTop = linValueTop*2;//this is the top half of the equation (2logPmica(c1,c2)) now for the bottom:
						double linValueBottom = Math.log(node1entropy.doubleValue())+Math.log(node2entropy.doubleValue());
						linValueFinal = linValueTop/linValueBottom;
					}
				}
			}
			
			//Resnik
			double resnikValue =-Math.log(probability.doubleValue());
		
			BigDecimal[] semSimVals = new BigDecimal[3];
			semSimVals[0] = new BigDecimal(linValueFinal);
			semSimVals[1] = new BigDecimal(resnikValue);
			semSimVals[2] = new BigDecimal(jiangValue);
			return semSimVals;
		}
	}
	
	public BigDecimal jiangSemSim(DAGnode node1, DAGnode node2)
	{
		lowestCommonAncestor(node1, node2);
		int LCAresult=1000000;
		
		DAGnode [] LCAResults = new DAGnode[lcaResultsStore.size()];
		LCAResults=(DAGnode[]) lcaResultsStore.toArray(new DAGnode[0]);
		if(lcaResultsStore.size()>1)
		{	
			for(int i=0;i<LCAResults.length;i++)
			{
				if(LCAResults[i].annotations.size()<LCAresult)
				{
					LCAresult=LCAResults[i].annotations.size();
				}
			}
		}
		else
		{
			LCAresult=LCAResults[0].annotations.size();
		}
		//get the number of annotations on the same GO tree
		HashSet<Integer> annoNumber = new HashSet<Integer>();
		for(int i=0;i<noNodes;i++)
		{
			if(DAGedges[i].root!=null)
			{
				if(DAGedges[i].root.namespace.equals(node1.namespace)==true)
				{
					if(DAGedges[i].root.annotations.isEmpty()==false)
					{
						annoNumber.addAll(DAGedges[i].root.annotations);
					}
				}
			}
		}
		Definitions defs = new Definitions();
		if(node1.nodeValue.equals(defs.getMFrootValue())==true||node2.nodeValue.equals(defs.getMFrootValue())==true||node1.nodeValue.equals(defs.getBProotValue())==true||node2.nodeValue.equals(defs.getBProotValue())==true||node1.nodeValue.equals(defs.getCCrootValue())==true||node2.nodeValue.equals(defs.getCCrootValue())==true)
		{
			LCAresult=annoNumber.size(); //if any of the nodes if a root of DAG, then entropy must be all/all
		}
		
		BigDecimal LCAr = new BigDecimal(LCAresult);
		BigDecimal noAnnotations = new BigDecimal(annoNumber.size());
		BigDecimal probability = new BigDecimal(0); 
		probability = LCAr.divide(noAnnotations, 5, BigDecimal.ROUND_FLOOR); 
		
		double jiangValue = Math.log(probability.doubleValue());
		jiangValue = jiangValue*2; //this is the first step; 2logPmica(c1,c2)
		BigDecimal node1entropy = new BigDecimal(node1.annotations.size());
		BigDecimal node2entropy = new BigDecimal(node2.annotations.size());
		node1entropy = node1entropy.divide(noAnnotations, 5, BigDecimal.ROUND_FLOOR);
		node2entropy = node2entropy.divide(noAnnotations, 5, BigDecimal.ROUND_FLOOR);
		jiangValue = jiangValue-Math.log(node1entropy.doubleValue())-Math.log(node2entropy.doubleValue()); //step 2; dJiang(c1,c2) now obtained
		//M value = max possible value of dJiang(c1,c2), in principle this will be: 2log(n/n)-log(n/n)-log(1/n) which works out as 0-0-log(1/n)
		//where n= number of annotation nodes in relevant GO tree
		BigDecimal mtemp = new BigDecimal(1);
		mtemp = mtemp.divide(noAnnotations,5,BigDecimal.ROUND_FLOOR);
		BigDecimal mtemp2 = new BigDecimal(annoNumber.size());
		mtemp2 = mtemp2.divide(noAnnotations,5,BigDecimal.ROUND_FLOOR);
		double mValue = (2*Math.log(mtemp2.doubleValue())-Math.log(mtemp2.doubleValue())-Math.log(mtemp.doubleValue()));
		jiangValue = (1-jiangValue)/mValue;
		return new BigDecimal(jiangValue);
	}
	
	public Object[] jiangHSM(String namespace, int ontologyNodes)
	{
		System.out.println("Calculating Jiang HSM...");
		
		double[][] hsmJiang;
		
    	//create the HSM
		hsmJiang = new double[ontologyNodes][ontologyNodes];
			
		BigDecimal jiangValue;
		DAGnode[] nodeSet = new DAGnode[ontologyNodes];
    	
		int index=0;
		for(int u=0;u<noNodes;u++)
		{
			if(DAGedges[u].root!=null)
			{
				if(DAGedges[u].root.namespace.equals(namespace)==true)
				{
					if(DAGedges[u].root.nodeValue.startsWith("virtual")==false) 
					{ 
						nodeSet[index]=DAGedges[u].root;
						index++;
					}
				}
			}
		}
		
		for(int x=0;x<ontologyNodes;x++)
		{
			for(int y=0;y<ontologyNodes;y++)
			{
				jiangValue = jiangSemSim(nodeSet[x],nodeSet[y]);
				hsmJiang[x][y] = jiangValue.doubleValue();
			}	
		}	    	
		
		Matrix mHSMjiang = new Matrix(hsmJiang);
		//print hsm to file
	    try {
			printMatrix(mHSMjiang, nodeSet, "HSM_Jiang");
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
		System.out.println("Jiang HSM complete: ");
    	
    	Object[] hsmresults=new Object[2];
    	hsmresults[0] = new Object();
    	hsmresults[0] = mHSMjiang;
    	hsmresults[1] = new Object();
    	hsmresults[1] = nodeSet;
    	
    	return hsmresults;
	}
	
	public Matrix jiangISM(int ontologyNodes, Matrix[] step3results, Matrix hsmJiang, DAGnode[] axisLabels)
    {
    	System.out.println("Calulating Jiang ISM...");
    	
    	Matrix ismJiang = new Matrix(ontologyNodes,ontologyNodes);
		ismJiang = (step3results[2].plus(hsmJiang)).times(0.5);
		
		//print jiang ism to file
    	try {
			printMatrix(ismJiang, axisLabels, "ISM_Jiang");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return ismJiang;
    }
	
	public BigDecimal linSemSim(DAGnode node1, DAGnode node2)
	{
		lowestCommonAncestor(node1, node2);
		int LCAresult=1000000;
		
		DAGnode [] LCAResults = new DAGnode[lcaResultsStore.size()];
		LCAResults=(DAGnode[]) lcaResultsStore.toArray(new DAGnode[0]);
		if(lcaResultsStore.size()>1)
		{	
			for(int i=0;i<LCAResults.length;i++)
			{
				if(LCAResults[i].annotations.size()<LCAresult)
				{
					LCAresult=LCAResults[i].annotations.size();
				}
			}
		}
		else
		{
			LCAresult=LCAResults[0].annotations.size();
		}
		//get the number of annotations on the same GO tree
		HashSet<Integer> annoNumber = new HashSet<Integer>();
		for(int i=0;i<noNodes;i++)
		{
			if(DAGedges[i].root!=null)
			{
				if(DAGedges[i].root.namespace.equals(node1.namespace)==true)
				{
					if(DAGedges[i].root.annotations.isEmpty()==false)
					{
						annoNumber.addAll(DAGedges[i].root.annotations);
					}
				}
			}
		}
		Definitions defs = new Definitions();
		if(node1.nodeValue.equals(defs.getMFrootValue())==true||node2.nodeValue.equals(defs.getMFrootValue())==true||node1.nodeValue.equals(defs.getBProotValue())==true||node2.nodeValue.equals(defs.getBProotValue())==true||node1.nodeValue.equals(defs.getCCrootValue())==true||node2.nodeValue.equals(defs.getCCrootValue())==true)
		{
			LCAresult=annoNumber.size(); //if any of the nodes if a root of DAG, then entropy must be all/all
		}
		
		BigDecimal LCAr = new BigDecimal(LCAresult);
		BigDecimal noAnnotations = new BigDecimal(annoNumber.size());
		BigDecimal probability = new BigDecimal(0); 
		probability = LCAr.divide(noAnnotations, 5, BigDecimal.ROUND_FLOOR); 
		
		double linValueFinal;
		if(LCAresult==annoNumber.size())
		{
			linValueFinal=0;
		}
		else
		{
			if((node1.nodeValue.equals(defs.getBProotValue())==true&&node2.nodeValue.equals(defs.getBProotValue())==true)||(node1.nodeValue.equals(defs.getCCrootValue())==true&&node2.nodeValue.equals(defs.getCCrootValue())==true)||(node1.nodeValue.equals(defs.getMFrootValue())==true&&node2.nodeValue.equals(defs.getMFrootValue())==true))
			{
				linValueFinal=0; //if both nodes are the root, the lin equation will divide by 0, as this result is not important the end result is set to 0
			}
			else
			{
				double linValueTop = Math.log(probability.doubleValue());
				if(linValueTop==0)
				{
					linValueFinal=0;
				}
				linValueTop = linValueTop*2; //this is the top half of the equation (2logPmica(c1,c2)) now for the bottom:
				BigDecimal node1entropy = new BigDecimal(node1.annotations.size());
				BigDecimal node2entropy = new BigDecimal(node2.annotations.size());
				node1entropy = node1entropy.divide(noAnnotations, 5, BigDecimal.ROUND_FLOOR);
				node2entropy = node2entropy.divide(noAnnotations, 5, BigDecimal.ROUND_FLOOR);
				if(node1entropy.doubleValue()==1&&node2entropy.doubleValue()==1)
				{
					linValueFinal=0;
				}
				double linValueBottom = Math.log(node1entropy.doubleValue())+Math.log(node2entropy.doubleValue());
				linValueFinal = linValueTop/linValueBottom;
			}
		}
		return new BigDecimal(linValueFinal);
	}

	public Object[] linHSM(String namespace, int ontologyNodes)
	{
		System.out.println("Calculating Lin HSM...");
		
		double[][] hsmLin;
		Object[] linHSMresults = new Object[2];
		
    	//create the HSM
		hsmLin = new double[ontologyNodes][ontologyNodes];
			
		BigDecimal linValueFinal;
		DAGnode[] nodeSet = new DAGnode[ontologyNodes];
    	
		int index=0;
		for(int u=0;u<noNodes;u++)
		{
			if(DAGedges[u].root!=null)
			{
				if(DAGedges[u].root.namespace.equals(namespace)==true)
				{
					if(DAGedges[u].root.nodeValue.startsWith("virtual")==false) 
					{ 
						nodeSet[index]=DAGedges[u].root;
						index++;
					}
				}
			}
		}
		
		for(int x=0;x<ontologyNodes;x++)
		{
			for(int y=0;y<ontologyNodes;y++)
			{
				linValueFinal = linSemSim(nodeSet[x],nodeSet[y]);
				hsmLin[x][y] = linValueFinal.doubleValue();
			}	
		}	    	
		
		Matrix mHSMlin = new Matrix(hsmLin);
		//print hsm to file
	    try {
			printMatrix(mHSMlin, nodeSet, "HSM_Lin");
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
		System.out.println("Lin HSM complete: ");
    	
    	linHSMresults[0] = new Object();
    	linHSMresults[0] = mHSMlin;
    	linHSMresults[1] = new Object();
    	linHSMresults[1] = nodeSet;
    	
    	return linHSMresults;
	}
	
	public Matrix linISM(int ontologyNodes, Matrix[] step3results, Matrix hsmLin, DAGnode[] axisLabels)
    {
    	System.out.println("Calulating Lin ISM...");
    	
    	Matrix ismLin = new Matrix(ontologyNodes,ontologyNodes);
		ismLin = (step3results[2].plus(hsmLin)).times(0.5);
		
		//print lin ism to file
    	try {
			printMatrix(ismLin, axisLabels, "ISM_Lin");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return ismLin;
    }
		
	public BigDecimal resnikSemSim(DAGnode node1, DAGnode node2)
	{
		lowestCommonAncestor(node1, node2);
		int LCAresult=1000000;
		
		DAGnode [] LCAResults = new DAGnode[lcaResultsStore.size()];
		LCAResults=(DAGnode[]) lcaResultsStore.toArray(new DAGnode[0]);
		if(lcaResultsStore.size()>1)
		{	
			for(int i=0;i<LCAResults.length;i++)
			{
				if(LCAResults[i].annotations.size()<LCAresult)
				{
					LCAresult=LCAResults[i].annotations.size();
				}
			}
		}
		else
		{
			LCAresult=LCAResults[0].annotations.size();
		}
		//get the number of annotations on the same GO tree
		HashSet<Integer> annoNumber = new HashSet<Integer>();
		for(int i=0;i<noNodes;i++)
		{
			if(DAGedges[i].root!=null)
			{	
				if(DAGedges[i].root.namespace.equals(node1.namespace)==true)
				{
					if(DAGedges[i].root.annotations.isEmpty()==false)
					{
						annoNumber.addAll(DAGedges[i].root.annotations);
					}
				}
			}
		}
		Definitions defs = new Definitions();
		if(node1.nodeValue.equals(defs.getMFrootValue())==true||node2.nodeValue.equals(defs.getMFrootValue())==true||node1.nodeValue.equals(defs.getBProotValue())==true||node2.nodeValue.equals(defs.getBProotValue())==true||node1.nodeValue.equals(defs.getCCrootValue())==true||node2.nodeValue.equals(defs.getCCrootValue())==true)
		{
			LCAresult=annoNumber.size(); //if any of the nodes if a root of DAG, then entropy must be all/all
		}
		BigDecimal LCAr = new BigDecimal(LCAresult);
		BigDecimal noAnnotations = new BigDecimal(annoNumber.size());
		BigDecimal probability = new BigDecimal(0);
		probability = LCAr.divide(noAnnotations, 5, BigDecimal.ROUND_FLOOR); 
		double resnikValue =-Math.log(probability.doubleValue());
		return new BigDecimal(resnikValue);
	}
	
	public Object[] resnikHSM(String namespace, int ontologyNodes)
	{
		System.out.println("Calculating Resnik HSM...");
		
		double[][] hsmResnik;
		Object[] hsmresults = new Object[2];
		
    	//create the HSM
		hsmResnik = new double[ontologyNodes][ontologyNodes];
			
		BigDecimal resnikValue;
		DAGnode[] nodeSet = new DAGnode[ontologyNodes];
    	
		int index=0;
		for(int u=0;u<noNodes;u++)
		{
			if(DAGedges[u].root!=null)
			{
				if(DAGedges[u].root.namespace.equals(namespace)==true)
				{
					if(DAGedges[u].root.nodeValue.startsWith("virtual")==false) 
					{ 
						nodeSet[index]=DAGedges[u].root;
						index++;
					}
				}
			}
		}
		
		for(int x=0;x<ontologyNodes;x++)
		{
			for(int y=0;y<ontologyNodes;y++)
			{
				resnikValue = resnikSemSim(nodeSet[x],nodeSet[y]);
				hsmResnik[x][y] = resnikValue.doubleValue();
			}	
		}	    	
		
		Matrix mHSMresnik = new Matrix(hsmResnik);
		//print hsm to file
	    try {
			printMatrix(mHSMresnik, nodeSet, "HSM_Resnik");
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    hsmresults[0] = new Object();
	    hsmresults[0] = mHSMresnik;
	    hsmresults[1] = new Object();
	    hsmresults[1] = nodeSet;
	    
		System.out.println("Resnik HSM complete: ");
    	
    	return hsmresults;
	}
	
	public Matrix resnikISM(int ontologyNodes, Matrix[] step3results, Matrix hsmResnik, DAGnode[] axisLabels)
    {
    	System.out.println("Calulating Resnik ISM...");
    	
    	Matrix ismResnik = new Matrix(ontologyNodes,ontologyNodes);
		ismResnik = (step3results[2].plus(hsmResnik)).times(0.5);
		
		//print resnik ism to file
    	try {
			printMatrix(ismResnik, axisLabels, "ISM_Resnik");
		} catch (IOException e) {
			e.printStackTrace();
		}
    	return ismResnik;
    }
	
	public boolean lowestCommonAncestor(DAGnode node1, DAGnode node2)
	{
		String [] LCAbadresults = new String[2];
		if(node1.annotations.isEmpty()==true||node2.annotations.isEmpty()==true)
		{
			System.out.println("no annotations on nodes provided");
			LCAbadresults[0]="no annotations on nodes provided";
			LCAbadresults[1]="0";
			return false;
		}
		else
		{
			Integer [] nodeAannos = new Integer[node1.annotations.size()];
			nodeAannos=(Integer[]) node1.annotations.toArray(new Integer[0]);
			Integer [] nodeBannos = new Integer[node2.annotations.size()];
			nodeBannos=(Integer[]) node2.annotations.toArray(new Integer[0]); 
			
			int [] targetVal= new int[2];
			targetVal[0]=-1;
			boolean singleVal=false;
			outerLoop:
			for(int i=0;i<nodeAannos.length;i++)
			{
				for(int j=0;j<nodeBannos.length;j++)
				{
					if(nodeAannos[i].equals(nodeBannos[j])==true)
					{
						targetVal[0]=nodeAannos[i];
						singleVal=true;
						break outerLoop;
					}
				}
			}
			if(targetVal[0]!=-1)
			{
				lcaResultsStore.clear();
				LCAcalc(targetVal, GOleaves, singleVal); 
			}
			else
			{
				targetVal[0] = nodeAannos[0];
				targetVal[1] = nodeBannos[0];
				lcaResultsStore.clear(); 
				LCAcalc(targetVal, GOleaves, singleVal); 
			}
		}
		return true;
	}
	
	public void LCAcalc(int [] targetVal, HashSet<DAGnode> leaves, boolean singleVal)
	{
		HashSet<DAGnode> parents = new HashSet<DAGnode>();
		DAGnode [] leafArray = new DAGnode[leaves.size()];
		leafArray=(DAGnode[]) leaves.toArray(new DAGnode[0]);
		
		DAGnode currentNode=null;
		
		for(int i=0;i<leaves.size();i++)
		{
			if(leafArray[i]!=null)
			{
				if(leafArray[i].nodeValue.startsWith("virtual")==false)
				{
					currentNode = leafArray[i];
			
					DAGnode parent=null;
					if(currentNode.next!=null)
					{
						parent=currentNode.next;
						//go through all parents
						while(parent!=null)
						{
							int parentIndex = parent.storageIndex;
						//if parent contains the target value & therefore is the lowest common ancestor
							if(singleVal==true)
							{
								if(DAGedges[parentIndex].root!=null)//nodes have been pruned so some references'll be incorrect 
								{
									if(DAGedges[parentIndex].root.annotations.contains(targetVal[0])==true)
									{//return the node
										lcaResultsStore.add(DAGedges[parentIndex].root);
									}
								}
							}
							else
							{ //or parent contains the target values & therefore is the lowest common ancestor
								if(DAGedges[parentIndex].root!=null)//nodes have been pruned so some references'll be incorrect 
								{
									if(DAGedges[parentIndex].root.annotations.contains(targetVal[0])==true&&DAGedges[parentIndex].root.annotations.contains(targetVal[1])==true)
									{//return the node
										lcaResultsStore.add(DAGedges[parentIndex].root);
									}
								}
							}
						//else add the parent to the next set of nodes to look through the parents of & move to the current nodes next parent (if any)
							parents.add(DAGedges[parentIndex].root);
							parent=parent.next;
						}
					}
				}
			}
		}
		//on to the next stage
		if(parents.isEmpty()==false)
		{
			LCAcalc(targetVal, parents, singleVal);
		}
		return;
	}
}

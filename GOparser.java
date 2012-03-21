import java.io.*;
import java.util.Scanner;

public class GOparser {
	
	public int OntologyPrep(File goFile) throws FileNotFoundException
	{
		//this method returns the number of go nodes in the ontology file
		int noNodes=0;
		boolean lastLine=false;
		Scanner quickScan = new Scanner(goFile);
		String scanString = quickScan.nextLine();
		while(quickScan.hasNextLine()||lastLine==true)
		{
			if(scanString.equals("[Term]")==true)
			{
				if(quickScan.next().equals("id:")==true)
				{
					String nodeCheck = quickScan.next(); //outsource this declaration
					if(nodeCheck.contains("GO:"))
					{
						noNodes++;
					}
				}
			}
			if(lastLine!=true)
			{
				scanString=quickScan.nextLine();
				if(quickScan.hasNextLine()==false)
				{
					lastLine=true;
				}
			}
			else
			{
				lastLine=false;//stop it looping after the last line
			}
		}
		System.out.println("Number of Nodes: "+noNodes);
		return noNodes;
	}
	
	public void parseGO(File gofile, DAGstructure DAG, String goDecision) throws FileNotFoundException
	{
		Scanner goScan = new Scanner(gofile);
		String scanString = goScan.next();
		
		String nodeValue="";
		String namespace="";
		String [] relationsType = new String[50];
		String [] relationsVal = new String[50];
		boolean afterFirstPass = false; //stops write loop quitting before start
		int relationsIndex=0;
		
		while(goScan.hasNext())
		{
			nodeValue="";
			namespace="";
			for(int i=0;i<50;i++)
			{
				relationsType[i] = null;
				relationsVal[i] = null;
			}
			afterFirstPass = false; //stops write loop quitting before start
			relationsIndex=0;
			if(scanString.equals("[Term]")==true) //new node
			{
				while(true)
				{
					if(scanString.equals("[Term]")==true && afterFirstPass==true)
					{
						break;
					}
					afterFirstPass=true;
					if(goScan.hasNext()==true)
					{
						scanString = goScan.next();
					}
					else
					{
						break;
					}
					if(scanString.equals("id:")==true)
					{
						String nodeChecker = goScan.next();
						if(nodeChecker.contains("GO:"))
						{
							nodeValue=nodeChecker;
						}
					}
					if(scanString.equals("namespace:")==true)
					{
						namespace = goScan.next();
					}
					if(scanString.equals("is_a:")==true)
					{
						relationsVal[relationsIndex] = goScan.next(); 
						relationsType[relationsIndex] = "is_a";
						relationsIndex++;
					}
					if(scanString.equals("relationship:")==true)
					{
						relationsType[relationsIndex] = goScan.next(); 
						relationsVal[relationsIndex] = goScan.next();
						relationsIndex++;
					}
				}
				//add new node with this info
				//add new node instance for each relationship type
				int i=0;
				boolean notRun=true;
				while(i<relationsIndex||notRun==true)
				{
					if(namespace.equals(goDecision)==true)
					{
						DAG.addNode(nodeValue, namespace);
						if(relationsVal[i]!=null||relationsType[i]!=null)
						{
							DAG.addNeighbour(nodeValue, relationsVal[i], namespace, relationsType[i]);
						}
					}
					i++;
					notRun=false;
				}
			}
			else
			{
				scanString = goScan.next();
			}
		}
	}
	
	public int AnnotationPrep(File annotationFile) throws FileNotFoundException
	{
		//this method returns the number of annotations in an annotation file
		char skipCharacter = '!';
		int noAnnotations=0;
		boolean lastLine=false;
		Scanner quickScan = new Scanner(annotationFile);
		String scanString = quickScan.nextLine();
		while(quickScan.hasNextLine()||lastLine==true)
		{
			if(scanString.charAt(0)==skipCharacter)
			{
				scanString = quickScan.nextLine();
			}	
			else
			{
				noAnnotations++;
				if(lastLine!=true)
				{
					scanString=quickScan.nextLine();
					if(quickScan.hasNextLine()==false)
					{
						lastLine=true;
					}
				}
				else
				{
					lastLine=false;//stop it looping after the last line
				}
			}
		}
		return noAnnotations;
	}
	
	public void parseAnnotation(File annotationFile, DAGstructure DAG) throws FileNotFoundException
	{
		Scanner annoScan = new Scanner(annotationFile);
		String scanString = annoScan.nextLine();
		String [] wholeLine = new String[17];
		
		String DB = "";
		String DB_ObjectID = "";
		String GO_id = "";
		String aspect = "";
		String DB_ObjectType = "";
		char skipCharacter = '!';
		int lineIndex=0;
		boolean lastLine =false;
		int annotationStorageIndex=0;
		
		while(annoScan.hasNextLine()||lastLine==true)
		{
			for(int i=0;i<wholeLine.length;i++)
			{
				wholeLine[i]="";
			}
			DB = "";
			DB_ObjectID = "";
			GO_id = "";
			aspect = "";
			DB_ObjectType = "";
			lineIndex=3; //the first 3 array items will always be there
			
			if(scanString.equals("!")==true||scanString.charAt(0)==skipCharacter)
			{
				scanString = annoScan.nextLine();
			}
			else
			{
				wholeLine = scanString.split("\t");
			
			DB = wholeLine[0];
			DB_ObjectID = wholeLine[1];
			if(wholeLine[3].contains("GO:"))
			{
				GO_id=wholeLine[lineIndex];
				lineIndex++;
			}
			else
			{
				lineIndex++;
				GO_id=wholeLine[lineIndex];
				lineIndex++;
			}
			lineIndex++;
			lineIndex++;
			if(wholeLine[lineIndex].length()==1)
			{
				aspect = wholeLine[lineIndex];
				lineIndex++;
			}
			else
			{
				lineIndex++;
				aspect = wholeLine[lineIndex];
				lineIndex++;
			}
			if(wholeLine[lineIndex+1].contains("taxon"))
			{
				DB_ObjectType=wholeLine[lineIndex];
				lineIndex++;
				lineIndex++;
			}
			else if(wholeLine[lineIndex+3].contains("taxon"))
			{
				lineIndex++;
				lineIndex++;
				DB_ObjectType=wholeLine[lineIndex];
				lineIndex++;
				lineIndex++;
			}
			else //occurs if only 1 of the optional fields is there
			{
				lineIndex++;
				DB_ObjectType= wholeLine[lineIndex];
				lineIndex++;
				lineIndex++;
			}
			DAG.annotationElements[annotationStorageIndex]=new AnnoNode(DB, DB_ObjectID, GO_id, aspect, DB_ObjectType);
			for(int j=0; j<DAG.noNodes; j++)
			{
				if(DAG.DAGedges[j].root!=null)
				{
					if(DAG.DAGedges[j].root.nodeValue.equals(GO_id)==true)
					{
						DAG.DAGedges[j].root.annotations.add(annotationStorageIndex);
					}
				}
			}
		    annotationStorageIndex++;
			if(lastLine!=true)
			{
				scanString=annoScan.nextLine();
				if(annoScan.hasNextLine()==false)
				{
					lastLine=true;
				}
			}
			else
			{
				lastLine=false;//stop it looping after the last line
			}
			}
		}
	}
}

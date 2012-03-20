import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import Jama.Matrix;

public class RunProgram {
	public static void main(String args[]) throws IOException
	{
		String currentDirectory = new File("").getAbsolutePath();
		GOparser goParserInstance = new GOparser();
		File goFile = new File(currentDirectory+"/gene_ontology.1_2.obo");
		if(goFile.isFile()==false)
		{
			System.out.println("Gene Ontology file not in current directory");
			System.out.println("Filepath Expected:"+currentDirectory+"/gene_ontology.1_2.obo");
			System.exit(0);
		}
		Scanner usrInput = new Scanner(System.in);
		boolean correctInput=true;
		
		String goDecision="", annotationDecision="", semsimDecision="";
		int annotationChoice=0, goChoice=0, semsimChoice=0;
		
		System.out.println("Program Started");
		System.out.println("Which GO tree do you wish to mount the annotation file upon?:");
		System.out.println("");
		System.out.println("1. Molecular Function");
		System.out.println("2. Biological Process");
		System.out.println("3. Cellular Component");
		System.out.println("");
		System.out.println("Enter the number of the desired GO tree: ");
		goChoice = usrInput.nextInt();
		switch(goChoice)
		{
		case 1: goDecision="molecular_function";
		break;
		case 2: goDecision="biological_process";
		break;
		case 3: goDecision="cellular_component";
		break;
		default: correctInput=false;
		break;
		}
		System.out.println("Please select one of the following available GO annotations or ");
		System.out.println("place your desired annotation file in the software main directory and input it's name here:");
		System.out.println("");
		System.out.println("1. Agrobacterium tumefaciens; GP:82 A:248 Db:PAMGO");
		System.out.println("2. Oomycetes; GP:30 A:126 Db:PAMGO");
		System.out.println("3. Leishmania major; GP:10 A:27 Db:Sanger GeneDB");
		System.out.println("4. Dickeya dadantii; GP:124 A:296 Db:PAMGO");
		System.out.println("5. Solanaceae; GP:148 A:269 Db:SGN");
		System.out.println("6. Plasmodium falciparum; GP:2198 A:4606 Db:Sanger GeneDB");
		System.out.println("");
		System.out.println("GP: Gene Products annotated.  A: Annotations.  Db: Database.");
		System.out.println("");
		System.out.println("Enter the number of the desired annotation: ");
		String annotationChoiceString = usrInput.next();
		int fileSupplied=0;
		try
		{
			annotationChoice = Integer.parseInt(annotationChoiceString);
		}
		catch (java.lang.NumberFormatException nfe)
		{
			File fileTester = new File(currentDirectory+"/"+annotationChoiceString);
			fileSupplied=2;
			if(fileTester.isFile()==true)
			{
				fileSupplied=1;
			}
		}
		//annotationChoice = usrInput.nextInt();
		File annotationFile = null;
		if(fileSupplied==1)
		{
			annotationFile = new File(currentDirectory+"/"+annotationChoiceString);
		} //gene_association.PAMGO_Atumefaciens
		else if(fileSupplied==2)
		{
			System.out.println("Incorrect Input");
			System.exit(0);
		}
		else 
		{
			//System.out.println("hit");
			switch(annotationChoice)
			{
			case 1: annotationDecision="PAMGO_Atumefaciens";
			break;
			case 2: annotationDecision="PAMGO_Oomycetes";
			break;
			case 3: annotationDecision="GeneDB_Lmajor";
			break;
			case 4: annotationDecision="PAMGO_Ddadantii";
			break;
			case 5: annotationDecision="sgn";
			break;
			case 6: annotationDecision="GeneDB_Pfalciparum";
			break;
			default: {
				correctInput=false;
			}
			break;
			}
			annotationFile = new File(currentDirectory+"/annotations/gene_association."+annotationDecision);
		}
		if(annotationFile.isFile()==false)
		{
			System.out.println("Annotation file not in annotations directory, add them to application directory and enter them by name if problem persists");
			System.out.println("Filepath Expected:"+currentDirectory+"/annotations/gene_association."+annotationDecision);
			System.exit(0);
		}
		System.out.println("What would you like to calculate? (Bare in mind that calculating an ISM will give you its corresponding HSM):");
		System.out.println("");
		System.out.println("1. HSM using Lin's Sematic Similarity Measure");
		System.out.println("2. HSM using Resnik's Sematic Similarity Measure");
		System.out.println("3. HSM using Jiang's Sematic Similarity Measure");
		System.out.println("4. The 3 HSM's described above");
		System.out.println("5. ISM based on Lin's Sematic Similarity Measure");
		System.out.println("6. ISM based on Resnik's Sematic Similarity Measure");
		System.out.println("7. ISM based on Jiang's Sematic Similarity Measure");
		System.out.println("8. The 3 ISM's described above");
		System.out.println("");
		System.out.println("If you wish to simply calculate the Simantic Similarity of pairs of genes, pick the most appropriate option");
		System.out.println("");
		System.out.println("Enter the number of the calculation you wish to perform: ");
		semsimChoice = usrInput.nextInt();
		int noNodes = goParserInstance.OntologyPrep(goFile);
		int noAnnotations = goParserInstance.AnnotationPrep(annotationFile);
		DAGstructure DAG = new DAGstructure(noNodes, noAnnotations);
		
		if(correctInput==false)
		{
			semsimChoice=-1;
		}
		Object[] semsimResults = new Object[4];
		switch(semsimChoice)
		{
		case 1: {
			System.out.println("Importing Data...");
			goParserInstance.parseGO(goFile, DAG, goDecision);
			System.out.println("GO Tree Imported");
			goParserInstance.parseAnnotation(annotationFile, DAG);
			System.out.println("Annotation File Imported");
			DAG.propagate();
			semsimResults = DAG.semanticSimilarity(goDecision, semsimChoice);
			break;
		}
		case 2: {
			System.out.println("Importing Data...");
			goParserInstance.parseGO(goFile, DAG, goDecision);
			System.out.println("GO Tree Imported");
			goParserInstance.parseAnnotation(annotationFile, DAG);
			System.out.println("Annotation File Imported");
			DAG.propagate();
			semsimResults = DAG.semanticSimilarity(goDecision, semsimChoice);
			break;
		}
		case 3: { 
			System.out.println("Importing Data...");
			goParserInstance.parseGO(goFile, DAG, goDecision);
			System.out.println("GO Tree Imported");
			goParserInstance.parseAnnotation(annotationFile, DAG);
			System.out.println("Annotation File Imported");
			DAG.propagate();
			semsimResults = DAG.semanticSimilarity(goDecision, semsimChoice);
			break;
		}
		case 4: {
			System.out.println("Importing Data...");
			goParserInstance.parseGO(goFile, DAG, goDecision);
			System.out.println("GO Tree Imported");
			goParserInstance.parseAnnotation(annotationFile, DAG);
			System.out.println("Annotation File Imported");
			DAG.propagate();
			semsimResults = DAG.semanticSimilarity(goDecision, semsimChoice);
			break;
		}
		case 5: {
			System.out.println("Importing Data...");
			goParserInstance.parseGO(goFile, DAG, goDecision);
			System.out.println("GO Tree Imported");
			goParserInstance.parseAnnotation(annotationFile, DAG);
			System.out.println("Annotation File Imported");
			DAG.propagate();
			semsimResults = DAG.semanticSimilarity(goDecision, semsimChoice);
			break;
		}
		case 6: {
			System.out.println("Importing Data...");
			goParserInstance.parseGO(goFile, DAG, goDecision);
			System.out.println("GO Tree Imported");
			goParserInstance.parseAnnotation(annotationFile, DAG);
			System.out.println("Annotation File Imported");
			DAG.propagate();
			semsimResults = DAG.semanticSimilarity(goDecision, semsimChoice);
			break;
		}
		case 7: {
			System.out.println("Importing Data...");
			goParserInstance.parseGO(goFile, DAG, goDecision);
			System.out.println("GO Tree Imported");
			goParserInstance.parseAnnotation(annotationFile, DAG);
			System.out.println("Annotation File Imported");
			DAG.propagate();
			semsimResults = DAG.semanticSimilarity(goDecision, semsimChoice);
			break;
		}
		case 8: {
			System.out.println("Importing Data...");
			goParserInstance.parseGO(goFile, DAG, goDecision);
			System.out.println("GO Tree Imported");
			goParserInstance.parseAnnotation(annotationFile, DAG);
			System.out.println("Annotation File Imported");
			DAG.propagate();
			semsimResults = DAG.semanticSimilarity(goDecision, semsimChoice);
	        break;
		}
		default: {
			System.out.println("Incorrect Input");
			correctInput=false;
			break;
		}
		}
		System.out.println("The results are to large to output here so have been written to seperate files within the outputs folder.");
		if(correctInput==true)
		{
			System.out.println("Would you like to see individual Semantic Similarity values? (y/n)");
			semsimDecision = usrInput.next();
			if(semsimDecision.equals("y")==true||semsimDecision.equals("Y")==true)
			{
				boolean quit=false;
				while(quit==false)
				{
					DAGnode[] axisLabels = (DAGnode[])semsimResults[0];
					String node1="",node2="";
					int nodeA=-1, nodeB=-1;
					System.out.println("Pick 2 nodes from the following list: ");
					int counter=1;
					for(int i=0; i<axisLabels.length; i++)
					{
						System.out.print(axisLabels[i].nodeValue+" ");
						if(counter%3==0)
						{
							System.out.println("");
						}
						counter++;
					}
					System.out.println("");
					System.out.println("Node 1: ");
					node1=usrInput.next();
					System.out.println("Node 2: ");
					node2=usrInput.next();
					for(int i=0; i<axisLabels.length; i++)
					{
						if(axisLabels[i].nodeValue.equals(node1)==true)
						{
							nodeA=i;//axis[i];
						}		
						if(axisLabels[i].nodeValue.equals(node2)==true)
						{
							nodeB=i;//axis[i];
						}
					}
					if(semsimChoice!=4&&semsimChoice!=8)
					{
						Matrix resultMatrix= (Matrix)semsimResults[1];
						System.out.println("");
						if(nodeA>0 && nodeB>0)
						{
						System.out.println("The Semantic Similarity between "+node1+" and "+node2+" using the chosen similarity measure is:");
						System.out.println(resultMatrix.get(nodeA, nodeB));
						}
						else
						{
							System.out.println("Incorrect Input");
						}
					}	
					else //TODO what about the HSM's in choice 8?
					{
						Matrix[] resultMatrixArray = new Matrix[3];
						resultMatrixArray[0] = (Matrix)semsimResults[1]; //lin
						resultMatrixArray[1] = (Matrix)semsimResults[2]; //resnik
						resultMatrixArray[2] = (Matrix)semsimResults[3]; //jiang
						int matrixChoice=0;
						System.out.println("");
						System.out.println("Which similarity measure would you like to use the results of?: ");
						System.out.println("");
						System.out.println("1. Lin");
						System.out.println("2. Resnik");
						System.out.println("3. Jiang");
						System.out.println("");
						System.out.println("Enter the number corresponding to your choice: ");
						matrixChoice=usrInput.nextInt();
						switch(matrixChoice)
						{
						case 1:{
							System.out.println("");
							System.out.println("The Semantic Similarity between "+node1+" and "+node2+" using the chosen similarity measure is:");
							System.out.println(resultMatrixArray[0].get(nodeA, nodeB));
							break;
						}	
						case 2:{
							System.out.println("");
							System.out.println("The Semantic Similarity between "+node1+" and "+node2+" using the chosen similarity measure is:");
							System.out.println(resultMatrixArray[1].get(nodeA, nodeB));
							break;
						}	
						case 3:{
							System.out.println("");
							System.out.println("The Semantic Similarity between "+node1+" and "+node2+" using the chosen similarity measure is:");
							System.out.println(resultMatrixArray[2].get(nodeA, nodeB));
							break;
						}	
						}
					}
					System.out.println("");
					System.out.println("Would you like to find another value? (y/n)");
					String quitChoice = usrInput.next();
					if(quitChoice.equals("n")==true||quitChoice.equals("N")==true)
					{
						quit=true;
					}
				}
			}
		}
		System.out.println("Thank you for using this software");
	}
}

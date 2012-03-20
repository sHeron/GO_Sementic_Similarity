
public class Definitions {
	private String molecularFunctionRootValue;
	private String biologicalProcessRootValue;
	private String cellularComponentRootValue;
	Definitions()
	{
		molecularFunctionRootValue="GO:0003674";
		biologicalProcessRootValue="GO:0008150";
		cellularComponentRootValue="GO:0005575";
	}
	public String getMFrootValue()
	{
		return molecularFunctionRootValue;
	}
	public String getBProotValue()
	{
		return biologicalProcessRootValue;
	}
	public String getCCrootValue()
	{
		return cellularComponentRootValue;
	}
}

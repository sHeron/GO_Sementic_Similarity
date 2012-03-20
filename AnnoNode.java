public class AnnoNode {

	public String DB;
	public String DB_ObjectID;
	public String GO_id;
	public String aspect;
	public String DB_ObjectType;
	AnnoNode(String DB, String DB_ObjectID, String GO_id, String aspect, String DB_ObjectType)
	{
		this.DB = DB;
		this.DB_ObjectID = DB_ObjectID;
		this.GO_id = GO_id;
		this.aspect = aspect;
		this.DB_ObjectType = DB_ObjectType;
	}
}

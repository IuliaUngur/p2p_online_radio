import java.io.Serializable;


public class MessageObject implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int id;
	private String path;
	
	public MessageObject(int id, String path){
		
		this.id = id;
		this.path = path;
		
	}

	public int getId() {
		return id;
	}

	public String getPath() {
		return path;
	}
	
	
	
}

package data;

import java.util.ArrayList;
import java.util.List;

public class Node {
	
	private int id;	
	private String content;
	private String type;
	private int color;
	private int typeId;
		
	public List<Edge> neighbors;
	
	
	public Node(int id, String type, String content) throws Exception {
		this(id, type);
		this.content = content;
	}
	
	public Node(int id, String type, int typeId, int color) throws Exception {
		this(id, type);
		this.typeId = typeId;
		this.color = color;
	}
	
	public Node(int id, String type) throws Exception {
		this.id = id;
		this.neighbors = new ArrayList<Edge>();
		this.type = type.toLowerCase().intern();
	}
	
	public int getColor() {
		return color;
	}
	
	public int getTypeId() {
		return typeId;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getContent() {
		return content;
	}
	
	public String getType() {
		return type;
	}
	

	@Override
	public int hashCode() {
		return id;
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Node && ((Node)o).id == this.id;
	}

	
}

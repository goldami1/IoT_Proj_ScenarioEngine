package tmpContainers;

import com.google.gson.annotations.SerializedName;

public class VenNameIDContainer {

	@SerializedName("id")
	private short id;
	@SerializedName("name")
	private String name;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public short getId() {
		return id;
	}

	public void setId(short id) {
		this.id = id;
	}
	
	public VenNameIDContainer(String i_name, short i_id)
	{
		name=i_name;
		id=i_id;
	}
	
}

package server;

public class Command {
	private String name;
	private String form;
	private Boolean isImplemented;
	
	public Command(String name, String form, Boolean isImplemented) {
		this.name = name;
		this.form = form;
		this.isImplemented = isImplemented;
	}

	public Boolean getIsImplemented() {
		return isImplemented;
	}

	public void setIsImplemented(Boolean isImplemented) {
		this.isImplemented = isImplemented;
	}

	public String getForm() {
		return form;
	}

	public void setForm(String form) {
		this.form = form;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		String commandTxt = "* "+this.getName()+" "+this.getForm()+" ";
		if(this.getIsImplemented()){
			commandTxt += "1";
		}else{
			commandTxt += "0";
		}
		commandTxt+="\n";
		
		return commandTxt;
	}
}

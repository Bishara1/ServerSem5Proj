package common;

import java.io.Serializable;

public class Message implements Serializable {
	private static final long serialVersionUID = 1L;
	private Object content;
	private Command command;
	
	/**
	 *  constructor that gets the information of message and sets all attribute to new values
	 * @param content content of message
	 * @param command command of message
	 */
	public Message(Object content, Command command) {
		this.content = content;
		this.command = command;
	}
	
	/**
	 * @return content 
	 */
	public Object getContent() {
		return content;
	}

	
	/**
	 * Sets content
	 * @param content contents from message
	 */
	public void setContent(Object content) {
		this.content = content;
	}

	/**
	 * @return command
	 */
	public Command getCommand() {
		return command;
	}

	
	/**
	 * Sets command
	 * @param command command of message
	 */
	public void setCommand(Command command) {
		this.command = command;
	}
	
	
	/**
	 * returns the contents of this message
	 */
	@Override
	public String toString() {
		return "Message of type: " + command + " " + content ;
	}
}

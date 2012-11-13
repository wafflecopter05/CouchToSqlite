package edu.asu.beastd;

import java.io.*;
import java.util.*;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

/**
 * Form class deals with JSON creation/initialization.
 * @author BEASTD
 */
public class Form {
	
	/** 
	LEGACY (RIP)

	String _id, _rev, type, lastUpdateTime, dateOfService, name, inkStrokes, patientId, visitorId, templateId, signerUserId;
	Boolean isSigned;
	int dateSigned;
	
	public Form(){
		isSigned = false;
		inkStrokes = "";
		_id = "";
		_rev = "";
		type = "";
		lastUpdateTime = "";
		name = "";
		patientId = "";
		visitorId = "";
		templateId = "";
		dateOfService = "";
		dateSigned = 0;
		signerUserId = "";
	}
	
	public void readFile(Scanner scan) throws IOException{
		System.out.println("Please enter file name.");
		String fileName = scan.nextLine();
		File file = new File(fileName);
		if(!file.exists()){
			System.out.println(fileName + " does not exist.");
		}
		if(!(file.isFile() && file.canRead())){
			System.out.println(file.getName() + " cannot be read from");
		}
		
		try{	//parse the file
			FileInputStream fis = new FileInputStream(file);
			Scanner input = new Scanner(fis);
			String current;
			current = input.next();
			current = input.next();
			while(input.hasNext()){
				if(current.equals("\"_id\":")){
					current = input.next();
					if(current.charAt(current.length()-1) == (',')){
						current = current.substring(0, current.length()-1);
					}
					_id = current;
					current = input.next();
				}
				else if(current.equals("\"_rev\":")){
					current = input.next();
					if(current.charAt(current.length()-1) == (',')){
						current = current.substring(0, current.length()-1);
					}
					_rev = current;
					current = input.next();
				}
				else if(current.equals("\"type\":")){
					current = input.next();
					if(current.charAt(current.length()-1) == (',')){
						current = current.substring(0, current.length()-1);
					}
					type = current;
					current = input.next();
				}
				else if(current.equals("\"lastUpdateTime\":")){
					current = input.next();
					if(current.charAt(current.length()-1) == (',')){
						current = current.substring(0, current.length()-1);
					}
					lastUpdateTime = current;
					current = input.next();
				}
				else if(current.equals("\"name\":")){
					//check for entire name (spaces)
					current = input.next();
					String n = "";
					while(!(current.equals("\"patientId\":"))){	//patientId should be next in file.
						n += current + " "; 
						current = input.next();
					}
					n = n.trim();
					if(n.charAt(n.length()-1) == (',')){
						n = n.substring(0, n.length()-1);
					}
					name = n;
				}
				else if(current.equals("\"patientId\":")){
					current = input.next();
					if(current.charAt(current.length()-1) == (',')){
						current = current.substring(0, current.length()-1);
					}
					patientId = current;
					current = input.next();
				}
				else if(current.equals("\"templateId\":")){
					current = input.next();
					if(current.charAt(current.length()-1) == (',')){
						current = current.substring(0, current.length()-1);
					}
					templateId = current;
					current = input.next();
				}
				else if(current.equals("\"dateOfService\":")){
					current = input.next();
					if(current.charAt(current.length()-1) == (',')){
						current = current.substring(0, current.length()-1);
					}
					dateOfService = current;
					current = input.next();
				}
				else if(current.equals("\"inkStrokes\":")){
					current = input.next();
					if(current.charAt(current.length()-1) == (',')){
						current = current.substring(0, current.length()-1);
					}
					inkStrokes = current;
					current = input.next();
				}
				else if(current.equals("\"visitorId\":")){
					current = input.next();
					if(current.charAt(current.length()-1) == (',')){
						current = current.substring(0, current.length()-1);
					}
					visitorId = current;
					current = input.next();
				}
			}
			input.close();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public String toString(){
		String str = "";
		str += "_id: " + _id + " ";
		str += "_rev: " + _rev + " ";
		str += "type: " + type + " ";
		str += "lastUpdateTime: " + lastUpdateTime + " ";
		str += "name: " + name + " ";
		str += "patientId: " + " ";
		str += "templateId: " + templateId + " ";
		str += "dateOfService: " + dateOfService + " ";
		str += "visitorId: " + visitorId + " ";
		str += "inkStrokes: " + inkStrokes;
		
		return str;
	}
	**/
	
	/**
	 * Creates a JSONObject from a file.
	 * @param filepath Location of file.
	 * @author BEASTD
	 * @return JSONObject of file
	 * @throws FileNotFoundException
	 */
	public JSONObject JsonParsing(String filepath) throws FileNotFoundException {
		InputStream is = new FileInputStream(filepath);
		Scanner scan = new Scanner(is).useDelimiter("\\A");
		String jsonString = scan.hasNext() ? scan.next() : "";
		scan.close();
		
		JSONParser parser = new JSONParser();
		Object obj = new Object();
		try {
			obj = parser.parse(jsonString);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return (JSONObject) obj;
	}
}

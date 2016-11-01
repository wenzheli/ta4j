package eu.verdelhan.ta4j.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class SecurityCodeConverter {
	public Map<String, String> secodeToCode = new HashMap<String, String>();;
	
	public SecurityCodeConverter() throws IOException{
		init();
	}
	
	public void init() throws IOException{
		// read from the file and construct the mapping
		String path = "/Users/wenzheli/Documents/workspace/benew/ta4j/ta4j/resource/code_secode_name.csv";
		BufferedReader br = null;
		String line = "";
		br = new BufferedReader(new FileReader(path));
		br.readLine();    // skip the header line
		while ((line = br.readLine()) != null) {
			String[] strs = line.split(",");
			secodeToCode.put(strs[1], format(strs[0]));
		}
	}
	
	/**
	 * Format the string such that each string has length of 6
	 * @param s
	 * @return
	 */
	private String format(String s){
		int len = s.length();
		for (int i = 0; i < 6 - len; i++){
			s = "0" + s;
		}
		
		return s;
	}
	
	public String getCodeFromSecode(String secode){
		return secodeToCode.get(secode);
	}	
	
	public static void main(String[] args) throws IOException{
		SecurityCodeConverter converter = new SecurityCodeConverter();
	}
}

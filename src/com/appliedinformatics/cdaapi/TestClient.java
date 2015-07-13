package com.appliedinformatics.cdaapi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;

/**
 * Test Client on localhost. Reads test CDA_Samples and makes REST requests
 *  
 * @author Chintan Patel <chintan@trialx.com>
 *
 */
public class TestClient {

	public static void main(String[] args) throws IOException {
		ClientResource requestResource = new ClientResource("http://localhost:8182/bbplus");
	    String str  = readFileAsString("/Users/aamirbhatt/workspace/pa_frontend/CCDATest5.xml");
		//Add CDA XML by making a POST request on CDAResource
	    Representation rep = new StringRepresentation(str);
	    
	    Form form = new Form();
	    form.add("bbfile",str);
	    form.add("op_format", "json");
	    
	    Representation reply = requestResource.post(form);
	    
	    System.out.println("JSON format:");
	    System.out.println(reply.getText());
	    
	    //Get a section, make a GET Request on CDAResource
	    requestResource = new ClientResource("http://localhost:8182/bbplus/12375680/demographics");
	    Reference ref = requestResource.getReference();
	    ref.addQueryParameter("patient", "996-756-495");
	   // ref.addQueryParameter("section", "problems");
	    //ref.addQueryParameter("section", "allergies");
	    //ref.addQueryParameter("section", "results");
	    ref.addQueryParameter("section", "demographics");

	    requestResource.setReference(ref);
	    Representation res = requestResource.get();
	    
	    //Print the output on the console
	    System.out.println("DEMOGRAPHICS:");
	    System.out.println(res.getText());
	    System.out.println(res.toString());
	}
	
	private static String readFileAsString(String filePath) throws IOException {
        StringBuffer fileData = new StringBuffer();
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
        }
        reader.close();
        return fileData.toString();
    }
}

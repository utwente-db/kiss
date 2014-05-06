package nl.utwente.db.kiss;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.utwente.db.neogeo.utils.FileUtils;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@MultipartConfig
public class MessagesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    public final static File MESSAGE_FILE = new File(KissUtils.ROOT_FOLDER + "messages.json");

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	response.setHeader("Access-Control-Allow-Origin", "*");
    	
    	String messages = FileUtils.getFileAsString(MESSAGE_FILE);
    	response.getWriter().write(messages);
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	String currentMessages = FileUtils.getFileAsString(MESSAGE_FILE);
    	JSONArray currentMessagesArray = null;
    	
    	try {
			currentMessagesArray = new JSONArray(currentMessages);
		} catch (JSONException e) {
			throw new RuntimeException("Unable to parse JSON of currentMessages", e);
		}
    	
    	// Dutch message
    	Map<String, String> newMessageMapNL = new HashMap<String, String>();
    	newMessageMapNL.put("title", request.getParameter("title_nl"));
    	newMessageMapNL.put("text", request.getParameter("text_nl"));
    	JSONObject newMessageNL = new JSONObject(newMessageMapNL);
    	
    	// English message
    	Map<String, String> newMessageMapEN = new HashMap<String, String>();
    	newMessageMapEN.put("title", request.getParameter("title_en"));
    	newMessageMapEN.put("text", request.getParameter("text_en"));
    	JSONObject newMessageEN = new JSONObject(newMessageMapEN);
    	
    	Map<String, JSONObject> newMessageMap = new HashMap<String, JSONObject>();
    	newMessageMap.put("en", newMessageEN);
    	newMessageMap.put("nl", newMessageNL);
    	JSONObject newMessage = new JSONObject(newMessageMap);

    	currentMessagesArray.put(newMessage);
    	
    	FileUtils.writeFile(MESSAGE_FILE, currentMessagesArray.toString());
    	
    	response.getWriter().write("Uw bericht is succesvol opgeslagen. Binnen 1 minuut zal dit op alle apparaten zichtbaar zijn");
    }
}

package nl.utwente.db.kiss;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jettison.json.JSONArray;

@MultipartConfig
public class ImageListServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	response.setHeader("Access-Control-Allow-Origin", "*");
    	
    	String exerciseId = request.getParameter("exerciseId");
    	File imageDirectory = new File(KissUtils.ROOT_FOLDER + "images/" + exerciseId);
    	
    	if (!imageDirectory.exists()) {
    		imageDirectory.mkdirs();
    	}
    	
    	List<String> paths = new ArrayList<String>();
    	
    	for (String fileName : imageDirectory.list()) {
    		paths.add(exerciseId + "/" + fileName);
    	}
    	
    	JSONArray result = new JSONArray(paths);
    	response.getWriter().write(result.toString());
    }
}
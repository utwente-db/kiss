package nl.utwente.db.kiss;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import nl.utwente.db.neogeo.utils.FileUtils;
import nl.utwente.db.neogeo.utils.StringUtils;

@MultipartConfig
public class GPSUploadServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	throw new RuntimeException("Invalid request, use POST instead.");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String deviceId = request.getParameter("deviceId");
        
        Part data = getFile(request);
        
    	String fileName = storeData(deviceId, data);
    	PrintWriter writer = response.getWriter();
    	
    	writer.write(new File(fileName).getCanonicalPath());
    }
    
    protected String storeData(String deviceId, Part data) throws IOException {
		String folderPath = KissUtils.ROOT_FOLDER + "gps/" + deviceId;
		File directory = new File(folderPath);
		
		directory.mkdirs();
		
		String fileName = folderPath + "/" + (System.currentTimeMillis() / 1000) + ".txt";
		String dataAsString = StringUtils.inputStreamToString(data.getInputStream());
		
		FileUtils.writeFile(fileName, dataAsString);
		
		return fileName;
	}
	
	protected Part getFile(HttpServletRequest request) {
		try {
			return request.getPart("data");
		} catch (IOException e) {
			throw new RuntimeException("Unable to load attachment", e);
		} catch (ServletException e) {
			throw new RuntimeException("Unable to load attachment", e);
		}
	}
}
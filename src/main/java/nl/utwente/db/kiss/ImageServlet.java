package nl.utwente.db.kiss;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@MultipartConfig
public class ImageServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	response.setHeader("Access-Control-Allow-Origin", "*");
    	
    	String imagePath = request.getParameter("imagePath");
    	String fullPath = KissUtils.ROOT_FOLDER + "images/" + imagePath;

    	File image = new File(fullPath);
        
        int length = 0;
        ServletOutputStream outStream = response.getOutputStream();
        ServletContext context = getServletConfig().getServletContext();
        String mimetype = context.getMimeType(fullPath);
        
        // sets response content type
        if (mimetype == null) {
            mimetype = "image/jpg";
        }
        
        response.setContentType(mimetype);
        response.setContentLength((int)image.length());
        
        byte[] byteBuffer = new byte[4096];
        DataInputStream in = new DataInputStream(new FileInputStream(image));
        
        // reads the file's bytes and writes them to the response stream
        while ((in != null) && ((length = in.read(byteBuffer)) != -1)) {
            outStream.write(byteBuffer, 0, length);
        }
        
        in.close();
        outStream.close();
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	response.setContentType("text/html;charset=UTF-8");
    	String exerciseId = request.getParameter("exerciseId");
    	String deviceId = request.getParameter("deviceId");

        // Create path components to save the file
        Part filePart = request.getPart("data");
        String fileName = getFileName(filePart);
        
        String folderPath = KissUtils.ROOT_FOLDER + "images/" + exerciseId;
        String filePath = folderPath + "/" + deviceId + "-" + fileName;
        
        File folder = new File(folderPath);
        
        if (!folder.exists()) {
        	folder.mkdirs();
        }

        OutputStream out = null;
        InputStream fileContent = null;
        
        PrintWriter writer = response.getWriter();

        try {
            out = new FileOutputStream(new File(filePath));
            fileContent = filePart.getInputStream();

            int read = 0;
            byte[] bytes = new byte[1024];

            while ((read = fileContent.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        } finally {
            if (out != null) {
                out.close();
            }

            if (fileContent != null) {
            	fileContent.close();
            }
            
            if (writer != null) {
                writer.close();
            }
        }
    }
    
    private String getFileName(Part part) {
        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        
        return null;
    }
}
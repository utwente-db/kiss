package nl.utwente.db.kiss;

import java.io.File;
import java.io.IOException;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import nl.utwente.db.neogeo.utils.FileUtils;

@MultipartConfig
public class MessagesServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	response.setHeader("Access-Control-Allow-Origin", "*");
    	
    	String messages = FileUtils.getFileAsString(new File(KissUtils.ROOT_FOLDER + "messages.json"));
    	response.getWriter().write(messages);
    }
}

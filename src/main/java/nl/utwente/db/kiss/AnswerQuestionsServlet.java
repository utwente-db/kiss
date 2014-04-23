package nl.utwente.db.kiss;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import nl.utwente.db.neogeo.utils.FileUtils;
import nl.utwente.db.neogeo.utils.StringUtils;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@MultipartConfig
public class AnswerQuestionsServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    public final static String ROOT_FOLDER = "/data/tomcat/";
	public final static String PROPERTY_FILE_FOLDER = ROOT_FOLDER + "properties/";
	public final static String QUESTIONS_FOLDER = ROOT_FOLDER + "questions/";

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    	throw new RuntimeException("Invalid request, use POST instead.");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String dataType = request.getParameter("dataType");
        String teamId = request.getParameter("teamId").toUpperCase();
        String deviceId = request.getParameter("deviceId");
        String language = request.getParameter("language");
        
        if (language == null) {
        	// Best guess
        	language = "nl";
        }
        
        Part data = getFile(request);
        
    	String fileName = storeData(dataType, teamId, deviceId, language, data);
    	PrintWriter writer = response.getWriter();
    	
    	writer.write(new File(fileName).getCanonicalPath());
    }
    
    protected String storeData(String dataType, String teamId, String deviceId, String language, Part data) throws IOException {
		String folderPath = ROOT_FOLDER + "results/" + teamId + "/" + deviceId;
		File directory = new File(folderPath);
		
		directory.mkdirs();
		
		String fileName = folderPath + "/" + dataType + "-" + (System.currentTimeMillis() / 1000) + ".txt";
		String dataAsString = StringUtils.inputStreamToString(data.getInputStream());
		
		FileUtils.writeFile(fileName, dataAsString);
		
		if ("answers".equals(dataType)) {
			updateQuestionScores(teamId, deviceId, language, folderPath, fileName);
		} else if ("gps".equals(dataType)) {
			updateGPSDataScores(teamId, deviceId, folderPath, fileName);
		}
		
		return fileName;
	}

	private void updateQuestionScores(String teamId, String deviceId, String language, String folderPath, String fileName) {
		String scoreFileName = folderPath + "/questionscore.txt";
		int score = calculateQuestionScore(teamId, deviceId, language, fileName);
		
		FileUtils.writeFile(scoreFileName, "" + score);
	}
	
	private void updateGPSDataScores(String teamId, String deviceId, String folderPath, String fileName) {
		String scoreFileName = folderPath + "/gpsdatascore.txt";
		File scoreFile = new File(scoreFileName);
		
		int score = 0;
		
		if (scoreFile.exists()) {
			score = Integer.valueOf(FileUtils.getFileAsString(scoreFile));
		}
		
		score += calculateGPSDataScore(teamId, deviceId, fileName);
		FileUtils.writeFile(scoreFileName, "" + score);
	}

	private int calculateQuestionScore(String teamId, String deviceId, String language, String fileName) {
		FileReader fileReader;
		BufferedReader bufferedReader;
		
		try {
			fileReader = new FileReader(fileName);
			bufferedReader = new BufferedReader(fileReader);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unable to read file which should just have been created. Check file permissions!", e);
		}
		
		String propertyFileName = PROPERTY_FILE_FOLDER + "/" + teamId + ".properties";
		Properties properties = new Properties();
		
		try {
			properties.load(new FileInputStream(propertyFileName));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unable to read properties file for team id " + teamId, e);
		} catch (IOException e) {
			throw new RuntimeException("Unable to read properties file for team id " + teamId, e);
		}
		
		String thisLine = null;
		int score = 0;
		
		try {
			while ((thisLine = bufferedReader.readLine()) != null) {
				String[] answerParts = thisLine.split(",");
				
				String questionId = answerParts[0];
				
				if ("Question".equals(questionId)) {
					// Header line
					continue;
				}
				
				String answer = answerParts[1];
				
				double latitude = Double.valueOf(answerParts[2]);
				double longitude = Double.valueOf(answerParts[3]);
				
				String questionFileName = properties.getProperty(questionId);
				
				JSONParser parser = new JSONParser();
				JSONObject json;
				
				try {
					json = (JSONObject)parser.parse(FileUtils.getFileAsString(new File(QUESTIONS_FOLDER + language + "/" + questionFileName)));
				} catch (ParseException e) {
					throw new RuntimeException("Unable to read question file: " + questionFileName, e);
				}
				
				String correctAnswer = (String)json.get("correctAnswer");

				if (!answer.equalsIgnoreCase(correctAnswer)) {
					continue;
				}
				
				double expectedLatitude = Double.valueOf((String)json.get("latitude"));
				double expectedLongitude = Double.valueOf((String)json.get("longitude"));
				
				score += Math.round(1000 / scoreDistance(latitude, longitude, expectedLatitude, expectedLongitude));
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to read file", e);
		}
		
		return score;
	}
	
	public static void main(String[] args) {
		String teamId = "AAA15";
		String deviceId = "android-bf2dadbd-2a80-486f-85bc-c9ee31b26eca";
		String fileName = ROOT_FOLDER + teamId + "/" + deviceId + "/answers-1374274161.txt";

//		System.out.println(new AnswerQuestionsServlet().calculateQuestionScore(teamId, deviceId, fileName));
		System.out.println(new AnswerQuestionsServlet().calculateGPSDataScore(teamId, deviceId, fileName));
	}

	private int calculateGPSDataScore(String teamId, String deviceId, String fileName) {
		FileReader fileReader;
		BufferedReader bufferedReader;
		
		try {
			fileReader = new FileReader(fileName);
			bufferedReader = new BufferedReader(fileReader);
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Unable to read file which should just have been created. Check file permissions!", e);
		}
		
		String thisLine = null;
		int score = 0;
		
		try {
			while ((thisLine = bufferedReader.readLine()) != null) {
				if ("Latitude".equals(thisLine.split(",")[0])) {
					continue;
				}
				
				score++;
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to read file", e);
		}
		
		return score;
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
	
	protected double scoreDistance(double lat1, double lon1, double lat2, double lon2) {
		double radiusEarth = 6371007.0;
		
		double dLat = toRad(lat2 - lat1);
		double dLon = toRad(lon2 - lon1);
		double rLat1 = toRad(lat1);
		double rLat2 = toRad(lat2);

		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(rLat1) * Math.cos(rLat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double distance = radiusEarth * c;

		return distance;
	}
	
	protected double toRad(double degrees) {
	    return degrees * Math.PI / 180;
	}
}

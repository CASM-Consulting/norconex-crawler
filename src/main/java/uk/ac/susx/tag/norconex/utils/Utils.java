package uk.ac.susx.tag.norconex.utils;

import com.google.common.io.Files;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class Utils {
	
	/** 
	 * @param url to be stripped
	 * @return The host domain of the url, excluding 'www'
	 * @throws URISyntaxException
	 */
	public static String getDomain(String url) throws URISyntaxException {
		URI uri = new URI(url);
		String domain = uri.getHost();
	    return (domain.startsWith("www.")) ? domain.substring(4) : domain;
	}

	public static String[] buildArguments(String[] args) {
		List<String> splitArgs = new ArrayList<>();
		for(String arg : args){
			splitArgs.addAll(Arrays.asList(arg.split("\\s+")));
		}
		String[] corrArgs = splitArgs.toArray(new String[splitArgs.size()]);
		return corrArgs;
	}

	// Processes a M52 job json to scraper rules
	public static String processJobJSON(String json) throws IncorrectScraperJSONException {
		JSONObject jobj = new JSONObject(json);
		try {
			return jobj.getJSONArray("components").getJSONObject(0).getJSONObject("opts").getJSONArray("fields").toString();
		} catch (Exception e) {
			throw new IncorrectScraperJSONException();
		}
	}


	public static String processScraperJSON(String json){
		// BUG FOUND - NEED TO USE job.json!!
		return null;
	}

	// returns a web scraper based on a job spect of last_scrape file
	public static String processJSON(File scraperLocation) throws IOException, IncorrectScraperJSONException {
		String json = Files.toString(scraperLocation, Charset.defaultCharset());
		return (scraperLocation.getName().equals("last_scrape.json")) ? processScraperJSON(json) : processJobJSON(json);
	}

	public static Properties getProperties(String propertiesPath) {
		return getProperties(Paths.get(propertiesPath));
	}

	public static Properties getProperties(Path propertiesLocation) {

		Properties properties = new Properties();
		try (BufferedReader br = new BufferedReader(new FileReader(propertiesLocation.toFile()))) {
			properties.load(br);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return properties;
	}

}

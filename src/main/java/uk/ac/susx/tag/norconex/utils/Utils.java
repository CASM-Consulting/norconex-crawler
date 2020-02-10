package uk.ac.susx.tag.norconex.utils;

import com.google.common.io.Files;
import com.norconex.collector.http.doc.HttpDocument;
import com.norconex.commons.lang.file.ContentType;
import org.json.JSONObject;
import uk.ac.susx.tag.norconex.scraping.GeneralSplitterFactory;
import uk.ac.susx.tag.norconex.scraping.POJOHTMLMatcherDefinition;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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

	/**
	 * Builds a set of command line arguments that fits JQM's expected format
	 * @param args
	 * @return
	 */
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

	/**
	 * Returns the m52 job file for a path
	 * @param scraperLocation
	 * @return
	 */
	public static Path getJobFile(Path scraperLocation) {
		return Paths.get(scraperLocation.toAbsolutePath().toString(),"job.json");
	}

	/**
	 * Transform json pojo object to splitter structure
	 * @param matcherList
	 * @return
	 */
	public static Map<String, List<Map<String, String>>> buildScraperDefinition(List<POJOHTMLMatcherDefinition> matcherList) {

		Map<String, List<Map<String, String>>> fields = new HashMap<>();
		for(POJOHTMLMatcherDefinition matcher : matcherList) {
			List<Map<String, String>> tags = matcher.getTagDefinitions();
			fields.put(matcher.field,tags);
		}
		return fields;

	}

	/**
	 * Initialise and build all scrapers in a single directory.
	 * @param scrapersLocation
	 */
	public static Map<String, GeneralSplitterFactory> initScrapers(Path scrapersLocation) throws IOException {

		// Used if you wish the pre-processor to contain all scrapers.
		Map<String, GeneralSplitterFactory> scrapersJson = new HashMap<>();

		List<Path> scrapers = java.nio.file.Files.walk(scrapersLocation)
				.filter(file -> file.getFileName().toString().equals("job.json"))
				.collect(Collectors.toList());
		System.out.println(scrapers.size());

		for (Path path : scrapers) {
			try {
				File file = path.toFile();
				String processed = Utils.processJSON(file);
				Map<String, List<Map<String, String>>> scraperDefs = buildScraperDefinition(GeneralSplitterFactory.parseJsonTagSet(processed));
//				logger.info("Adding scraper: " + file.getParentFile().getName());
				scrapersJson.put(file.getParentFile().getName(), new GeneralSplitterFactory(scraperDefs));
//				logger.info("Added scraper for: " + file.getParentFile().getName() + " " + scrapersJson.get(file.getParentFile().getName().replace(".json", "")));
				System.out.println("Added scraper for: " + file.getParentFile().getName() + " " + scrapersJson.get(file.getParentFile().getName().replace(".json", "")));
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IncorrectScraperJSONException e) {
				e.printStackTrace();
			}
		}
		return scrapersJson;
	}

	/**
	 * Initialise and build a single scraper.
	 * @param scraperLocation
	 */
	public static GeneralSplitterFactory initScraper(Path scraperLocation){

		// Used if you wish the pre-processor to only be repsonsible for a single scraper.
		GeneralSplitterFactory scraperJson = null;

		File file = getJobFile(scraperLocation).toFile();
		String processed = null;
		try {

			processed = Utils.processJSON(file);
			Map<String, List<Map<String, String>>> scraperDefs = buildScraperDefinition(GeneralSplitterFactory.parseJsonTagSet(processed));
//			logger.info("Adding scraper: " + file.getName());
			scraperJson = new GeneralSplitterFactory(scraperDefs);
//			logger.info("Scraper successfully added for: " + file.getName());

		} catch (IOException e) {
			e.printStackTrace();
		} catch (IncorrectScraperJSONException e) {
			e.printStackTrace();
		}

		return scraperJson;

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


	/**
	 * Class used to serialise web page metadata to json
	 * TODO: Make scraped metadata less hardcoded
	 */
	public class WebPage {

		private final String url;
		private final String html;
		private final String parent;
		private final int depth;
		private String article;
		private String title;
		private String date;

		public WebPage(String url, String html, String parent, int depth) {
			this.url = url;
			this.html = html;
			this.parent = parent;
			this.depth = depth;
		}

		public String getArticle() {return article;}
		public String getTitle() {return title;}
		public String getDate() {return date;}
		public String getUrl() {return url;}
		public String getHtml() {return html;}
		public String getParent() {return parent;}
		public int getDepth() {return depth;}

		public void setArticle(String article) {
			this.article = article;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public void setDate(String date) {
			this.date = date;
		}

	}

	// Check this is text based only content for M52
	public static boolean isText(HttpDocument doc) {
		ContentType ct = doc.getContentType();
		String contenFam = ct.getContentFamily().getId();
		return (ContentType.TEXT.getContentFamily().getId().equals(contenFam) || ContentType.HTML.getContentFamily().getId().equals(contenFam) ||
				ContentType.CSV.getContentFamily().getId().equals(contenFam) || ContentType.XML.getContentFamily().getId().equals(contenFam)) ?
				true : false;
	}


}

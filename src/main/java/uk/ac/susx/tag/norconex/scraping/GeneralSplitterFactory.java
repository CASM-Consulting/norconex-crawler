package uk.ac.susx.tag.norconex.scraping;

// java imports
import 	java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// nutch imports
import org.apache.commons.io.FileUtils;

// jsoup imports
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;
import org.jsoup.select.Elements;

// logging imports
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.susx.tag.norconex.utils.InvalidCSSQueryException;
import uk.ac.susx.tag.norconex.utils.Utils;

public class GeneralSplitterFactory {

	private static final Logger LOG = LoggerFactory.getLogger(GeneralSplitterFactory.class);

	private static String domain;

	public final String ROOT = "root";

	private Map<String,List<Map<String,String>>> fields;

	public static final String CUSTOM = "custom";
	public static final String TAG = "tag";
	public static final String CLASS = "class";
	public static final String ATT = "att";
	public static final String EXCLUDE = "exclude";
	public static final String EXCLUDE_MARK = "!";

	public GeneralSplitterFactory() {}

	public GeneralSplitterFactory(Map<String,List<Map<String,String>>> fields) {
		this.fields = fields;
	}

	public GeneralSplitterFactory(Map<String,List<Map<String,String>>> fields, String domain) {
		this(fields);
		this.domain = domain;
	}

	//	@Override
	public IForumSplitter create() {
		return new GeneralSplitter();
	}

	//	@Override
	public boolean correctDomain(String url) {
		try {
			return (domain != null) ? Utils.getDomain(url).equals(domain) : true;
		} catch (URISyntaxException e) {
			LOG.debug(e.getMessage());
		}
		return true;
	}

	/**
	 * A method that assumes that this is
	 * @param json
	 * @return
	 */
	public static List<POJOHTMLMatcherDefinition> parseJsonTagSet(String json) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, new TypeReference<List<POJOHTMLMatcherDefinition>>(){});
	}

	public class GeneralSplitter implements IForumSplitter {

		public GeneralSplitter() {}

		@Override
		public void mapFields(LinkedList<Post> posts) {

			if(fields.size() > 1) {
				for (String field : fields.keySet()) {
					List<String> queries = new ArrayList<>();
					List<String> exclusionQueries = new ArrayList<>();
					for (Map<String, String> query : fields.get(field)) {
						try {
							final String queryStr = createQuery(query);
							if(queryStr.startsWith(EXCLUDE_MARK)) {
								exclusionQueries.add(queryStr.substring(1));
							} else {
								queries.add(createQuery(query));
							}
						} catch (InvalidCSSQueryException e) {
							LOG.error(e.getMessage());
						}
					}
					for (Post post : posts) {
						// parse the html to a dom model
						Document doc = Jsoup.parse(post.postHTML());

						// remove all excluded elements from the tree
						excludeElements(doc,exclusionQueries);
						// get all text content from remainder of document
						List<Element> out = getContent(doc, new ArrayList<>(), queries);
						List<String> meta = new ArrayList<>();

						StringBuilder sb = new StringBuilder();
						out.stream()
								// necessary (and efficient) queries in order to capture only that text which hasn't been excluded
								.flatMap(elem -> elem.select("*").stream())
								.flatMap(elem -> elem.textNodes().stream())
								.map(node -> node.text())
								.forEach(txt -> sb.append(" ").append(txt));

						meta.add(sb.toString());
						post.put(field, meta);
					}

				}
			}
		}

		/**
		 * Removes matching queries and all children from the Dom tree
		 * @param doc
		 * @param exclusionQueries
		 * @return
		 */
		private void excludeElements(Document doc, List<String> exclusionQueries) {
			for(String query : exclusionQueries) {
				doc.select(query).remove();
			}
		}

		@Override
		public LinkedList<Post> split(Document doc) {

			LinkedList<Post> posts = new LinkedList<>();
			List<String> queries = new ArrayList<>();
			List<Map<String,String>> queryList = fields.get(fields.keySet().stream()
					.filter(field -> field.endsWith("/" + ROOT))
					.collect(Collectors.toList())
					.get(0));

			for(Map<String,String> query : queryList) {
				try {
					queries.add(createQuery(query));
				} catch (InvalidCSSQueryException e) {
					LOG.error(e.getMessage());
				}
			}

			for(Element element : getContent(doc, new ArrayList<>(), queries)) {
				posts.add(new Post(element.html(),element.text()));
			}

			this.mapFields(posts);
			return posts;
		}

	}

	private static boolean isAttributeRequest(String query) {
		return (Pattern.matches(".*\\[.*\\].*",query) && !Pattern.matches(".*\\[.*=.*\\].*",query));
	}

	/**
	 * @return List of elements which contain the content for a specific requested part of a web-page.
	 */
	public static List<Element> getContent(Element currElem, List<Element> output, List<String> queries) {

		if(queries.size() == 1) {
			String query = queries.get(0);
			Elements elems = currElem.select(query);
			if(isAttributeRequest(query)) {
				String attr = query.substring(query.indexOf("[")+1,query.indexOf("]"));
				List<Element> attributes = elems.stream()
						.filter(el -> el.hasAttr(attr))
						.map(el -> el.attr(attr))
						.map(str -> new Element(Tag.valueOf(attr),attr).text(str))
						.collect(Collectors.toList());
				output.addAll(attributes);
			}
			else {
				output.addAll(elems);
			}
		}
		else {
			for(Element newElem : currElem.select(queries.get(0))) {
				return getContent(newElem,output,queries.subList(1, queries.size()));
			}
		}

		return output;
	}

	private static boolean isExcludeQuery(String query) {
		return query.startsWith(EXCLUDE_MARK);
	}

	public static List<List<POJOHTMLMatcherDefinition>> getDirectoryOfJsonTagSets(Path directory) throws IOException {
		Collection<File> scrapers = FileUtils.listFiles(directory.toFile(), new String[]{"json"}, true);
		List<List<POJOHTMLMatcherDefinition>> scraperDefs = new ArrayList<>();
		for(File scraper : scrapers) {
			scraperDefs.add(getTagSetFromJson(scraper.toPath()));
		}
		return scraperDefs;
	}

	public static List<POJOHTMLMatcherDefinition> getTagSetFromJson(Path jsonLocation) throws IOException {
		return parseJsonTagSet(FileUtils.readFileToString(jsonLocation.toFile()));
	}

	/**
	 * @param labels css labels for query (tag -> class -> attribute)
	 * Currently only supports single instances of each.
	 * @return A css query according to the jsoup select syntax
	 * @throws InvalidCSSQueryException
	 */
	public static String createQuery(Map<String,String> labels) throws InvalidCSSQueryException {
		StringBuilder sb = new StringBuilder();
		if(labels == null || labels.size() == 0) {
			throw new InvalidCSSQueryException(null,null);
		}

		// Converts the query into an exlude rule
		if(Boolean.valueOf(labels.get(EXCLUDE))) {
			sb.append(EXCLUDE_MARK);
		}

		// If it's a custom query then simply return said query
		if(labels.containsKey(CUSTOM)) {
			return labels.get(CUSTOM);
		}

		// must atleast contain an html tag to be valid
		if(labels.containsKey(TAG)) {
			sb.append(labels.get(TAG));
		}
		else {
			throw new InvalidCSSQueryException(TAG, labels.get(TAG));
		}

		// add the class label to the css query.
		if(labels.containsKey(CLASS)) {
			sb.append(".").append(labels.get(CLASS));
		}
		if(labels.containsKey(ATT)) {
			sb.append("[").append(labels.get(ATT)).append("]");
		}

		return sb.toString();

	}

}

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cleans simple, validating HTML 4/5 into plain-text words using regular
 * expressions.
 *
 * @see <a href="https://validator.w3.org/">validator.w3.org</a>
 * @see <a href="https://www.w3.org/TR/html51/">HTML 5.1 Specification</a>
 * @see <a href="https://www.w3.org/TR/html401/">HTML 4.01 Specification</a>
 *
 * @see java.util.regex.Pattern
 * @see java.util.regex.Matcher
 * @see java.lang.String#replaceAll(String, String)
 */
public class HTMLCleaner {

	/**
	 * Replaces all HTML entities with a single space. For example,
	 * "2010&ndash;2012" will become "2010 2012".
	 *
	 * @param html
	 *            text including HTML entities to remove
	 * @return text without any HTML entities
	 */
	public  String stripEntities(String html) {
		if(html != null){
			html = html.replaceAll("&\\S.*?;", " ");
			return html;
		}
		return null;
	}

	/**
	 * Replaces all HTML comments with a single space. For example, "A<!-- B
	 * -->C" will become "A C".
	 *
	 * @param html
	 *            text including HTML comments to remove
	 * @return text without any HTML comments
	 */
	public  String stripComments(String html) {
		if(html != null){
			
			html = html.replaceAll("<!--[.\\s\\S]*?-->", " ");
			return html;
		}
		return null;
	}

	/**
	 * Replaces all HTML tags with a single space. For example, "A<b>B</b>C"
	 * will become "A B C".
	 *
	 * @param html
	 *            text including HTML tags to remove
	 * @return text without any HTML tags
	 */
	public  String stripTags(String html) {
		if(html != null){
			
			html = html.replaceAll("<[^>]*>", " ");
			
			
			return html;
		}
		return null;
	}

	/**
	 * Replaces everything between the element tags and the element tags
	 * themselves with a single space. For example, consider the html code: *
	 *
	 * <pre>
	 * &lt;style type="text/css"&gt;body { font-size: 10pt; }&lt;/style&gt;
	 * </pre>
	 *
	 * If removing the "style" element, all of the above code will be removed,
	 * and replaced with a single space.
	 *
	 * @param html
	 *            text including HTML elements to remove
	 * @param name
	 *            name of the HTML element (like "style" or "script")
	 * @return text without that HTML element
	 */
	public   String stripElement(String html, String name) {
		
		if(html != null){
			html = html.replaceAll("(?i)<"+ name +"[\\s\\S]*?</" +name+ "[\\s\\S]*?>", " ");
			return html;
		}
		return null;
	}
	//some more strip methods that cover unicode handling, extra space inbetween words, etc.
	public  String stripNonLetters(String string){
		if(string != null){
			string = string.toLowerCase();
			string = string.replaceAll("(?U)[^\\p{Alpha}\\p{Space}]+", " ");
			string = string.replaceAll("\\s{2,}", " ");
			Pattern p = Pattern.compile("[^\\p{L}]+", Pattern.UNICODE_CHARACTER_CLASS);
			Matcher m = p.matcher(string);
			string = m.replaceAll(" ");
			string = string.trim();
			return string;
		}
		return null;
	}
	
	public String stripNonLettersAndNumbers(String string){
		
		string = string.replaceAll("\\P{Alnum}", " ");
		string = string.replaceAll("\\s{2,}", " ");
		string = string.toLowerCase();
		
		return string;
	}

	/**
	 * Removes all HTML (including any CSS and JavaScript).
	 *
	 * @param html
	 *            text including HTML to remove
	 * @return text without any HTML, CSS, or JavaScript
	 */
	public String stripHTML(String html) {
		
		html = stripComments(html);
		html = stripElement(html, "head");
		html = stripElement(html, "style");
		html = stripElement(html, "script");
		html = stripTags(html);
		html = stripEntities(html);
		
		html = stripNonLetters(html);
		
		
		return html;
	}
}

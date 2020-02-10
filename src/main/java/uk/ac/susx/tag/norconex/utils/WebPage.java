package uk.ac.susx.tag.norconex.utils;

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

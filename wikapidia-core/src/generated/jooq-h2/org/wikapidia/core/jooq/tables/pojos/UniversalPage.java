/**
 * This class is generated by jOOQ
 */
package org.wikapidia.core.jooq.tables.pojos;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "3.0.0"},
                            comments = "This class is generated by jOOQ")
@java.lang.SuppressWarnings({ "all", "unchecked" })
public class UniversalPage implements java.io.Serializable {

	private static final long serialVersionUID = -1666853481;

	private java.lang.Long    id;
	private java.lang.Short   langId;
	private java.lang.Integer pageId;
	private java.lang.String  title;
	private java.lang.Short   pageType;
	private java.lang.Integer univId;

	public java.lang.Long getId() {
		return this.id;
	}

	public void setId(java.lang.Long id) {
		this.id = id;
	}

	public java.lang.Short getLangId() {
		return this.langId;
	}

	public void setLangId(java.lang.Short langId) {
		this.langId = langId;
	}

	public java.lang.Integer getPageId() {
		return this.pageId;
	}

	public void setPageId(java.lang.Integer pageId) {
		this.pageId = pageId;
	}

	public java.lang.String getTitle() {
		return this.title;
	}

	public void setTitle(java.lang.String title) {
		this.title = title;
	}

	public java.lang.Short getPageType() {
		return this.pageType;
	}

	public void setPageType(java.lang.Short pageType) {
		this.pageType = pageType;
	}

	public java.lang.Integer getUnivId() {
		return this.univId;
	}

	public void setUnivId(java.lang.Integer univId) {
		this.univId = univId;
	}
}

package edu.ucdavis.dss.ipa.api.components.term.views;

import edu.ucdavis.dss.ipa.entities.Tag;

public class TermTrackView {
	private long id;
	private String name;

	public TermTrackView(Tag tag) {
		if (tag == null) return;
		setId(tag.getId());
		setName(tag.getName());
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}

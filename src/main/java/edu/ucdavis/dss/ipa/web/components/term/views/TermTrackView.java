package edu.ucdavis.dss.ipa.web.components.term.views;

import edu.ucdavis.dss.ipa.entities.Track;

public class TermTrackView {
	private long id;
	private String name;

	public TermTrackView(Track track) {
		if (track == null) return;
		setId(track.getId());
		setName(track.getName());
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

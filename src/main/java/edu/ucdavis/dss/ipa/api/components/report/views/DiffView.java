package edu.ucdavis.dss.ipa.api.components.report.views;

import org.javers.core.diff.Change;

import java.util.List;

public class DiffView {
	private List<SectionDiffDto> sections;
	private List<Change> changes;

	public DiffView(List<SectionDiffDto> sections, List<Change> changes) {
		setSections(sections);
		setChanges(changes);
	}

	public List<SectionDiffDto> getSections() {
		return sections;
	}

	public void setSections(List<SectionDiffDto> sections) {
		this.sections = sections;
	}

	public List<Change> getChanges() {
		return changes;
	}

	public void setChanges(List<Change> changes) {
		this.changes = changes;
	}
}

package edu.ucdavis.dss.ipa.api.components.report.views;

import org.javers.core.diff.Change;

import java.util.List;

public class DiffView {
	private SectionDiffDto section;
	private List<Change> changes;

	public DiffView(SectionDiffDto section, List<Change> changes) {
		setSection(section);
		setChanges(changes);
	}

	public SectionDiffDto getSection() {
		return section;
	}

	public void setSection(SectionDiffDto section) {
		this.section = section;
	}

	public List<Change> getChanges() {
		return changes;
	}

	public void setChanges(List<Change> changes) {
		this.changes = changes;
	}
}

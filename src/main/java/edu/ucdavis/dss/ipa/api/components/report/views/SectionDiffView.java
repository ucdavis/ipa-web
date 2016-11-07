package edu.ucdavis.dss.ipa.api.components.report.views;

import org.javers.core.diff.Change;

import java.util.List;

public class SectionDiffView {
	private SectionDiffDto ipaSection, dwSection;
	private List<Change> changes;

	public SectionDiffView(SectionDiffDto ipaSection, SectionDiffDto dwSection, List<Change> changes) {
		setIpaSection(ipaSection);
		setDwSection(dwSection);
		setChanges(changes);
	}

	public SectionDiffDto getIpaSection() {
		return ipaSection;
	}

	public void setIpaSection(SectionDiffDto ipaSection) {
		this.ipaSection = ipaSection;
	}

	public SectionDiffDto getDwSection() {
		return dwSection;
	}

	public void setDwSection(SectionDiffDto dwSection) {
		this.dwSection = dwSection;
	}

	public List<Change> getChanges() {
		return changes;
	}

	public void setChanges(List<Change> changes) {
		this.changes = changes;
	}
}

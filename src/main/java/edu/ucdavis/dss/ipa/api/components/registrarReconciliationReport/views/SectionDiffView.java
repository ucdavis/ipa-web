package edu.ucdavis.dss.ipa.api.components.registrarReconciliationReport.views;

import edu.ucdavis.dss.ipa.entities.SyncAction;
import org.javers.core.diff.Change;

import java.util.List;

public class SectionDiffView {
	private SectionDiffDto ipaSection, dwSection;
	private List<Change> changes;
	private List<SyncAction> syncActions;

	public SectionDiffView(
			SectionDiffDto ipaSection,
			SectionDiffDto dwSection,
			List<Change> changes,
			List<SyncAction> syncActions) {
		setIpaSection(ipaSection);
		setDwSection(dwSection);
		setChanges(changes);
		setSyncActions(syncActions);
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

	public List<SyncAction> getSyncActions() {
		return syncActions;
	}

	public void setSyncActions(List<SyncAction> syncActions) {
		this.syncActions = syncActions;
	}
}

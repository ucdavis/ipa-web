package edu.ucdavis.dss.ipa.services.jpa;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import edu.ucdavis.dss.ipa.entities.Course;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.javers.common.collections.Optional;
import org.javers.core.Javers;
import org.javers.core.JaversBuilder;
import org.javers.core.diff.Change;
import org.javers.core.diff.Diff;
import org.javers.core.diff.ListCompareAlgorithm;
import org.javers.core.diff.changetype.NewObject;
import org.javers.core.diff.changetype.ObjectRemoved;
import org.javers.core.diff.changetype.ValueChange;
import org.javers.core.diff.changetype.container.ContainerElementChange;
import org.javers.core.diff.changetype.container.SetChange;
import org.javers.core.diff.changetype.container.ValueAdded;
import org.javers.core.diff.changetype.container.ValueRemoved;

import edu.ucdavis.dss.dw.dto.DwSectionGroup;
import edu.ucdavis.dss.ipa.diff.entities.DiffEntity;
import edu.ucdavis.dss.ipa.diff.entities.DiffSectionGroup;
import edu.ucdavis.dss.ipa.diff.mapper.DwSectionGroupMapper;
import edu.ucdavis.dss.ipa.diff.mapper.JpaSectionGroupMapper;
import edu.ucdavis.dss.ipa.diff.mapper.SectionGroupGrouper;
import edu.ucdavis.dss.ipa.entities.Schedule;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.repositories.DataWarehouseRepository;
import edu.ucdavis.dss.ipa.services.DwSyncService;
import edu.ucdavis.dss.ipa.services.ScheduleService;

@Service
public class JpaDwSyncService implements DwSyncService {
	private static final Logger log = LogManager.getLogger("DwSyncService");

	@Inject DataWarehouseRepository dwRepository;
	@Inject ScheduleService scheduleService;

	@Override
	@Transactional
	public Diff identifyDifferencesFromDw(Long scheduleId) {
		// Raw set of section groups from dwRepository
		Set<DwSectionGroup> dwSectionGroups = null;
		// Set of all section groups from scheduleService converted to DiffSectionGroup
		Set<DiffSectionGroup> sectionGroups = null;

		Schedule schedule = this.scheduleService.findById(scheduleId);
		if(schedule == null) {
			log.error("Cannot sync with DW: could not find schedule with ID: " + scheduleId);
			return null;
		}

		try {
			dwSectionGroups = dwRepository.getSectionGroupsByDeptCodeAndYear(schedule.getWorkgroup().getCode(), schedule.getYear());

			if(dwSectionGroups != null) {
				log.info("Received " + dwSectionGroups.size() + " course offerings from DW.");
			} else {
				log.error("dwClient returned NULL section groups!");
				return null; // cannot continue past this point
			}
		} catch (Exception e) {
			ExceptionLogger.logAndMailException(this.getClass().getName(), e);
			return null;
		}

		// Get all section groups in all course offerings in all course offering groups in the schedule
		sectionGroups = new HashSet<DiffSectionGroup>();
		for (Course course : schedule.getCourses()) {
			sectionGroups.addAll(course.getSectionGroups().stream()
					// Convert JPA SectionGroups to DiffSectionGroups for diffing
					.map(new JpaSectionGroupMapper())
					// Remove all null values (probably not necessary, but just to be safe...)
					.filter(sg -> sg != null)
					// Sections can be in different DiffSectionGroups even
					// when they should be grouped together, so put them in
					// the same DiffSectionGroups
					.collect(Collectors.groupingBy(DiffSectionGroup::javersId,
							Collectors.reducing(null, new SectionGroupGrouper())))
					.values());
		}

		// Set of converted dwSectionGroups
		Set<DiffSectionGroup> mappedDwSectionGroups = new HashSet<DiffSectionGroup>();
		mappedDwSectionGroups.addAll(dwSectionGroups.stream()
				// Convert DW SectionGroups to DiffSectionGroups for diffing
				.map(new DwSectionGroupMapper())
				.filter(dwSg -> dwSg != null)
				// Put all Sections together in a SectionGroup if they share a
				// termCode, course number, and course title (these comprise the
				// javersId for a DiffSectionGroup)
				.collect(Collectors.groupingBy(DiffSectionGroup::javersId,
						Collectors.reducing(null, new SectionGroupGrouper())))
				.values());

		// Even if there are the same number of SectionGroups as DwSectionGroups, there might
		// be different ones.
		Javers javers = JaversBuilder
				.javers()
				.withListCompareAlgorithm(ListCompareAlgorithm.LEVENSHTEIN_DISTANCE)
				.build();
		Diff diff = javers.compareCollections(sectionGroups, mappedDwSectionGroups, DiffSectionGroup.class);

		// List<Change> reducedChanges = reduceChanges(diff.getChanges());
		
		return diff;
	}


	@Override
	/**
	 * Doesn't actually return JSON yet, 'cause we don't know what format the JSON should be in.
	 */
	public String jsonDifferencesFromDw(Long scheduleId) {
		Diff rawChanges = identifyDifferencesFromDw(scheduleId);
		List<Change> changes = reduceChanges(rawChanges.getChanges());

		Javers javers = JaversBuilder.javers().build();
		return javers.getJsonConverter().toJson(changes);
	}

	/**
	 * Retains ValueChanges and converts SetChanges within the diff into
	 * ValueChanges if possible (i.e., helps detect course title renames or
	 * course number changes and such)
	 * 
	 * @param changes
	 * @return Flat list of all detected property changes extracted from potentially
	 *   paired objects.
	 * @see #reduceSetChanges(SetChange, Map)
	 */
	private List<Change> reduceChanges(List<Change> changes) {
		Map<String, Object> newAndRemoved = new HashMap<String, Object>();
		List<Change> newChanges = new ArrayList<Change>();
		List<SetChange> setChanges = new ArrayList<SetChange>();
		
		for (Change change : changes) {
			String changeType = change.getClass().getSimpleName();
			if (changeType.equals("ValueChange")) {
				ValueChange c = (ValueChange) change;
				newChanges.add(c);
			}

			// ObjectRemoved and NewObject can basically be ignored as far
			// as calculating the diff output because they cannot happen
			// independently of SetChanges. They should probably still be
			// compared in case there are property changes within them, though,
			// that JaVers won't detect because they're considered different
			// objects.
			else if (changeType.equals("ObjectRemoved")) {
				ObjectRemoved r = (ObjectRemoved) change;
				Optional<Object> r_obj = r.getAffectedObject();
				if (r_obj.isPresent())
					newAndRemoved.put(r.getAffectedGlobalId().toString(), r_obj.get());
			}
			else if (changeType.equals("NewObject")) {
				NewObject n = (NewObject) change;
				Optional<Object> n_obj = n.getAffectedObject();
				if (n_obj.isPresent())
					newAndRemoved.put(n.getAffectedGlobalId().toString(), n_obj.get());
			}

			// Save SetChanges to loop through later (we need all ObjectRemoveds
			// and NewObjects to be able to do the processing)
			else if (changeType.equals("SetChange")) {
				SetChange s = (SetChange) change;
				setChanges.add(s);
			}
		}

		for (SetChange chg : setChanges) {
			newChanges.addAll(reduceSetChanges(chg, newAndRemoved));
		}
		
		return newChanges;
	}
	
	/**
	 * Converts SetChanges (i.e., added and removed objects within a Set (e.g.,
	 * List, Set) within the raw diff (from JaVers) into ValueChanges if
	 * possible (i.e., helps detect course title renames or course number
	 * changes and such).
	 * 
	 * Looks through each possible pair of added and removed objects within the
	 * given SetChange and sees how similar they are. Finds the <i>n</i> most
	 * similar ones, which should hopefully be the same objects (just renamed),
	 * and compares them to see what's changed.
	 * 
	 * @param change
	 * @return
	 */
	private List<Change> reduceSetChanges(SetChange change, Map<String, Object> newAndRemoved) {
		// Objects within a SetChange have the same parent by definition
		List<String> removed = new ArrayList<String>();
		List<String> added = new ArrayList<String>();
		List<Change> newChanges = new ArrayList<Change>();
		Map<String[], Double> differences = new HashMap<String[], Double>();
		Javers javers = JaversBuilder
				.javers()
				.withListCompareAlgorithm(ListCompareAlgorithm.LEVENSHTEIN_DISTANCE)
				.build();

		// Find the names of added and removed objects so we can use the corresponding
		// objects from newAndRemoved to calculate similarities
		for (ContainerElementChange sc : change.getChanges()) {
			if (sc.getClass().getSimpleName().equals("ValueRemoved")) {
				removed.add(((ValueRemoved) sc).getRemovedValue().toString());
			}
			else if (sc.getClass().getSimpleName().equals("ValueAdded")) {
				added.add(((ValueAdded) sc).getAddedValue().toString());
			}
		}

		// Find added/removed pairs by finding the most similar added/removed pairs
		for (String rm : removed) {
			DiffEntity objRm = (DiffEntity) newAndRemoved.get(rm);

			if (objRm != null)
				for (String add : added) {
					DiffEntity objAdd = (DiffEntity) newAndRemoved.get(add);

					// Nothing happens if objects in the Set were only removed or
					// added, because similarities only gets filled when there are
					// added/removed pairs.
					if (objAdd != null)
						differences.put(new String[]{add, rm}, objRm.calculateDifferences(objAdd));
				}
		}

		// There can only be as many pairs as there are entities in the smallest set of entities
		int maxNumPairs = removed.size() < added.size() ? removed.size() : added.size();

		// Sort by similarity
		Map<String[], Double> sortedDifferences = new HashMap<String[], Double>(); // Differences are sorted into this Map in the next line
		differences.entrySet().stream().sorted(Comparator.comparing(e -> e.getValue())).limit(maxNumPairs)
			.forEachOrdered(e -> sortedDifferences.put(e.getKey(), e.getValue()));

		// Make JaVers compare the two unless the differences are above threshold
		for (String[] pairs : sortedDifferences.keySet()) {
			if (sortedDifferences.get(pairs) > DiffEntity.THRESHOLD)
				continue;

			// Remove the pair being compared from the removed and added Lists so
			// we can re-add unpaired changes to newChanges
			added.remove(pairs[0]);
			removed.remove(pairs[1]);

			// Don't compare if the pair doesn't have any differences
			if (sortedDifferences.get(pairs) == 0)
				continue;

			// Calculate ValueChanges from a new and removed pair
			DiffEntity removedEntity = (DiffEntity) newAndRemoved.get(pairs[1]);
			DiffEntity addedEntity = (DiffEntity) newAndRemoved.get(pairs[0]);
			removedEntity.syncJaversIds(addedEntity);
			Diff diff = javers.compare(removedEntity, addedEntity);
			
			// Recursively go through new SetChanges
			newChanges.addAll(reduceChanges(diff.getChanges()));
		}

		// Add unpaired changes back to a SetChange that we'll add back to newChanges
		List<ContainerElementChange> unpairedAddedAndRemoved = new ArrayList<ContainerElementChange>();
		unpairedAddedAndRemoved.addAll(added.stream().map(a ->
					new ValueAdded(newAndRemoved.get(a) != null ? newAndRemoved.get(a) : a))
				.collect(Collectors.toList()));
		unpairedAddedAndRemoved.addAll(removed.stream().map(r ->
					new ValueRemoved(newAndRemoved.get(r) != null ? newAndRemoved.get(r) : r))
				.collect(Collectors.toList()));
		if (unpairedAddedAndRemoved.size() > 0) {
			SetChange unpairedChanges = new SetChange(change.getAffectedGlobalId(), change.getProperty(), unpairedAddedAndRemoved);
			newChanges.add(unpairedChanges);
		}

		return newChanges;
	}
}

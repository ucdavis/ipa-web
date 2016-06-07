package edu.ucdavis.dss.ipa.diff.entities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Interface for all Diff entity objects. Defines methods that calculate
 * whether or not entities are different. Calculations are used to detect
 * renamed entities that JaVers can't detect.
 * 
 * @author Eric Lin
 */
public interface DiffEntity {
	/**
	 * Threshold value (weighted percent difference) for determining whether or
	 * not two things are dissimilar. Two entities have to be at least 70%
	 * similar to be considered the same entity. Used in {@link
	 * JpaDwSyncService} and {@link #calculateSetDifferences(Set, Set)} directly
	 * for deciding whether or not to pair things.
	 */
	static final double THRESHOLD = 0.3;
	
	
	/**
	 * Returns the (weighted) percent difference between this object and the
	 * given object, assuming they're objects of the same class. Value should
	 * never exceed 1.
	 * 
	 * @param o
	 * @return
	 * @see #calculateDifferences(Object)
	 */
	public double uncheckedCalculateDifferences(Object o);

	/**
	 * Makes the ids between the two entities the same so JaVers doesn't think
	 * they're different entities when comparing for differences.
	 * 
	 * @param entity
	 */
	public void syncJaversIds(DiffEntity entity);
	
	/**
	 * Changes the parent id so JaVers doesn't think elements have different
	 * parents when comparing for differences. (JaVers actually has no concept
	 * of parents, but that's beside the point).
	 */
	public default void syncJaversParentIds(String parentId) { };

	/**
	 * Loops over two sets of <i>n</i> entities and runs {@link
	 * #calculateDifferences(Object)} between every possible pair between the
	 * two sets. Then sorts the results of calculateDifferences to find the
	 * pairs with the smallest values from calculateDifferences.
	 * 
	 * @param entities
	 * @param otherEntities
	 * @return Sum of differences between the <i>n</i> pairs of entities with the
	 *         smallest differences
	 */
	public default double calculateSetDifferences(Set<? extends DiffEntity> entities, Set<? extends DiffEntity> otherEntities) {
		// No differences if both are null
		if (entities == null && otherEntities == null)
			return 0;
		
		// Max differences if either is null
		if (otherEntities == null)
			return entities.size();
		else if (entities == null)
			return otherEntities.size();

		// Add up the similarities for the top pairs
		List<Double> similarities = new ArrayList<Double>();
		int unpairedDifferences = 0;
		for (DiffEntity entity : entities)
			for (DiffEntity otherEntity : otherEntities)
				similarities.add(entity.calculateDifferences(otherEntity));

		// Cull everything that isn't a pair
		similarities.removeIf(e -> e > THRESHOLD);

		// Sort them to find the most similar pairs
		Collections.sort(similarities);

		// There can only be as many pairs as there are entities in the smallest
		// set of entities; reduce the set to maxNumPairs pairs.
		int maxNumPairs = similarities.size();
		if (maxNumPairs > entities.size() || maxNumPairs > otherEntities.size())
			maxNumPairs = entities.size() < otherEntities.size() ? entities.size() : otherEntities.size();
		// Sanity check
		maxNumPairs = maxNumPairs > 0 ? maxNumPairs : 0;

		// Everything that isn't paired is a 100% difference, so add 1 for each
		// item that the list of pairs doesn't have
		unpairedDifferences += maxNumPairs < otherEntities.size() ? otherEntities.size() - maxNumPairs : 0;
		unpairedDifferences += maxNumPairs < entities.size() ? entities.size() - maxNumPairs : 0;

		// Add up all the differences
		return unpairedDifferences + similarities.subList(0, maxNumPairs).stream().reduce(0.0, (m1, m2) -> m1 + m2);
	}

	/**
	 * Safely calculates the (weighted) percent difference between this object
	 * and the given object.
	 * 
	 * @param o
	 * @return
	 */
	public default double calculateDifferences(Object o) {
		if (this == o)
			return 0;
		if (o == null)
			return 1;
		if (getClass() != o.getClass())
			return 1;

		return uncheckedCalculateDifferences(o);
	}
}

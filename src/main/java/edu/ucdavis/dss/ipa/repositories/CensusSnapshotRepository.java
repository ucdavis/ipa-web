package edu.ucdavis.dss.ipa.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface CensusSnapshotRepository extends CrudRepository<CensusSnapshot, Long> {

	public CensusSnapshot findBySectionIdAndSnapshotCode(Long id, String code);

	public List<CensusSnapshot> findBySectionId(long sectionId);
}
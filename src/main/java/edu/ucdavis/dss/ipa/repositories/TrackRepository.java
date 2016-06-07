package edu.ucdavis.dss.ipa.repositories;

import org.springframework.data.repository.CrudRepository;

import edu.ucdavis.dss.ipa.entities.Track;

public interface TrackRepository extends CrudRepository<Track, Long> {

	Track findOneByWorkgroupIdAndName(long workgroupId, String trackName);

}

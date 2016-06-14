package edu.ucdavis.dss.ipa.services.jpa;

import edu.ucdavis.dss.ipa.entities.CourseOfferingGroup;
import edu.ucdavis.dss.ipa.entities.Track;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.repositories.TrackRepository;
import edu.ucdavis.dss.ipa.services.ScheduleService;
import edu.ucdavis.dss.ipa.services.TrackService;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class JpaTrackService implements TrackService {
	@Inject TrackRepository trackRepository;
	@Inject ScheduleService scheduleService;

	@Override
	@Transactional
	public Track saveTrack(Track track)
	{
		return this.trackRepository.save(track);
	}

	@Override
	public Track findOneById(Long id) {
		return this.trackRepository.findOne(id);
	}

	@Override
	public List<CourseOfferingGroup> getCourseOfferingGroupsByTrackId(Long id) {
		Track track = this.findOneById(id);
		return track.getCourseOfferingGroups();
	}

	@Override
	public List<Track> searchTracks(String query, Workgroup workgroup) {

		List<Track> results = new ArrayList<Track>();
		List<Track> tracks = new ArrayList<Track>();

		tracks = workgroup.getTracks();
		
		for (Track track : tracks) {
			if(track.getName().toLowerCase().contains(query.toLowerCase())) {
				results.add(track);
			}
		}
		
		return results;
	}

	@Override
	public Track archiveTrackByTrackId(Long id) {
		
		Track track = this.trackRepository.findOne(id);

		// Remove this track from COGs only in active schedules
		for (CourseOfferingGroup cog : track.getCourseOfferingGroups()){
			boolean isHistorical = scheduleService.isScheduleClosed(cog.getSchedule().getId());
			if (!isHistorical) {
				List<Track> tracks = cog.getTracks();
				tracks.remove(track);
				cog.setTracks(tracks);
			}
		}

		track.setArchived(true);
		return this.trackRepository.save(track);
	}

	@Override
	public Track findOrCreateTrackByWorkgroupAndTrackName(Workgroup workgroup, String trackName) {
		if (workgroup == null) return null;

		Track track = this.trackRepository.findOneByWorkgroupIdAndName(workgroup.getId(), trackName);

		if (track == null) {
			track = new Track();
			track.setWorkgroup(workgroup);
			track.setName(trackName);
			track = this.trackRepository.save(track);
		} else if (track.isArchived()) {
			track.setArchived(false);
			track = this.trackRepository.save(track);
		}

		return track;
	}
}

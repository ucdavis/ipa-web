package edu.ucdavis.dss.ipa.services.jpa;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import edu.ucdavis.dss.ipa.entities.*;
import edu.ucdavis.dss.ipa.services.TermService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.stereotype.Service;

import edu.ucdavis.dss.ipa.repositories.LocationRepository;
import edu.ucdavis.dss.ipa.services.LocationService;

import java.util.List;

@Service
public class JpaLocationService implements LocationService {
    @Inject LocationRepository locationRepository;
    @Inject WorkgroupService workgroupService;
    @Inject TermService termService;

    @Override
    @Transactional
    public Location save(Location activity)
    {
        return this.locationRepository.save(activity);
    }

    @Override
    public Location findOneById(Long id) {
        return this.locationRepository.findById(id).orElse(null);
    }

    @Transactional
    @Override
    public Location archiveById(Long id) {
        Location location = this.locationRepository.findById(id).orElse(null);

        if(location == null) { return null; }

        // Remove this location from COGs only in active schedules
        for (Activity activity : location.getActivities()) {
            String termCode = null;

            if (activity.getSection() != null) {
                termCode = activity.getSection().getSectionGroup().getTermCode();
            }

            if (activity.getSectionGroup().getTermCode() != null) {
                termCode = activity.getSectionGroup().getTermCode();
            }

            boolean isHistorical = termService.isHistoricalByTermCode(termCode);
            if (!isHistorical) {
                activity.setLocation(null);
            }
        }

        location.setArchived(true);
        return this.locationRepository.save(location);
    }

    @Override
    public List<Location> findByWorkgroupId(long workgroupId) {
        return locationRepository.findByWorkgroupId(workgroupId);
    }

    @Override
    public Location findOrCreateByWorkgroupAndDescription(Workgroup workgroup, String description) {
        if (workgroup == null) return null;

        Location location = this.locationRepository.findOneByWorkgroupIdAndDescription(workgroup.getId(), description);

        if (location == null) {
            location = new Location();
            location.setWorkgroup(workgroup);
            location.setDescription(description);
            location = this.locationRepository.save(location);
        } else if (location.isArchived()) {
            location.setArchived(false);
            location = this.locationRepository.save(location);
        }

        return location;
    }


}

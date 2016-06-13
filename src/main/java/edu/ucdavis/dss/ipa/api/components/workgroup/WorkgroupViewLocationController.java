package edu.ucdavis.dss.ipa.api.components.workgroup;

import edu.ucdavis.dss.ipa.api.components.workgroup.views.WorkgroupView;
import edu.ucdavis.dss.ipa.api.components.workgroup.views.factories.WorkgroupViewFactory;
import edu.ucdavis.dss.ipa.entities.Location;
import edu.ucdavis.dss.ipa.entities.Track;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.services.LocationService;
import edu.ucdavis.dss.ipa.services.TrackService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin // TODO: make CORS more specific depending on profile
public class WorkgroupViewLocationController {

    @Inject WorkgroupViewFactory workgroupViewFactory;
    @Inject LocationService locationService;
    @Inject WorkgroupService workgroupService;

    @PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
    @RequestMapping(value = "/api/workgroupView/{workgroupCode}/locations", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Location addLocation(@PathVariable String workgroupCode, @RequestBody Location location, HttpServletResponse httpResponse) {
        Workgroup workgroup = workgroupService.findOneByCode(workgroupCode);

        return locationService.findOrCreateByWorkgroupAndDescription(workgroup, location.getDescription());
    }

    @PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
    @RequestMapping(value = "/api/workgroupView/{workgroupCode}/locations/{locationId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public Location updateLocation(@PathVariable String workgroupCode, @PathVariable long locationId,
                           @RequestBody Location location, HttpServletResponse httpResponse) {
        Location editedLocation = locationService.findOneById(locationId);
        editedLocation.setDescription(location.getDescription());
        return locationService.save(editedLocation);
    }

    @PreAuthorize("hasPermission(#workgroupCode, 'workgroup', 'academicCoordinator')")
    @RequestMapping(value = "/api/workgroupView/{workgroupCode}/locations/{locationId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public void archiveLocation(@PathVariable String workgroupCode, @PathVariable long locationId, HttpServletResponse httpResponse) {
        locationService.archiveById(locationId);
    }

}

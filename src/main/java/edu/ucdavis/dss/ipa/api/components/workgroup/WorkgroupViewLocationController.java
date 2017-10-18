package edu.ucdavis.dss.ipa.api.components.workgroup;

import edu.ucdavis.dss.ipa.api.components.workgroup.views.factories.WorkgroupViewFactory;
import edu.ucdavis.dss.ipa.entities.Location;
import edu.ucdavis.dss.ipa.entities.Workgroup;
import edu.ucdavis.dss.ipa.security.authorization.Authorizer;
import edu.ucdavis.dss.ipa.services.LocationService;
import edu.ucdavis.dss.ipa.services.WorkgroupService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin
public class WorkgroupViewLocationController {

    @Inject WorkgroupViewFactory workgroupViewFactory;
    @Inject LocationService locationService;
    @Inject WorkgroupService workgroupService;

    @RequestMapping(value = "/api/workgroupView/{workgroupId}/locations", method = RequestMethod.POST, produces="application/json")
    @ResponseBody
    public Location addLocation(@PathVariable Long workgroupId, @RequestBody Location location, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        Workgroup workgroup = workgroupService.findOneById(workgroupId);

        return locationService.findOrCreateByWorkgroupAndDescription(workgroup, location.getDescription());
    }

    @RequestMapping(value = "/api/workgroupView/{workgroupId}/locations/{locationId}", method = RequestMethod.PUT, produces="application/json")
    @ResponseBody
    public Location updateLocation(@PathVariable Long workgroupId, @PathVariable long locationId,
                           @RequestBody Location location, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        Location editedLocation = locationService.findOneById(locationId);
        editedLocation.setDescription(location.getDescription());
        return locationService.save(editedLocation);
    }

    @RequestMapping(value = "/api/workgroupView/{workgroupId}/locations/{locationId}", method = RequestMethod.DELETE, produces="application/json")
    @ResponseBody
    public Location archiveLocation(@PathVariable Long workgroupId, @PathVariable long locationId, HttpServletResponse httpResponse) {
        Authorizer.hasWorkgroupRole(workgroupId, "academicPlanner");

        return locationService.archiveById(locationId);
    }

}

package edu.ucdavis.dss.ldap;

import edu.ucdavis.dss.ldap.dto.LdapPerson;
import edu.ucdavis.dss.ldap.mappers.LdapPersonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DefaultTlsDirContextAuthenticationStrategy;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.support.LdapEncoder;

import java.util.List;


/**
 * Created by okadri on 6/23/16.
 */
public class LdapClient {
    private LdapTemplate ldapTemplate = null;
    private static final Logger log = LoggerFactory.getLogger("LdapLogger");
    private String url, base, user, password;

    public LdapClient(String url, String base, String user, String password) {
        this.url = url;
        this.base = base;
        this.user = user;
        this.password = password;
    }

    public boolean bind() {
        if(ldapTemplate != null) return true;

        LdapContextSource ctxSrc = new LdapContextSource();

        ctxSrc.setUrl(this.url);
        ctxSrc.setBase(this.base);
        ctxSrc.setUserDn(this.user);
        ctxSrc.setPassword(this.password);
        ctxSrc.setAuthenticationStrategy(new DefaultTlsDirContextAuthenticationStrategy());
        ctxSrc.afterPropertiesSet();

        ldapTemplate = new LdapTemplate(ctxSrc);

        return true;
    }

    /**
     * Constructs the proper LDAP search query for the given affiliation.
     *
     * @param affiliation The affiliation to match ucdAppointmentDepartmentCode, e.g. "staff:student"
     * @return the sub-query string
     */
    private String setupLdapStringByAffiliation(String affiliation) {

        return ("(&(ucdPersonAffiliation=" + LdapEncoder.filterEncode(affiliation) + "*))");
    }

    public List<LdapPerson> getAllStaff() {
        if (ldapTemplate != null) {
            List<LdapPerson> allStaff = ldapTemplate.search("",
                    setupLdapStringByAffiliation("staff"),
                    new LdapPersonMapper());
            log.debug("LDAP Staff total is "
                    + Integer.toString(allStaff.size()));

            return allStaff;
        } else {
            throw new IllegalStateException("LDAP not bound.");
        }
    }

    public List<LdapPerson> getAllFaculty() {
        if (ldapTemplate != null) {
            List<LdapPerson> allFaculty = ldapTemplate.search("",
                    setupLdapStringByAffiliation("faculty"),
                    new LdapPersonMapper());
            log.debug("LDAP Faculty total is "
                    + Integer.toString(allFaculty.size()));

            return allFaculty;
        } else {
            throw new IllegalStateException("LDAP not bound.");
        }
    }

    public List<LdapPerson> getAllStudents() {
        if (ldapTemplate != null) {
            List<LdapPerson> allStudents = ldapTemplate.search("",
                    setupLdapStringByAffiliation("student:graduate"),
                    new LdapPersonMapper());
            log.debug("LDAP Students total is "
                    + Integer.toString(allStudents.size()));

            return allStudents;
        } else {
            throw new IllegalStateException("LDAP not bound.");
        }
    }

    public List<LdapPerson> getAllLogins(List<String> loginIds) {
        if (ldapTemplate != null) {
            String loginIdFilter = "(&";

            for (String loginId : loginIds) {
                loginIdFilter += "(uid='" + LdapEncoder.nameEncode(loginId) + "')";
            }

            loginIdFilter += ")";

            List<LdapPerson> people = ldapTemplate.search("", loginIdFilter,
                    new LdapPersonMapper());
            log.debug("Given " + loginIds.size() + " loginIds, found "
                    + Integer.toString(people.size()));

            return people;
        } else {
            throw new IllegalStateException("LDAP not bound.");
        }
    }

    public long countStaff() {
        return getAllStaff().size();
    }

    public long countFaculty() {
        return getAllFaculty().size();
    }

    public long countStudents() {
        return getAllStudents().size();
    }

    public List<LdapPerson> getAllEmployeeIds(List<String> instructorEmployeeIds) {
        if ((ldapTemplate != null) && (instructorEmployeeIds.size() > 0)) {
            String employeeIdFilter = "(&";

            for (String employeeId : instructorEmployeeIds) {
                if (employeeId.length() > 0) {
                    employeeIdFilter += "(ucdPersonSid='" + LdapEncoder.filterEncode(employeeId) + "')";
                }
            }

            employeeIdFilter += ")";

            log.debug("Using LDAP filter: " + employeeIdFilter);

            List<LdapPerson> people = ldapTemplate.search("", employeeIdFilter,
                    new LdapPersonMapper());

            log.debug("Given " + instructorEmployeeIds.size()
                    + " employee IDs, found " + Integer.toString(people.size()));

            return people;
        } else {
            throw new IllegalStateException("LDAP not bound.");
        }
    }

    public LdapPerson getPersonByUcdPersonPIDM(String ucdPersonPIDM) {
        if ((ldapTemplate != null) && (ucdPersonPIDM != null)) {
            String employeePIDMFilter = "(ucdPersonPIDM='" + LdapEncoder.filterEncode(ucdPersonPIDM) + "')";

            log.debug("Using LDAP filter: " + employeePIDMFilter);

            List<LdapPerson> people = ldapTemplate.search("", employeePIDMFilter,
                    new LdapPersonMapper());

            log.debug("LDAP returned " + Integer.toString(people.size()) + " people. Will only process the first.");

            return people.get(0);
        } else {
            throw new IllegalStateException("LDAP not bound.");
        }
    }

    public LdapPerson getPersonByLoginId(String loginId) {
        if ((ldapTemplate != null) && (loginId != null)) {
            String employeeLoginIdFilter = "(uid=" + LdapEncoder.filterEncode(loginId) + ")";

            log.debug("Using LDAP filter: " + employeeLoginIdFilter);

            List<LdapPerson> people = ldapTemplate.search("", employeeLoginIdFilter,
                    new LdapPersonMapper());

            log.debug("LDAP returned " + Integer.toString(people.size()) + " people. Will only process the first.");

            if(people.size() > 0) {
                return people.get(0);
            } else {
                return null;
            }
        } else {
            throw new IllegalStateException("LDAP not bound.");
        }
    }

    public LdapPerson getPersonByUcdStudentSID(String ucdStudentSID) {
        if ((ldapTemplate != null) && (ucdStudentSID != null)) {

            String employeeSIDFilter = "(ucdStudentSID='" + LdapEncoder.filterEncode(ucdStudentSID) + "')";

            log.debug("Using LDAP filter: " + employeeSIDFilter);

            List<LdapPerson> people = ldapTemplate.search("", employeeSIDFilter,
                    new LdapPersonMapper());

            log.debug("LDAP returned " + Integer.toString(people.size()) + " people. Will only process the first.");

            return people.get(0);
        } else {
            throw new IllegalStateException("LDAP not bound.");
        }
    }

}

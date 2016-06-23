package edu.ucdavis.dss.ldap.mappers;

import edu.ucdavis.dss.ldap.dto.LdapPerson;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;

import org.springframework.ldap.core.AttributesMapper;

/**
 * Created by okadri on 6/23/16.
 */
public class LdapPersonMapper implements AttributesMapper<LdapPerson> {

    @Override
    public LdapPerson mapFromAttributes(Attributes attributes) throws NamingException {
        LdapPerson ldapPerson = new LdapPerson();

        Attribute first = attributes.get("givenName");
        Attribute last = attributes.get("sn");
        Attribute displayName = attributes.get("displayName");
        Attribute mail = attributes.get("mail");
        Attribute telephoneNumber = attributes.get("telephoneNumber");
        Attribute street = attributes.get("street");
        Attribute ucdPersonUUID = attributes.get("ucdPersonUUID");
        Attribute eduPersonPrincipalName = attributes.get("eduPersonPrincipalName");
        Attribute uid = attributes.get("uid");
        Attribute employeeNumber = attributes.get("employeeNumber");
        Attribute ucdStudentSid = attributes.get("ucdStudentSID");
        Attribute ucdPersonPIDM = attributes.get("ucdPersonPIDM");

        if(first != null) { ldapPerson.setFirst((String)first.get()); }
        if(last != null) { ldapPerson.setLast((String)last.get()); }

        if(displayName != null) {
            ldapPerson.setDisplayName((String)displayName.get());
        } else if (first != null && last != null) {
            ldapPerson.setDisplayName(first.get() + " " + last.get());
        }

        if(mail != null) { ldapPerson.setMail((String)mail.get()); }
        if(telephoneNumber != null) { ldapPerson.setTelephoneNumber((String)telephoneNumber.get()); }
        if(street != null) { ldapPerson.setStreet((String)street.get()); }
        if(ucdPersonUUID != null) { ldapPerson.setUcdPersonUUID((String)ucdPersonUUID.get()); }
        if(employeeNumber != null) { ldapPerson.setEmployeeNumber((String)employeeNumber.get()); }
        if(ucdStudentSid != null) { ldapPerson.setUcdStudentSid((String)ucdStudentSid.get()); }
        if(ucdPersonPIDM != null) { ldapPerson.setUcdPersonPIDM((String)ucdPersonPIDM.get()); }

        // If eduPersonPrincipalName is missing, use UID, else skip
        if(eduPersonPrincipalName != null) {
            String str = (String)eduPersonPrincipalName.get();
            ldapPerson.setLoginId(str.substring(0, str.indexOf("@")));
        } else if (uid != null) {
            ldapPerson.setLoginId((String)uid.get());
        } else {
            return null;
        }

        return ldapPerson;
    }
}

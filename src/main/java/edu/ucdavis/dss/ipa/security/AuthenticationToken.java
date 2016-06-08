package edu.ucdavis.dss.ipa.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;

public class AuthenticationToken extends AbstractAuthenticationToken {
    private AuthUserDetails userDetails;

    public AuthenticationToken(AuthUserDetails userDetails) {
        super(userDetails.getAuthorities());
        super.setAuthenticated(true);

        this.userDetails = userDetails;

        setDetails(userDetails);
    }

    @Override
    public Object getCredentials() { return "NA"; }

    @Override
    public Object getPrincipal() { return userDetails; }

    //public AuthUserDetails getUserDetails() { return userDetails; }

    //public boolean hasPermission(PermissionEnum permissionType) { return getUserDetails() != null && getUserDetails().hasPermission(permissionType); }

    //public Long getUserId() { return userDetails.getId(); }

    @Override
    public String getName() { return userDetails.getName(); }

    @Override
    public String toString() { return getPrincipal().toString(); }
}

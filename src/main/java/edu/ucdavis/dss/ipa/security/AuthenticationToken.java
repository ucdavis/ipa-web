package edu.ucdavis.dss.ipa.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.Collection;

public class AuthenticationToken extends AbstractAuthenticationToken {
    private AuthenticationPrincipal userPrincipal;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public AuthenticationToken(Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
    }


//    public AuthenticationToken(AuthenticationPrincipal userPrincipal) {
//        //super(userDetails.getAuthorities());
//        super.setAuthenticated(true);
//
//        this.userPrincipal = userPrincipal;
//
//        setDetails(userDetails);
//    }

    @Override
    public Object getCredentials() { return "NA"; }

    @Override
    public Object getPrincipal() { return userPrincipal; }

    //public AuthUserDetails getUserDetails() { return userDetails; }

    //public boolean hasPermission(PermissionEnum permissionType) { return getUserDetails() != null && getUserDetails().hasPermission(permissionType); }

    //public Long getUserId() { return userDetails.getId(); }

    @Override
    public String getName() { return userPrincipal.toString(); }; //.Details.getName(); }

    @Override
    public String toString() { return getPrincipal().toString(); }
}

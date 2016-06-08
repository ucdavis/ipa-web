package edu.ucdavis.dss.ipa.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static java.util.stream.Collectors.toSet;
import static org.springframework.util.StringUtils.isEmpty;

public class AuthUserDetails implements UserDetails, Principal {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String userType;
    private String loginId;
    private String firstName;
    private String lastName;
    private String email;
    private String name;

    private Set<String> permissions = new HashSet<>(); //Sets.newHashSet();

    public AuthUserDetails(Long id) {
        //Preconditions.checkNotNull(id);

        this.id = id;
    }

//    public AuthUserDetails(User user) { this(user, null); }

//    public AuthUserDetails(User user, Collection<PermissionEnum> permissions) {
//        //Preconditions.checkNotNull(user);
//
//        id = user.getId();
//        userType = user.getClassUnproxied().getSimpleName();
//        name = user.getName();
//
//        if ((user.isA(Person.class))) {
//            Person person = (Person) user;
//
//            loginId = person.getLoginId();
//            email = person.getEmail();
//            firstName = person.getFirstName();
//            lastName = person.getLastName();
//        }
//        else {
//            loginId = user.getId().toString();
//            email = "";
//            firstName = "";
//            lastName = user.getName();
//        }
//
//        if (permissions != null) {
//            permissions.stream()
//                    .map(EnumWithDescription::getCode)
//                    .forEach(code -> this.permissions.add(code));
//        }
//    }

    //@JsonProperty
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    //@JsonProperty
    public String getUserType() { return userType; }
    public void setUserType(String userType) { this.userType = userType; }

    //@JsonProperty
    public String getLoginId() { return loginId; }
    public void setLoginId(String loginId) { this.loginId = loginId; }

    //@JsonProperty
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    //@JsonProperty
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    //@JsonProperty
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    //@JsonProperty
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    //public Set<String> getPermissions() { return permissions; }
    //public void setPermissions(Set<String> permissions) { this.permissions = permissions; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(toSet());
    }

    //public boolean hasPermission(PermissionEnum permissionType) { return permissions.contains(permissionType.getCode()); }

    @Override
    public String getPassword() { return null; }

    @Override
    public String getUsername() { return loginId; }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }

    @Override
    public String toString() {
        if (!isEmpty(getEmail())) {
            return String.format( "%s ( %s, %s )", getName(), getLoginId(), getEmail() );
        }
        else {
            return String.format( "%s ( %s )", getName(), getLoginId() );
        }
    }
}

package berlin.htw.hrz.kb

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

/**
 * Created by didschu on 14.07.16.
 */
class MyUserDetails extends User {

    // extra instance variables
    final String fullname
    final String email

    MyUserDetails(String username, String password, boolean enabled, boolean accountNonExpired,
                  boolean credentialsNonExpired, boolean accountNonLocked,
                  Collection<GrantedAuthority> authorities, String fullname,
                  String email) {


        super(username, password, enabled, accountNonExpired, credentialsNonExpired,
                accountNonLocked, authorities)

        this.fullname = fullname
        this.email = email
    }
}

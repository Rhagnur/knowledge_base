/*
 * Created by didschu
 */
package berlin.htw.hrz.kb

import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper

/**
 * Needed for extending the principal data of the user
 */
class MyUserDetailsContextMapper implements UserDetailsContextMapper{
    UserDetails mapUserFromContext(DirContextOperations ctx, String username,
                                   Collection authorities) {

        String fullname = ctx.originalAttrs.gecos.values[0]
        String email = ctx.originalAttrs.mail.values[0].toString().toLowerCase()

        new MyUserDetails(username, '', true, true, true, true,
                authorities, fullname, email)
    }

    void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        throw new IllegalStateException("Only retrieving data from AD is currently supported")
    }
}

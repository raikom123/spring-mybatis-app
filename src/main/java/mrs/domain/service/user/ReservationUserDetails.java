package mrs.domain.service.user;

import java.util.Collection;
import lombok.Getter;
import mrs.domain.model.mybatis.Usr;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class ReservationUserDetails implements UserDetails {

    private static final long serialVersionUID = -8483333600109767964L;

    private final Usr user;

    public ReservationUserDetails(Usr usr) {
        this.user = usr;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return AuthorityUtils.createAuthorityList("ROLE_" + user.getRoleName());
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserId();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

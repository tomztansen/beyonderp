package com.vaadinerp.security.repository;

import com.vaadinerp.security.entity.AppUserFavoriteMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

public interface AppUserFavoriteMenuRepository extends JpaRepository<AppUserFavoriteMenu, Long> {
    List<AppUserFavoriteMenu> findByUsername(String username);
    Optional<AppUserFavoriteMenu> findByUsernameAndMenuCode(String username, String menuCode);
    @Transactional
    void deleteByUsernameAndMenuCode(String username, String menuCode);
}

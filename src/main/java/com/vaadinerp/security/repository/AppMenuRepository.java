package com.vaadinerp.security.repository;

import com.vaadinerp.security.entity.AppMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppMenuRepository extends JpaRepository<AppMenu, String> {
    List<AppMenu> findAllByOrderByDisplayOrderAsc();
    List<AppMenu> findByParentMenuCodeIsNullOrderByDisplayOrderAsc();
    List<AppMenu> findByParentMenuCodeOrderByDisplayOrderAsc(String parentMenuCode);
}

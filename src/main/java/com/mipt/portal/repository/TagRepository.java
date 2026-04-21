package com.mipt.portal.repository;

import com.mipt.portal.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long>, CustomTagRepository {
}

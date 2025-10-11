package com.oem.evwarranty.repository;

import com.oem.evwarranty.model.Template;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends CrudRepository<Template,Long> {
}

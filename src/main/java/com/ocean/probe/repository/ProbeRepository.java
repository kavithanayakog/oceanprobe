package com.ocean.probe.repository;

import com.ocean.probe.model.ProbeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProbeRepository extends JpaRepository<ProbeEntity, Long> { }

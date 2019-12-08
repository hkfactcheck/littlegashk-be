package io.littlegashk.webapp.service;

import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

@Transactional
@Service
public interface MigrationService {

  void migrateTags();
}

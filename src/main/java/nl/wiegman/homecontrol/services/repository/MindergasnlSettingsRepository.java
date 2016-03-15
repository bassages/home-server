package nl.wiegman.homecontrol.services.repository;

import nl.wiegman.homecontrol.services.model.api.MindergasnlSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

@Transactional
public interface MindergasnlSettingsRepository extends JpaRepository<MindergasnlSettings, Long> {

}

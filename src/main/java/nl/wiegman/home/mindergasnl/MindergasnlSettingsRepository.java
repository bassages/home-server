package nl.wiegman.home.mindergasnl;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

@Transactional
public interface MindergasnlSettingsRepository extends JpaRepository<MindergasnlSettings, Long> {

}

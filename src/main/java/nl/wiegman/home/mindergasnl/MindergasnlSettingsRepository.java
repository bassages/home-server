package nl.wiegman.home.mindergasnl;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

@Transactional
public interface MindergasnlSettingsRepository extends JpaRepository<MindergasnlSettings, Long> {

}

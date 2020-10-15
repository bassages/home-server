package nl.homeserver.energie.mindergasnl;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface MindergasnlSettingsRepository extends JpaRepository<MindergasnlSettings, Long> {

    Optional<MindergasnlSettings> findOneByIdIsNotNull();
}

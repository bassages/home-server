package nl.homeserver.klimaat;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

@Transactional
public interface KlimaatSensorRepository extends JpaRepository<KlimaatSensor, Short> {

    Optional<KlimaatSensor> findFirstByCode(String code);
}

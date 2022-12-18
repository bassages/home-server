package nl.homeserver.climate;

import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

@Transactional
interface KlimaatSensorRepository extends JpaRepository<KlimaatSensor, Short> {

    Optional<KlimaatSensor> findFirstByCode(String code);
}

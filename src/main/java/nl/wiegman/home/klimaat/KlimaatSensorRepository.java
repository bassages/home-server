package nl.wiegman.home.klimaat;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

@Transactional
public interface KlimaatSensorRepository extends JpaRepository<KlimaatSensor, Short> {

    KlimaatSensor findFirstByCode(String code);
}

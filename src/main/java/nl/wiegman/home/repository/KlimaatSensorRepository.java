package nl.wiegman.home.repository;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;

import nl.wiegman.home.model.KlimaatSensor;

@Transactional
public interface KlimaatSensorRepository extends JpaRepository<KlimaatSensor, Short> {

    KlimaatSensor findFirstByCodeIgnoreCase(String omschrijving);

    KlimaatSensor findFirstByCode(String code);
}

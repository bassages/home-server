package nl.wiegman.homecontrol.services.service;

import nl.wiegman.homecontrol.services.model.api.Kosten;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

@Transactional
public interface KostenRepository extends JpaRepository<Kosten, Long> {

}

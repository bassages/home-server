package nl.wiegman.home.repository;

import nl.wiegman.home.model.Klimaat;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.transaction.Transactional;

@Transactional
public interface BinnenklimaatRepository extends JpaRepository<Klimaat, Long> {

}

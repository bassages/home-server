package nl.homeserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource("/integrationtests.properties")
public abstract class RepositoryIntegrationTest {

    @Autowired
    protected TestEntityManager entityManager;

}

package nl.homeserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource("/integrationtests.properties")
public abstract class RepositoryIntegrationTest {

    @Autowired
    protected TestEntityManager entityManager;

}

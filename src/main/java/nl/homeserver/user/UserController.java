package nl.homeserver.user;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nl.homeserver.config.Paths;

@RestController
@RequestMapping(Paths.API + "/user")
class UserController {

    @GetMapping
    public Principal user(final Principal user) {
        return user;
    }
}

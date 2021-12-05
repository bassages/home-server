package nl.homeserver.user;

import nl.homeserver.config.Paths;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping(Paths.API + "/user")
class UserController {

    @GetMapping
    public UserDto user(final Principal user) {
        return new UserDto(user.getName());
    }
}

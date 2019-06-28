package com.example.clientsideencryption.repository;

import com.example.clientsideencryption.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig
@SpringBootTest
class UserRepositoryTest {

    static {
        System.setProperty("aws.profile", "develop");
    }
    @Autowired
    UserRepository userRepository;

    @Test
    void test() {

        final User user = new User("yusuke","hasegawa");
        userRepository.save(user);
        assertThat(user.getId()).isNotNull();

        final Optional<User> ret = userRepository.findById(user.getId());
        assertThat(ret.isPresent()).isTrue();
        assertThat(ret.get()).isEqualTo(user);
    }
}
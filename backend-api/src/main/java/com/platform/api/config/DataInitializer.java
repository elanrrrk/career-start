package com.platform.api.config;

import com.platform.api.entity.Case;
import com.platform.api.entity.Role;
import com.platform.api.entity.User;
import com.platform.api.repository.CaseRepository;
import com.platform.api.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CaseRepository caseRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, CaseRepository caseRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.caseRepository = caseRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        User admin = userRepository.findByEmail("admin@platform.com").orElseGet(() ->
            userRepository.save(new User("admin@platform.com", passwordEncoder.encode("admin123"), Role.ADMIN))
        );

        User expert = userRepository.findByEmail("expert@platform.com").orElseGet(() ->
            userRepository.save(new User("expert@platform.com", passwordEncoder.encode("expert123"), Role.EXPERT))
        );

        userRepository.findByEmail("client@platform.com").orElseGet(() ->
            userRepository.save(new User("client@platform.com", passwordEncoder.encode("client123"), Role.CLIENT))
        );

        // Создаем тестовый опубликованный кейс, если база пустая
        if (caseRepository.count() == 0) {
            Case demoCase = new Case(
                "Анализ архитектуры высоконагруженного сервиса",
                "Необходимо провести аудит REST API и спроектировать масштабируемую схему данных.",
                "Архитектура",
                List.of("PostgreSQL", "Java", "HighLoad"),
                90,
                expert
            );
            demoCase.setPublished(true);
            caseRepository.save(demoCase);
        }
    }
}
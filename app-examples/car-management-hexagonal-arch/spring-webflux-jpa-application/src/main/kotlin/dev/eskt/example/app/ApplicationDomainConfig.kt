package dev.eskt.example.app

import dev.eskt.example.domain.UseCase
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@ComponentScan(
    basePackages = [
        "dev.eskt.example.domain",
    ],
    includeFilters = [
        ComponentScan.Filter(
            type = FilterType.ANNOTATION,
            classes = [
                UseCase::class,
            ],
        ),
    ],
)
class ApplicationDomainConfig

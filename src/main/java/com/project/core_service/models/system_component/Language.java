package com.project.core_service.models.system_component;

import jakarta.annotation.Nonnull;

// TODO: Nicer way to represent this?
public record Language(@Nonnull String name, @Nonnull String version) {
}
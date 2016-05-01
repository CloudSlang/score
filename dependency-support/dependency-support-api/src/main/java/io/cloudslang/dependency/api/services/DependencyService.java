package io.cloudslang.dependency.api.services;

import java.util.List;

public interface DependencyService {
    List<String> resolveDependencies(List<String> resources);
}

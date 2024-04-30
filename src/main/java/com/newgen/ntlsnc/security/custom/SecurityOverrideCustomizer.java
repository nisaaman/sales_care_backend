package com.newgen.ntlsnc.security.custom;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author nisa
 * @date 6/12/22
 * @time 9:59 AM
 */
@Component
public class SecurityOverrideCustomizer implements OpenApiCustomiser {

    public static final String UNSECURED = "security.open";

    public static final List<Function<PathItem,     Operation>> OPERATION_GETTERS = Arrays.asList(
            PathItem::getGet, PathItem::getPost, PathItem::getDelete, PathItem::getHead,
            PathItem::getOptions, PathItem::getPatch, PathItem::getPut, PathItem::getTrace);

    private static Stream<Operation> getOperations(PathItem pathItem) {
        return OPERATION_GETTERS.stream()
                .map(getter -> getter.apply(pathItem))
                .filter(Objects::nonNull);
    }

    private static List<String> filterTags(List<String> tags) {
        return tags.stream()
                .filter(t -> !t.equals(UNSECURED))
                .collect(Collectors.toList());
    }

    @Override
    public void customise(OpenAPI openApi) {
        openApi.getPaths().forEach((path, item) -> getOperations(item).forEach(operation -> {
            List<String> tags = operation.getTags();
            if (tags != null && tags.contains(UNSECURED)) {
                operation.setSecurity(Collections.emptyList());
                operation.setTags(filterTags(tags));
            }
        }));
    }
}

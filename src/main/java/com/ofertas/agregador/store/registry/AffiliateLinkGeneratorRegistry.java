package com.ofertas.agregador.store.registry;

import com.ofertas.agregador.domain.enums.StoreType;
import com.ofertas.agregador.store.contract.AffiliateLinkGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Resolve qual AffiliateLinkGenerator usar para cada loja em runtime.
 * Spring injeta automaticamente todos os beans que implementam a interface —
 * adicionar uma nova loja não exige alterar esta classe (Strategy + Open/Closed).
 */
@Component
public class AffiliateLinkGeneratorRegistry {

    private final Map<StoreType, AffiliateLinkGenerator> generators;

    public AffiliateLinkGeneratorRegistry(List<AffiliateLinkGenerator> generators) {
        this.generators = generators.stream()
                .collect(Collectors.toMap(AffiliateLinkGenerator::getStoreType, g -> g));
    }

    public Optional<AffiliateLinkGenerator> resolve(StoreType storeType) {
        return Optional.ofNullable(generators.get(storeType));
    }
}

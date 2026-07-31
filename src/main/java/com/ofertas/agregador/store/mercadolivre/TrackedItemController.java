package com.ofertas.agregador.store.mercadolivre;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mercadolivre/tracked-items")
public class TrackedItemController {

    private static final Logger log = LoggerFactory.getLogger(TrackedItemController.class);

    private final MercadoLivreTrackedItemRepository repository;

    public TrackedItemController(MercadoLivreTrackedItemRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<TrackedItemView> listAll() {
        return repository.findAll().stream()
                .map(TrackedItemView::from)
                .toList();
    }

    @PostMapping
    public ResponseEntity<?> upsert(@RequestBody TrackedItemRequest request) {
        if (!request.isValid()) {
            return ResponseEntity.badRequest().body("itemId e affiliateUrl são obrigatórios");
        }

        String itemId = request.itemId().trim();

        MercadoLivreTrackedItem entity = repository.findById(itemId)
                .orElseGet(() -> new MercadoLivreTrackedItem(itemId, request.affiliateUrl().trim()));

        entity.setAffiliateUrl(request.affiliateUrl().trim());
        entity.setActive(true);

        repository.save(entity);
        log.info("Item rastreado do Mercado Livre salvo: {}", itemId);

        return ResponseEntity.status(HttpStatus.CREATED).body(TrackedItemView.from(entity));
    }

    @PatchMapping("/{itemId}/toggle-active")
    public ResponseEntity<?> toggleActive(@PathVariable String itemId) {
        return repository.findById(itemId)
                .map(entity -> {
                    entity.setActive(!entity.isActive());
                    repository.save(entity);
                    log.info("Item {} agora está active={}", itemId, entity.isActive());
                    return ResponseEntity.ok(TrackedItemView.from(entity));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> delete(@PathVariable String itemId) {
        if (!repository.existsById(itemId)) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(itemId);
        log.info("Item {} removido da lista de rastreados", itemId);
        return ResponseEntity.noContent().build();
    }
}
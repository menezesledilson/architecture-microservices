package br.com.microservices.orchestrated.orderservice.core.service;

import br.com.microservices.orchestrated.orderservice.core.document.Event;
import br.com.microservices.orchestrated.orderservice.core.dto.EventFilters;
import br.com.microservices.orchestrated.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.security.oauthbearer.internals.secured.ValidateException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private final EventRepository repository;

    public void notifyEnding(Event event) {
        event.setOrderId(event.getOrderId());
        event.setCreatedAt(LocalDateTime.now());
        save(event);
        log.info("Order {} with saga notified! TransactionId: {} ", event.getOrderId(), event.getTransactionId());
    }

    public List<Event> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFilters filters) {
        validateEmtyFilters(filters);
        if(!isEmpty(filters.getOrderId())){
            return findByOrderId(filters.getOrderId());
        }else {
            return findByTransactionId(filters.getTransactionId());
        }
    }
    private  Event findByOrderId(String orderId){
        return repository
                .findTop1ByIdOrderByCreatedAtDesc(orderId)
                .orElseThrow (()-> new ValidateException("Event not found by orderID."));

    }
    private  Event findByTransactionId(String transactionId){
        return repository
                .findTop1ByTransactionIdOrderByCreatedAtDesc(transactionId)
                .orElseThrow (()-> new ValidateException("Event not found by transactionId."));
    }

    private void validateEmtyFilters(EventFilters filters) {
        if (isEmpty(filters.getOrderId()) && isEmpty(filters.getTransactionId())) {
            throw new ValidateException("OrderId or TransactionId must be informed.");
        }
    }

    public Event save(Event event) {
        return repository.save(event);
    }
}

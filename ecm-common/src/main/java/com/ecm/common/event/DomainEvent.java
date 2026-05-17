package com.ecm.common.event;

import java.time.Instant;

/**
 * Marker interface for all domain events in LifecycleCM.
 *
 * <p>Domain events are fired internally within ecm-library-service when significant
 * business state changes occur. They are the integration point between the core
 * document services and secondary concerns like workflow task creation and retention
 * schedule computation.</p>
 *
 * <p>Events are synchronous in Phase 1 (Spring ApplicationEvent). The interface is
 * defined here so that a future phase can swap to an async broker (Spring Integration,
 * SQS, Kafka) without changing the event types or their consumers.</p>
 *
 * <p>All events carry: the document ID, the document number, and the instant the
 * event occurred. Subtype interfaces add event-specific fields.</p>
 *
 * <p>Not responsible for: dispatching, routing, or consuming events — those are
 * owned by the event publisher in each service and by listener classes annotated
 * with @EventListener.</p>
 */
public interface DomainEvent {

    /** Internal UUID of the document that triggered this event. Never null. */
    String getDocumentId();

    /** Human-readable document number (e.g. DOC-2025-0000441). Never null. */
    String getDocumentNumber();

    /** The instant this event was created. Used for ordering and audit records. */
    Instant getOccurredAt();
}

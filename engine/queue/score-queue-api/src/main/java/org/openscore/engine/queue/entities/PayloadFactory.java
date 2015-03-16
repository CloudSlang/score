package org.openscore.engine.queue.entities;

import org.openscore.facade.entities.Execution;

public interface PayloadFactory {
    Payload createPayload(Execution execution);
}

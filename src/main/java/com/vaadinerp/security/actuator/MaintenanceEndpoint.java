package com.vaadinerp.security.actuator;

import com.vaadinerp.security.service.MaintenanceService;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * GET  /actuator/maintenance                       -> status
 * POST /actuator/maintenance {"action":"start","message":"...","seconds":60}
 * POST /actuator/maintenance {"action":"stop"}
 * POST /actuator/maintenance {"action":"broadcast","message":"..."}
 * Dilindungi ActuatorBasicAuthFilter (basic-auth + header X-Requested-By untuk POST).
 */
@Component
@Endpoint(id = "maintenance")
public class MaintenanceEndpoint {

    private final MaintenanceService maintenance;

    public MaintenanceEndpoint(MaintenanceService maintenance) {
        this.maintenance = maintenance;
    }

    @ReadOperation
    public Map<String, Object> status() {
        return maintenance.status();
    }

    @WriteOperation
    public Map<String, Object> write(String action, @Nullable String message, @Nullable Integer seconds) {
        switch (action == null ? "" : action.toLowerCase()) {
            case "start" -> maintenance.start(message, seconds != null ? seconds : 60);
            case "stop" -> maintenance.stop();
            case "broadcast" -> maintenance.broadcast(message != null ? message : "");
            default -> throw new IllegalArgumentException("action must be start, stop, or broadcast");
        }
        return maintenance.status();
    }
}

package com.atlas.flight.controller;

import com.atlas.flight.service.FlightCatalogReconciler;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/flights/reconciliation")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class FlightReconciliationController {

  private final FlightCatalogReconciler reconciler;

  @PostMapping
  public ResponseEntity<String> reconcile() {
    reconciler.reconcile();
    return ResponseEntity.ok("SUCCESS");
  }
}

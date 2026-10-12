package com.marketsdata.controller;

import com.marketsdata.model.DashboardRequest;
import com.marketsdata.model.DashboardResponse;
import com.marketsdata.service.DashboardService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The single endpoint: POST /api/dashboard.
 *
 * Takes the prompt from the React frontend and returns whichever dashboards
 * Claude decided to show. The Vite dev server proxies /api here, so no CORS is
 * needed in that setup; {@code @CrossOrigin} is a convenience for calling the
 * backend directly (e.g. the frontend without the proxy).
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @PostMapping("/dashboard")
    public DashboardResponse createDashboard(@RequestBody DashboardRequest request) {
        return dashboardService.createDashboard(request.prompt());
    }
}

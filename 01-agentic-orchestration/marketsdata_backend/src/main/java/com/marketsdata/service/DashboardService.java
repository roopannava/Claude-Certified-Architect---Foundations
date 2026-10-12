package com.marketsdata.service;

import com.marketsdata.model.DashboardResponse;

/**
 * Turns a user's prompt into one or more dashboards, letting Claude decide which
 * dashboard tools to call.
 */
public interface DashboardService {

    DashboardResponse createDashboard(String prompt);
}

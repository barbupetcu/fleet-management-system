package com.barbu.fleetmanagement.manager.api.model;

import java.util.List;

public record PaginatedResponse<E>(List<E> content, int page, int pageSize, long total) {

}

package com.example.mobileproject.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PagedResponse<T> {
    @SerializedName("items")
    private List<T> items;

    @SerializedName("total")
    private int total;

    @SerializedName("page")
    private int page;

    @SerializedName("page_size")
    private int pageSize;

    @SerializedName("total_pages")
    private int totalPages;

    public List<T> getItems() {
        return items;
    }

    public int getTotal() {
        return total;
    }

    public int getPage() {
        return page;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean hasMorePages() {
        return page < totalPages - 1;
    }
}
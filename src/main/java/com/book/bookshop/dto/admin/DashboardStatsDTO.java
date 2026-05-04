package com.book.bookshop.dto.admin;


public class DashboardStatsDTO {
    private long ordersCount;
    private long usersCount;
    private long productsCount;

    public DashboardStatsDTO(long ordersCount, long usersCount, long productsCount) {
        this.ordersCount = ordersCount;
        this.usersCount = usersCount;
        this.productsCount = productsCount;
    }

    public long getOrdersCount() {
        return ordersCount;
    }

    public void setOrdersCount(long ordersCount) {
        this.ordersCount = ordersCount;
    }

    public long getUsersCount() {
        return usersCount;
    }

    public void setUsersCount(long usersCount) {
        this.usersCount = usersCount;
    }

    public long getProductsCount() {
        return productsCount;
    }

    public void setProductsCount(long productsCount) {
        this.productsCount = productsCount;
    }
}


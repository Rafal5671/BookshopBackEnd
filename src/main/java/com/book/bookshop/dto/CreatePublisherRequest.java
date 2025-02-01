package com.book.bookshop.dto;

public class CreatePublisherRequest {
    private String name;

    public CreatePublisherRequest() {
    }

    public CreatePublisherRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}


package com.book.bookshop.dto;


public class PublishersDTO {
    private Integer publisherId;
    private String name;

    public PublishersDTO(Integer publisherId, String name) {
        this.publisherId = publisherId;
        this.name = name;
    }

    // gettery i settery
    public Integer getPublisherId() {
        return publisherId;
    }
    public void setPublisherId(Integer publisherId) {
        this.publisherId = publisherId;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}


package com.book.bookshop.dto.admin.requests.update;

import com.book.bookshop.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UpdateOrderStatusRequest {
    private OrderStatus status;
}

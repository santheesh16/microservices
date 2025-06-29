package com.personal.order_service.dto;


import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
    private List<OrderListItemsDto> orderListItemsDtoList;

}




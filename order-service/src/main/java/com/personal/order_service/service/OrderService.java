package com.personal.order_service.service;

import com.personal.order_service.dto.InventoryResponse;
import com.personal.order_service.dto.OrderListItemsDto;
import com.personal.order_service.dto.OrderRequest;
import com.personal.order_service.model.Order;
import com.personal.order_service.model.OrderLineItems;
import com.personal.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;


    public void placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItems = orderRequest.getOrderListItemsDtoList()
                .stream()
                .map(this::mapToDto).toList();
        order.setOrderLineItemList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemList().stream().map(OrderLineItems::getSkuCode).toList();

        //Call Inventory Service, and place order if product is in stock
        InventoryResponse[] inventoryResponses = webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode", skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        if (inventoryResponses.length > 0){
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Products is not in stock. Please try again later !");
        }
    }

    private OrderLineItems mapToDto(OrderListItemsDto orderListItemsDto){
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setQuantity(orderListItemsDto.getQuantity());
        orderLineItems.setPrice(orderListItemsDto.getPrice());
        orderLineItems.setSkuCode(orderListItemsDto.getSkuCode());
        return orderLineItems;
    }
}

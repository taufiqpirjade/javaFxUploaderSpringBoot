package com.codetreatise.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.codetreatise.bean.OrderData;

public interface OrderRepository extends JpaRepository<OrderData, Integer> {

}

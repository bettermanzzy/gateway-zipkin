///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package org.apache.servicecomb.spring.boot2.gateway.tracing;
//
//import java.lang.invoke.MethodHandles;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.cloud.gateway.filter.GatewayFilter;
//import org.springframework.cloud.gateway.filter.GatewayFilterChain;
//import org.springframework.cloud.gateway.filter.GlobalFilter;
//import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
//import org.springframework.core.Ordered;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.server.reactive.ServerHttpRequest.Builder;
//import org.springframework.http.server.reactive.ServerHttpResponse;
//import org.springframework.http.server.reactive.ServerHttpRequest;
//import org.springframework.stereotype.Component;
//import org.springframework.web.server.ServerWebExchange;
//
//import brave.Span;
//import brave.Tracer;
//import brave.Tracer.SpanInScope;
//import brave.http.HttpClientHandler;
//import brave.http.HttpTracing;
//import reactor.core.publisher.Mono;
//
////@Component
//class TracePostGatewayFilter implements GlobalFilter {
//
//  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
//
//  private final Tracer tracer;
//
//  private final HttpClientHandler<ServerHttpRequest, ServerHttpResponse> clientHandler;
//
//  TracePostGatewayFilter(HttpTracing tracer, HttpClientHandler<ServerHttpRequest, ServerHttpResponse> clientHandler) {
//    this.tracer = tracer.tracing().tracer();
//    this.clientHandler = clientHandler;
//  }
//
//  @Override
//  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//
//    ServerHttpRequest.Builder builder;
//    return chain.filter(exchange).then(Mono.just(exchange))
//        .map(serverWebExchange -> {
////          ServerHttpRequest.Builder builder = serverWebExchange.getRequest().mutate();
//          ServerHttpRequest request = serverWebExchange.getRequest();
//          ServerHttpResponse response = serverWebExchange.getResponse();
//          System.out.println("resp post : " + response.getHeaders());
//          System.out.println("request post : " + request.getHeaders());
//          System.out.println("trace post : " + tracer.currentSpan());
//          clientHandler.handleReceive(response, null, tracer.currentSpan());
//          System.out.println("Closed span {"+ tracer.currentSpan() +"} for {"+ request.getMethod().name() + "}" );
//
//          return serverWebExchange;
//        }).then();
//
//  }
//
////  @Override
////  public int getOrder() {
////    return 0;
////  }
//
//}

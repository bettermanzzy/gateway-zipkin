/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.servicecomb.spring.boot2.gateway.tracing;

import static org.apache.servicecomb.core.Const.CSE_CONTEXT;

import java.lang.invoke.MethodHandles;
import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;

import brave.Span;
import brave.Tracer;
import brave.Tracer.SpanInScope;
import brave.http.HttpClientHandler;
import brave.http.HttpTracing;
import brave.propagation.TraceContext.Injector;
import reactor.core.publisher.Mono;

//@Component
class TracePreGatewayFilter implements GlobalFilter ,Ordered{

  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private final Tracer tracer;

  private final HttpTracing tracing;

  private final HttpClientHandler<ServerHttpRequest, ServerHttpResponse> clientHandler;

 // private final Injector<ServerHttpRequest> injector;

  TracePreGatewayFilter(HttpTracing tracing, HttpClientHandler<ServerHttpRequest, ServerHttpResponse> clientHandler) {

    this.tracing = tracing;
    this.tracer = tracing.tracing().tracer();
  //  this.injector = ;
    this.clientHandler = clientHandler;

  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
//    ServerHttpRequest.Builder builder = exchange.getRequest().mutate();
//    ServerHttpRequest request = builder.header("create", "create span").build();

    ServerHttpRequest request = exchange.getRequest();

    final Map<String, ServerHttpRequest> requestMap = new HashMap<>();
    Span span = clientHandler.handleSend(tracing.tracing().propagation().injector(
        (request2, name, value) -> {

          ServerHttpRequest newRequest = requestMap.get("newRequest");

          if (newRequest != null) {
            request2 = newRequest;
          }

          System.out.println("error name:" + name + " value: " + value);
          System.out.println("error req1:" + request2.getHeaders());
          requestMap.put("newRequest", request2.mutate().header(name, value).build());
          System.out.println("error req2:" + request2.getHeaders());
        }), request);

    request = requestMap.get("newRequest");

    System.out.println("span create 220 OK");
    System.out.println("pre context : " + request.getHeaders());

    request = saveHeadersAsInvocationContext(request,span,exchange);
    System.out.println("save span in invocation");

    SpanInScope scope = tracer.withSpanInScope(span);
    //log.info("Generated tracing span {} for {}", span, request.getMethod().name());
    System.out.println("Generated tracing span {" + span + "} for {" + request.getMethod().name() + "}");
    System.out.println("pre tracer : " + tracer.currentSpan());

    exchange.getAttributes().put(SpanInScope.class.getName(), scope);

    return chain.filter(exchange.mutate().request(request).build()).then(Mono.just(exchange))
        .map(serverWebExchange -> {
          ServerHttpResponse response = serverWebExchange.getResponse();
          ((SpanInScope) serverWebExchange.getAttribute(SpanInScope.class.getName())).close();

          System.out.println("resp post : " + response.getHeaders());
          System.out.println("trace post : " + tracer.currentSpan());
          clientHandler.handleReceive(response, null, tracer.currentSpan());
          System.out.println("Closed span {"+ tracer.currentSpan() +"} for {"+ serverWebExchange.getRequest().getMethod().name() + "}" );

          return serverWebExchange;
        }).then();
  }

  private ServerHttpRequest saveHeadersAsInvocationContext(ServerHttpRequest request, Span span,ServerWebExchange exchange) {
    try {
      return  request.mutate().header(CSE_CONTEXT, JsonUtils.writeValueAsString(request.getHeaders().toSingleValueMap())).build();
    } catch (JsonProcessingException e) {

      clientHandler.handleReceive(exchange.getResponse(), e, span);
      throw new IllegalStateException("Unable to write request headers as json to " + CSE_CONTEXT, e);
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

}
